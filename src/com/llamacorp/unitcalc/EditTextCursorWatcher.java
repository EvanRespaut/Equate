package com.llamacorp.unitcalc;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

public class EditTextCursorWatcher extends EditText {
	private Calculator mCalc;
	private final Context context;

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

		Toast.makeText(context, "Cut!", Toast.LENGTH_SHORT).show();
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
		Toast.makeText(context, "Pasted text = " + textToPaste, Toast.LENGTH_SHORT).show();
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
	 * Updates the text with current value from calc.
	 * Preserves calc's cursor selections
	 */
	public void updateTextFromCalc(){
		//setText will reset selection to 0,0, so save it right now
		int selStart = mCalc.getSelectionStart();
		int selEnd = mCalc.getSelectionEnd();

		//update the main display
		setText(mCalc.toString());
		//updating the text restarts selection to 0,0, so load in the current selection
		setSelection(selStart, selEnd);
		if(selStart == mCalc.toString().length())
			setCursorVisible(false);
		else 
			setCursorVisible(true);
	}


	@Override   
	protected void onSelectionChanged(int selStart, int selEnd) { 
		//Toast.makeText(getContext(), "selStart is " + selStart + "selEnd is " + selEnd, Toast.LENGTH_LONG).show();
		if(mCalc!=null){
			//if expression is empty, no need to set selection (happens if user clicks "UnicCalc")
			if(mCalc.toString().equals(""))
				return;
			mCalc.setSelection(selStart, selEnd);
			setCursorVisible(true);
		}
	}

}