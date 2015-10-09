package com.llamacorp.equate.unit;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Unit  /*implements JsonSerializer<Unit>, JsonDeserializer<Unit> */{
	private static final String JSON_NAME = "name";
//	private static final String JSON_LONG_NAME = "long_name";
	private static final String JSON_VALUE = "value";


	private String mDispName;
	private String mLongName;
	protected double mValue;

	protected Unit(String name, String longName, double value){
		mDispName = name;
		mLongName = longName;
		mValue = value;
	}	

	protected Unit(){
		this("", "", 0);
	}

//	protected Unit(JSONObject json) throws JSONException {
//		this(json.getString(JSON_NAME),
//				json.getString(JSON_LONG_NAME),
//				json.getDouble(JSON_VALUE));
//	}

	/**
	 * Load in user saved data to an existing Unit.  Example of this is the
    * value from a Currency that has been updated.  Method makes sure Unit
    * name of saved data matches this Unit to avoid inadvertently writing saved
    * data to the wrong Unit
	 * @param json JSON object containing saved data
	 * @return boolean if this Unit matches saved Unit
	 * @throws JSONException if can't find JSON data
	 */
	protected boolean loadJSON(JSONObject json) throws JSONException {
      //if the Units moved around (and therefore have different names), don't
      // want to load the wrong value
      if(!json.getString(JSON_NAME).equals(toString()))
         return false;

      mValue = json.getDouble(JSON_VALUE);

      return true; //successful

      /*
		String unitType = json.getString(JSON_TYPE);
		String packageName = Unit.class.getPackage().getName();
		Unit u;
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
      */
	}

	/**
	 * Save user changeable data of this Unit into a JSON object
	 * @return JSON object with saved Unit data
	 * @throws JSONException
	 */
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();

		//json.put(JSON_TYPE, this.getClass().getSimpleName());
		json.put(JSON_NAME, toString());
		//json.put(JSON_LONG_NAME, getLongName());
		json.put(JSON_VALUE, getValue());
		return json;
	}

	protected void setLongName(String longName){
		mLongName = longName;
	}
	
	protected String getLongName() {
		return mLongName;
	}

	public String getName(){
		return toString();
	}

	public String getGenericLongName(){
		return mLongName;
	}	
	
	public String getLowercaseGenericLongName(){
		return getGenericLongName().toLowerCase(Locale.US);
	}
	
	public String getLowercaseLongName(){
		return getLongName().toLowerCase(Locale.US);
	}

	protected void setValue(double value){
		mValue = value;
	}
	
	protected double getValue() {
		return mValue;
	}

	protected void setDispName(String name){
		mDispName = name; 
	}
	
	public String toString(){
		return mDispName;
	}

	public boolean isDynamic(){
		//TODO this should be replaced with an interface
		return this instanceof UnitCurrency;
	}
	
	public boolean isHistorical(){
		//TODO this should be replaced with an interface
		return this instanceof UnitHistCurrency;
	}
	

	public abstract String convertTo(Unit toUnit, String expressionToConv);

	@Override
	public boolean equals(Object other){
		if (other == null) return false;
		if (other == this) return true;  //if objects are pointing to the same ref
		//now check if a cast into Unit is possible and then check compatibility
		if (!(other instanceof Unit))return false;
		Unit otherUnit = (Unit)other;
		return (otherUnit.getValue() == this.getValue() &&
				otherUnit.toString().equals(this.toString()));
	}
}
