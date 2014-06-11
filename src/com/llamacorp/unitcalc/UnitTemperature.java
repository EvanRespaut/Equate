package com.llamacorp.unitcalc;

import org.json.JSONException;
import org.json.JSONObject;


public class UnitTemperature extends Unit {
    public static final double FAHRENHEIT = 1;
    public static final double CELSIUS = 2;
    public static final double KELVIN = 3;

	public UnitTemperature(String name, double tempType){
		super(name, tempType);
	}
	
	public UnitTemperature(){
		super("", 0);
	}

	public UnitTemperature(JSONObject json) throws JSONException {
		super(json);
	}
	
	@Override
	public String convertFrom(Unit fromUnit, String toConv){
		if(getValue() == FAHRENHEIT){
			if(fromUnit.getValue() == CELSIUS)
				return "(" + toConv + "-32)*5/9";
		}
		return "";
	}
}
