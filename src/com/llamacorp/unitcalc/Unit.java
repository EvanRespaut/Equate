package com.llamacorp.unitcalc;

public class Unit{
	private String mDispName;
	private double mValue;

	public Unit(String name, double value){
		mDispName = name;
		mValue = value;
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
