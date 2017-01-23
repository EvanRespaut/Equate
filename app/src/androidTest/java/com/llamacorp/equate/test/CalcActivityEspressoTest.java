package com.llamacorp.equate.test;


import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

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
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkArgument;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class CalcActivityEspressoTest {

	@Rule
	public MyActivityTestRule<CalcActivity> mActivityTestRule =
			  new MyActivityTestRule<>(CalcActivity.class);


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


//		ViewInteraction unitScrollView = onView(withText("Force"));
//		unitScrollView.perform(scrollTo(), click());

	}

	@Test
	public void testCalcActivity2() {
		onView(withId(getButtonID("C"))).perform(longClick());
		clickButtons("C1+2");
		assertExpressionEquals("1+2");

		clickButtons("=");
		assertExpressionEquals("3");

//		ViewInteraction appCompatTextView = onView(
//				  allOf(withId(R.id.list_item_result_textPrevAnswer), withText("3"), isDisplayed()));
//		appCompatTextView.perform(click());
	}


	@Test
	public void testNavigationDrawer() {
		// Open Drawer to click on navigation.
		onView(withId(R.id.drawer_layout))
				  .check(matches(isClosed(Gravity.START))) // Left Drawer should be closed.
				  .perform(open()); // Open Drawer

		ViewInteraction appCompatCheckedTextView = onView(
				  allOf(withId(R.id.design_menu_item_text), withText("Settings"), isDisplayed()));
		appCompatCheckedTextView.perform(click());

		ViewInteraction linearLayout = onView(
				  allOf(childAtPosition(
							 withId(android.R.id.list),
							 0),
							 isDisplayed()));
		linearLayout.perform(click());

		ViewInteraction appCompatCheckedTextView2 = onView(
				  allOf(withId(android.R.id.text1), withText("Length"),
							 childAtPosition(
										allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
												  withParent(withClassName(is("android.widget.FrameLayout")))),
										3),
							 isDisplayed()));
		appCompatCheckedTextView2.perform(click());

		ViewInteraction appCompatCheckedTextView3 = onView(
				  allOf(withId(android.R.id.text1), withText("Weight"),
							 childAtPosition(
										allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
												  withParent(withClassName(is("android.widget.FrameLayout")))),
										2),
							 isDisplayed()));
		appCompatCheckedTextView3.perform(click());

		ViewInteraction appCompatButton = onView(
				  allOf(withId(android.R.id.button1), withText("OK"),
							 withParent(allOf(withClassName(is("android.widget.LinearLayout")),
										withParent(withClassName(is("android.widget.LinearLayout"))))),
							 isDisplayed()));
		appCompatButton.perform(click());

		ViewInteraction appCompatImageButton = onView(
				  allOf(withContentDescription("Navigate up"),
							 withParent(allOf(withId(R.id.action_bar),
										withParent(withId(R.id.action_bar_container)))),
							 isDisplayed()));
		appCompatImageButton.perform(click());
	}

	@Test
	public void recordingTabView() {
		clickButtons("C12345");

		onView(allOf(withText("Currency"))).perform(
				  new ViewAction() {
					  @Override
					  public Matcher<View> getConstraints() {
						  return isEnabled(); // no constraints, they are checked above
					  }

					  @Override
					  public String getDescription() {
						  return "click Currency button";
					  }

					  @Override
					  public void perform(UiController uiController, View view) {
						  view.performClick();
					  }
				  }
		);

		clickButtons("26");

		onView(allOf(withText("Energy"))).perform(
				  new ViewAction() {
					  @Override
					  public Matcher<View> getConstraints() {
						  return isEnabled(); // no constraints, they are checked above
					  }

					  @Override
					  public String getDescription() {
						  return "click Currency button";
					  }

					  @Override
					  public void perform(UiController uiController, View view) {
						  view.performClick();
					  }
				  }
		);
		clickButtons("b");

		onView(allOf(withText("Power"))).perform(click());

		clickButtons("67");
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

	private void assertResultPreviewInvisible() {
		onView(withId(R.id.resultPreview)).check(matches(
				  withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
	}

	private void assertResultPreviewEquals(String expected) {
		onView(withId(R.id.resultPreview)).check(matches(allOf(isDisplayed(),
				  withText(expected))));
	}

	private void assertExpressionEquals(String expected) {
		getTextDisplay().check(matches(expressionEquals(expected)));
	}

	/**
	 * Method to check an expression contains the text given by the testString
	 * parameter.  This method also checks the test string isn't null and to turn
	 * it into a Matcher<String>.
	 * @param testString is the string to check the expression against
	 * @return a Matcher<View> that can be used to turn into a View Interaction
	 */
	private static Matcher<View> expressionEquals(String testString) {
		// use precondition to fail fast when a test is creating an invalid matcher
		checkArgument(!(testString.equals(null)));
		return expressionEquals(is(testString));
	}

	/**
	 * Note that ideal the method below should implement a describeMismatch
	 * method (as used by BaseMatcher), but this method is not invoked by
	 * ViewAssertions.matches() and it won't get called. This means that I'm not
	 * sure how to implement a custom error message.
	 */
	private static Matcher<View> expressionEquals(final Matcher<String> testString) {
		return new BoundedMatcher<View, EditText>(EditText.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("with expression text: " + testString);
			}

			@Override
			protected boolean matchesSafely(EditText item) {
				return testString.matches(item.getText().toString());
			}
		};
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
		InvalidButtonViewException(String message) {
			super(message);
		}
	}
}
