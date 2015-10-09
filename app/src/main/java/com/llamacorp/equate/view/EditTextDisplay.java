package com.llamacorp.equate.view;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.text.Html;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

import com.llamacorp.equate.Calculator;
import com.llamacorp.equate.ExpSeparatorHandler;
import com.llamacorp.equate.Expression;
import com.llamacorp.equate.R;
import com.llamacorp.equate.SISuffixHelper;

public class EditTextDisplay extends EditText {
	private Calculator mCalc;
	private Context mContext;

	private float mTextSize = 0f;
	private float mMinTextSize;

	private int mSelStart = 0;
	private int mSelEnd = 0;

	private String mTextPrefix ="";
	private String mExpressionText="";
	private String mTextSuffix="";

	private ExpSeparatorHandler mSepHandler;

	private ValueAnimator mColorAnimation;


	//TODO might not need this
	// (This was in the original TextView) System wide time for last cut or copy action.
	static long LAST_CUT_OR_COPY_TIME;


	public EditTextDisplay(Context context) {
		this(context, null);
	}	

	public EditTextDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		setUpEditText(context, attrs);
	}

	public EditTextDisplay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setUpEditText(context, attrs);
	}

	private void setUpEditText(Context context, AttributeSet attrs){
		mContext = context;
		mSepHandler = new ExpSeparatorHandler();

		//grab custom resource variable
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EditTextDisplay, 0, 0);
		try {
			mMinTextSize = ta.getDimension(R.styleable.EditTextDisplay_minimumTextSize,
					getTextSize());
		} finally {	ta.recycle();}
	}

	/** Set the singleton calc to this EditText for its own use */
	public void setCalc(Calculator calc) {
		mCalc = calc;		
	} 


	/**
	 * Disable soft keyboard from appearing, use in conjunction with
     * android:windowSoftInputMode="stateAlwaysHidden|adjustNothing"
	 */
	public void disableSoftInputFromAppearing() {
		setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		setTextIsSelectable(true);
	}

	/**
	 * Updates the text and selection with current value from calc
	 */
	public void updateTextFromCalc(){
		mTextPrefix = "";
		mExpressionText = getSepDispText();
		mTextSuffix = "";

		//setText will reset selection to 0,0, so save it right now
		mSelStart = mSepHandler.translateToSepIndex(mCalc.getSelectionStart());
		mSelEnd = mSepHandler.translateToSepIndex(mCalc.getSelectionEnd());

		//if expression not invalid and unit selected, display it after the expression
		if(!mCalc.isExpressionInvalid() &&  mCalc.isUnitSelected()){
			mTextSuffix = " " + mCalc.getCurrUnitType().getCurrUnit().toString();
			//about to do conversion
			if(!mCalc.isSolved()){
				mTextPrefix = getResources().getString(R.string.word_Convert) + " ";
				mTextSuffix = mTextSuffix + " " + getResources().getString(R.string.word_to) + ":";

				//bump cursor position to the right by prefix text length
				mSelStart = mSelStart + mTextPrefix.length();
				mSelEnd = mSelEnd + mTextPrefix.length();
			}
		}

		if(mCalc.isSolved() &&
				  mCalc.getNumberFormat() == Expression.NumFormat.ENGINEERING){
			mTextSuffix = " " + SISuffixHelper.getSuffixName(mExpressionText);
		}
		//update the main display
		setTextHtml(mExpressionText);

		//Set up a animator to highlight parts red and fade to white
		setupHighlighting();

		//updating the text restarts selection to 0,0, so load in the current selection
		setSelection(mSelStart, mSelEnd);

		//if expression not solved, set cursor to visible (and visa-versa)
		setCursorVisible(!mCalc.isSolved());
	}


	/**
	 * Helper method to setup the highlighting
	 */
	public void setupHighlighting() {
		if(mCalc.isHighlighted()){
			Integer colorFrom = Color.RED;
			Integer colorTo = Color.WHITE;
			final int ANIMATE_DURR = 600; //ms
			mColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
			mColorAnimation.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animator) {
					String coloredExp;
					//if the highlight got canceled during the async animation update, cancel
					if(!mCalc.isHighlighted()){
						animator.cancel();
						coloredExp = mExpressionText;
					}
					else {
						ArrayList<Integer> highlist = mCalc.getHighlighted();
						highlist = mSepHandler.translateIndexListToSep(highlist);
						int color = (Integer)animator.getAnimatedValue();
						int len = highlist.size();
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
					setTextHtml(coloredExp);

					//updating the text restarts selection to 0,0, so load in the current selection
					setSelection(mSelStart, mSelEnd);
				}
			});
			mColorAnimation.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					clearHighlighted();
				}
			});
			mColorAnimation.setDuration(ANIMATE_DURR);
			mColorAnimation.start();
		}
	}


	public void clearHighlighted(){
		mCalc.clearHighlighted();

		//only need to change the text if we have a animator running
		if(mColorAnimation != null && mColorAnimation.isRunning()){
			//update the main display
			setTextHtml(mExpressionText);

			//updating the text restarts selection to 0,0, so load in the current selection
			setSelection(mSelStart, mSelEnd);
		}
	}

	/**
	 * Helper method used to set the main display with HTML formating, without
	 * highlighting
	 * @param expStr is the main expression to update
	 */
	private void setTextHtml(String expStr){
		setText(Html.fromHtml("<font color='gray'>" + mTextPrefix + "</font>" +
				expStr + 
				"<font color='gray'>" + mTextSuffix + "</font>"));
	}


	/** Sets the current selection to the end of the expression */
	public void setSelectionToEnd(){
		int expLen = mExpressionText.length() + mTextPrefix.length();
		setSelection(expLen, expLen);
	}


	/**
	 * Helper method returns the Expression text from calc seperated by commas
	 * This function will also set up update mSepHandler such that getting
	 * shiftedIndexs will work with the current text.
	 */
	private String getSepDispText(){
		//return mCalc.toString();
		return mSepHandler.getSepText(mCalc.toString());
	}



	@Override
	protected void onTextChanged(CharSequence text, int start, int before, int after) {
		super.onTextChanged(text, start, before, after);
		layoutText();
	}


	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) layoutText();
	}	

	/** Helper method to size text */
	private void layoutText() {
		Paint paint = getPaint();
		if (mTextSize != 0f) paint.setTextSize(mTextSize);
		//if min text size is the same as normal size, just leave
		if(mMinTextSize == getTextSize()) return;
		float textWidth = paint.measureText(getText().toString());
		float boxWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		float textSize = getTextSize();
		if (textWidth > boxWidth) {
			float scaled = textSize * boxWidth / textWidth;
			if(scaled < mMinTextSize)
				scaled = mMinTextSize;
			paint.setTextSize(scaled);
			mTextSize = textSize;
		} 
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
	private void onTextCut(){
		int selStart = getSelectionStart();
		int selEnd = getSelectionEnd();

		CharSequence copiedText = getText().subSequence(selStart, selEnd);

		ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(ClipData.newPlainText(null, copiedText));

		//cut deletes the selected text
		mCalc.parseKeyPressed("b");

		//this was in the original function, keep for now
		LAST_CUT_OR_COPY_TIME = SystemClock.uptimeMillis();

		Toast.makeText(mContext, "Cut: \"" + copiedText + "\"", Toast.LENGTH_SHORT).show();
	}



	/** Try to paste the current clipboard text into this EditText */
	private void onTextPaste(){
		String textToPaste;
		ClipboardManager clipboard =  (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE); 
		ClipData clip = clipboard.getPrimaryClip();
		textToPaste = clip.getItemAt(0).coerceToText(getContext()).toString();
		Toast.makeText(mContext, "Pasted: \"" + textToPaste + "\"", Toast.LENGTH_SHORT).show();
		mCalc.pasteIntoExpression(textToPaste);
	}	



	@Override   
	protected void onSelectionChanged(int selStart, int selEnd) { 
		if(mCalc!=null){
			int preLen = mTextPrefix.length();
			int expLen = mExpressionText.length();

			int fixedSelStart = mSepHandler.makeIndexValid(selStart - preLen) + preLen;
			int fixedSelEnd = mSepHandler.makeIndexValid((selEnd - preLen)) + preLen;

			if(fixedSelStart != selStart || fixedSelEnd != selEnd){
				setSelection(fixedSelStart, fixedSelEnd);
				return;
			}

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
			mCalc.setSelection(mSepHandler.translateFromSepIndex(selStart-preLen),
					mSepHandler.translateFromSepIndex(selEnd-preLen));
			setCursorVisible(true);
		}
	}

}