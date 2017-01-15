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
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CalcActivityEspressoTest {

	@Rule
	public ActivityTestRule<CalcActivity> mActivityTestRule = new ActivityTestRule<>(CalcActivity.class);

	@Test
	public void testCalcActivity() {
		ViewInteraction appCompatButtonClear = onView(
				  allOf(withId(R.id.clear_button), withText("C"), isDisplayed()));
		appCompatButtonClear.perform(click());

		ViewInteraction appCompatButton8 = onView(
				  allOf(withId(R.id.eight_button), withText("8"), isDisplayed()));
		appCompatButton8.perform(click());

		ViewInteraction appCompatButtonPlus = onView(
				  allOf(withId(R.id.plus_button), withText("+"), isDisplayed()));
		appCompatButtonPlus.perform(click());

		ViewInteraction appCompatButton6 = onView(
				  allOf(withId(R.id.six_button), withText("6"), isDisplayed()));
		appCompatButton6.perform(click());

		ViewInteraction appCompatButtonEquals = onView(
				  allOf(withId(R.id.equals_button), withText("="), isDisplayed()));
		appCompatButtonEquals.perform(click());

		ViewInteraction editDisplay = onView(
				  allOf(withId(R.id.textDisplay), withText("14"),
							 childAtPosition(
										childAtPosition(
												  IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
												  0),
										3),
							 isDisplayed()));
		editDisplay.check(matches(withText("14")));

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
