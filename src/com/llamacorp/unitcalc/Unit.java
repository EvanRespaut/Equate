package com.llamacorp.unitcalc;

import java.lang.reflect.Constructor;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Unit  /*implements JsonSerializer<Unit>, JsonDeserializer<Unit> */{
	private static final String JSON_NAME = "name";
	private static final String JSON_LONG_NAME = "long_name";
	private static final String JSON_VALUE = "value";
	private static final String JSON_TYPE = "type";


	private String mDispName;
	private String mLongName;
	protected double mValue;

	//intercept's only known need is temp conversions
	protected Unit(String name, String longName, double value){
		mDispName = name;
		mLongName = longName;
		mValue = value;
	}	

	protected Unit(){
		this("", "", 0);
	}

	protected Unit(JSONObject json) throws JSONException {
		this(json.getString(JSON_NAME),
				json.getString(JSON_LONG_NAME),
				json.getDouble(JSON_VALUE)); 
	}

	/**
	 * Convert a JSON array into it's proper Unit
	 * @param json is they array to convert into Unit
	 * @return generic Unit class contacting a subclasses Unit
	 * @throws JSONException if exception is found
	 */
	static protected Unit getUnit(JSONObject json) throws JSONException {
		String unitType = json.getString(JSON_TYPE);
		String packageName = Unit.class.getPackage().getName();
		Unit u =null;
		try {
			Class<?> c = Class.forName(packageName + "." + unitType);
			Constructor<?> con = c.getConstructor(JSONObject.class);
			u = (Unit) con.newInstance(json);
		} catch (ClassNotFoundException e) { //for Class.forName(packageName + "." + unitType);
			throw new IllegalAccessError(unitType + " was not found");
		}   catch (NoSuchMethodException e){ //for u = (Unit) con.newInstance(json);
			throw new NoSuchMethodError(unitType + " doesn't have a JSONObject constructor");
		} catch (Exception e){//for c.getConstructor(JSONObject.class);
			throw new IllegalAccessError("problem with " + unitType + " class");
		}
		return u;
	}

	/**
	 * Save the current Unit into a JSON object
	 * @return JSON object encapsulating Unit
	 * @throws JSONException
	 */
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();

		json.put(JSON_TYPE, this.getClass().getSimpleName());
		json.put(JSON_NAME, toString());
		json.put(JSON_LONG_NAME, getLongName());
		json.put(JSON_VALUE, getValue());
		return json;
	}

	protected String getLongName() {
		return mLongName;
	}
	
	public String getLowercaseLongName(){
		return getLongName().toLowerCase(Locale.US);
	}

	protected double getValue() {
		return mValue;
	}

	public String toString(){
		return mDispName;
	}

	public boolean isDynamic(){
		//TODO this should be replaced with an interface
		return this instanceof UnitCurrency;
	}
	

	protected abstract String convertTo(Unit toUnit, String expressionToConv);

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
