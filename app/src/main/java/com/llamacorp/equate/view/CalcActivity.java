package com.llamacorp.equate.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.llamacorp.equate.Calculator;
import com.llamacorp.equate.R;
import com.llamacorp.equate.view.ConvKeysFragment.OnConvertKeySelectedListener;
import com.llamacorp.equate.view.ResultListFragment.OnResultSelectedListener;
import com.viewpagerindicator.TabPageIndicator;

public class CalcActivity  extends FragmentActivity
		  implements OnResultSelectedListener, OnConvertKeySelectedListener{
	private Context mAppContext;  //used for toasts and the like

	private ResultListFragment mResultListFrag;	//scroll-able history
	private EditTextDisplay mDisplay;  		//main display
	private ViewPager mUnitTypeViewPager;			//controls and displays UnitType 

	private Button mEqualsButton; //used for changing color

	private static final int[] BUTTON_IDS = {
			  R.id.zero_button, R.id.one_button,  R.id.two_button, R.id.three_button,
			  R.id.four_button,	R.id.five_button, R.id.six_button, R.id.seven_button,
			  R.id.eight_button,R.id.nine_button,

			  R.id.plus_button,
			  R.id.minus_button,
			  R.id.multiply_button,
			  R.id.divide_button,
			  R.id.percent_button,

			  R.id.decimal_button,
			  R.id.equals_button,
			  //		R.id.ee_button,
			  //		R.id.power_button,

			  R.id.clear_button,

			  R.id.open_para_button,
			  R.id.close_para_button,

	};

	//main calculator object
	public Calculator mCalc;// = new Calculator();

	//Crude fix: used to tell the ConvKeyViewPager what unit to select after
	// scrolling to correct UnitType
	private int unitPosToSelectAfterScroll=-1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppContext = this;
		setContentView(R.layout.activity_calc);

		//either get old calc or create a new one
		mCalc = Calculator.getCalculator(this);

		//main result display
		mDisplay = (EditTextDisplay)findViewById(R.id.textDisplay);
		mDisplay.setCalc(mCalc);
		mDisplay.disableSoftInputFromAppearing();

		//hold click will select all text
		mDisplay.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mDisplay.selectAll();
				//return false so Android consumes the rest of the event
				return false;
			}
		});

		//clicking display will set solve=false, and will make the cursor visible
		mDisplay.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					//once the user clicks on part of the expression, don't want # to delete it
					mCalc.setSolved(false);
					mDisplay.setCursorVisible(true);
					mDisplay.clearHighlighted();
				}
				return false;
			}
		});

		//use fragment manager to make the result list
		FragmentManager fm = getSupportFragmentManager();
		mResultListFrag = (ResultListFragment) fm.findFragmentById(R.id.resultListfragmentContainer);

		if(mResultListFrag == null){
			mResultListFrag = new ResultListFragment();
			fm.beginTransaction().add(R.id.resultListfragmentContainer, mResultListFrag).commit();
		}

		setupUnitTypePager();


		for(int id : BUTTON_IDS) {
			final Button button = (Button)findViewById(id);

			//used for coloring the equals button
			if(id == R.id.equals_button) mEqualsButton = button;

			if(id == R.id.percent_button) {
				((AnimatedHoldButton) button)
						  .setPrimaryText(mCalc.mPreferences.getPercentButMain());
				((AnimatedHoldButton) button)
						  .setSecondaryText(mCalc.mPreferences.getPercentButSec());
			}

			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					int buttonId = view.getId();
					String buttonValue = "";
					switch (buttonId) {
						case R.id.plus_button:
							buttonValue = "+";
							break;
						case R.id.minus_button:
							buttonValue = "-";
							break;
						case R.id.multiply_button:
							buttonValue = "*";
							break;
						case R.id.divide_button:
							buttonValue = "/";
							break;
						case R.id.percent_button:
							if(mCalc.mPreferences.getPercentButMain().equals("%"))
								buttonValue = "%";
							else
								buttonValue = "E";
							break;
						case R.id.decimal_button:
							buttonValue = ".";
							break;
						case R.id.equals_button:
							buttonValue = "=";
							break;
						//					case R.id.ee_button: buttonValue="E";
						//					break;
						//					case R.id.power_button: buttonValue="^";
						//					break;
						case R.id.clear_button:
							buttonValue = "c";
							break;
						case R.id.open_para_button:
							buttonValue = "(";
							break;
						case R.id.close_para_button:
							buttonValue = ")";
							break;
						case R.id.backspace_button:
							buttonValue = "b";
							break;
						default:
							//this for loop checks for numerical values
							for (int i = 0; i < 10; i++)
								if (buttonId == BUTTON_IDS[i])
									buttonValue = String.valueOf(i);
					}
					//pass button to calc, change conv key colors (maybe) and update screen
					numButtonPressed(buttonValue);
				}
			});

			final LinearLayout mUnitContain = (LinearLayout)findViewById(R.id.unit_container);


			button.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					int buttonId = view.getId();
					String buttonValue="";
					switch(buttonId){
						case R.id.multiply_button: buttonValue = "^";
							break;
						case R.id.equals_button: buttonValue = "g";
							break;
						case R.id.percent_button:
							if(mCalc.mPreferences.getPercentButSec().equals("EE"))
								buttonValue = "E";
							else
								buttonValue = "%";
							break;
						case R.id.nine_button: mCalc.refreshAllDynamicUnits(true);
							break;
						case R.id.minus_button: buttonValue = "n";
							break;
						case R.id.divide_button: buttonValue = "i";
							break;
						case R.id.eight_button:
							if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
								if (mUnitContain.getVisibility() == LinearLayout.GONE) {
									mUnitContain.setVisibility(LinearLayout.VISIBLE);
									updateScreen(true, true);
								} else
									mUnitContain.setVisibility(LinearLayout.GONE);
							}
							break;
						case R.id.open_para_button:
							buttonValue = "[";
							break;
						case R.id.close_para_button:
							buttonValue = "]";
							break;
						default:
							return true;
					}
					//pass button to calc, change conv key colors (maybe) and update screen
					if(!buttonValue.equals(""))
						numButtonPressed(buttonValue);
					return true;
				}
			});

			//extra long click for buttons with settings
			if(button instanceof AnimatedHoldButton){
				final AnimatedHoldButton ahb = (AnimatedHoldButton)button;
				ahb.setOnExtraLongClickListener(new AnimatedHoldButton.OnExtraLongClickListener() {
					@Override
					public void onExtraLongClick(View view) {
						int buttonId = view.getId();
						if (buttonId == R.id.percent_button){
							//TODO add code to pop up dialog to switch buttons
							//TODO dialog reads "Set primary button function:"
							//TODO options will be %, E, ^, 1/x, and +/-
							//simple swap
							String main = mCalc.mPreferences.getPercentButMain();
							String sec = mCalc.mPreferences.getPercentButSec();
							mCalc.mPreferences.setPercentButMain(sec);
							mCalc.mPreferences.setPercentButSec(main);
							ViewUtils.toastLong("Button changed to " + sec, mAppContext);
							ahb.setPrimaryText(sec);
							ahb.setSecondaryText(main);
							ahb.invalidate();
						}
					}
				});
			}
		}


		ImageButton backspaceButton = (ImageButton) findViewById(R.id.backspace_button);
		backspaceButton.setOnTouchListener(new View.OnTouchListener() {
			private Handler mColorHoldHandler;
			private Handler mResetHandler;
			private View mView;
			private static final int RESET_HOLD_TIME = 2200;
			private static final int CLEAR_HOLD_TIME = 300;
			//private int startTime;

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						mView = view;
						mInc = 0;

						if (mColorHoldHandler != null) return true;
						mColorHoldHandler = new Handler();
						mColorHoldHandler.postDelayed(mBackspaceColor, 10);

						if (mResetHandler != null) return true;
						mResetHandler = new Handler();
						mResetHandler.postDelayed(mBackspaceReset, RESET_HOLD_TIME);

						break;
					case MotionEvent.ACTION_UP:
						if (mColorHoldHandler == null) return true;
						if (mResetHandler == null) return true;
						numButtonPressed("b");
						view.setBackgroundColor(getResources().getColor(R.color.op_button_normal));
						mColorHoldHandler.removeCallbacks(mBackspaceColor);
						mColorHoldHandler = null;

						mResetHandler.removeCallbacks(mBackspaceReset);
						mResetHandler = null;
						break;
				}
				return false;
			}


			Runnable mBackspaceReset = new Runnable() {
				@Override
				public void run() {
					mCalc.resetCalc();
					setupUnitTypePager();
					updateScreen(true);
					ViewUtils.toastCentered("Calculator reset", mAppContext);
				}
			};


			private int mInc;
			//set up the runnable for when backspace is held down
			Runnable mBackspaceColor = new Runnable() {
				private int mStartColor = getResources().getColor(R.color.op_button_pressed);
				private int mEndColor = getResources().getColor(R.color.backspace_button_held);

				private static final int NUM_COLOR_CHANGES = 10;

				@Override
				public void run() {
					//after clear had been performed and 100ms is up, set color back to default
					if (mInc == -1){
						mView.setBackgroundColor(mEndColor);
						return;
					}
					//color the button black for a second and then clear
					if (mInc == NUM_COLOR_CHANGES){
						numButtonPressed("c");
						mView.setBackgroundColor(Color.argb(255, 0, 0, 0));
						mColorHoldHandler.postDelayed(this, 100);
						mInc = -1;
						return;
					}
					mColorHoldHandler.postDelayed(this, CLEAR_HOLD_TIME / NUM_COLOR_CHANGES);

					float deltaRed = (float) Color.red(mStartColor) + ((float) Color.red(mEndColor) - (float) Color.red(mStartColor)) * ((float) mInc * (float) mInc * (float) mInc) / ((float) NUM_COLOR_CHANGES * (float) NUM_COLOR_CHANGES * (float) NUM_COLOR_CHANGES);

					int deltaGreen = Color.green(mStartColor) + ((Color.green(mEndColor) - Color.green(mStartColor)) * mInc) / NUM_COLOR_CHANGES;
					int deltaBlue = Color.blue(mStartColor) + ((Color.blue(mEndColor) - Color.blue(mStartColor)) * mInc) / NUM_COLOR_CHANGES;

					mView.setBackgroundColor(Color.argb(255, (int) deltaRed, deltaGreen, deltaBlue));
					mInc++;
				}
			};
		});
	}


	private void setupUnitTypePager(){
		//use fragment manager to make the result list
		FragmentManager fm = getSupportFragmentManager();

		mUnitTypeViewPager = (ViewPager)findViewById(R.id.convertKeyPager);

		mUnitTypeViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
			@Override
			public int getCount(){
				return mCalc.getUnitTypeSize();
			}

			@Override
			public Fragment getItem(int pos){
				return ConvKeysFragment.newInstance(pos);
			}

			@Override
			public CharSequence getPageTitle(int pos) {
				return mCalc.getUnitTypeName(pos % mCalc.getUnitTypeSize());
			}
		});

		TabPageIndicator unitTypePageIndicator = (TabPageIndicator)findViewById(R.id.titles);
		unitTypePageIndicator.setViewPager(mUnitTypeViewPager);

		//need to tell calc when a new UnitType page is selected
		unitTypePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			//as the page is being scrolled to
			@Override
			public void onPageSelected(int pos) {
				//update the calc with current UnitType selection
				mCalc.setUnitTypePos(pos);

				//if we just switched to a dynamic unit, attempt an update
				if(mCalc.getCurrUnitType().containsDynamicUnits())
					mCalc.refreshAllDynamicUnits(false);

				//clear selected unit from adjacent convert key fragment so you
				//a bit of it
				int currUnitTypePos = mUnitTypeViewPager.getCurrentItem();
				clearUnitSelection(currUnitTypePos-1);
				clearUnitSelection(currUnitTypePos);
				clearUnitSelection(currUnitTypePos+1);
				mCalc.getCurrUnitType().clearUnitSelection();

				//if this change in UnitType was result of unit-ed result selection,
				// select that unit
				if(unitPosToSelectAfterScroll != -1){
					ConvKeysFragment frag = getConvKeyFrag(mUnitTypeViewPager.getCurrentItem());
					if (frag != null) frag.selectUnitAtUnitArrayPos(unitPosToSelectAfterScroll);
					unitPosToSelectAfterScroll = -1;
				}
				//clear out the unit in expression if it's now cleared
				updateScreen(false);

				//move the cursor to the right end (helps usability a bit)
				mDisplay.setSelectionToEnd();
			}

			@Override
			public void onPageScrolled(int pos, float posOffset, int posOffsetPixels) {}

			@Override
			public void onPageScrollStateChanged(int state) {}
		});

		//set page back to the previously selected page
		mUnitTypeViewPager.setCurrentItem(mCalc.getUnitTypePos());
	}



	/**
	 * Called when any non convert key is pressed
	 * @param keyPressed ASCII representation of the key pressed ("1", "=" "*", etc)
	 */
	public void numButtonPressed(String keyPressed){
		//pass button value to CalcAcitvity to pass to calc
		boolean performedSolve = mCalc.parseKeyPressed(keyPressed);

		//TODO this logic belongs in Calculator, but passing back to display
		// a dialog is hard
		if(keyPressed.equals("=") && mCalc.isUnitSelected() && !mCalc.isSolved()){
			new AlertDialog.Builder(this)
					  .setMessage("Click a different unit to convert")
					  .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						  public void onClick(DialogInterface dialog, int which) {}})
					  .show();
		}

		//update the result list and do it with the normal scroll (not fast)
		updateScreen(performedSolve);
	}



	/**
	 * Selects the a unit (used by result list)
	 * @see com.llamacorp.equate.view.ResultListFragment.OnResultSelectedListener
	 */
	public void selectUnitAtUnitArrayPos(int unitPos, int unitTypePos){
		//if not on right page, scroll there first
		if(unitTypePos != mUnitTypeViewPager.getCurrentItem()){
			unitPosToSelectAfterScroll = unitPos;
			mUnitTypeViewPager.setCurrentItem(unitTypePos);
		}
		else {
			ConvKeysFragment frag = getConvKeyFrag(mUnitTypeViewPager.getCurrentItem());
			if (frag != null) frag.selectUnitAtUnitArrayPos(unitPos);
		}
	}


	/** Grabs newest data from Calculator, updates the main display, and gives an
	 * option to scroll down the result list
	 * @param updateResult pass true to update result list
	 * @param instaScroll pass true to scroll instantly, otherwise use animation
	 */
	private void updateScreen(boolean updateResult, boolean instaScroll){
		mDisplay.updateTextFromCalc(); //Update EditText view

		//if we hit equals, update result list
		if(updateResult)
			mResultListFrag.refresh(instaScroll);
	}

	/**
	 * Grabs newest data from Calculator, updates the main display
	 * @param updateResult whether or not to update result
	 */
	public void updateScreen(boolean updateResult){
		//no insta scroll for previous expression
		updateScreen(updateResult, false);

		//see if colored convert button should be not colored (if backspace or 
		//clear were pressed, or if expression solved)
		if(!mCalc.isUnitSelected())
			clearUnitSelection(mUnitTypeViewPager.getCurrentItem());
	}


	/**
	 * Changes equals button color according the the input boolean value.
	 * Equals button is colored normally when button is not selected. When 
	 * a unit is selected, equals button looks like a regular op button
	 */
	public void setEqualButtonColor(boolean unHighlighted){
		mEqualsButton.setSelected(unHighlighted);
	}



	/**
	 * Clear the unit selection for unit type fragment at position pos
	 * @param unitTypeFragPos the position of the desired unit type fragment 
	 * from which to clear selected units 
	 */
	private void clearUnitSelection(int unitTypeFragPos){
		ConvKeysFragment currFragAtPos = getConvKeyFrag(unitTypeFragPos);
		if(currFragAtPos != null)
			currFragAtPos.clearButtonSelection();
	}


	/**
	 * Helper function to return the convert key fragment at position pos
	 * @param pos the position of the desired convert key fragment
	 * @return will return the fragment or null if it doesn't exist at that position
	 */
	private ConvKeysFragment getConvKeyFrag(int pos){
		FragmentStatePagerAdapter tempAdapter =
				  (FragmentStatePagerAdapter) mUnitTypeViewPager.getAdapter();
		//make sure we aren't trying to access an invalid page fragment
		if(pos < tempAdapter.getCount() && pos >= 0){
			return (ConvKeysFragment) tempAdapter.
					  instantiateItem(mUnitTypeViewPager, pos);
		}
		else return null;
	}


	@Override
	public void onPause() {
		super.onPause();
		try {
			Calculator.getCalculator(this).saveState();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onResume(){
		super.onResume();

		//maybe fixes that random crash?
		if(mCalc == null)
			return;

		if(mCalc.getCurrUnitType().containsDynamicUnits())
			mCalc.refreshAllDynamicUnits(false);

		//only set display to Equate if no expression is there yet
		if(mCalc.toString().equals("") && mCalc.getResultList().size()==0){
			mDisplay.setText(R.string.app_name);
			mDisplay.setCursorVisible(false);
		}
		else{
			updateScreen(true, true);
			mDisplay.setSelectionToEnd();
			//pull ListFrag's focus, to be sure EditText's cursor blinks when app starts
			mDisplay.requestFocus();
		}
	}
}
