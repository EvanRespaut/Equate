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
import android.util.SparseArray;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.llamacorp.unitcalc.ConvertKeysFragment.OnConvertKeySelectedListener;
import com.llamacorp.unitcalc.ResultListFragment.OnResultSelectedListener;

public class CalcActivity  extends FragmentActivity implements OnResultSelectedListener, OnConvertKeySelectedListener{

	private ViewPager mViewPager; 

	private List<Button> calcButton;
	//private List<Button> convButton;
	private TextView mDisplay;
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
	public Calculator calc;// = new Calculator();

	//maps id's of buttons to convert values
	SparseArray<Double> units = new SparseArray<Double>();

	//called when any non convert key is pressed
	public void numButtonPressed(String keyPressed){
		//pass button value to CalcAcitvity to pass to calc
		calc.parseKeyPressed(keyPressed);

		//see if colored convert button should be not colored (if backspace or clear were pressed)
		if(!calc.currUnitIsSet()){
			//NOT SURE IF THIS IS A PROPER WAY TO DO THIS
			FragmentStatePagerAdapter tempAdapter = (FragmentStatePagerAdapter) mViewPager.getAdapter();
			ConvertKeysFragment currFrag = (ConvertKeysFragment) tempAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
			//clear the currently selected key
			currFrag.clearKeySelection();
		}

		updateScreen(keyPressed.equals("="));
	}

	static final String strExpressionEnd = " =";

	////this is just a temp test function
	//public String getPrevResult(){
	//	return calc.toStringLastExpression();
	//}


	/**
	 * Updates the current and previous answers
	 * @param updatePrev whether or not to update previous answer
	 */
	public void updateScreen(boolean updatePrev){
		mDisplay.setText(calc.toString());

		//if we hit equals, update prev expression
		if(updatePrev){
			FragmentManager fm = getSupportFragmentManager();
			ResultListFragment prevResultFragment = (ResultListFragment)fm.findFragmentById(R.id.resultListfragmentContainer);
			prevResultFragment.refresh();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calc);

		//either get old calc or create a new one
		calc = Calculator.getCalculator(getApplicationContext());

		//main result display
		mDisplay = (TextView)findViewById(R.id.textDisplay);

		//use fragment manager to make the result list
		FragmentManager fm = getSupportFragmentManager();
		Fragment resultFragment = fm.findFragmentById(R.id.resultListfragmentContainer);

		if(resultFragment == null){
			resultFragment = new ResultListFragment();
			fm.beginTransaction().add(R.id.resultListfragmentContainer, resultFragment).commit();			
		}

		//
		mViewPager = (ViewPager)findViewById(R.id.convertKeyPager);
		
		mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
			@Override
			public int getCount(){
				return calc.getUnitTypeSize();
			}

			@Override
			public Fragment getItem(int pos){
				return ConvertKeysFragment.newInstance(pos);
			}
		});
		mViewPager.setPageMargin(10);



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
					//user released button before reapeat could fire
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calc, menu);
		return true;
	}
}
