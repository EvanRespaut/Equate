package com.llamacorp.equate.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.ComparisonFailure;
import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import android.widget.TextView;

import com.llamacorp.equate.R;
import com.llamacorp.equate.view.CalcActivity;
import com.llamacorp.equate.view.ConvKeysFragment;
import com.llamacorp.equate.view.EditTextDisplay;
import com.llamacorp.equate.view.ResultListFragment;

/**
 * FOR THESE TESTS TO WORK, BE SURE SCREEN IS ON
 * Also remember that backspace uses touch events instead of clicks
 */
public class CalcAndroidTester extends ActivityInstrumentationTestCase2<CalcActivity> {
	//seems to work all the way down to zero?
	private static final int DELAY_BETWEEN_BUTTON_PRESSES = 0;
	private static final int AFTER_CURSOR_MOVE = 0;
	private static final int AFTER_CONTEXT_ACTION = 300;
	private static final int DOUBLE_CLICK_PREVENTION_DELAY = 310; //double tap is 300 or less
	private static final int DELAY_AFTER_SWIPE = 50;

	//private static final int DELAY_BEFORE_SELECTED_READ = 300;

	private EditTextDisplay mExpressionTextView;
	private CalcActivity mActivity;
	private ResultListFragment mResultFragment;
	private ListView mResultListView;
	private ViewPager mConKeysViewPager; 
	private ConvKeysFragment mConvertFragment;

	Map<String, Integer> mUnitToPos = new HashMap<>();

	private int[] convertButtonIds = {	
			R.id.convert_button1,
			R.id.convert_button2,
			R.id.convert_button3,
			R.id.convert_button4,
			R.id.convert_button5,
			R.id.convert_button6,
			R.id.convert_button7,
			R.id.convert_button8,
			R.id.convert_button9,
			R.id.convert_button10};
	private Instrumentation inst;

	public CalcAndroidTester() {
		super(CalcActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		inst = getInstrumentation();


		int inc=0;
		mUnitToPos.put("oz", inc++);
		mUnitToPos.put("lb", inc++);
		mUnitToPos.put("short ton", inc++);
		mUnitToPos.put("long ton", inc++);
		mUnitToPos.put("stone", inc++);

		mUnitToPos.put("\u00B5g", inc++);
		mUnitToPos.put("mg", inc++);
		mUnitToPos.put("g", inc++);
		mUnitToPos.put("kg", inc++);
		mUnitToPos.put("metric ton", inc++);

		inc=0;
		mUnitToPos.put("in", inc++);
		mUnitToPos.put("ft", inc++);
		mUnitToPos.put("yard", inc++);
		mUnitToPos.put("mi", inc++);
		mUnitToPos.put("km", inc++);

		mUnitToPos.put("nm", inc++);
		mUnitToPos.put("um", inc++);
		mUnitToPos.put("mm", inc++);
		mUnitToPos.put("cm", inc++);
		mUnitToPos.put("m", inc++);


		super.setUp();
		//Sets the initial touch mode for the Activity under test. This must be called before
		//getActivity()
		setActivityInitialTouchMode(true);

		//Get a reference to the Activity under test, starting it if necessary.
		mActivity = getActivity();

		FragmentManager fm = mActivity.getSupportFragmentManager();
		mResultFragment = (ResultListFragment) fm.findFragmentById(R.id.resultListfragmentContainer);
		mResultListView = mResultFragment.getListView();

		mConKeysViewPager = (ViewPager)getActivity().findViewById(R.id.convertKeyPager);

		FragmentStatePagerAdapter tempAdapter = (FragmentStatePagerAdapter) mConKeysViewPager.getAdapter();
		mConvertFragment = (ConvKeysFragment) tempAdapter.instantiateItem(mConKeysViewPager, mConKeysViewPager.getCurrentItem());

		mExpressionTextView = (EditTextDisplay) mActivity.findViewById(R.id.textDisplay);
	}

	//	@MediumTest
	//	public void testPreConditions(){
	//		assertTrue(mActivity.mCalc != null);
	//		assertNotNull("mActivity is null", mActivity);
	//		assertNotNull("expressionTextView is null", mExpressionTextView);
	//	}

	@MediumTest
	public void test1CalcFunctions(){
		getActivity().mCalc.resetCalc();
		
		clickButtons(".1+b)4");
		assertExpressionEquals("(.1)*4");

		clickButtons("=");
		assertExpressionEquals("0.4");

		//now take 0.4, divide it by the last answer ("a0" is answer 0 answers ago) and get result
		clickButtons("/a0=");
		assertExpressionEquals("1");

		clickButtons("q1bbb-6.1E0)^(a0+q0=");
		assertExpressionEquals("36");

		clickButtons(".5=");
		assertQueryAnswerExprConvbutton(".5","0.5","0.5","");

		clickButtons("q0=");
		assertQueryAnswerExprConvbutton(".5","0.5","0.5","");

		clickButtons("+bq0=");
		assertPrevAnswerEquals("Syntax Error", 0);
		assertExpressionEquals("Syntax Error");

		//clear out the syntax error and try to click it again (should do nothing)
		clickButtons("ba0");
		assertPrevAnswerEquals("Syntax Error", 0);
		assertExpressionEquals("");

		clickButtons("=");
		assertExpressionEquals("");

		clickButtons("-=");
		assertExpressionEquals("");

		clickButtons("54+46=");		
		assertExpressionEquals("100");

		moveCursorToPos(1);
		clickButtons("3");
		assertExpressionEquals("1300");
		//clear out unit and selection
		holdButton("b");

		clickButtons("48-6155.1");
		assertCursorVisible(true);
		setSelection(3,7);
		assertCursorVisible(true);
		clickButtons("47=");
		assertCursorVisible(false);
		assertExpressionEquals("0.9");

		//select ".9" replace with 1300
		setSelection(1,3);
		clickButtons("a1");
		assertExpressionEquals("0100");

		moveCursorToPos(2);
		assertCursorVisible(true);
		clickButtons("3=");		
		assertExpressionEquals("1300");

		moveCursorToPos(0);
		clickButtons("q1+");
		assertExpressionEquals("48-47.1+1300");

		setSelection(3,8);
		contextCommand(android.R.id.cut);
		assertExpressionEquals("48-1300");

		setSelection(0,4);
		contextCommand(android.R.id.paste);
		assertExpressionEquals("47.1+300");

		clickButtons("=");
		assertExpressionEquals("347.1");

		setSelection(1,3);		
		contextCommand(android.R.id.copy);
		holdButton("b");
		contextCommand(android.R.id.paste);
		setSelection(0,0);		
		assertExpressionEquals("47");

		holdButton("b");
		clickButtons("-5=");
		assertExpressionEquals("-5");
		
		clickButtons("3-a0=");
		assertExpressionEquals("8");

		//don't go into the next test without a small delay otherwise expression view throws NP (maybe?)
		sleep(700);
	}


	@MediumTest
	public void test2UnitConvertions(){
		holdButton("b");
		assertConvButtonSelected("");

		clickConvButton("in");
		//TODO come up with less crude way of clicking ok on dialog (hitting backspace works for now)
		clickButtons("b");
		
		clickConvButton("in");
		assertConvButtonSelected("");

		clickConvButton("in");
		clickButtons("b");
		assertConvButtonSelected("");

		clickButtons("36");
		clickConvButton("in");
		assertExpressionEquals("36 in");
		assertConvButtonSelected("in");

		clickConvButton("ft");
		assertQueryAnswerExprConvbutton("36 in", "3 ft", "3 ft", "ft");

		clickConvButton("nm");
		assertQueryAnswerExprConvbutton("3 ft","9.144E8 nm","9.144E8 nm","nm");

		clickButtons("b");
		assertExpressionEquals("");
		assertConvButtonSelected("");

		clickConvButton("cm");
		assertQueryAnswerExprConvbutton("3 ft","9.144E8 nm","","cm");
		clickButtons("b");

		clickConvButton("m");
		clickButtons("3+1=");
		assertQueryAnswerExprConvbutton("3+1 m","4 m","4 m","m");

		clickConvButton("km");
		assertQueryAnswerExprConvbutton("4 m","0.004 km","0.004 km","km");

		clickButtons("ba1");
		assertExpressionEquals("4 m"); //assertEquals("4", getExp());
		assertConvButtonSelected("m"); //assertTrue(isConvButtonSelected("m"));

		clickConvButton("cm");
		assertQueryAnswerExprConvbutton("4 m","400 cm","400 cm","cm");

		clickConvButton("cm");
		assertQueryAnswerExprConvbutton("4 m","400 cm","400","");

		clickButtons("b1609+.344");
		clickConvButton("m");
		clickButtons("=");
		assertQueryAnswerExprConvbutton("1609+.344 m","1609.344 m","1609.344 m","m");

		clickButtons("+a0");
		clickConvButton("mi");
		assertQueryAnswerExprConvbutton("1609.344+1609.344 m","2 mi","2 mi","mi");

		clickButtons("/q2=");
		assertQueryAnswerExprConvbutton("2/4 mi","0.5 mi","0.5 mi","mi");
	}

	@MediumTest
	public void test3Swiping(){
		holdButton("b");
		//test to make sure swiping left clears current unit
		clickConvButton("in");
		clickButtons("12+24=");
		assertConvButtonSelected("in");
		assertExpressionEquals("36 in");
		swipeConvKeysToLeft();

		//see if "in" is still selected a bit off screen in the screen to the left
		FragmentStatePagerAdapter tempAdapter = (FragmentStatePagerAdapter) mConKeysViewPager.getAdapter();
		mConvertFragment = (ConvKeysFragment) tempAdapter.instantiateItem(mConKeysViewPager, mConKeysViewPager.getCurrentItem()+1);
		int convertButtonPos = mUnitToPos.get("in");
		assertTrue(!mConvertFragment.getView().findViewById(convertButtonIds[convertButtonPos]).isSelected());


		swipeConvKeysToRight();
		assertExpressionEquals("36");
		assertConvButtonSelected("");

		swipeConvKeysToLeft();
		clickButtons("16");
		clickConvButton("oz");
		assertConvButtonSelected("oz");		
		clickConvButton("lb");
		assertQueryAnswerExprConvbutton("16 oz","1 lb","1 lb","lb");

		//test to be sure clicking Unit-ed result scrolls to it
		swipeConvKeysToRight();
		clickButtons("ba0");
		assertExpressionEquals("1 lb");
		assertConvButtonSelected("lb");


		swipeConvKeysToLeft();
		swipeConvKeysToLeft();
		swipeConvKeysToLeft();
		swipeConvKeysToLeft();
		swipeConvKeysToLeft();
		swipeConvKeysToRight();
		swipeConvKeysToRight();
		swipeConvKeysToRight();
		swipeConvKeysToRight();
		swipeConvKeysToRight();
		swipeConvKeysToRight();
	}



	//These functions would prevent the need to sleep in other places
	//However, they could turn up fall positives--like if a key is pushed and isn't supposed to be selected as
	//a result, the prog takes too long to select it, this says its ok, and moves on but really the key gets 
	//selected later.  Overcome this problem by testing one time later with a delay after all events
	private enum TypeFlag {	EXPRESSION, QUERY, ANSWER, CONVERT_BUTTON}	

	/** Used to test the result query, answer, current expression, and 
	 * convert button's selected state all at once
	 */
	private void assertQueryAnswerExprConvbutton(String prevQuery, String prevAns, String curExp, String convButton){
		assertPrevQueryEquals(prevQuery, 0);
		assertPrevAnswerEquals(prevAns, 0);
		assertExpressionEquals(curExp);
		assertConvButtonSelected(convButton);
	}

	private void assertPrevQueryEquals(String expected, int numberOfResultsBack){
		myAssertEquals(expected, TypeFlag.QUERY, numberOfResultsBack);}

	private void assertPrevAnswerEquals(String expected, int numberOfResultsBack){
		myAssertEquals(expected, TypeFlag.ANSWER, numberOfResultsBack);}

	private void assertExpressionEquals(String expected){
		myAssertEquals(expected, TypeFlag.EXPRESSION, 0);}

	private void assertConvButtonSelected(String expected){
		myAssertEquals(expected, TypeFlag.CONVERT_BUTTON, 0);}

	private void myAssertEquals(String expected, TypeFlag typeFlag, int numberOfResultsBack){
		long TIMEOUT = 2000;
		long startTime = SystemClock.uptimeMillis();

		while(true){
			try{
				String actual="";
				if(typeFlag.equals(TypeFlag.QUERY))
					actual = getPrevQuery(numberOfResultsBack);
				else if (typeFlag.equals(TypeFlag.ANSWER))
					actual = getPrevAnswer(numberOfResultsBack);
				else if (typeFlag.equals(TypeFlag.EXPRESSION))
					actual = getExp();
				else if (typeFlag.equals(TypeFlag.CONVERT_BUTTON)){
					isConvButtonSelected(expected);
					return;
				}
				assertEquals(expected, actual);
				//it worked, leave
				return; 
			}
			catch (ComparisonFailure c){
				//we haven't timed out
				if((SystemClock.uptimeMillis()-startTime) < TIMEOUT)
					sleep(50);
				else throw c;
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void assertCursorVisible(boolean wantCursorVisible) {
		if (Build.VERSION.SDK_INT >= 16) {
			if(wantCursorVisible)
				assertTrue(mExpressionTextView.isCursorVisible());
			else
				assertTrue(!mExpressionTextView.isCursorVisible());				
		} else {

		}
	}


	//used to lock in the adapter size before adding to prev Expression
	//private int prevNumInListView=0;

	/**
	 * Helper function clicks a button specified by a string
	 * @param buttonString 1-9 for numbers, +-* etc operators; also, "a1" is one answer ago, "q0" is the last query
	 */
	private void clickButtons(String buttonString){
		String s="";
		for(int i=0;i<buttonString.length();i++){
			s=buttonString.substring(i, i+1);
			if(s.equals("a")){
				i++;
				clickPrevAnswer(Integer.parseInt(buttonString.substring(i, i+1)));
			}
			else if(s.equals("q")){
				i++;
				clickPrevQuery(Integer.parseInt(buttonString.substring(i, i+1)));
			}
			else{
				//	if(s.equals("="))
				//prevNumInListView= mResultListView.getAdapter().getCount();
				MyTouchUtils2.myClickView(this, getButton(s),DELAY_BETWEEN_BUTTON_PRESSES);
			}
		}
	}



	private void holdButton(String buttonString){
		TouchUtils.longClickView(this, getButton(buttonString));
	}


	/**
	 * Clicks the answer in the prev expression
	 * @param numberOfResultsBack 0 for last answer, 1 for two answer ago, etc
	 */	
	private void clickPrevAnswer(int numberOfResultsBack){
		clickPrevExpression(numberOfResultsBack, true);
	}

	/**
	 * Clicks the query in the prev expression
	 * @param numberOfResultsBack 0 for last query, 1 for two query ago, etc
	 */
	private void clickPrevQuery(int numberOfResultsBack){
		clickPrevExpression(numberOfResultsBack, false);
	}


	/**
	 * Helper method for helper method
	 * @param key is the String representation of the key pressed
	 */
	private void clickConvButton(String key){
		View convButton = getConvButton(key);
		//prevNumInListView= mResultListView.getAdapter().getCount();
		MyTouchUtils2.myClickView(this, convButton,DELAY_BETWEEN_BUTTON_PRESSES);
	}	

	private void contextCommand(final int commandId){
		mActivity.runOnUiThread(
				new Runnable() {
					public void run() {
						mExpressionTextView.onTextContextMenuItem(commandId);
					}
				});
		sleep(AFTER_CONTEXT_ACTION);
	}

	/**
	 * Select a range in expression's EditText box
	 * Note this function first sets the cursor at the start position since
	 * this is most similar to user operation of clicking and then dragging 
	 * in order to make a selection
	 * @param start the start of the selection
	 * @param end the end of the seleciton
	 */
	private void setSelection(final int start, final int end){
		moveCursorToPos(start);
		//no need to set selection
		if(start==end) return;
		mActivity.runOnUiThread(
				new Runnable() {
					public void run() {
						mExpressionTextView.setSelection(start, end);
					}
				});
		sleep(DELAY_BETWEEN_BUTTON_PRESSES);
	}

	private long lastDownClick=0;


	/**
	 * Moves the cursor in the expression's EditText box
	 * This method will be sure not to perform a double when selecting cursors in rapid selection
	 * by waiting for a timeout period.  
	 * @param start the position in the expression to place the cursor
	 */
	private void moveCursorToPos(final int start){
		mActivity.runOnUiThread(
				new Runnable() {
					public void run() {
						Rect bounds = new Rect();

						long TIMEOUT = 5000;
						long startTime = SystemClock.uptimeMillis();

						while(mExpressionTextView == null){
							if((SystemClock.uptimeMillis()-startTime) > TIMEOUT)
								break;
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if (mExpressionTextView == null) 
							throw new IllegalAccessError("In moveEditTextCursor, expressionTextView==null");

						if (mExpressionTextView.getPaint() == null) 
							throw new IllegalAccessError("In moveEditTextCursor, expressionTextView.getPaint()==null");

						Paint textPaint = mExpressionTextView.getPaint();
						String text = mExpressionTextView.getText().toString().substring(start);
						textPaint.getTextBounds(text,0,text.length(),bounds);
						int height = mExpressionTextView.getBottom()+mExpressionTextView.getHeight()/2;
						int width = mExpressionTextView.getWidth()-bounds.width()-mExpressionTextView.getPaddingRight();

						while(SystemClock.uptimeMillis() - lastDownClick < DOUBLE_CLICK_PREVENTION_DELAY){
							try {
								Thread.sleep(20);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						lastDownClick=SystemClock.uptimeMillis();
						MotionEvent mv = MotionEvent.obtain(lastDownClick, 
								lastDownClick+10, 
								MotionEvent.ACTION_DOWN, 
								width, height, 0);

						mExpressionTextView.dispatchTouchEvent(mv);
						//						mv.setAction(MotionEvent.ACTION_UP);
						mv = MotionEvent.obtain(SystemClock.uptimeMillis()+400, 
								SystemClock.uptimeMillis()+410, 
								MotionEvent.ACTION_UP, 
								width, height, 0);

						TIMEOUT = 5000;
						startTime = SystemClock.uptimeMillis();

						while(mExpressionTextView == null){
							if((SystemClock.uptimeMillis()-startTime) > TIMEOUT)
								break;
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if (mExpressionTextView == null) 
							throw new IllegalAccessError("In moveEditTextCursor, expressionTextView==null");

						mExpressionTextView.dispatchTouchEvent(mv);
						mv.recycle();
					}
				});
		sleep(AFTER_CURSOR_MOVE);
	}

	private void swipeConvKeysToRight(){
		swipeConvKeys(true);
	}

	private void swipeConvKeysToLeft(){
		swipeConvKeys(false);
	}

	private void swipeConvKeys(boolean swipeConvKeysRight){
		//get the middle of the height and width
		int height = mConKeysViewPager.getBottom() - mConKeysViewPager.getHeight()/2+50;
		int width = mConKeysViewPager.getWidth()/2;
		int movement = mConKeysViewPager.getWidth()/2-100;
		if(swipeConvKeysRight) movement = -movement;

		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis();
		MotionEvent mv = MotionEvent.obtain(downTime, 
				eventTime, 
				MotionEvent.ACTION_DOWN, 
				width-movement, height, 0);
		inst.sendPointerSync(mv);

		mv = MotionEvent.obtain(downTime, 
				eventTime+50, 
				MotionEvent.ACTION_MOVE, 
				width, height, 0);
		inst.sendPointerSync(mv);

		mv = MotionEvent.obtain(downTime, 
				eventTime+100, 
				MotionEvent.ACTION_UP, 
				width+movement, height, 0);
		inst.sendPointerSync(mv);

		mv.recycle();
		sleep(DELAY_AFTER_SWIPE);
	}


	/**
	 * Find out if a particular convert button is selected
	 * @param key is button who's slection is in question
	 * @return true if button was selected false otherwise
	 */
	private boolean isConvButtonSelected(String key) {
		View v = null;
		//empty string to check if non are selected
		if(key.equals("")){
			for(int i=0;i<convertButtonIds.length;i++){
				if(mConvertFragment.getView()==null)
					sleep(5000);
				v = mConvertFragment.getView().findViewById(convertButtonIds[i]);
				if(v.isSelected())
					return true;
			}
			//if no button selected
			return false;
		}

		return getConvButton(key).isSelected();
	}


	private View getConvButton(String key){
		int convertButtonPos = mUnitToPos.get(key);
		FragmentStatePagerAdapter tempAdapter = (FragmentStatePagerAdapter) mConKeysViewPager.getAdapter();
		mConvertFragment = (ConvKeysFragment) tempAdapter.instantiateItem(mConKeysViewPager, mConKeysViewPager.getCurrentItem());

		return  mConvertFragment.getView().findViewById(convertButtonIds[convertButtonPos]);
	}

	/**
	 * Helper method for helper method
	 * @param numberOfResultsBack
	 * @param isAnswer
	 */
	private void clickPrevExpression(int numberOfResultsBack, boolean isAnswer){
		final View mTextView = getPrevExpView(numberOfResultsBack, isAnswer);

		mActivity.runOnUiThread(
				new Runnable() {
					public void run() {
						mTextView.performClick();
					}
				});
		sleep(DELAY_BETWEEN_BUTTON_PRESSES);

	}

	private String getExp(){
		return mExpressionTextView.getText().toString();
	}

	private String getPrevAnswer(int numberOfResultsBack){
		return getPrevExpression(numberOfResultsBack, true);
	}


	private String getPrevQuery(int numberOfResultsBack){
		return getPrevExpression(numberOfResultsBack, false);
	}


	/**
	 * Helper method for helper method
	 * @param numberOfResultsBack
	 * @param isAnswer
	 */
	private String getPrevExpression(int numberOfResultsBack, boolean isAnswer){
		View mTextView =getPrevExpView(numberOfResultsBack, isAnswer);
		return ((TextView) mTextView).getText().toString();
	}

	private View getPrevExpView(int numberOfResultsBack, boolean isAnswer){
		//make sure the new item makes it into the list view
		//while(prevNumInListView == mResultListView.getAdapter().getCount()){
		//	sleep(50);
		//}
		//make sure we finish the scrolling animation down the list to show the new list item
		while( !(mResultListView.getLastVisiblePosition() == mResultListView.getAdapter().getCount() -1 &&
				mResultListView.getChildAt(mResultListView.getChildCount() - 1).getBottom() <= mResultListView.getHeight() ) ){
			sleep(50);
		}
		int pos = mResultListView.getChildCount() - numberOfResultsBack - 1;
		int textId;
		if(isAnswer) textId = R.id.list_item_result_textPrevAnswer;
		else textId = R.id.list_item_result_textPrevQuery;
		View mTextView;
		View v = mResultListView.getChildAt(pos);
		mTextView =  v.findViewById(textId);
		return mTextView;
	}


	/**
	 * Helper function takes a string of a key hit and passes back the correct View
	 * @param key
	 * @return
	 */
	private View getButton(String key){
		int[] numButtonIds = {
				R.id.zero_button,
				R.id.one_button, 
				R.id.two_button,
				R.id.three_button,
				R.id.four_button,
				R.id.five_button, 
				R.id.six_button,
				R.id.seven_button,
				R.id.eight_button,
				R.id.nine_button};

		int buttonId=0;

		switch(key){
		case "+": buttonId = R.id.plus_button;
		break;
		case "-": buttonId = R.id.minus_button;
		break;
		case "*": buttonId = R.id.multiply_button;
		break;
		case "/": buttonId = R.id.divide_button;
		break;
		case ".": buttonId = R.id.decimal_button;
		break;
		case "=": buttonId = R.id.equals_button;
		break;
//		case "E": buttonId = R.id.ee_button;
//		break;
//		case "^": buttonId = R.id.power_button;
//		break;
		case "(": buttonId = R.id.open_para_button;
		break;
		case ")": buttonId = R.id.close_para_button;
		break;
		case "b": buttonId = R.id.backspace_button;
		break;
		default: 					
			//this for loop checks for numerical values
			for(int i=0;i<10;i++)
				if(key.equals(Character.toString((char)(48+i))))
					buttonId=numButtonIds[i];
		}
		return mActivity.findViewById(buttonId);
	}


	/**
	 * Helper function to make sleeping easier
	 * @param sleepTime number of miliseconds to sleep for
	 */
	private static void sleep(int sleepTime){
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



	public static class MyTouchUtils2 {
		/**
		 * Simulate touching the center of a view and releasing.
		 * But with programmable delay in order to tune the runtime of your test-suite.
		 * 
		 * @param test The test cast that is being run
		 * @param v The view that should be clicked
		 */
		public static void myClickView(InstrumentationTestCase test, View v) {
			myClickView(test, v, 1000);
		}
		@SuppressWarnings("deprecation")
		public static void myClickView(InstrumentationTestCase test, View v, long del) {
			int[] xy = new int[2];
			v.getLocationOnScreen(xy);
			final int viewWidth = v.getWidth();
			final int viewHeight = v.getHeight();
			final float x = xy[0] + (viewWidth / 2.0f);
			float y = xy[1] + (viewHeight / 2.0f);
			Instrumentation inst = test.getInstrumentation();
			long downTime = SystemClock.uptimeMillis();
			long eventTime = SystemClock.uptimeMillis();
			MotionEvent event = MotionEvent.obtain(downTime, eventTime,
					MotionEvent.ACTION_DOWN, x, y, 0);
			inst.sendPointerSync(event);
			inst.waitForIdleSync();
			eventTime = SystemClock.uptimeMillis();
			event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 
					x + (ViewConfiguration.getTouchSlop() / 2.0f),
					y + (ViewConfiguration.getTouchSlop() / 2.0f), 0);
			inst.sendPointerSync(event);
			inst.waitForIdleSync();
			eventTime = SystemClock.uptimeMillis();
			event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
			inst.sendPointerSync(event);
			inst.waitForIdleSync();
			// programmable delay
			try {
				Thread.sleep(del);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

} 