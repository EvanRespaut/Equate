package com.llamacorp.equate.view;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.llamacorp.equate.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EspressoRecording {

	@Rule
	public ActivityTestRule<CalcActivity> mActivityTestRule = new ActivityTestRule<>(CalcActivity.class);

	@Test
	public void espressoRecording() {
		ViewInteraction appCompatButton = onView(
				  allOf(withId(R.id.one_button), withText("1"), isDisplayed()));
		appCompatButton.perform(click());

		ViewInteraction appCompatButton2 = onView(
				  allOf(withId(R.id.plus_button), withText("+"), isDisplayed()));
		appCompatButton2.perform(click());

		ViewInteraction appCompatButton3 = onView(
				  allOf(withId(R.id.two_button), withText("2"), isDisplayed()));
		appCompatButton3.perform(click());

		ViewInteraction appCompatTextView = onView(
				  allOf(withId(R.id.list_item_result_textPrevAnswer), withText("3"), isDisplayed()));
		appCompatTextView.perform(click());

	}

}
