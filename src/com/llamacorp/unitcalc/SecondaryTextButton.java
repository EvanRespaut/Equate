package com.llamacorp.unitcalc;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

class SecondaryTextButton extends Button {
	protected static final int SECONDARY_FONT_PERCENTAGE = 70;
	
	private float mTextX;
	private float mTextY;
	private float mTextSize = 0f;
	protected Paint mSecondaryPaint;
	protected String mSecondaryText;

	//the following are used to determine where to place the secondary text
	protected float mButtonHeight;
	protected float mButtonWidth;
	protected float mSecTextWidth;
	protected float mSecAdditionalXOffset;
	protected float mSecTextHeight;
	protected float mSecAdditionalYOffset;

	//x and y coordinates for the seconday text
	protected float mSecXCoord;
	protected float mSecYCoord;


	public SecondaryTextButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		//grab custom resource variable
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SecondaryTextButton, 0, 0);
		try {
			mSecondaryText = ta.getString(R.styleable.SecondaryTextButton_secondary_text);
		} finally {
			ta.recycle();
		}

		mSecondaryPaint = new Paint(getPaint());
	}


	/** Set secondary text string */
	public void setSecondaryText(String text){
		mSecondaryText = text;
	}


	private void layoutText() {
		Paint paint = getPaint();
		if (mTextSize != 0f) paint.setTextSize(mTextSize);
		float textWidth = paint.measureText(getText().toString());
		float width = getWidth() - getPaddingLeft() - getPaddingRight();
		float textSize = getTextSize();
		if (textWidth > width) {
			paint.setTextSize(textSize * width / textWidth);
			mTextX = getPaddingLeft();
			mTextSize = textSize;
		} else {
			mTextX = (getWidth() - textWidth) / 2;
		}
		mTextY = (getHeight() - paint.ascent() - paint.descent()) / 2;
		if (mSecondaryPaint != null)
			mSecondaryPaint.setTextSize(paint.getTextSize() * SECONDARY_FONT_PERCENTAGE / 100f);
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


	@Override
	protected void onDraw(Canvas canvas) {
		mSecondaryPaint.setColor(getResources().getColor(R.color.button_secondary_text));

		if(mSecondaryText != null){
			mButtonHeight = getHeight() - getPaddingTop() - getPaddingBottom();
			mButtonWidth = getWidth() - getPaddingLeft() - getPaddingRight();

			mSecTextWidth = mSecondaryPaint.measureText(mSecondaryText);
			mSecAdditionalXOffset = getContext().getResources().getDimensionPixelSize(R.dimen.button_ellipses_additional_offset_x);

			mSecTextHeight = mSecondaryPaint.getTextSize();
			mSecAdditionalYOffset = getContext().getResources().getDimensionPixelSize(R.dimen.button_ellipses_additional_offset_y);

			findSecondaryTextCoord();

			canvas.drawText(mSecondaryText, 0, mSecondaryText.length(), 
					mSecXCoord, 
					mSecYCoord, 
					mSecondaryPaint);
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

