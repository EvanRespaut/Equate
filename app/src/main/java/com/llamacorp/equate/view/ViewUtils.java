package com.llamacorp.equate.view;

import android.content.Context;

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
}
