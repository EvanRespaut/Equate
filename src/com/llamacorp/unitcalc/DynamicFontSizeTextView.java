package com.llamacorp.unitcalc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class DynamicFontSizeTextView extends TextView {
	private float mTextX;
	private float mTextY;
	private float mTextSize = 0f;

//	private int mMinFontSize = 0; 
	
	public DynamicFontSizeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//force TextView to take up one line from the start, else height may have empty space
		setMaxLines(1);
		/*
		//grab custom resource variable
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SecondaryTextButton, 0, 0);
		try {
			mMinFontSize = ta.getString(R.styleable.SecondaryTextButton_secondary_text);
		} finally {
			ta.recycle();
		}
		*/
	}

	
	private void layoutText() {
		Paint paint = getPaint();
		if (mTextSize != 0f) paint.setTextSize(mTextSize);
		float textWidth = paint.measureText(getText().toString());
		float width = getWidth() - getPaddingLeft() - getPaddingRight();
		float textSize = getTextSize();
		if (textWidth > width) {
			//TODO this seems to cause the not refreshing problem
			paint.setTextSize(textSize * width / textWidth);
			mTextX = getPaddingLeft();
			mTextSize = textSize;
		} else {
			mTextX = (getWidth() - textWidth) / 2;
		}
		mTextY = (getHeight() - paint.ascent() - paint.descent()) / 2;
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
	
	//TODO this is broken, making text only sometimes appear...
	@Override
	protected void onDraw(Canvas canvas) {
		getPaint().setColor(getCurrentTextColor());
		CharSequence text = getText();
		canvas.drawText(text, 0, text.length(), mTextX, mTextY, getPaint());
	}
}
