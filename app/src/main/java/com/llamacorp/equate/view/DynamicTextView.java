package com.llamacorp.equate.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.llamacorp.equate.R;

/**
 * Acts as a regular text view, but will dyamicly scale the font size down to
 * a certain size before going to two lines.  Min size will be specified in the
 * XML.  Created by Evan on 12/10/2016.
 */
public class DynamicTextView extends TextView {
	private float mTextSize = 0f;
	private float mMinTextSize;

	public DynamicTextView(Context context) {
		super(context);
	}

	public DynamicTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setUpTextView(context, attrs);
	}

	public DynamicTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setUpTextView(context, attrs);
	}


	private void setUpTextView(Context context, AttributeSet attrs) {
		//grab custom resource variable
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DynamicText, 0, 0);
		try {
			mMinTextSize = ta.getDimension(R.styleable.DynamicText_minimumTextSize,
					  getTextSize());
		} finally {
			ta.recycle();
		}
		//Log.d("DYN", "mStartingTextSize = " + getTextSize());
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

	/**
	 * Helper method to size text dynamically depending on the text size in
	 * relation to the width of the text view
	 */
	private void layoutText() {
		if (getText().equals("")) return;
		Paint paint = getPaint();
		if (mTextSize != 0f)
			paint.setTextSize(mTextSize);
		//if min text size is the same as normal size, just leave
		if (mMinTextSize == getTextSize()) return;
		float textWidth = paint.measureText(getText().toString());
		float boxWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		// if the view doesn't exist, the box width will be 0 or negative
		if (boxWidth <= 0f) return;
		float textSize = getTextSize();
		if (textWidth > boxWidth){
			float scaled = textSize * boxWidth / textWidth;
			//scaled = scaled*0.9f;
			if (scaled < mMinTextSize)
				scaled = mMinTextSize;
			paint.setTextSize(scaled);
			mTextSize = textSize;
		}
	}
}
