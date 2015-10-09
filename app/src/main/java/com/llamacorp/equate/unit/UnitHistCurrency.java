package com.llamacorp.equate.unit;

import java.util.ArrayList;
import java.util.Locale;

public class UnitHistCurrency extends Unit {
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

