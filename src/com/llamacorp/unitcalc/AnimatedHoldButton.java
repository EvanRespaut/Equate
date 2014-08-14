package com.llamacorp.unitcalc;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

class AnimatedHoldButton extends SecondaryTextButton {
	protected static final int SECONDARY_FONT_PERCENTAGE = 85;

	protected float mSecAdditionalXOffset = getContext().getResources().
			getDimensionPixelSize(R.dimen.button_secondary_text_additional_offset_x);
	protected float mSecAdditionalYOffset = getContext().getResources().
			getDimensionPixelSize(R.dimen.button_secondary_text_additional_offset_y);

	
	private OnClickListener mClickListen = null;
	private OnLongClickListener mLongClickListen = null;
	private Handler mColorHoldHandler;
	private boolean mLongClickPerformed=false;

	private int mInc;
	private static final int CLICK_HOLD_TIME=300;	


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public AnimatedHoldButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void findSecondaryTextCoord(){
		mSecXCoord = mButtonWidth - mSecTextWidth - mSecAdditionalXOffset;
		mSecYCoord = 0 + mSecTextHeight + mSecAdditionalYOffset;
	}


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

			setBackgroundColor(Color.argb(255, (int)deltaRed, (int)deltaGreen, (int)deltaBlue));
			mInc++;
		}
	};		


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
}

