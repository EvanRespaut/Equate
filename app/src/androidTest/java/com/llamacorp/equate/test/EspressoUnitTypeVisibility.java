package com.llamacorp.equate.test;


import android.content.Context;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.view.Gravity;

import com.llamacorp.equate.R;
import com.llamacorp.equate.ResourceArrayParser;
import com.llamacorp.equate.view.CalcActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;

@RunWith(AndroidJUnit4.class)
public class EspressoUnitTypeVisibility {

	@Rule
	public MyActivityTestRule<CalcActivity> mActivityTestRule =
			  new MyActivityTestRule<>(CalcActivity.class);


	@Test
	public void testCheckUnitTypeNames() {
		// check that all unit types are visible (none removed)
		checkUnitTypesVisible(new ArrayList<String>());

		ArrayList<String> toRemoveArray = new ArrayList<>();
		toRemoveArray.add("Weight");
		toRemoveArray.add("Length");
		toRemoveArray.add("Energy");
		toRemoveArray.add("Temperature");

		// hide some unit types
		hideUnitTypes(toRemoveArray);

		//check hidden unit types are gone
		checkUnitTypesVisible(toRemoveArray);

		// clear remaining unit types
		ArrayList<String> toRemoveArray2 = new ArrayList<>();
		toRemoveArray2.add("Currency");
		toRemoveArray2.add("Area");
		toRemoveArray2.add("Volume");
		toRemoveArray2.add("Speed");
		toRemoveArray2.add("Time");
		toRemoveArray2.add("Fuel Economy");
		toRemoveArray2.add("Power");
		toRemoveArray2.add("Force");
		toRemoveArray2.add("Torque");
		toRemoveArray2.add("Pressure");
		toRemoveArray2.add("Digital Storage");

		// hide all unit types
		hideUnitTypes(toRemoveArray2);

		// add together all removed elements
		toRemoveArray.addAll(toRemoveArray2);

		//check hidden unit types are gone
		checkUnitTypesVisible(toRemoveArray);

	}


	private void checkUnitTypesVisible(ArrayList<String> removedUnitTypes) {
		Context targetContext = InstrumentationRegistry.getTargetContext();
		Resources resources = targetContext.getResources();

		ArrayList<String> removedTabNames = ResourceArrayParser
				  .getTabNamesFromNames(removedUnitTypes, resources);

		ArrayList<String> visibleUnitTypes = ResourceArrayParser.
				  getUnitTypeTabNameArrayList(resources);

		visibleUnitTypes.removeAll(removedTabNames);

		// check visible unit types are visible
		for (String s : visibleUnitTypes) {
			onView(allOf(withText(s), isDescendantOfA(withId(R.id.unit_container))))
					  .check(matches(withEffectiveVisibility(
								 ViewMatchers.Visibility.VISIBLE)));
		}

		if (visibleUnitTypes.size() == 0 )
			onView(withId(R.id.unit_container)).check(matches(
					  withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
		else {
			// check hidden unit types are in fact gone
			for (String s : removedTabNames) {
				onView(allOf(withText(s), isDescendantOfA(withId(R.id.unit_container))))
						  .check(doesNotExist());
			}
		}
	}


	private void hideUnitTypes(ArrayList<String> unitTypesToHide) {
		// Open Drawer to click on navigation.
		onView(withId(R.id.drawer_layout))
				  .check(matches(isClosed(Gravity.START))) // Left Drawer should be closed.
				  .perform(open()); // Open Drawer

		// Click settings
		onView(allOf(withId(R.id.design_menu_item_text), withText("Settings"),
				  isDisplayed())).perform(click());

		// Open dialog to select displayed unit types
		onView(allOf(withText("Displayed Unit Types"), isDisplayed()))
				  .perform(click());

		// Uncheck some unit types
		for (String unitName : unitTypesToHide) {
			onData(hasToString(unitName)).check(matches(isChecked())).perform(click());
		}

//		// this will click the 0th element of the adapter view
//		onData(is(instanceOf(String.class)))
//				.inAdapterView(allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")), isDisplayed()))
//				.atPosition(0).perform(click());

		onView(allOf(withText("OK"), isDisplayed())).perform(click());

		// Leave settings activity, go back to calculator
		onView(allOf(withContentDescription("Navigate up"),
				  withParent(allOf(withId(R.id.action_bar),
							 withParent(withId(R.id.action_bar_container)))),
				  isDisplayed())).perform(click());

	}
}
