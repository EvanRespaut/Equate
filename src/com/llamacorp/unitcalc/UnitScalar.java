package com.llamacorp.unitcalc;

import org.json.JSONException;
import org.json.JSONObject;

public class UnitScalar extends Unit {

	//intercept's only known need is temp conversions
	public UnitScalar(String name, String longName, double value){
		super(name, longName, value);
	}	
	
	public UnitScalar(String name, double value){
		super(name, name, value);
	}	
	
	public UnitScalar(){
		super();
	}

	public UnitScalar(JSONObject json) throws JSONException {
		super(json);
	}


	public String convertTo(Unit toUnit, String expressionToConv){
		return expressionToConv + "*" + toUnit.getValue() + "/" + getValue();
	}
}
