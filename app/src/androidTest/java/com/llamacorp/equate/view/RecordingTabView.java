package com.llamacorp.equate.view;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RecordingTabView {

	@Rule
	public ActivityTestRule<CalcActivity> mActivityTestRule = new ActivityTestRule<>(CalcActivity.class);

	@Test
	public void recordingTabView() {
		ViewInteraction tabView = onView(
				  allOf(withText("Weight"), isDisplayed()));
		tabView.perform(click());

	}

}
