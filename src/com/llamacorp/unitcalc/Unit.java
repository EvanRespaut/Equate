package com.llamacorp.unitcalc;

import java.lang.reflect.Constructor;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Unit  /*implements JsonSerializer<Unit>, JsonDeserializer<Unit> */{
	private static final String JSON_NAME = "name";
	private static final String JSON_LONG_NAME = "long_name";
	private static final String JSON_VALUE = "value";
	private static final String JSON_TYPE = "type";


	private String mDispName;
	private String mLongName;
	private double mValue;

	//intercept's only known need is temp conversions
	public Unit(String name, String longName, double value){
		mDispName = name;
		mLongName = longName;
		mValue = value;
	}	

	public Unit(){
		this("", "", 0);
	}

	public Unit(JSONObject json) throws JSONException {
		this(json.getString(JSON_NAME),
				json.getString(JSON_LONG_NAME),
				json.getDouble(JSON_VALUE)); 

	}


	static public Unit getUnit(JSONObject json) throws JSONException {
		System.out.println(json.toString());
		String unitType = json.getString(JSON_TYPE);
		String packageName = Unit.class.getPackage().getName();
		Unit u =null;
		try {
			Class<?> c = Class.forName(packageName + "." + unitType);
			Constructor<?> con = c.getConstructor(JSONObject.class);
			u = (Unit) con.newInstance(json);
		}
		catch (Exception e){
			//probably bad form, this catches a bunch of exceptions
			e.printStackTrace();
		}
		return u;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();

		json.put(JSON_TYPE, this.getClass().getSimpleName());
		json.put(JSON_NAME, toString());
		json.put(JSON_LONG_NAME, getLongName());
		json.put(JSON_VALUE, getValue());
		return json;
	}


	public String getLongName() {
		return mLongName;
	}

	public double getValue() {
		return mValue;
	}

	public String toString(){
		return mDispName;
	}


	public abstract String convertTo(Unit toUnit, String expressionToConv);

	@Override
	public boolean equals(Object other){
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof UnitScalar))return false;
		UnitScalar otherUnit = (UnitScalar)other;
		return (otherUnit.getValue() == this.getValue() &&
				otherUnit.toString().equals(this.toString()));
	}
}
