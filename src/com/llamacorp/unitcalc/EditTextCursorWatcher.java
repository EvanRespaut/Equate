package com.llamacorp.unitcalc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

public class EditTextCursorWatcher extends EditText {
	private Calculator mCalc;
	
	public EditTextCursorWatcher(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public EditTextCursorWatcher(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public EditTextCursorWatcher(Context context) {
		super(context);

	}

	public void setCalc(Calculator calc) {
		mCalc=calc;		
	} 
	
	/**
	 * Disable soft keyboard from appearing, use in conjunction with android:windowSoftInputMode="stateAlwaysHidden|adjustNothing"
	 * @param editText
	 */
	@SuppressLint("NewApi")
	public void disableSoftInputFromAppearing() {
		if (Build.VERSION.SDK_INT >= 11) {
			setRawInputType(InputType.TYPE_CLASS_TEXT);
			setTextIsSelectable(true);
		} else {
			setRawInputType(InputType.TYPE_NULL);
			setFocusable(true);
		}
	}


	@Override   
	protected void onSelectionChanged(int selStart, int selEnd) { 
		//Toast.makeText(getContext(), "selStart is " + selStart + "selEnd is " + selEnd, Toast.LENGTH_LONG).show();
		if(mCalc!=null)
			mCalc.setSelection(selStart, selEnd);
		Log.d("test", "onSelectionChange called. selStart=" + String.valueOf(selStart));
		//if the selection is at the end of expression, don't show cursor
		//if(selStart==mCalc.toString().length())
		//	setCursorVisible(false);
		//else
		//	setCursorVisible(true);
	}

}