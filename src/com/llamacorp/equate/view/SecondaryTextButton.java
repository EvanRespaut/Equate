package com.llamacorp.equate.view;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

import com.llamacorp.equate.R;

class SecondaryTextButton extends Button {
	protected static final int SECONDARY_FONT_PERCENTAGE = 70;
	
	private float mTextX;
	private float mTextY;
	private float mTextSize = 0f;
	
	protected Paint mSecondaryPaint;
	protected String mSecondaryText;
	protected float mSecondaryTextSize;

	//the following are used to determine where to place the secondary text
	protected float mButtonHeight;
	protected float mButtonWidth;
	protected float mSecTextWidth;
	protected float mSecAdditionalXOffset;
	protected float mSecTextHeight;
	protected float mSecAdditionalYOffset;

	//x and y coordinates for the secondary text
	protected float mSecXCoord;
	protected float mSecYCoord;


	public SecondaryTextButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		final float defaultSecondaryTextSize = getPaint().getTextSize() * SECONDARY_FONT_PERCENTAGE / 100f;
		
		//grab custom resource variable
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SecondaryTextButton, 0, 0);
		try {
			mSecondaryText = ta.getString(R.styleable.SecondaryTextButton_secondary_text);
			mSecondaryTextSize = ta.getDimension(R.styleable.SecondaryTextButton_secondary_text_font_size,
												 defaultSecondaryTextSize);
		} finally {
			ta.recycle();
		}

		mSecondaryPaint = new Paint(getPaint());
	}


	/** Set secondary text string */
	public void setSecondaryText(String text){
		mSecondaryText = text;
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
		float textWidth = paint.measureText(getText().toString());
		float boxWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		float textSize = getTextSize();
		if (textWidth > boxWidth) {
			paint.setTextSize(textSize * boxWidth / textWidth);
			mTextX = getPaddingLeft();
			mTextSize = textSize;
		} else {
			mTextX = (getWidth() - textWidth) / 2;
		}
		mTextY = (getHeight() - paint.ascent() - paint.descent()) / 2;
		if (mSecondaryPaint != null)
			mSecondaryPaint.setTextSize(mSecondaryTextSize);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//draw the text in the upper corner
		mSecondaryPaint.setColor(getResources().getColor(R.color.button_secondary_text));

		if(mSecondaryText != null){
			mButtonHeight = getHeight() - getPaddingTop() - getPaddingBottom();
			mButtonWidth = getWidth() - getPaddingLeft() - getPaddingRight();

			mSecTextWidth = mSecondaryPaint.measureText(mSecondaryText);
			mSecAdditionalXOffset = getContext().getResources()
					.getDimensionPixelSize(R.dimen.button_ellipses_additional_offset_x);

			mSecTextHeight = mSecondaryPaint.getTextSize();
			mSecAdditionalYOffset = getContext().getResources()
					.getDimensionPixelSize(R.dimen.button_ellipses_additional_offset_y);

			findSecondaryTextCoord();

			canvas.drawText(mSecondaryText, 0, mSecondaryText.length(), 
					mSecXCoord, mSecYCoord, mSecondaryPaint);
		}
		
		getPaint().setColor(getCurrentTextColor());
		CharSequence text = getText();
		canvas.drawText(text, 0, text.length(), mTextX, mTextY, getPaint());

	}
	
	
	/** Calculate where to put secondary text
	 * This method should get overloaded to change text location */
	protected void findSecondaryTextCoord(){
		mSecXCoord = mButtonWidth - mSecTextWidth - mSecAdditionalXOffset;
		mSecYCoord = mButtonHeight - 0 - mSecAdditionalYOffset;
	}
}

