package com.llamacorp.equate;

import org.json.JSONException;
import org.json.JSONObject;

public class Hints {
	private static String JSON_PERFORMED_CONVERT = "perf_conv";
	
	private boolean mHasClickedUnit;
	
	public Hints(){
		mHasClickedUnit = false;
	}
	
	public Hints(JSONObject json) throws JSONException {
		mHasClickedUnit = json.getBoolean(JSON_PERFORMED_CONVERT); 
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(JSON_PERFORMED_CONVERT, mHasClickedUnit);
		return json;
	}	
	
	public boolean isHasClickedUnit(){
		return mHasClickedUnit;
	}
	
	public void setHasClickedUnitTrue(){
		mHasClickedUnit = true;
	}
}
