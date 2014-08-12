package com.llamacorp.unitcalc;
import java.util.EventListener;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

class CustomButton extends Button {
	static final int CLICK_FEEDBACK_INTERVAL = 10;
	static final int CLICK_FEEDBACK_DURATION = 350;
	float mTextX;
	float mTextY;
	long mAnimStart;
	EventListener mListener;
	Paint mFeedbackPaint;
	private Drawable mBackground;
	final Paint mHintPaint;
	final Paint mSecondaryPaint;
	String mSecondaryText;
	float mTextSize = 0f;
	private OnClickListener mClickListen = null;
	private OnLongClickListener mLongClickListen = null;
	private Handler mColorHoldHandler;
	private boolean mLongClickPerformed=false;


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public CustomButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomButton, 0, 0);
		try {
			mSecondaryText = ta.getString(R.styleable.CustomButton_secondary_text);
		} finally {
			ta.recycle();
		}

		//		Calculator calc = (Calculator) context;
		init();
		//		mListener = calc.mListener;
		//		setOnClickListener(mListener);
		//		setOnLongClickListener(mListener);

		mBackground = getBackground();

		mHintPaint = new Paint(getPaint());
		mSecondaryPaint = new Paint(getPaint());

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
		if (mSecondaryPaint != null)
			mSecondaryPaint.setTextSize(paint.getTextSize() * 85 / 100f);
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
		mSecondaryPaint.setColor(getResources().getColor(R.color.button_secondary_text));
		CharSequence hint = getHint();
		float buttonHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		float buttonWidth = getWidth() - getPaddingLeft() - getPaddingRight();

		if (hint != null) {

			float hintTextWidth = mHintPaint.measureText(getHint().toString());
			float hintAdditionalXOffset = getContext().getResources().getDimensionPixelSize(R.dimen.button_ellipses_additional_offset_x);

			float hintTextHeight = 0f; //mHintPaint.getTextSize();//TODO this is wrong...
			float hintAdditionalYOffset = getContext().getResources().getDimensionPixelSize(R.dimen.button_ellipses_additional_offset_y);

			canvas.drawText(getHint(), 0, getHint().length(), buttonWidth - hintTextWidth - hintAdditionalXOffset, 
					buttonHeight - hintTextHeight - hintAdditionalYOffset, mHintPaint);
		}
		if(mSecondaryText != null){
			float secondaryTextWidth = mSecondaryPaint.measureText(mSecondaryText);
			float secondaryAdditionalXOffset = getContext().getResources().getDimensionPixelSize(R.dimen.button_secondary_text_additional_offset_x);

			float secondaryTextHeight = mSecondaryPaint.getTextSize();
			float secondaryAdditionalYOffset = getContext().getResources().getDimensionPixelSize(R.dimen.button_secondary_text_additional_offset_y);

			canvas.drawText(mSecondaryText, 0, mSecondaryText.length(), 
					buttonWidth - secondaryTextWidth - secondaryAdditionalXOffset, 
					0 + secondaryTextHeight + secondaryAdditionalYOffset, 
					mSecondaryPaint);
		}

		getPaint().setColor(getCurrentTextColor());
		CharSequence text = getText();
		canvas.drawText(text, 0, text.length(), mTextX, mTextY, getPaint());
	}

	
	private int mInc;
	private static final int CLICK_HOLD_TIME=300;	

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mInc=0;
			mLongClickPerformed = false;
			
			if (mColorHoldHandler != null) return true;
			mColorHoldHandler = new Handler();
			mColorHoldHandler.postDelayed(mColorRunnable, 10);
			break;
		case MotionEvent.ACTION_UP:
			if (mColorHoldHandler == null) return true;
			if(!mLongClickPerformed) 
				myClickButton();

			setBackgroundColor(getResources().getColor(R.color.op_button_normal));
			mColorHoldHandler.removeCallbacks(mColorRunnable);
			mColorHoldHandler = null;

			break;
		}
		return true;
	}
	


	//set up the runnable for when backspace is held down
	Runnable mColorRunnable = new Runnable() {
		private int mGradStartCol = getResources().getColor(R.color.op_button_pressed);
		private int mGradEndCol = getResources().getColor(R.color.op_button_long_press_accent);
		private int mAccentColor = getResources().getColor(R.color.op_button_pressed);
		private int mFinalColor = getResources().getColor(R.color.op_button_pressed);

		private static final int NUM_COLOR_CHANGES=10;

		@Override 
		public void run() {
			//after clear had been performed and 100ms is up, set color to final
			if(mInc==-1){
				setBackgroundColor(mFinalColor);
				return;
			}
			//color the button black for a second and then clear
			if(mInc==NUM_COLOR_CHANGES){
				myLongClickButton();
				mLongClickPerformed = true;
				setBackgroundColor(mAccentColor);
				//only post again so it runs to catch the final bit of code
				mColorHoldHandler.postDelayed(this, 100);
				mInc=-1;
				return;
			}
			mColorHoldHandler.postDelayed(this, CLICK_HOLD_TIME/NUM_COLOR_CHANGES);

			float deltaRed= (float)Color.red(mGradStartCol) + ((float)Color.red(mGradEndCol)-(float)Color.red(mGradStartCol))*((float)mInc)/((float)NUM_COLOR_CHANGES);
			float deltaGreen= (float)Color.green(mGradStartCol) + ((float)Color.green(mGradEndCol)-(float)Color.green(mGradStartCol))*((float)mInc)/((float)NUM_COLOR_CHANGES);
			float deltaBlue= (float)Color.blue(mGradStartCol) + ((float)Color.blue(mGradEndCol)-(float)Color.blue(mGradStartCol))*((float)mInc)/((float)NUM_COLOR_CHANGES);
//			int deltaGreen= Color.green(mStartColor) + ((Color.green(mEndColor)-Color.green(mStartColor))*mInc)/NUM_COLOR_CHANGES;
//			int deltaBlue= Color.blue(mStartColor) + ((Color.blue(mEndColor)-Color.blue(mStartColor))*mInc)/NUM_COLOR_CHANGES;

			setBackgroundColor(Color.argb(255, (int)deltaRed, (int)deltaGreen, (int)deltaBlue));
			mInc++;
		}
	};		
	
	

	
	
	@SuppressWarnings("deprecation")
	public void setBackgroundColorToDefault(){
		setBackgroundDrawable(mBackground);
	}


	private void myClickButton(){
		if(mClickListen != null)
			mClickListen.onClick(this);
	}

	private void myLongClickButton(){
		if(mLongClickListen != null)
			mLongClickListen.onLongClick(this);
	}
	
	@Override
	public void setOnClickListener(OnClickListener l){
		mClickListen = l;
	}


	@Override
	public void setOnLongClickListener(OnLongClickListener l){
		mLongClickListen = l;
	}

	/*
	public void animateClickFeedback() {
		mAnimStart = System.currentTimeMillis();
		invalidate();
	}
	 */
	/*
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

