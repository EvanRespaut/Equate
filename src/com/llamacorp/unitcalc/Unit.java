package com.llamacorp.unitcalc;

public class Unit{
	private String mDispName;
	private double mValue;
	//0 for most conversions; 32 and -160/9 for Celsius and Fahrenheit
	private double mIntercept;

	
	//intercept's only known need is temp conversions
	public Unit(String name, double value, double intercept){
		mDispName = name;
		mValue = value;
		mIntercept = intercept;
	}	
	
	public Unit(String name, double value){
		this(name, value, 0);
	}

	public Unit(){
		this("", 0, 0);
	}

	public double getValue() {
		return mValue;
	}

	public double getIntercept() {
		return mIntercept;
	}
	
	public String toString(){
		return mDispName;
	}

	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Unit))return false;
	    Unit otherUnit = (Unit)other;
		return (otherUnit.getValue() == this.getValue() &&
				otherUnit.toString().equals(this.toString()) &&
				otherUnit.getIntercept() == this.getIntercept());
	}
}
