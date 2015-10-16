package com.llamacorp.equate;

import org.json.JSONException;
import org.json.JSONObject;

public class Preferences {
	private static String JSON_PERCENT_BUT_MAIN = "percent_main";
	private static String JSON_PERCENT_BUT_SEC = "percent_sec";

	private String mPercentButMain; //main function of percent button
	private String mPercentButSec; //secondary function of percent button

	public Preferences(){
		setPercentButMain("%");
		setPercentButSec("EE");
	}
	
	public Preferences(JSONObject json) throws JSONException {
		setPercentButMain(json.getString(JSON_PERCENT_BUT_MAIN));
		setPercentButSec(json.getString(JSON_PERCENT_BUT_SEC));
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(JSON_PERCENT_BUT_MAIN, getPercentButMain());
		json.put(JSON_PERCENT_BUT_SEC, getPercentButSec());
		return json;
	}

	public String getPercentButSec() {
		return mPercentButSec;
	}

	public void setPercentButSec(String mPercentButSec) {
		this.mPercentButSec = mPercentButSec;
	}

	public String getPercentButMain() {
		return mPercentButMain;
	}

	public void setPercentButMain(String mPercentButMain) {
		this.mPercentButMain = mPercentButMain;
	}
}
