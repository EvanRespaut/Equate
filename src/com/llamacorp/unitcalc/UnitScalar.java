package com.llamacorp.unitcalc;

import org.json.JSONException;
import org.json.JSONObject;


public class UnitScalar extends Unit {
	private static final String JSON_INVERTED = "inverted";
	
	//flag used to distinguish if the Unit needs to be inverted before converting
	//for example min/miles should be inverted to miles/min before conv into mph
	private boolean mInverted=false; 

	public UnitScalar(String name, String longName, double value, boolean inverted){
		super(name, longName, value);
		mInverted=inverted;
	}	
	
	public UnitScalar(String name, String longName, double value){
		super(name, longName, value);
	}	
	
	//TODO do we need this?
	public UnitScalar(String name, double value){
		super(name, name, value);
	}	
	
	public UnitScalar(){
		super();
	}

	/** Load in the extra inverted flag */
	public UnitScalar(JSONObject json) throws JSONException {
		super(json);
		mInverted = json.getBoolean(JSON_INVERTED);
	}
	
	/** Save in the extra inverted flag */
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put(JSON_INVERTED, isInverted());
		return json;
	}

	public boolean isInverted(){
		return mInverted;
	}
	
	/**
	 * Perform Unit conversion
	 * @param toUnit is desired unit to convert into
	 * @param expressionToConv is the expression to convert
	 * @return returns the expression string to be evaluated, will 
	 * contain something like 33*fromValue/toValue.  Will possibly
	 * also include inversion for inverted units. For example, 33 min/mile
	 * to mph would be 1/33*...
	 */
	public String convertTo(Unit toUnit, String expressionToConv){
		String invertFrom = "";
		String invertTo = "";
		
		if(isInverted())
			invertFrom = "1/";
		if(((UnitScalar)toUnit).isInverted()){
			invertFrom = "1/";
			invertTo = "1/";
		}
		return invertFrom + expressionToConv + "*" + invertTo + "(" 
					+ toUnit.getValue() + "/" + getValue() + ")";
	}
}
