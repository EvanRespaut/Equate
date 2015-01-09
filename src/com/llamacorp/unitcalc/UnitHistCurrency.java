package com.llamacorp.unitcalc;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UnitHistCurrency extends Unit {
	private static String JSON_NAME_PREFIX_TAG = "suf";
	private static String JSON_LONG_NAME_PREFIX_TAG = "long";
	private static String JSON_YEAR_TAG = "year";
	private static String JSON_OFFSET_TAG = "offset";
	private static String JSON_VALUES_TAG = "values";
	
	private static String GENERIC_PREFIX = "Historical ";
	private static String GENERIC_SUFFIX = " (CPI)";
	
	private String mNamePrefix;
	private String mLongNamePrefix;
	
	private int mYearIndex = 0;
	private int mIndexStartYearOffset;
	private ArrayList<Double> mHistoricalValues;
	

	public UnitHistCurrency(String name, String longName, ArrayList<Double> values,
			int indexStartYear, int defaultStartYear){
		mNamePrefix = name;
		mLongNamePrefix = longName;
		mHistoricalValues = values;
		mIndexStartYearOffset = indexStartYear;
		if(defaultStartYear - indexStartYear < values.size())
			mYearIndex = defaultStartYear - indexStartYear;
		setYearIndex(mYearIndex);
	}	

	/** Load in the update time */
	public UnitHistCurrency(JSONObject json) throws JSONException {
		mNamePrefix = json.getString(JSON_NAME_PREFIX_TAG);
		mLongNamePrefix = json.getString(JSON_LONG_NAME_PREFIX_TAG);
		mYearIndex = json.getInt(JSON_YEAR_TAG);
		mIndexStartYearOffset = json.getInt(JSON_OFFSET_TAG);
		
		mHistoricalValues = new ArrayList<Double>();
		JSONArray jUnitArray = json.getJSONArray(JSON_VALUES_TAG);
		for (int i = 0; i < jUnitArray.length(); i++) {
			mHistoricalValues.add(jUnitArray.getDouble(i));
		}
		setYearIndex(mYearIndex);
	}

	/** Save the update time */
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		
		json.put(JSON_NAME_PREFIX_TAG, mNamePrefix);
		json.put(JSON_LONG_NAME_PREFIX_TAG, mLongNamePrefix);
		json.put(JSON_YEAR_TAG, mYearIndex);
		json.put(JSON_OFFSET_TAG, mIndexStartYearOffset);
		
		JSONArray jUnitArray = new JSONArray();
		for (Double d : mHistoricalValues)
			jUnitArray.put(d);
		json.put(JSON_VALUES_TAG, jUnitArray);
		
		return json;
	}
	
	@Override
	public String convertTo(Unit toUnit, String expressionToConv) {
		return expressionToConv + "*" + toUnit.getValue() + "/" + getValue();
	}

	/**
	 * Index value 0 corresponds to 2014, 1 to 2013 etc
	 * @param reversedIndex
	 */
	public void setYearIndexReversed(int reversedIndex){
		setYearIndex(mHistoricalValues.size()-1-reversedIndex);
	}
	
	private void setYearIndex(int index){
		mYearIndex = index;
		setValue(mHistoricalValues.get(mYearIndex));
		refreshNames();
	}
	
	/**
	 * @return an array of all years in decrementing order 
	 * (2014, 2013,... etc)
	 */
	public CharSequence[] getPossibleYearsReversed(){
		int arraySize = mHistoricalValues.size();
		CharSequence[] cs = new CharSequence[arraySize];
		for(int i=0;i<arraySize;i++)
			cs[i] = String.valueOf(mIndexStartYearOffset + arraySize - 1 - i);
		return cs;
	}
	
	public String getGenericLongName(){
		return GENERIC_PREFIX + mLongNamePrefix + GENERIC_SUFFIX;
	}		
	
	public String getLowercaseGenericLongName(){
		String temp = GENERIC_PREFIX + mLongNamePrefix;
		return temp.toLowerCase(Locale.US) + GENERIC_SUFFIX;
	}
	
	private void refreshNames(){
		String sYear = String.valueOf(getSelectedYear());
		setDispName(mNamePrefix + " [" + sYear + "]");
		setLongName(mLongNamePrefix + " in " + sYear);		
	}
	
	public int getReversedYearIndex(){
		return mHistoricalValues.size() - 1 - mYearIndex;
	}
	
	private int getSelectedYear(){
		return mYearIndex + mIndexStartYearOffset;
	}
}

