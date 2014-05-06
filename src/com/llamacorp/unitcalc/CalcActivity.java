package com.llamacorp.unitcalc;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.llamacorp.unitcalc.ConvertKeysFragment.OnConvertKeySelectedListener;
import com.llamacorp.unitcalc.ResultListFragment.OnResultSelectedListener;

public class CalcActivity  extends FragmentActivity implements OnResultSelectedListener, OnConvertKeySelectedListener{

	private ViewPager mViewPager; 

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
	 * Selects the coloring for a selected unit key
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
			currFrag.clearKeySelection();
		}
	}


	private void updateScreenWithInstaScrollOption(boolean updatePrev, boolean instaScroll){
		/*
		//save text selection
		int selEnd = mDisplay.getSelectionEnd();
		int oldTextLen = mDisplay.getText().length();
		int newTextLen = calc.toString().length();
		mDisplay.setText(calc.toString());
		//if we were at the end before, put us at the end now
		if(selEnd==oldTextLen) selEnd = newTextLen;
		//else selEnd = selEnd + newTextLen - oldTextLen;
		 selEnd = newTextLen;
		mDisplay.setSelection(selEnd, selEnd);
		//THESE TWO FUNCTIONS SHOULD BE TIED TOGETHER BY EXPRESSION AND A LISTENER
		//or does expression even need to keep track of this????
		calc.setSelection(selEnd, selEnd);
		 */

		//setText will reset selection to 0,0, so save it right now
		int selStart = mCalc.getSelectionStart();
		int selEnd = mCalc.getSelectionEnd();

		Log.d("t", "before setText");
		//update the main display
		mDisplay.setText(mCalc.toString());
		Log.d("t", "after setText");
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

		//use fragment manager to make the result list
		FragmentManager fm = getSupportFragmentManager();
		Fragment resultFragment = fm.findFragmentById(R.id.resultListfragmentContainer);

		if(resultFragment == null){
			resultFragment = new ResultListFragment();
			fm.beginTransaction().add(R.id.resultListfragmentContainer, resultFragment).commit();			
		}


		mViewPager = (ViewPager)findViewById(R.id.convertKeyPager);

		mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
			@Override
			public int getCount(){
				return mCalc.getUnitTypeSize();
			}

			@Override
			public Fragment getItem(int pos){
				return ConvertKeysFragment.newInstance(pos);
			}
		});
		//add a little break between pages
		mViewPager.setPageMargin(10);

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
			}

			@Override
			public void onPageScrolled(int pos, float posOffset, int posOffsetPixels) {}

			@Override
			public void onPageScrollStateChanged(int state) {}
		});


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

		/*
		int convKeyID = 0;
		for(int i = 0; i < units.size(); i++) {
			convKeyID = units.keyAt(i);

			Button button = (Button)findViewById(convKeyID);
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					double convertTo = units.get(view.getId());
					//pass button id straight to calc
					calc.parseConvertKey(convertTo);
					//update screen (aka result)
					updateScreen(true);

					//clear previously selected convert button
					for(Button b:convButton)
						b.setSelected(false);	

					//set this convert button to selected
					if(calc.currUnitIsSet())
						view.setSelected(true);
				}
			});
			//add to our list of conv buttons
			convButton.add(button);			
		}
		 */

		ImageButton backspaceButton = (ImageButton) findViewById(R.id.backspace_button);
		backspaceButton.setOnTouchListener(new View.OnTouchListener() {

			//this handler is now associated with current thread's Looper
			private Handler mHandler;

			@Override 
			public boolean onTouch(View view, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (mHandler != null) return true;
					mHandler = new Handler();
					mHandler.postDelayed(mBackspaceRepeat, 400);
					//pass backspace "b"  to calc, change conv key colors (maybe) and update screen
					numButtonPressed("b");
					break;
				case MotionEvent.ACTION_UP:
					if (mHandler == null) return true;
					//user released button before repeat could fire
					mHandler.removeCallbacks(mBackspaceRepeat);
					mHandler = null;
					break;
				}
				return false;
			}

			//set up the runnable for when backspace is held down
			Runnable mBackspaceRepeat = new Runnable() {
				@Override 
				public void run() {
					mHandler.postDelayed(this, 100);
					//pass backspace "b" to calc, change conv key colors (maybe) and update screen
					numButtonPressed("b");
				}
			};
		});
	}


	@Override
	public void onResume(){
		super.onResume();

		//only set display to UnitCalc if no expression is there yet
		if(mCalc.toString().equals("") && mCalc.getPrevExpressions().size()==0)
			mDisplay.setText(R.string.app_name);
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
