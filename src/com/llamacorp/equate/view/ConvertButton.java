package com.llamacorp.equate.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.llamacorp.equate.R;

public class ConvertButton extends SecondaryTextButton {

	private String mTopText;
	private String mBotText;

	private float mTopTextX;
	private float mBotTextX;

	private float mTopTextY;
	private float mBotTextY;
	
	private boolean mUnderline = true;

	public ConvertButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	/** Helper method to size text */
	protected void layoutText() {
		String text = getText().toString();
		
		//TODO crude, fix
		//historical currency (USD [1953) is too long, put on two lines
		if(text.contains(" [")){
			text = text.replace(" [", "/[");
			setText(text);
			mUnderline = false;
		}
		
		if(text.contains("/")){
			if(getText().toString().contains("[1966]"))
				System.out.println("dog");
			if(text.contains(getResources().getString(R.string.convert_arrow)))
				layoutTextDividedWithArrow();
			else 
				layoutTextDivided();

			if (mSecondaryPaint != null)
				mSecondaryPaint.setTextSize(mSecondaryTextSize);
		}
		else
			super.layoutText();
	}




	private void layoutTextDivided() {
		Paint paint = getPaint();
		if (mTextSize != 0f) paint.setTextSize(mTextSize);

		//get the larger half of the divided text
		String[] halves = getText().toString().split("/");
		mTopText = halves[0];
		mBotText = halves[1];
		float topTextWidth = paint.measureText(mTopText);
		float botTextWidth = paint.measureText(mBotText);
		float maxWidth = Math.max(botTextWidth, topTextWidth);

		float boxWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		float textSize = getTextSize();
		if (maxWidth > boxWidth) {
			paint.setTextSize(textSize * boxWidth / maxWidth);
			mTextSize = textSize;
		} 
		mTopTextX = (getWidth() - paint.measureText(mTopText)) / 2;
		mBotTextX = (getWidth() - paint.measureText(mBotText)) / 2;

		mTopTextY = (getHeight()) / 2 - paint.descent();
		mBotTextY = (getHeight())/2  - paint.ascent()*4/5;

	}

	private void layoutTextDividedWithArrow() {
		super.layoutText();
	}



	/**
	 * Overloaded method, will be called by onDraw in SecondaryTextButton
	 */
	@Override
	protected void drawMainText(Canvas canvas){
		String text = (String) getText();
		if(text.contains("/")){

			getPaint().setColor(getCurrentTextColor());

			if(mUnderline)
				getPaint().setUnderlineText(true);
			canvas.drawText(mTopText, 0, mTopText.length(), mTopTextX, mTopTextY, getPaint());
			
			getPaint().setUnderlineText(false);
			canvas.drawText(mBotText, 0, mBotText.length(), mBotTextX, mBotTextY, getPaint());

		}
		else
			super.drawMainText(canvas);

	}

}
