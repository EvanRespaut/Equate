package com.llamacorp.unitcalc;

public class UnitTypeCurrency extends UnitType{
	
	public UnitTypeCurrency (String name){
		super(name);
	}
	
	public UnitTypeCurrency (String name, String URL){
		this(name);
		mURL = URL;
	}
}
