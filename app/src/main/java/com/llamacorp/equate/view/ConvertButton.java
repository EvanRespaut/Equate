package com.llamacorp.equate.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.llamacorp.equate.R;

public class ConvertButton extends SecondaryTextButton {

	private String mTopText;
	private String mBotText;
	private String mArrowText;

	private float mTopTextX;
	private float mBotTextX;

	private float mTopTextY;
	private float mBotTextY;

	private float mArrowTextX;
	private float mArrowTextY;

	private boolean mUnderline = true;

	public ConvertButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mArrowText = getResources().getString(R.string.convert_arrow);
	}


	/** Helper method to size text */
	protected void layoutText() {
		String text = getText().toString();

		//TODO crude, fix
		//historical currency (USD [1953]) is too long, put on two lines
		if(text.contains(" [")){
			text = text.replace(" [", "/[");
			setText(text);
			mUnderline = false;
		}

		if(text.contains("/")){
			layoutTextDivided();
		}
		else
			super.layoutText();
	}




	private void layoutTextDivided() {
		Paint paint = getPaint();
		if (mTextSize != 0f) paint.setTextSize(mTextSize);

		String text = getText().toString();

		float arrowWidth = 0;
		String dividedText = text;
		if(text.contains(mArrowText)){
			//get the larger half of the divided text
			dividedText = text.replace(mArrowText, "");

			arrowWidth = paint.measureText(mArrowText);
		}

		//get the larger half of the divided text
		String[] halves = dividedText.split("/");
		mTopText = halves[0];
		mBotText = halves[1];
		float topTextWidth = paint.measureText(mTopText);
		float botTextWidth = paint.measureText(mBotText);
		float maxWidth =  Math.max(botTextWidth, topTextWidth);

		float boxWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		float textSize = getTextSize();
		if ((arrowWidth + maxWidth) > boxWidth) {
			paint.setTextSize(textSize * boxWidth / (arrowWidth + maxWidth));
			mTextSize = textSize;
		} 

		mTopTextX = (getWidth() - topTextWidth + arrowWidth) / 2;
		mBotTextX = (getWidth() - botTextWidth + arrowWidth) / 2;

		mTopTextY = (getHeight()) / 2 - paint.descent();
		mBotTextY = (getHeight())/2  - paint.ascent()*4/5;

		if(text.contains(mArrowText)){
			mArrowTextX = (getWidth() - maxWidth - arrowWidth) / 2;
			mArrowTextY = (getHeight() - paint.ascent() - paint.descent()) / 2;
		}
		
		
		if (mSecondaryPaint != null)
			mSecondaryPaint.setTextSize(mSecondaryTextSize);
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

			if(text.contains(mArrowText)){
				canvas.drawText(mArrowText, 0, mArrowText.length(), mArrowTextX, 
						mArrowTextY, getPaint());
			}
		}
		else
			super.drawMainText(canvas);

	}

}
