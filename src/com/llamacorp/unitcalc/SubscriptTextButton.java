package com.llamacorp.unitcalc;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

class SubscriptTextButton extends Button {
	private float mTextX;
	private float mTextY;
	private final Paint mSecondaryPaint;
	private String mSecondaryText;
	private float mTextSize = 0f;

	public SubscriptTextButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomButton, 0, 0);
	    try {
	    	mSecondaryText = ta.getString(R.styleable.CustomButton_secondary_text);
	    } finally {
	        ta.recycle();
	    }
		
		mSecondaryPaint = new Paint(getPaint());
	}
	
	
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
			mSecondaryPaint.setTextSize(paint.getTextSize() * 70 / 100f);
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
		float buttonHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		float buttonWidth = getWidth() - getPaddingLeft() - getPaddingRight();

		if(mSecondaryText != null){
			float secondaryTextWidth = mSecondaryPaint.measureText(mSecondaryText);
			float secondaryAdditionalXOffset = getContext().getResources().getDimensionPixelSize(R.dimen.button_ellipses_additional_offset_x);
			
			float secondaryTextHeight = 0f; //mSecondaryPaint.getTextSize();//TODO this is wrong...
			float secondaryAdditionalYOffset = getContext().getResources().getDimensionPixelSize(R.dimen.button_ellipses_additional_offset_y);

			canvas.drawText(mSecondaryText, 0, mSecondaryText.length(), buttonWidth - secondaryTextWidth - secondaryAdditionalXOffset, 
					buttonHeight - secondaryTextHeight - secondaryAdditionalYOffset, mSecondaryPaint);
		}

		getPaint().setColor(getCurrentTextColor());
		CharSequence text = getText();
		canvas.drawText(text, 0, text.length(), mTextX, mTextY, getPaint());
	}
}

