package com.llamacorp.unitcalc;

import java.util.ArrayList;

public class UnitType {
	//this is for communication with the parent
	OnConvertionListener mCallback;

	// Parent class must implement this interface
	public interface OnConvertionListener {
		public void convertFromTo(Unit fromUnit, Unit toUnit);
	}


	private ArrayList<Unit> mUnitArray;
	private int mCurrUnitPos;
	private boolean mIsUnitSelected;

	/**
	 * Constructor
	 * @param hosting class must implement a function to do raw number conversion
	 */	
	public UnitType(Object parent){
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnConvertionListener) parent;
		} catch (ClassCastException e) {
			throw new ClassCastException(parent.toString()
					+ " must implement OnConvertKeySelectedListener");
		}

		mUnitArray = new ArrayList<Unit>();
		mIsUnitSelected = false;
	}

	/**
	 * Used to build a UnitType
	 */
	public void addUnit(String title, double value){
		Unit u = new Unit(title, value);
		mUnitArray.add(u);
	}


	/**
	 * Allows selection of a unit using a Unit, if that selection is plausible
	 * @return -1 if selection failed, otherwise the array position of the selected unit
	 */		
	public int selectUnit(Unit unitSelection){
		for(int i=0;i<mUnitArray.size();i++){
			if(unitSelection.equals(mUnitArray.get(i))){
				selectUnit(i);
				return i; //found the unit
			}
		}
		return -1;  //if we didn't find the unit
	}


	/**
	 * If mCurrUnit not set, set mCurrUnit
	 * If mCurrUnit already set, call functions to perform a convert
	 */		
	public boolean selectUnit(int pos){
		//used to tell caller if we needed to do a conversion
		boolean didConvert = false;
		//If we've already selected a unit, do conversion
		if(mIsUnitSelected){
			convert(pos);
			didConvert = true;
		}

		//Select new unit regardless
		mCurrUnitPos = pos;
		//Engage set flag
		mIsUnitSelected = true;
		return didConvert;
	}


	/**
	 * Resets mIsUnitSelected flag
	 */		
	public void clearUnitSelection(){
		mIsUnitSelected = false;
	}

	public boolean getIsUnitSelected(){
		return mIsUnitSelected;
	}

	/**
	 * @param Index of Unit in the mUnitArray list
	 * @return String name to be displayed on convert button
	 */
	public String getUnitDisplayName(int pos){
		return mUnitArray.get(pos).toString();
	}

	public int size() {
		return mUnitArray.size();
	}

	public int getCurrUnitPos(){
		return mCurrUnitPos;
	}

	/**
	 * Used to gather values to convert to and from and then call parent class to do raw conversion
	 * Convert from unit is specified by the current Unit
	 * @param newUnitPos is the Unit position to convert to
	 */
	private void convert(int newUnitPos){
		Unit fromUnit = mUnitArray.get(mCurrUnitPos);
		Unit toUnit = mUnitArray.get(newUnitPos);
		mCallback.convertFromTo(fromUnit, toUnit);
	}
}