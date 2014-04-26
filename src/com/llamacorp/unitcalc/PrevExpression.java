package com.llamacorp.unitcalc;

public class PrevExpression {
	private String mQuerry;
	private String mAnswer;
	private Unit mQuerryUnit;
	private Unit mAnswerUnit;
	boolean mContainsUnits;


	public PrevExpression(String querry){
		mQuerry=querry;
		mAnswer="";
		mQuerryUnit = new Unit();
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
		return mQuerryUnit;
	}

	public void setQuerryUnit(Unit querryUnit) {
		mQuerryUnit = querryUnit;
		mContainsUnits=true;
	}

	public Unit getAnswerUnit() {
		return mAnswerUnit;
	}

	public void setAnswerUnit(Unit answerUnit) {
		mAnswerUnit = answerUnit;
		mContainsUnits=true;
	}

	public boolean containsUnits() {
		return mContainsUnits;
	}


	public String getTextQuerry() {
		if(mContainsUnits)
			return mQuerry + " " + mQuerryUnit;
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
