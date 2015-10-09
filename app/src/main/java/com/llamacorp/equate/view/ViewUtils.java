package com.llamacorp.equate.view;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class  ViewUtils {
	public static float pixelsToSp(Context context, float px) {
		if(context != null){
			float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
			return px/scaledDensity;
		}
		else return 0;
	}


	public static float spToPixels(Context context, float sp) {
		if(context != null){
			float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
			return sp*scaledDensity;
		}
		else return 0;
	}
	

	public static float pixelsToDp(Context context, float px) {
		if(context != null){
			float density = context.getResources().getDisplayMetrics().density;
			return px / density;
		}
		else return 0;
	}
	
	public static int floatToInt(float fl){
		return (int) Math.ceil(fl);
	}

	public static void toastLongCentered(String text, Context c) {
		final Toast toast = Toast.makeText(c, text, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static void toastCentered(String text, Context c) {
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
}
