package com.llamacorp.unitcalc;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.llamacorp.unitcalc.ConvKeysFragment.OnConvertKeySelectedListener;
import com.llamacorp.unitcalc.ResultListFragment.OnResultSelectedListener;
import com.viewpagerindicator.TabPageIndicator;

public class CalcActivity  extends FragmentActivity implements OnResultSelectedListener, OnConvertKeySelectedListener{
	private ViewPager mConvKeysViewPager; 
	private ResultListFragment mResultFragment;

    private static final String[] CONTENT = new String[] { "Temp", "Weight", "Length", "Area", "Volume"};
	
	private List<Button> calcButton;
	private EditTextCursorWatcher mDisplay;

	private static final int[] BUTTON_IDS = {
		R.id.zero_button,
		R.id.one_button, 
		R.id.two_button,
		R.id.three_button,
		R.id.four_button,
		R.id.five_button, 
		R.id.six_button,
		R.id.seven_button,
		R.id.eight_button,
		R.id.nine_button,

		R.id.plus_button,
		R.id.minus_button,
		R.id.multiply_button,
		R.id.divide_button,

		R.id.decimal_button,
		R.id.equals_button,
		R.id.ee_button,
		R.id.power_button,
		R.id.percent_button,

		R.id.open_para_button,
		R.id.close_para_button,

	};

	//main calculator object
	public Calculator mCalc;// = new Calculator();

	/**
	 * Called when any non convert key is pressed
	 * @param keyPressed ASCII representation of the key pressed ("1", "=" "*", etc)
	 */
	public void numButtonPressed(String keyPressed){
		//pass button value to CalcAcitvity to pass to calc
		mCalc.parseKeyPressed(keyPressed);

		//update the result list and do it with the normal scroll (not fast)
		updateScreen(keyPressed.equals("="));
	}

	//Crude fix: used to tell the ConvKeyViewPager what unit to select after scrolling to correct UnitType
	private Unit unitToSelectAfterScroll;

	/**
	 * Selects the a unit (used by result list)
	 * @see com.llamacorp.unitcalc.ResultListFragment.OnResultSelectedListener#selectUnit(int)
	 */
	public void selectUnit(Unit unit, int unitTypePos){
		//if not on right page, scroll there first
		if(unitTypePos!=mConvKeysViewPager.getCurrentItem()){
			unitToSelectAfterScroll=unit;
			mConvKeysViewPager.setCurrentItem(unitTypePos);
		}
		else
			getConvKeyFrag(mConvKeysViewPager.getCurrentItem()).selectUnit(unit);
	}


	/**
	 * Updates the current expression and result list
	 * @param updateResult whether or not to update result
	 */
	public void updateScreen(boolean updateResult){
		//no insta scroll for previous expression
		updateScreenWithInstaScrollOption(updateResult, false);

		//see if colored convert button should be not colored (if backspace or clear were pressed, or if expression solved)
		if(!mCalc.isUnitIsSet())
			clearConvKeyForFragPos(mConvKeysViewPager.getCurrentItem());
	}


	/**
	 * Function 
	 * @param updateResult
	 * @param instaScroll
	 */
	private void updateScreenWithInstaScrollOption(boolean updateResult, boolean instaScroll){
		//Update EditText view
		mDisplay.updateTextFromCalc();

		//if we hit equals, update result list
		if(updateResult){
			FragmentManager fm = getSupportFragmentManager();
			ResultListFragment resultListFragment = (ResultListFragment)fm.findFragmentById(R.id.resultListfragmentContainer);
			resultListFragment.refresh(instaScroll);

			//make a little gray divider above expression when prev expression hits it

			View divider = findViewById(R.id.prev_curr_exp_divider);
			ListView mResultListView = mResultFragment.getListView();
			//don't try this unless result list has something there
			if(mResultListView.getChildCount()>0){
				//test to see if the last child's bottom edge is greater than the the total result list height
				//note that 0 is top of screen; also note that an extra child height is needed to reach the bottom
				if(mResultListView.getChildAt(mResultListView.getChildCount() - 1).getBottom() + 
						mResultListView.getChildAt(mResultListView.getChildCount() - 1).getHeight() >= mResultListView.getHeight())
					divider.setBackgroundColor(getResources().getColor(R.color.prev_curr_exp_divider));
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calc);

		//either get old calc or create a new one
		mCalc = Calculator.getCalculator(this);

		//main result display
		mDisplay = (EditTextCursorWatcher)findViewById(R.id.textDisplay);
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
				}
				return false;
			}
		});

		//use fragment manager to make the result list
		FragmentManager fm = getSupportFragmentManager();
		mResultFragment = (ResultListFragment) fm.findFragmentById(R.id.resultListfragmentContainer);

		if(mResultFragment == null){
			mResultFragment = new ResultListFragment();
			fm.beginTransaction().add(R.id.resultListfragmentContainer, mResultFragment).commit();			
		}


		mConvKeysViewPager = (ViewPager)findViewById(R.id.convertKeyPager);

		mConvKeysViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
			@Override
			public int getCount(){
				//return 10000;
				return mCalc.getUnitTypeSize();
			}

			@Override
			public Fragment getItem(int pos){
				//				System.out.println("pos="+pos);
				//				System.out.println("pos % mCalc.getUnitTypeSize()"+pos % mCalc.getUnitTypeSize());
				//				return ConvKeysFragment.newInstance(pos % mCalc.getUnitTypeSize());
				//				//TODO try debugging this 
				return ConvKeysFragment.newInstance(pos);
			}

	        @Override
	        public CharSequence getPageTitle(int position) {
	            return CONTENT[position % CONTENT.length];
	        }
		});

		/*
		CirclePageIndicator viewPageIndicator = (CirclePageIndicator)findViewById(R.id.titles);
		viewPageIndicator.setViewPager(mConvKeysViewPager);
		viewPageIndicator.setFillColor(Color.GRAY);
		viewPageIndicator.setStrokeColor(Color.GRAY);
		*/
		TabPageIndicator viewPageIndicator = (TabPageIndicator)findViewById(R.id.titles);
		viewPageIndicator.setViewPager(mConvKeysViewPager);
		
		//need to tell calc when a new UnitType page is selected
		viewPageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			//as the page is being scrolled to
			@Override
			public void onPageSelected(int pos) {
				//update the calc with current UnitType selection
				mCalc.setUnitTypePos(pos);

				//set the margins so that you can see a bit of the left and right pages
				DisplayMetrics metrics = getResources().getDisplayMetrics();
				int padLeft=(int) (metrics.density * 8f + 0.5f);
				int padRight=(int) (metrics.density * 8f + 0.5f);
				//				if(mViewPager.getCurrentItem()==0)
				//					padLeft=0;
				//				if(mViewPager.getCurrentItem()==mViewPager.getAdapter().getCount()-1)
				//					padRight=0;
				mConvKeysViewPager.setPadding(padLeft, 0, padRight, 0);

				//clear selected unit from adjacent convert key fragment so you can't see a bit of them
				int currConvKeyPos = mConvKeysViewPager.getCurrentItem();
				clearConvKeyForFragPos(currConvKeyPos-1);
				clearConvKeyForFragPos(currConvKeyPos+1);

				//if user clicks unit-ed result, scroll to that UnitType fragment
				if(unitToSelectAfterScroll!=null){
					getConvKeyFrag(mConvKeysViewPager.getCurrentItem()).selectUnit(unitToSelectAfterScroll);
					unitToSelectAfterScroll=null;
				}
			}

			@Override
			public void onPageScrolled(int pos, float posOffset, int posOffsetPixels) {}

			@Override
			public void onPageScrollStateChanged(int state) {}
		});


		DisplayMetrics metrics = getResources().getDisplayMetrics();

		int padLeft=(int) (metrics.density * 8f + 0.5f);
		int padRight=padLeft;
		//		if(mViewPager.getCurrentItem()==0)
		//			padLeft=0;
		//		if(mViewPager.getCurrentItem()==mViewPager.getAdapter().getCount()-1)
		//			padRight=0;

		mConvKeysViewPager.setPadding(padLeft, 0, padRight, 0);

		//this shows the edges of the next page work
		mConvKeysViewPager.setClipToPadding(false);
		//add a little break between pages
		mConvKeysViewPager.setPageMargin(8);
		//set page back to the previously selected page
		mConvKeysViewPager.setCurrentItem(mCalc.getUnitTypePos());


		calcButton = new ArrayList<Button>();


		for(int id : BUTTON_IDS) {
			Button button = (Button)findViewById(id);
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					int buttonId = view.getId();
					String buttonValue="";
					switch(buttonId){
					case R.id.plus_button: buttonValue="+";
					break;
					case R.id.minus_button: buttonValue="-";
					break;
					case R.id.multiply_button: buttonValue="*";
					break;
					case R.id.divide_button: buttonValue="/";
					break;
					case R.id.decimal_button: buttonValue=".";
					break;
					case R.id.equals_button: buttonValue="=";
					break;
					case R.id.ee_button: buttonValue="E";
					break;
					case R.id.power_button: buttonValue="^";
					break;
					case R.id.percent_button: buttonValue="%";
					break;
					case R.id.open_para_button: buttonValue="(";
					break;
					case R.id.close_para_button: buttonValue=")";
					break;
					case R.id.backspace_button: buttonValue="b";
					break;
					default: 					
						//this for loop checks for numerical values
						for(int i=0;i<10;i++)
							if(buttonId==BUTTON_IDS[i])
								buttonValue=String.valueOf(i);
					}
					//pass button to calc, change conv key colors (maybe) and update screen
					numButtonPressed(buttonValue);
				}
			});

			//custom text for EE button
			//if(id==R.id.ee_button){
			/*SpannableString text = new SpannableString("    EE  ^\n");   


				// make "Lorem" (characters 0 to 5) red   
				text.setSpan(new ForegroundColorSpan(Color.GRAY), text.length()-2, text.length()-1, 0);   
				text.setSpan(new SuperscriptSpan(), text.length()-2, text.length()-1, 0);  
				text.setSpan(new RelativeSizeSpan(1.1f), text.length()-2, text.length()-1, 0);  

				// shove our styled text into the TextView           
				button.setText(text, BufferType.SPANNABLE);  
			 */
			//}
			//add to our list of num buttons
			calcButton.add(button);
		}


		ImageButton backspaceButton = (ImageButton) findViewById(R.id.backspace_button);
		backspaceButton.setOnTouchListener(new View.OnTouchListener() {
			private Handler mColorHoldHandler;
			private View mView;
			//private int startTime;

			@Override 
			public boolean onTouch(View view, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mView=view;
					mInc=0;

					if (mColorHoldHandler != null) return true;
					mColorHoldHandler = new Handler();
					mColorHoldHandler.postDelayed(mBackspaceColor, 10);

					//startTime = System.currentTimeMillis();

					break;
				case MotionEvent.ACTION_UP:
					if (mColorHoldHandler == null) return true;
					numButtonPressed("b");
					view.setBackgroundColor(getResources().getColor(R.color.op_button_normal));
					mColorHoldHandler.removeCallbacks(mBackspaceColor);
					mColorHoldHandler = null;
					break;
				}
				return false;
			}



			private int mInc;
			//set up the runnable for when backspace is held down
			Runnable mBackspaceColor = new Runnable() {
				private int mStartColor = getResources().getColor(R.color.op_button_pressed);
				private int mEndColor = getResources().getColor(R.color.backspace_button_held);

				private static final int NUM_COLOR_CHANGES=10;
				private static final int BACKSPACE_HOLD_TIME=500;			

				@Override 
				public void run() {
					if(mInc==-1){
						mView.setBackgroundColor(mEndColor);
						return;
					}
					if(mInc==NUM_COLOR_CHANGES){
						numButtonPressed("c");
						//System.out.println("TOTAL TIME =" + String.valueOf(System.currentTimeMillis()-startTime));
						mView.setBackgroundColor(Color.argb(255, 0, 0, 0));
						mColorHoldHandler.postDelayed(this, 100);
						mInc=-1;
						return;
					}
					mColorHoldHandler.postDelayed(this, BACKSPACE_HOLD_TIME/NUM_COLOR_CHANGES);

					float deltaRed= (float)Color.red(mStartColor) + ((float)Color.red(mEndColor)-(float)Color.red(mStartColor))*((float)mInc*(float)mInc*(float)mInc)/((float)NUM_COLOR_CHANGES*(float)NUM_COLOR_CHANGES*(float)NUM_COLOR_CHANGES);
					//					int deltaGreen= Color.green(mStartColor) + ((Color.green(mEndColor)-Color.green(mStartColor))*mInc^3)/NUM_COLOR_CHANGES^3;
					//					int deltaBlue= Color.blue(mStartColor) + ((Color.blue(mEndColor)-Color.blue(mStartColor))*mInc^3)/NUM_COLOR_CHANGES^3;

					//					int deltaRed= Color.red(mStartColor) + ((Color.red(mEndColor)-Color.red(mStartColor))*mInc)/NUM_COLOR_CHANGES;
					int deltaGreen= Color.green(mStartColor) + ((Color.green(mEndColor)-Color.green(mStartColor))*mInc)/NUM_COLOR_CHANGES;
					int deltaBlue= Color.blue(mStartColor) + ((Color.blue(mEndColor)-Color.blue(mStartColor))*mInc)/NUM_COLOR_CHANGES;

					mView.setBackgroundColor(Color.argb(255, (int)deltaRed, deltaGreen, deltaBlue));
					System.out.println("deltaRed="+deltaRed);
					System.out.println("((Color.red(mEndColor)-Color.red(mStartColor))*mInc^3)"+((Color.red(mEndColor)-Color.red(mStartColor))*mInc^3));
					System.out.println("mInc="+mInc);
					mInc++;
				}
			};			
		});
	}


	@Override
	public void onResume(){
		super.onResume();

		//maybe fixes that random crash?
		if(mCalc==null)
			return;

		//only set display to UnitCalc if no expression is there yet
		if(mCalc.toString().equals("") && mCalc.getResultList().size()==0){
			mDisplay.setText(R.string.app_name);
			mDisplay.setCursorVisible(false);
		}
		else{
			updateScreenWithInstaScrollOption(true, true);
			mDisplay.setSelectionToEnd();
		}
	}

	
	/**
	 * Clear the unit selection for convert key fragment at position pos
	 * @param pos the position of the desired convert key fragment to clear selected units from 
	 */
	private void clearConvKeyForFragPos(int pos){
		ConvKeysFragment currFragLeft = getConvKeyFrag(pos);
		if(currFragLeft!=null)
			currFragLeft.clearButtonSelection();
	}
	
	
	/**
	 * Helper function to return the convert key fragment at position pos
	 * @param pos the position of the desired convert key fragment
	 * @return will return the fragment or null if it doesn't exist at that position
	 */
	private ConvKeysFragment getConvKeyFrag(int pos){
		FragmentStatePagerAdapter tempAdapter = (FragmentStatePagerAdapter) mConvKeysViewPager.getAdapter();
		//make sure we aren't trying to access an invalid page fragment
		if(pos<tempAdapter.getCount() && pos>=0){
			ConvKeysFragment currFrag = (ConvKeysFragment) tempAdapter.instantiateItem(mConvKeysViewPager, pos);
			return currFrag;
		}
		else return null;
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calc, menu);
		return true;
	}
}
