package com.llamacorp.unitcalc.view;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.text.Html;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

import com.llamacorp.unitcalc.Calculator;
import com.llamacorp.unitcalc.R;

public class EditTextCursorWatcher extends EditText {
	private Calculator mCalc;
	private final Context context;

	private int mSelStart = 0;
	private int mSelEnd = 0;

	private int mHighlightIndex1;

	private String mTextPrefex="";
	private String mExpressionText="";
	private String mTextSuffix="";
	private ValueAnimator mColorAnimation;


	// (This was in the original TextView) System wide time for last cut or copy action.
	static long LAST_CUT_OR_COPY_TIME;

	public EditTextCursorWatcher(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context=context;
	}

	public EditTextCursorWatcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
	}

	public EditTextCursorWatcher(Context context) {
		super(context);
		this.context=context;
	}

	/** Set the singleton calc to this EditText for its own use */
	public void setCalc(Calculator calc) {
		mCalc=calc;		
	} 


	/**
	 *  Custom paste and cut commands, leave the default copy operation
	 */
	@Override
	public boolean onTextContextMenuItem(int id) {
		boolean consumed = true;

		switch (id){
		case android.R.id.cut:
			onTextCut();
			break;
		case android.R.id.paste:
			onTextPaste();
			break;
		case android.R.id.copy:
			consumed = super.onTextContextMenuItem(id);
		}
		//update the view with calc's selection and text
		updateTextFromCalc();
		return consumed;
	}

	/** Try to cut the current clipboard text */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void onTextCut(){
		int selStart = getSelectionStart();
		int selEnd = getSelectionEnd();

		CharSequence copiedText = getText().subSequence(selStart, selEnd);

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB){
			ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setPrimaryClip(ClipData.newPlainText(null, copiedText));
		} 
		else{
			ClipboardManager clipboard = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE); 
			clipboard.setText(copiedText);
		}

		//cut deletes the selected text
		mCalc.parseKeyPressed("b");

		//this was in the original function, keep for now
		LAST_CUT_OR_COPY_TIME = SystemClock.uptimeMillis();

		Toast.makeText(context, "Cut: \"" + copiedText + "\"", Toast.LENGTH_SHORT).show();
	}



	/** Try to paste the current clipboard text into this EditText */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void onTextPaste(){
		String textToPaste;
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB){
			ClipboardManager clipboard =  (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE); 
			ClipData clip = clipboard.getPrimaryClip();
			textToPaste = clip.getItemAt(0).coerceToText(getContext()).toString();
		} 
		else{
			ClipboardManager clipboard = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE); 
			if(clipboard.hasText())
				textToPaste = clipboard.getText().toString();
			else return;
		}
		Toast.makeText(context, "Pasted: \"" + textToPaste + "\"", Toast.LENGTH_SHORT).show();
		mCalc.pasteIntoExpression(textToPaste);
	}	



	/**
	 * Disable soft keyboard from appearing, use in conjunction with android:windowSoftInputMode="stateAlwaysHidden|adjustNothing"
	 * @param editText
	 */
	@SuppressLint("NewApi")
	public void disableSoftInputFromAppearing() {
		if (Build.VERSION.SDK_INT >= 11) {
			setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			setTextIsSelectable(true);
		} else {
			setRawInputType(InputType.TYPE_NULL);
			setFocusable(true);
		}
	}

	/**
	 * Updates the text with current value from calc
	 * Preserves calc's cursor selections
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void updateTextFromCalc(){
		//setText will reset selection to 0,0, so save it right now
		mSelStart = mCalc.getSelectionStart();
		mSelEnd = mCalc.getSelectionEnd();

		mTextPrefex = "";
		mExpressionText = mCalc.toString();
		mTextSuffix = "";
		//if expression not empty/invalid and unit selected, display it after the expression
		if(!mCalc.isExpressionInvalid() && !mCalc.isExpressionEmpty() && mCalc.isUnitSelected()){
			mTextSuffix = " " + mCalc.getCurrUnitType().getCurrUnit().toString();
			//about to do conversion
			if(!mCalc.isSolved()){
				mTextPrefex = getResources().getString(R.string.word_Convert) + " ";
				mTextSuffix = mTextSuffix + " " + getResources().getString(R.string.word_to) + ":";

				mSelStart = mSelStart + mTextPrefex.length();
				mSelEnd = mSelEnd + mTextPrefex.length();
			}
		}
		//update the main display
		setText(Html.fromHtml("<font color='gray'>" + mTextPrefex + "</font>" + 
				mExpressionText + 
				"<font color='gray'>" + mTextSuffix + "</font>"));


		ArrayList<Integer> highlist = mCalc.getHighlighted();
		mHighlightIndex1 = highlist.get(0);
		//if index1 isn't highlighted, neither is index2 (since they're sorted)
		if(mHighlightIndex1 != -1){
			Integer colorFrom = Color.RED;
			Integer colorTo = Color.WHITE;
			mColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
			mColorAnimation.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animator) {
					ArrayList<Integer> highlist = mCalc.getHighlighted();
					mHighlightIndex1 = highlist.get(0);
					String coloredExp = "";
					//if the highlight got canceled during the async animation update, cancel
					if(mHighlightIndex1 == -1){
						animator.cancel();
						coloredExp = mExpressionText;
					}
					else {
						int color = (Integer)animator.getAnimatedValue();
						int len=2;
						coloredExp = mExpressionText.substring(0,  highlist.get(0));
						for(int i=0; i < len;i++){
							int finish = mExpressionText.length();
							if(i != len - 1) finish = highlist.get(i + 1);
							coloredExp = coloredExp + "<font color='" + color + "'>" + 
									mExpressionText.substring(highlist.get(i), highlist.get(i) + 1) +
									"</font>" + mExpressionText.substring(highlist.get(i) + 1, finish);
						}
					}

					//update the main display
					setText(Html.fromHtml("<font color='gray'>" + mTextPrefex + "</font>" + 
							coloredExp + 
							"<font color='gray'>" + mTextSuffix + "</font>"));

					//updating the text restarts selection to 0,0, so load in the current selection
					setSelection(mSelStart, mSelEnd);

				}
			});	
			mColorAnimation.addListener(new AnimatorListenerAdapter() 
			{
				@Override
				public void onAnimationEnd(Animator animation) 
				{
					clearHighlighted();
				}
			});
			mColorAnimation.setDuration(600);
			mColorAnimation.start();
		}
		//updating the text restarts selection to 0,0, so load in the current selection
		setSelection(mSelStart, mSelEnd);

		//if expression not solved, set cursor to visible (and visa-versa)
		setCursorVisible(!mCalc.isSolved());
	}

	public void clearHighlighted(){
		mCalc.clearHighlighted();
		
		//update the main display
		setText(Html.fromHtml("<font color='gray'>" + mTextPrefex + "</font>" + 
				mExpressionText + 
				"<font color='gray'>" + mTextSuffix + "</font>"));

		//updating the text restarts selection to 0,0, so load in the current selection
		setSelection(mSelStart, mSelEnd);
	}


	/** Sets the current selection to the end of the expression */
	public void setSelectionToEnd(){
		int expLen = mCalc.toString().length() + mTextPrefex.length();
		setSelection(expLen, expLen);
	}


	@Override   
	protected void onSelectionChanged(int selStart, int selEnd) { 
		if(mCalc!=null){
			int preLen = mTextPrefex.length();
			int expLen = mExpressionText.length();
			//check to see if the unit part of the expression has been selected
			if(selEnd > expLen + preLen){
				setSelection(selStart, expLen + preLen);
				return;
			}
			if(selStart > expLen + preLen){
				setSelection(expLen+preLen, selEnd);
				return;
			}
			if(selEnd < preLen){
				setSelection(selStart, preLen);
				return;
			}
			if(selStart < preLen){
				setSelection(preLen, selEnd);
				return;
			}	

			//save the new selection in the calc class
			mCalc.setSelection(selStart-preLen, selEnd-preLen);
			setCursorVisible(true);
		}
	}

}