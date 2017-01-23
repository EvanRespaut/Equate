package com.llamacorp.equate.view;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.llamacorp.equate.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RecodingSettings {

	@Rule
	public ActivityTestRule<CalcActivity> mActivityTestRule = new ActivityTestRule<>(CalcActivity.class);

	@Test
	public void recodingSettings() {
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
