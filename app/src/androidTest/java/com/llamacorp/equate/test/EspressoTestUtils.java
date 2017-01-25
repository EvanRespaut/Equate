package com.llamacorp.equate.test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Evan on 1/24/2017.
 */

public class EspressoTestUtils {
	public static void clickButtons(String buttonString) {
		for (int i = 0; i < buttonString.length(); i++) {
			String s = buttonString.substring(i, i + 1);
			clickButton(s);
		}
	}

	private static void clickButton(String s) {
		int id = getButtonID(s);
		boolean longClick = false;

		//special case buttons
		switch (s) {
			case "^":
				id = com.llamacorp.equate.R.id.multiply_button;
				longClick = true;
				break;
			case "%":
				id = com.llamacorp.equate.R.id.minus_button;
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
	private static int getButtonID(String s) {
		int[] numButtonIds = {
				  com.llamacorp.equate.R.id.zero_button,
				  com.llamacorp.equate.R.id.one_button,
				  com.llamacorp.equate.R.id.two_button,
				  com.llamacorp.equate.R.id.three_button,
				  com.llamacorp.equate.R.id.four_button,
				  com.llamacorp.equate.R.id.five_button,
				  com.llamacorp.equate.R.id.six_button,
				  com.llamacorp.equate.R.id.seven_button,
				  com.llamacorp.equate.R.id.eight_button,
				  com.llamacorp.equate.R.id.nine_button};

		int buttonId = -1;

		switch (s) {
			case "+":
				buttonId = com.llamacorp.equate.R.id.plus_button;
				break;
			case "-":
				buttonId = com.llamacorp.equate.R.id.minus_button;
				break;
			case "*":
				buttonId = com.llamacorp.equate.R.id.multiply_button;
				break;
			case "/":
				buttonId = com.llamacorp.equate.R.id.divide_button;
				break;
			case ".":
				buttonId = com.llamacorp.equate.R.id.decimal_button;
				break;
			case "=":
				buttonId = com.llamacorp.equate.R.id.equals_button;
				break;
			case "E":
				buttonId = com.llamacorp.equate.R.id.percent_button;
				break;
			case "^":
				buttonId = com.llamacorp.equate.R.id.multiply_button;
				break;
			case "(":
				buttonId = com.llamacorp.equate.R.id.open_para_button;
				break;
			case ")":
				buttonId = com.llamacorp.equate.R.id.close_para_button;
				break;
			case "b":
				buttonId = com.llamacorp.equate.R.id.backspace_button;
				break;
			case "C":
				buttonId = com.llamacorp.equate.R.id.clear_button;
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
