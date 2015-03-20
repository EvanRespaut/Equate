package com.llamacorp.equate;

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
	//used when we want to convert from this unit to this unit again (different year)
	private int mPreviousYearIndex = 0;
	private int mStartYearOffset;
	private ArrayList<Double> mHistoricalValueArray;


	public UnitHistCurrency(String name, String longName, ArrayList<Double> values,
			int indexStartYear, int defaultStartYear){
		mNamePrefix = name;
		mLongNamePrefix = longName;
		mHistoricalValueArray = values;
		mStartYearOffset = indexStartYear;
		if(defaultStartYear - indexStartYear < values.size())
			mYearIndex = defaultStartYear - indexStartYear;
		setYearIndex(mYearIndex);
	}	

	/** Load in the update time */
	public UnitHistCurrency(JSONObject json) throws JSONException {
		mNamePrefix = json.getString(JSON_NAME_PREFIX_TAG);
		mLongNamePrefix = json.getString(JSON_LONG_NAME_PREFIX_TAG);
		mYearIndex = json.getInt(JSON_YEAR_TAG);
		mStartYearOffset = json.getInt(JSON_OFFSET_TAG);

		mHistoricalValueArray = new ArrayList<Double>();
		JSONArray jUnitArray = json.getJSONArray(JSON_VALUES_TAG);
		for (int i = 0; i < jUnitArray.length(); i++) {
			mHistoricalValueArray.add(jUnitArray.getDouble(i));
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
		json.put(JSON_OFFSET_TAG, mStartYearOffset);

		JSONArray jUnitArray = new JSONArray();
		for (Double d : mHistoricalValueArray)
			jUnitArray.put(d);
		json.put(JSON_VALUES_TAG, jUnitArray);

		return json;
	}

	@Override
	public String convertTo(Unit toUnit, String expressionToConv) {
		double toUnitValue;
		if(this == toUnit)
			toUnitValue = getPreviousUnitValue();
		else
			toUnitValue = getValue();
		return expressionToConv + "*" + toUnit.getValue() + "/" + toUnitValue;
	}

	/**
	 * Index value 0 corresponds to 2014, 1 to 2013 etc
	 * @param reversedIndex
	 */
	public void setYearIndexReversed(int reversedIndex){
		setYearIndex(mHistoricalValueArray.size()-1-reversedIndex);
	}

	private void setYearIndex(int index){
		mPreviousYearIndex = mYearIndex;
		mYearIndex = index;
		setValue(mHistoricalValueArray.get(mYearIndex));
		refreshNames();
	}

	/**
	 * Used when conversion is being performed from one historical 
	 * year to another.  This function retrieves the first historical
	 * currency value selection
	 */
	private double getPreviousUnitValue(){
		return mHistoricalValueArray.get(mPreviousYearIndex);
	}

	/**
	 * @return an array of all years in decrementing order 
	 * (2014, 2013,... etc)
	 */
	public CharSequence[] getPossibleYearsReversed(){
		int arraySize = mHistoricalValueArray.size();
		CharSequence[] cs = new CharSequence[arraySize];
		for(int i=0;i<arraySize;i++)
			cs[i] = String.valueOf(mStartYearOffset + arraySize - 1 - i);
		return cs;
	}

	public String getGenericLongName(){
		return GENERIC_PREFIX + mLongNamePrefix + GENERIC_SUFFIX;
	}		

	public String getLowercaseGenericLongName(){
		String temp = GENERIC_PREFIX + mLongNamePrefix;
		return temp.toLowerCase(Locale.US) + GENERIC_SUFFIX;
	}

	public String getPreviousLowercaseLongName(){
		return getLongName(mPreviousYearIndex).toLowerCase(Locale.US);
	}

	public String getPreviousShortName(){
		return getShortName(mPreviousYearIndex);
	}		

	private void refreshNames(){
		setDispName(getShortName(mYearIndex));
		setLongName(getLongName(mYearIndex));		
	}

	private String getLongName(int index){
		return mLongNamePrefix + " in " + getSelectedYear(index);
	}

	private String getShortName(int index){
		return mNamePrefix + " [" + getSelectedYear(index) + "]";
	}

	private int getSelectedYear(int index){
		return index + mStartYearOffset;
	}	

	public int getReversedYearIndex(){
		return mHistoricalValueArray.size() - 1 - mYearIndex;
	}
}

