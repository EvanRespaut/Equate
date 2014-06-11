package com.llamacorp.unitcalc;

import org.json.JSONException;
import org.json.JSONObject;

public class UnitScalar extends Unit {
	//intercept's only known need is temp conversions
	public UnitScalar(String name, double value){
		super(name, value);
	}	

	public UnitScalar(){
		super();
	}

	public UnitScalar(JSONObject json) throws JSONException {
		super(json);
	}


	public String convertFrom(Unit fromUnit, String toConv){
		return toConv + "*" + getValue() + "/" + fromUnit.getValue();
	}
	
	/*
	private static final String JSON_NAME = "name";
	private static final String JSON_VALUE = "value";

	private String mDispName;
	private double mValue;

	//intercept's only known need is temp conversions
	public UnitScalar(String name, double value){
		mDispName = name;
		mValue = value;
	}	

	public UnitScalar(){
		this("", 0);
	}

	public UnitScalar(JSONObject json) throws JSONException {
		this(json.getString(JSON_NAME), 
				json.getDouble(JSON_VALUE)); 
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();

		json.put(JSON_NAME, toString());
		json.put(JSON_VALUE, getValue());
		return json;
	}

	public double getValue() {
		return mValue;
	}

	public String toString(){
		return mDispName;
	}


	public String convertFrom(UnitScalar fromUnit, String toConv){
		return toConv + "*" + getValue() + "/" + fromUnit.getValue();
	}

	@Override
	public boolean equals(Object other){
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof UnitScalar))return false;
		UnitScalar otherUnit = (UnitScalar)other;
		return (otherUnit.getValue() == this.getValue() &&
				otherUnit.toString().equals(this.toString()));
	}
	*/
}
