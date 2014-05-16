package com.llamacorp.unitcalc;

public class PrevExpression {
	private String mQuerry;
	private String mAnswer;
	private Unit mQueryUnit;
	private Unit mAnswerUnit;
	private int mUnitTypePos;
	boolean mContainsUnits;


	public PrevExpression(String querry){
		mQuerry=querry;
		mAnswer="";
		mQueryUnit = new Unit();
		mAnswerUnit = new Unit();
		mContainsUnits=false;
	}

	public PrevExpression(){
		this("");
	}

	public String getQuerry() {
		return mQuerry;
	}
	
	public void setQuerry(String querry) {
		mQuerry = querry;
	}

	public String getAnswer() {
		return mAnswer;
	}

	public void setAnswer(String answer) {
		mAnswer = answer;
	}

	public Unit getQuerryUnit() {
		return mQueryUnit;
	}

	/** Set the query and answer units, and the overarching UnitType array position
	 * @param queryUnit is the Unit to set for this query
	 * @param answerUnit is the Unit to set for this answer
	 * @param unitTypePos is the position in the UnitType array */
	public void setResultUnit(Unit queryUnit, Unit answerUnit, int unitTypePos) {
		mQueryUnit = queryUnit;
		mAnswerUnit = answerUnit;
		mUnitTypePos = unitTypePos;
		mContainsUnits=true;
	}

	public int getUnitTypePos() {
		return mUnitTypePos;
	}

	public Unit getAnswerUnit() {
		return mAnswerUnit;
	}

	public boolean containsUnits() {
		return mContainsUnits;
	}


	public String getTextQuerry() {
		if(mContainsUnits)
			return mQuerry + " " + mQueryUnit;
		else
			return mQuerry;	
	}

	public String getTextAnswer() {
		if(mContainsUnits)
			return mAnswer + " " + mAnswerUnit;
		else
			return mAnswer;
	}
}
