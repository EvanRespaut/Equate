package com.llamacorp.equate.unit;

public class UnitTemperature extends Unit {
	public static final double FAHRENHEIT = 1;
	public static final double CELSIUS = 2;
	public static final double KELVIN = 3;

	public UnitTemperature(String name, String longName, double tempType){
		super(name, longName, tempType);
	}

	public UnitTemperature(){
		super("", "", 0);
	}

//	public UnitTemperature(JSONObject json) throws JSONException {
//		super(json);
//	}

	@Override
	public String convertTo(Unit toUnit, String expressionToConv){
		//converting from Fahrenheit, always go to Celsius
		if(getValue() == FAHRENHEIT)
			expressionToConv = "(" + expressionToConv + "-32)*5/9";

		//converting from Kelvin, always go to Celsius
		if(getValue() == KELVIN)
			expressionToConv = "(" + expressionToConv + "-273.15)";

		//if we wanted Celsius, break
		if(toUnit.getValue() == CELSIUS)
			return expressionToConv;
		else if(toUnit.getValue() == FAHRENHEIT)
			return "(" + expressionToConv + "*9/5+32";
		else if(toUnit.getValue() == KELVIN)
			return expressionToConv + "+273.15";
		else 
			return "";
	}
}
