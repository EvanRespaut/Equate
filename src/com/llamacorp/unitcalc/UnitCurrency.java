package com.llamacorp.unitcalc;

public class UnitCurrency extends Unit {
	private String mURL;	

	public UnitCurrency(String name, String longName, double value, String URL){
		super(name, longName, value);
		mURL = URL;
	}	
	
	public String getURL() {
		return mURL;
	}
	
	@Override
	public String convertTo(Unit toUnit, String expressionToConv) {
		return expressionToConv + "*" + toUnit.getValue() + "/" + getValue();
	}

}
