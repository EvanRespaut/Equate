package com.llamacorp.equate.test;


import android.support.test.espresso.Espresso;
import android.support.test.runner.AndroidJUnit4;

import com.llamacorp.equate.test.IdlingResource.ViewPagerIdlingResource;
import com.llamacorp.equate.view.CalcActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.registerIdlingResources;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.llamacorp.equate.test.EspressoTestUtils.assertExpressionEquals;
import static com.llamacorp.equate.test.EspressoTestUtils.assertResultPreviewEquals;
import static com.llamacorp.equate.test.EspressoTestUtils.assertResultPreviewInvisible;
import static com.llamacorp.equate.test.EspressoTestUtils.clickButtons;
import static com.llamacorp.equate.test.EspressoTestUtils.clickPrevAnswer;
import static com.llamacorp.equate.test.EspressoTestUtils.clickPrevQuery;
import static com.llamacorp.equate.test.EspressoTestUtils.clickUnit;
import static com.llamacorp.equate.test.EspressoTestUtils.getPagerIdle;
import static com.llamacorp.equate.test.EspressoTestUtils.selectUnitTypeDirect;
import static com.llamacorp.equate.test.EspressoTestUtils.setUp;

@RunWith(AndroidJUnit4.class)
public class TestEspressoCalcActivity {
	private ViewPagerIdlingResource mPagerIdle;

	@Rule
	public MyActivityTestRule<CalcActivity> mActivityTestRule =
			new MyActivityTestRule<>(CalcActivity.class);

	@Before
	public void setUpTest() {
		setUp(mActivityTestRule);
		mPagerIdle = getPagerIdle(mActivityTestRule);
		registerIdlingResources(mPagerIdle);
	}

	@After
	public void unregisterIntentServiceIdlingResource() {
		if (mPagerIdle != null)
			Espresso.unregisterIdlingResources(mPagerIdle);
	}


	@Test
	public void testCalcActivity() {
		clickButtons("C");
		assertExpressionEquals("");
		assertResultPreviewInvisible();

		clickButtons("(");
		assertExpressionEquals("(");

		clickButtons(".");
		assertExpressionEquals("(.");

		clickButtons("1");
		assertExpressionEquals("(.1");
		assertResultPreviewEquals("= 0.1");

		clickButtons("+b");
		assertExpressionEquals("(.1");

		clickButtons(")4");
		assertExpressionEquals("(.1)*4");
		assertResultPreviewEquals("= 0.4");

		clickButtons("=");
		assertExpressionEquals("0.4");
		assertResultPreviewInvisible();

		clickButtons("C2E2+5%=");
		// if this test fails because a % is put instead of a E, make sure
		// that the Settings -> Accessibility -> Hold Time is set to long
		assertExpressionEquals("210");

		clickPrevQuery();
		assertExpressionEquals("2E2+5%");

		clickButtons("bb56=");
		assertExpressionEquals("256");

		clickButtons("+");
		clickPrevAnswer();
		clickButtons("=");
		assertExpressionEquals("512");

		clickButtons("Ca1+a0=");
		assertExpressionEquals("768");
	}


	@Test
	public void testClickUnitTypesDirect() {
		clickButtons("C12345");

		// used to be Currency, that proved to be unstable in test, probably due
		// to web updates.
		selectUnitTypeDirect("Temp");

		clickButtons("26");

		selectUnitTypeDirect("Energy");
		clickButtons("b");

		onView(withText("Power")).perform(click());

		clickButtons("67");

		clickButtons("C4*5=");
		assertExpressionEquals("20");

		// expression should clear out solve flag
		clickUnit("MW");

		//TODO fix this:
		assertExpressionEquals("Convert 20 MW to:");
	}


	@Test
	public void testOldSequence() {

		clickButtons("C(.1+b)4");
		assertExpressionEquals("(.1)*4");

		clickButtons("=");
		assertExpressionEquals("0.4");

		//now take 0.4, divide it by the last answer ("a0" is answer 0 answers ago) and get result
		clickButtons("/a0=");
		assertExpressionEquals("1");

		clickButtons("q1bbb-6.1E0)^(a0+q0=");
		assertExpressionEquals("36");

		clickButtons("1/2=");
		assertExpressionEquals("0.5");
//		assertQueryAnswerExprConvbutton(".5", "0.5", "0.5", "");

		clickButtons("a0+ba0=");
//		assertPrevAnswerEquals("Syntax Error", 0);
		assertExpressionEquals("Syntax Error");

		//clear out the syntax error and try to click it again (should do nothing)
		clickButtons("ba0");
//		assertPrevAnswerEquals("Syntax Error", 0);
		assertExpressionEquals("");

		clickButtons("=");
		assertExpressionEquals("");

		clickButtons("-=");
		assertExpressionEquals("");

		clickButtons("54+46=");
		assertExpressionEquals("100");

	}


	@Test
	public void testToggleSciNote() {
		clickButtons("C.00001=");
		assertExpressionEquals("1E-5");

		clickButtons("=");
		assertExpressionEquals("0.00001");

		clickButtons("C100*74=");
		assertExpressionEquals("7,400");

		clickButtons("=");
		assertExpressionEquals("7.4E3");
	}
}
