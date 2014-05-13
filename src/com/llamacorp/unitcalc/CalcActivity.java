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
import android.util.SparseArray;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.llamacorp.unitcalc.ConvertKeysFragment.OnConvertKeySelectedListener;
import com.llamacorp.unitcalc.ResultListFragment.OnResultSelectedListener;

public class CalcActivity  extends FragmentActivity implements OnResultSelectedListener, OnConvertKeySelectedListener{

	private ViewPager mViewPager; 
	private ResultListFragment mResultFragment;

	private List<Button> calcButton;
	//private List<Button> convButton;
	private EditTextCursorWatcher mDisplay;
	//private TextView mPrevDisplay;
	//private HorizontalScrollView mHorizontalScroll;

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

		R.id.open_para_button,
		R.id.close_para_button,

	};

	//main calculator object
	public Calculator mCalc;// = new Calculator();

	//maps id's of buttons to convert values
	SparseArray<Double> units = new SparseArray<Double>();

	//called when any non convert key is pressed
	public void numButtonPressed(String keyPressed){
		//pass button value to CalcAcitvity to pass to calc
		mCalc.parseKeyPressed(keyPressed);

		//update the prev expression and do it with the normal scroll (not fast)
		updateScreen(keyPressed.equals("="));
	}

	/**
	 * Selects the a unit (used by prev result list)
	 * @see com.llamacorp.unitcalc.ResultListFragment.OnResultSelectedListener#selectUnit(int)
	 */
	public void selectUnit(Unit unit){
		//NOT SURE IF THIS IS A PROPER WAY TO DO THIS
		FragmentStatePagerAdapter tempAdapter = (FragmentStatePagerAdapter) mViewPager.getAdapter();
		ConvertKeysFragment currFrag = (ConvertKeysFragment) tempAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
		currFrag.selectUnit(unit);
	}

	static final String strExpressionEnd = " =";


	/**
	 * Updates the current and previous answers
	 * @param updatePrev whether or not to update previous answer
	 */
	public void updateScreen(boolean updatePrev){
		//no insta scroll for previous expression
		updateScreenWithInstaScrollOption(updatePrev, false);

		//see if colored convert button should be not colored (if backspace or clear were pressed, or if expression solved)
		if(!mCalc.isUnitIsSet()){
			//NOT SURE IF THIS IS A PROPER WAY TO DO THIS
			FragmentStatePagerAdapter tempAdapter = (FragmentStatePagerAdapter) mViewPager.getAdapter();
			ConvertKeysFragment currFrag = (ConvertKeysFragment) tempAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
			//clear the currently selected key
			currFrag.clearButtonSelection();
		}
	}


	private void updateScreenWithInstaScrollOption(boolean updatePrev, boolean instaScroll){
		//setText will reset selection to 0,0, so save it right now
		int selStart = mCalc.getSelectionStart();
		int selEnd = mCalc.getSelectionEnd();

		//update the main display
		mDisplay.setText(mCalc.toString());
		//updating the text restarts selection to 0,0, so load in the current selection
		mDisplay.setSelection(selStart, selEnd);
		if(selStart == mCalc.toString().length())
			mDisplay.setCursorVisible(false);
		else 
			mDisplay.setCursorVisible(true);

		//if we hit equals, update prev expression
		if(updatePrev){
			FragmentManager fm = getSupportFragmentManager();
			ResultListFragment prevResultFragment = (ResultListFragment)fm.findFragmentById(R.id.resultListfragmentContainer);
			prevResultFragment.refresh(instaScroll);
		}


		//make a little gray divider above expression when prev expression hits it
		View divider = findViewById(R.id.prev_curr_exp_divider);
		ListView mResultListView = mResultFragment.getListView();
		//don't try this unless prev expression has something there
		if(mResultListView.getChildCount()>0){
			//test to see if the last child's bottom edge is greater than the the total result list height
			//note that 0 is top of screen; also note that an extra child height is needed to reach the bottom
			if(mResultListView.getChildAt(mResultListView.getChildCount() - 1).getBottom() + 
					mResultListView.getChildAt(mResultListView.getChildCount() - 1).getHeight() >= mResultListView.getHeight())
				divider.setBackgroundColor(getResources().getColor(R.color.prev_curr_exp_divider));
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
		//if end of expression clicked, cursor will apear for paste commands etc
		mDisplay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCalc.setSolved(false);
				mDisplay.setCursorVisible(true);
			}

		});

		//use fragment manager to make the result list
		FragmentManager fm = getSupportFragmentManager();
		mResultFragment = (ResultListFragment) fm.findFragmentById(R.id.resultListfragmentContainer);

		if(mResultFragment == null){
			mResultFragment = new ResultListFragment();
			fm.beginTransaction().add(R.id.resultListfragmentContainer, mResultFragment).commit();			
		}


		mViewPager = (ViewPager)findViewById(R.id.convertKeyPager);

		mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
			@Override
			public int getCount(){
				return 10000;
				//return mCalc.getUnitTypeSize();
			}

			@Override
			public Fragment getItem(int pos){
				System.out.println("pos="+pos);
				System.out.println("pos % mCalc.getUnitTypeSize()"+pos % mCalc.getUnitTypeSize());
				return ConvertKeysFragment.newInstance(pos % mCalc.getUnitTypeSize());
				//TODO try debugging this 
			}

		});

		//need to tell calc when a new UnitType page is selected
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			//as the page is being scrolled to
			@Override
			public void onPageSelected(int pos) {
				//clear out the unit in the last UnitType, and make sure it's not selected
				mCalc.getCurrUnitType().clearUnitSelection();
				updateScreen(false);
				//tell calc what the new UnitType is
				mCalc.setUnitTypePos(pos);


				DisplayMetrics metrics = getResources().getDisplayMetrics();

				int padLeft=(int) (metrics.density * 8f + 0.5f);
				int padRight=(int) (metrics.density * 8f + 0.5f);
				//				if(mViewPager.getCurrentItem()==0)
				//					padLeft=0;
				//				if(mViewPager.getCurrentItem()==mViewPager.getAdapter().getCount()-1)
				//					padRight=0;

				mViewPager.setPadding(padLeft, 0, padRight, 0);
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

		mViewPager.setPadding(padLeft, 0, padRight, 0);


		mViewPager.setClipToPadding(false);
		//add a little break between pages
		mViewPager.setPageMargin(8);




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


			/*
			class HoldBackspace extends AsyncTask<String, Void, String> {
				private View mView;
				private int mHoldTimeout;
				//amount of change in color for each step
				private int mStartColor;
				private int mEndColor;
				//used to indicate how many times the colors will change
				private static final int NUM_COLOR_CHANGES=50;

				public HoldBackspace(View view, int holdTimeout){
					this.mView=view;
					this.mHoldTimeout=holdTimeout;
				}

				private int mInc;
				@Override
				protected String doInBackground(String... params) {
					for (mInc = 0; mInc < NUM_COLOR_CHANGES; mInc++) {
						try {
							Thread.sleep(mHoldTimeout/NUM_COLOR_CHANGES);
						} catch (InterruptedException e) {
							Thread.interrupted();
						}							
						if(isCancelled()) return "Canceled"; 

						final int deltaRed= Color.red(mStartColor) + ((Color.red(mEndColor)-Color.red(mStartColor))*mInc)/NUM_COLOR_CHANGES;
						final int deltaGreen= Color.green(mStartColor) + ((Color.green(mEndColor)-Color.green(mStartColor))*mInc)/NUM_COLOR_CHANGES;
						final int deltaBlue= Color.blue(mStartColor) + ((Color.blue(mEndColor)-Color.blue(mStartColor))*mInc)/NUM_COLOR_CHANGES;

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if(latch==false){

									//System.out.println("deltaRed = "+deltaRed);
									//System.out.println("deltaGreen = "+deltaGreen);
									//System.out.println("deltaBlue = "+deltaBlue);
									//System.out.println("mInc = "+mInc);
									//System.out.println("----------");

									mView.setBackgroundColor(Color.argb(255, deltaRed, deltaGreen, deltaBlue));
									if(mInc==NUM_COLOR_CHANGES-1) colorEndTime=System.currentTimeMillis();
								}
							}
						});

					}
					return "Executed";
				}

				@Override
				protected void onPostExecute(String result) {
					numButtonPressed("c");
					long endtime = System.currentTimeMillis();
					System.out.println("total time="+ String.valueOf( endtime-startTime));
					//System.out.println("colorEndTime="+String.valueOf(colorEndTime-startTime));
					//System.out.println("---------");
					colorEndTime=0;
				}

				@Override
				protected void onPreExecute() {
					//mStartColor = getResources().getColor(R.color.op_button_pressed);
					//mEndColor = getResources().getColor(R.color.backspace_button_held);

					//mView=findViewById(R.id.backspace_button);
				}

				@Override
				protected void onProgressUpdate(Void... values) {}
			}
			 */

			/*
			Thread mThread = (new Thread(){
				int i;
		        @Override
		        public void run(){
		        	if(interrupted()) return;
		            for(i=0; i<255; i++){
		                colorHandler.post(new Runnable(){
		                    public void run(){
		                    	mView.setBackgroundColor(Color.argb(255, i, i, i));
		                    }
		                });
		                // next will pause the thread for some time
		                try{ sleep(10); }
		                catch(InterruptedException e){ break; }
		            }
		        }
		    });
			 */

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
		if(mCalc.toString().equals("") && mCalc.getPrevExpressions().size()==0){
			mDisplay.setText(R.string.app_name);
			mDisplay.setCursorVisible(false);
		}
		else
			updateScreenWithInstaScrollOption(true, true);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calc, menu);
		return true;
	}
}
