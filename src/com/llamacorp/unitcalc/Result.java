package com.llamacorp.unitcalc;

import org.json.JSONException;
import org.json.JSONObject;

public class Result {
	private static final String JSON_QUERY = "query";
	private static final String JSON_ANSWER = "answer";
	private static final String JSON_QUERY_UNIT = "query_unit";
	private static final String JSON_ANSWER_UNIT = "answer_unit";
	private static final String JSON_UNIT_TYPE_POS = "unit_type_pos";
	private static final String JSON_CONTAINS_UNITS = "conatains_units";
	private static final String JSON_TIMESTAMP = "timestamp";


	private String mQuery;
	private String mAnswer;
	private Unit mQueryUnit;
	private Unit mAnswerUnit;
	private int mUnitTypePos;
	boolean mContainsUnits;
	private String mTimestamp;

	public Result(String query, String answer){
		mQuery=query;
		mAnswer=answer;
		mQueryUnit = new UnitScalar();
		mAnswerUnit = new UnitScalar();
		mContainsUnits=false;
		mTimestamp="";
	}
	
	public Result(String query){
		this(query,"");
	}

	public Result(){
		this("","");
	}

	public Result(JSONObject json) throws JSONException {
		mQuery = json.getString(JSON_QUERY);
		mAnswer = json.getString(JSON_ANSWER);
		mQueryUnit = Unit.getUnit(json.getJSONObject(JSON_QUERY_UNIT)); 
		mAnswerUnit = Unit.getUnit(json.getJSONObject(JSON_ANSWER_UNIT)); 
		mUnitTypePos = json.getInt(JSON_UNIT_TYPE_POS);
		mContainsUnits = json.getBoolean(JSON_CONTAINS_UNITS);
		mTimestamp = json.getString(JSON_TIMESTAMP);
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		
		json.put(JSON_QUERY, getQuerry());
		json.put(JSON_ANSWER, getAnswer());
		json.put(JSON_QUERY_UNIT, mQueryUnit.toJSON());
		json.put(JSON_ANSWER_UNIT, mAnswerUnit.toJSON());
		json.put(JSON_UNIT_TYPE_POS, getUnitTypePos());
		json.put(JSON_CONTAINS_UNITS, containsUnits());
		json.put(JSON_TIMESTAMP, getTimestamp());
		
		return json;
	}

	public String getQuerry() {
		return mQuery;
	}

	public void setQuerry(String querry) {
		mQuery = querry;
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
		if(queryUnit.isDynamic())
			mTimestamp="[1:11 pm]";
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
	
	public String getTimestamp(){
		return mTimestamp;
	}

	public String getTextQuerry() {
		if(mContainsUnits)
			return mQuery + " " + mQueryUnit;
		else
			return mQuery;	
	}

	public String getTextAnswer() {
		if(mContainsUnits)
			return mAnswer + " " + mAnswerUnit;
		else
			return mAnswer;
	}
}
