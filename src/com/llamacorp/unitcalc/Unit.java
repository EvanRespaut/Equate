package com.llamacorp.unitcalc;

public class Unit{
	private String mDispName;
	private double mValue;
	//0 for most conversions; 32 and -160/9 for Celsius and Fahrenheit
	private double mIntercept;

	public Unit(String name, double value){
		mDispName = name;
		mValue = value;
		//mIntercept = intercept;
	}
	
	public Unit(){
		mDispName = "";
		mValue = 0;
	}

	public double getValue() {
		return mValue;
	}
	
	public String toString(){
		return mDispName;
	}

}
