package com.llamacorp.equate.test;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import android.widget.ListView;

import com.llamacorp.equate.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.anything;

/**
 * Created by Evan on 1/24/2017.
 */

public class EspressoTestUtils {
	/**
	 * Clicks on the tab for the provided Unit Type name. Note that the Unit Type
	 * doesn't need to be visible.
	 */
	public static void selectUnitTypeDirect(String unitTypeName) {
		onView(allOf(withText(unitTypeName))).perform(
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
	}

	/**
	 * Clicks a unit in the unit pager with the displayed name of unitName
	 */
	public static void clickUnit(String unitName) {
		onView(allOf(anyOf(withText(unitName), withText("➜ " + unitName)),
				  isDescendantOfA(withId(R.id.unit_pager))
				  , isDisplayed())).perform(click());
	}

	public static void clickPrevAnswer() {
		ResultClicker rc = new ResultClicker();
		rc.clickPrevAnswer();
	}

	public static void clickPrevQuery() {
		ResultClicker rc = new ResultClicker();
		rc.clickPrevQuery();
	}

	public static void clickButtons(String buttonString) {
		for (int i = 0; i < buttonString.length(); i++) {
			String s = buttonString.substring(i, i + 1);
			clickButton(s);
		}
	}

	private static void clickButton(String s) {
		int id;
		boolean longClick = false;

		//special case buttons
		switch (s) {
			case "^":
				id = R.id.multiply_button;
				longClick = true;
				break;
			case "E":
				id = R.id.percent_button;
				longClick = true;
				break;
			default:
				id = getButtonID(s);
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
	private static int getButtonID(String s) {
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
			case "%":
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

	private static class ResultClicker {
		private int numberOfAdapterItems;

		public void clickPrevAnswer() {
			updateNumberofResults();

			onData(anything())
					  .inAdapterView(withId(android.R.id.list))
					  .atPosition(numberOfAdapterItems - 1)
					  .onChildView(withId(R.id.list_item_result_textPrevAnswer))
					  .perform(click());
		}

		public void clickPrevQuery() {
			updateNumberofResults();

			onData(anything())
					  .inAdapterView(withId(android.R.id.list))
					  .atPosition(numberOfAdapterItems - 1)
					  .onChildView(withId(R.id.list_item_result_textPrevQuery))
					  .perform(click());
		}

		private void updateNumberofResults() {
			onView(withId(android.R.id.list)).check(matches(new TypeSafeMatcher<View>() {
				@Override
				public boolean matchesSafely(View view) {
					ListView listView = (ListView) view;
					//here we assume the adapter has been fully loaded already
					numberOfAdapterItems = listView.getAdapter().getCount();
					return true;
				}

				@Override
				public void describeTo(Description description) {
				}
			}));
		}

	}

	private static class InvalidButtonViewException extends RuntimeException {
		InvalidButtonViewException(String message) {
			super(message);
		}
	}
}
