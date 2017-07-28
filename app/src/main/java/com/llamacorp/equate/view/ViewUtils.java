package com.llamacorp.equate.view;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.widget.Toast;

import com.llamacorp.equate.R;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
public class ViewUtils {
	public static float pixelsToSp(Context context, float px) {
		if (context != null){
			float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
			return px / scaledDensity;
		} else return 0;
	}


	public static float spToPixels(Context context, float sp) {
		if (context != null){
			float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
			return sp * scaledDensity;
		} else return 0;
	}

	public static float pixelsToDp(Context context, float px) {
		if (context != null){
			float density = context.getResources().getDisplayMetrics().density;
			return px / density;
		} else return 0;
	}

	public static int floatToInt(float fl) {
		return (int) Math.ceil(fl);
	}

	public static void toastLongCentered(String text, Context c) {
		final Toast toast = Toast.makeText(c, text, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	static void toastCentered(String text, Context c) {
		final Toast toast = Toast.makeText(c, text, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static void toastLong(String text, Context c) {
		final Toast toast = Toast.makeText(c, text, Toast.LENGTH_LONG);
		toast.show();
	}

	public static void toast(String text, Context c) {
		final Toast toast = Toast.makeText(c, text, Toast.LENGTH_SHORT);
		toast.show();
	}

	@SuppressWarnings("deprecation")
	public static Spanned fromHtml(String html) {
		Spanned result;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
			result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
		} else {
			result = Html.fromHtml(html);
		}
		return result;
	}

	/**
	 * Class used to load the default timeout for a long press on a button click.
	 * This is necessary since Espresso tests sometimes hold the button down for
	 * long enough to trigger the long click action.
	 * @param context provided to access Resources
	 * @return the timeout for a long click in milliseconds
	 */
	static int getLongClickTimeout(Context context) {
		if (isRunningEspressoTest())
			return context.getResources().getInteger(R.integer.long_click_timeout_test);
		else
			return context.getResources().getInteger(R.integer.long_click_timeout);
	}


	private static AtomicBoolean isRunningTest;

	/**
	 * Determines if the app is currently running an Espresso test
	 * @return true if currently running an Espresso test
	 */
	private static synchronized boolean isRunningEspressoTest() {
		if (null == isRunningTest){
			boolean isTest;

			try {
				Class.forName("android.support.test.espresso.Espresso");
				isTest = true;
			} catch (ClassNotFoundException e) {
				isTest = false;
			}

			isRunningTest = new AtomicBoolean(isTest);
		}

		return isRunningTest.get();
	}
}
