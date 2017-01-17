package com.llamacorp.equate.test;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.llamacorp.equate.R;
import com.llamacorp.equate.view.CalcActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CalcActivityEspressoTest {

	@Rule
	public ActivityTestRule<CalcActivity> mActivityTestRule =
			  new ActivityTestRule<>(CalcActivity.class);

	@Test
	public void testCalcActivity() {
//		onView(withId(R.id.clear_button)).perform(click());
//
//		ViewInteraction appCompatButton8 = onView(
//				  allOf(withId(R.id.eight_button), withText("8"), isDisplayed()));
//		appCompatButton8.perform(click());
//
//		ViewInteraction appCompatButtonPlus = onView(
//				  allOf(withId(R.id.plus_button), withText("+"), isDisplayed()));
//		appCompatButtonPlus.perform(click());
//
//		ViewInteraction appCompatButton6 = onView(
//				  allOf(withId(R.id.six_button), withText("6"), isDisplayed()));
//		appCompatButton6.perform(click());
//
//		ViewInteraction appCompatButtonEquals = onView(
//				  allOf(withId(R.id.equals_button), withText("="), isDisplayed()));
//		appCompatButtonEquals.perform(click());
		clickButtons("C");
		assertExpressionEquals("");

		onView(withId(R.id.open_para_button)).perform(click());

//		clickButtons("(");
//		assertExpressionEquals("(");

		clickButtons(".");
		assertExpressionEquals("(.");

		clickButtons("1");
		assertExpressionEquals("(.1");

		clickButtons("+b");
		assertExpressionEquals("(.1");

		clickButtons(")4");
		assertExpressionEquals("(.1)*4");

		clickButtons("=");
		assertExpressionEquals("0.4");

//		ViewInteraction unitScrollView = onView(withText("Force"));
//		unitScrollView.perform(scrollTo(), click());

	}

	@Test
	public void testCalcActivity2() {
		clickButtons("C");
		assertExpressionEquals("");
		onView(withId(R.id.close_para_button)).perform(click());
		assertExpressionEquals(")");
	}

	@Test
	public void testCalcActivity8() {
		clickButtons("1C8");
		assertExpressionEquals("8");
	}

		private void assertExpressionEquals(String expected) {
		getTextDisplay().check(matches(withText(expected)));
	}

	private ViewInteraction getTextDisplay() {
		return onView(withId(R.id.textDisplay));
	}

	/**
	 * Helper function clicks a button specified by a string
	 *
	 * @param buttonString 1-9 for numbers, +-* etc operators; also, "a1" is one
	 *                     answer ago, "q0" is the last query
	 */
	private void clickButtons(String buttonString) {
		for (int i = 0; i < buttonString.length(); i++) {
			String s = buttonString.substring(i, i + 1);
			clickButton(s);
		}
	}

	private void clickButton(String s) {
		int id = getButtonID(s);
		boolean longClick = false;

		//special case buttons
		switch (s) {
			case "^":
				id = R.id.multiply_button;
				longClick = true;
				break;
			case "%":
				id = R.id.minus_button;
				longClick = true;
				break;
		}

		if (longClick) onView(withId(id)).perform(longClick());
		else onView(withId(id)).perform(click());
	}


	/**
	 * Helper function takes a string of a key hit and passes back the View id
	 *
	 * @param s plain text form of the button
	 * @return id of the button
	 */
	private int getButtonID(String s) {
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

		int buttonId = -1;

		switch (s) {
			case "+":
				buttonId = R.id.plus_button;
				break;
			case "-":
				buttonId = R.id.minus_button;
				break;
			case "*":
				buttonId = R.id.multiply_button;
				break;
			case "/":
				buttonId = R.id.divide_button;
				break;
			case ".":
				buttonId = R.id.decimal_button;
				break;
			case "=":
				buttonId = R.id.equals_button;
				break;
			case "E":
				buttonId = R.id.percent_button;
				break;
			case "^":
				buttonId = R.id.multiply_button;
				break;
			case "(":
				buttonId = R.id.open_para_button;
				break;
			case ")":
				buttonId = R.id.close_para_button;
				break;
			case "b":
				buttonId = R.id.backspace_button;
				break;
			case "C":
				buttonId = R.id.clear_button;
				break;
			default:
				//this for loop checks for numerical values
				for (int i = 0; i < 10; i++)
					if (s.equals(Character.toString((char) (48 + i))))
						buttonId = numButtonIds[i];
		}
		if (buttonId == -1) throw new InvalidButtonViewException(
				  "No View could be found for button = \"" + s + "\"");
		return buttonId;
	}


	private static class InvalidButtonViewException extends RuntimeException {
		public InvalidButtonViewException(String message) {
			super(message);
		}
	}

	private static Matcher<View> childAtPosition(
			  final Matcher<View> parentMatcher, final int position) {

		return new TypeSafeMatcher<View>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("Child at position " + position + " in parent ");
				parentMatcher.describeTo(description);
			}

			@Override
			public boolean matchesSafely(View view) {
				ViewParent parent = view.getParent();
				return parent instanceof ViewGroup && parentMatcher.matches(parent)
						  && view.equals(((ViewGroup) parent).getChildAt(position));
			}
		};
	}
}
