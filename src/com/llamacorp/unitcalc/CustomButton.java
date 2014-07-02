package com.llamacorp.unitcalc;



import java.util.EventListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Button with click-animation effect.
 */
class CustomButton extends Button {
	static final int CLICK_FEEDBACK_INTERVAL = 10;
	static final int CLICK_FEEDBACK_DURATION = 350;
	float mTextX;
	float mTextY;
	long mAnimStart;
	EventListener mListener;
	Paint mFeedbackPaint;
	final Paint mHintPaint;
	float mTextSize = 0f;

	public CustomButton(Context context, AttributeSet attrs) {
		super(context, attrs);
//		Calculator calc = (Calculator) context;
		init();
//		mListener = calc.mListener;
//		setOnClickListener(mListener);
//		setOnLongClickListener(mListener);
		mHintPaint = new Paint(getPaint());
	}
	
	private void init() {
		mFeedbackPaint = new Paint();
		mFeedbackPaint.setStyle(Style.STROKE);
		mFeedbackPaint.setStrokeWidth(2);

		mAnimStart = -1;
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
		if (mHintPaint != null)
			mHintPaint.setTextSize(paint.getTextSize() * 70 / 100f);
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

		mHintPaint.setColor(Color.GRAY);
		CharSequence hint = getHint();
		if (hint != null) {
			int offsetX = getContext().getResources().getDimensionPixelSize(R.dimen.button_hint_offset_x);
			int offsetY = (int) ((mTextY + getContext().getResources().getDimensionPixelSize(R.dimen.button_hint_offset_y) - mHintPaint.getTextSize()) / 2) - getPaddingTop();

			float textWidth = mHintPaint.measureText(hint.toString());
			float width = getWidth() - getPaddingLeft() - getPaddingRight() - mTextX - offsetX;
			float textSize = mHintPaint.getTextSize();
			if (textWidth > width) {
				mHintPaint.setTextSize(textSize * width / textWidth);
			}

			canvas.drawText(getHint(), 0, getHint().length(), mTextX + offsetX, mTextY - offsetY, mHintPaint);
		}

		getPaint().setColor(getCurrentTextColor());
		CharSequence text = getText();
		canvas.drawText(text, 0, text.length(), mTextX, mTextY, getPaint());
	}
/*
	public void animateClickFeedback() {
		mAnimStart = System.currentTimeMillis();
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = super.onTouchEvent(event);

		switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				if (isPressed()) {
					animateClickFeedback();
				} else {
					invalidate();
				}
				break;
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_CANCEL:
				mAnimStart = -1;
				invalidate();
				break;
		}

		return result;
	}
	@Override
	public void setTypeface(Typeface tf) {
		if(mHintPaint != null) {
			if (mHintPaint.getTypeface() != tf) {
				mHintPaint.setTypeface(tf);
			}
		}
		super.setTypeface(tf);
	}
	*/
}

