package com.llamacorp.unitcalc;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UnitHistCurrency extends Unit {
	private static String JSON_NAME_SUFFIX_TAG = "suf";
	private static String JSON_LONG_NAME_SUFFIX_TAG = "long";
	private static String JSON_YEAR_TAG = "year";
	private static String JSON_OFFSET_TAG = "offset";
	private static String JSON_VALUES_TAG = "values";
	
	private String mNameSuffix;
	private String mLongNameSuffix;
	
	private int mYearIndex = 0;
	private int mIndexStartYearOffset;
	private ArrayList<Double> mHistoricalValues;
	

	public UnitHistCurrency(String name, String longName, ArrayList<Double> values,
			int indexStartYear, int defaultStartYear){
		mNameSuffix = name;
		mLongNameSuffix = longName;
		mHistoricalValues = values;
		mIndexStartYearOffset = indexStartYear;
		if(defaultStartYear - indexStartYear < values.size())
			mYearIndex = defaultStartYear - indexStartYear;
		setYearIndex(mYearIndex);
	}	

	/** Load in the update time */
	public UnitHistCurrency(JSONObject json) throws JSONException {
		mNameSuffix = json.getString(JSON_NAME_SUFFIX_TAG);
		mLongNameSuffix = json.getString(JSON_LONG_NAME_SUFFIX_TAG);
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
		
		json.put(JSON_NAME_SUFFIX_TAG, mNameSuffix);
		json.put(JSON_LONG_NAME_SUFFIX_TAG, mLongNameSuffix);
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
		setYearIndex(mHistoricalValues.size()-reversedIndex);
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
			cs[i] = String.valueOf(mIndexStartYearOffset + arraySize - i);
		return cs;
	}
	
	private void refreshNames(){
		String sYear = String.valueOf(getSelectedYear());
		sYear = sYear + " ";
		setDispName(sYear + mNameSuffix);
		setLongName(sYear + mLongNameSuffix);		
	}
	
	public int getReversedYearIndex(){
		return mHistoricalValues.size() - mYearIndex;
	}
	
	private int getSelectedYear(){
		return mYearIndex + mIndexStartYearOffset;
	}
}

