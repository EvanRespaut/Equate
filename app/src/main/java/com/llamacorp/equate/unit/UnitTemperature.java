package com.llamacorp.equate.unit;

public class UnitTemperature extends Unit {
	public static final double FAHRENHEIT = 1;
	public static final double CELSIUS = 2;
	public static final double KELVIN = 3;

	public UnitTemperature(String name, String longName, double tempType) {
		super(name, longName, tempType);
	}

	public UnitTemperature() {
		super("", "", 0);
	}

//	public UnitTemperature(JSONObject json) throws JSONException {
//		super(json);
//	}

	@Override
	public String convertTo(Unit toUnit, String expressionToConvert) {
		//converting from Fahrenheit, always go to Celsius
		if (getValue() == FAHRENHEIT)
			expressionToConvert = "(" + expressionToConvert + "-32)*5/9";

		//converting from Kelvin, always go to Celsius
		if (getValue() == KELVIN)
			expressionToConvert = "(" + expressionToConvert + "-273.15)";

		//if we wanted Celsius, break
		if (toUnit.getValue() == CELSIUS)
			return expressionToConvert;
		else if (toUnit.getValue() == FAHRENHEIT)
			return "(" + expressionToConvert + "*9/5+32";
		else if (toUnit.getValue() == KELVIN)
			return expressionToConvert + "+273.15";
		else
			return "";
	}
}
