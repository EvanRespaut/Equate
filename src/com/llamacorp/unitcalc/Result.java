package com.llamacorp.unitcalc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
	private long mTimestamp;

	public Result(String query, String answer){
		mQuery=query;
		mAnswer=answer;
		mQueryUnit = new UnitScalar();
		mAnswerUnit = new UnitScalar();
		mContainsUnits=false;
		mTimestamp=0;
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
		mTimestamp = json.getLong(JSON_TIMESTAMP);
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();

		json.put(JSON_QUERY, getQuerry());
		json.put(JSON_ANSWER, getAnswer());
		json.put(JSON_QUERY_UNIT, mQueryUnit.toJSON());
		json.put(JSON_ANSWER_UNIT, mAnswerUnit.toJSON());
		json.put(JSON_UNIT_TYPE_POS, getUnitTypePos());
		json.put(JSON_CONTAINS_UNITS, containsUnits());
		json.put(JSON_TIMESTAMP, mTimestamp); 

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
		if(mAnswerUnit instanceof UnitCurrency){
			//the default unit (USD) doesn't get updated
			if(mAnswerUnit.toString().equals(UnitCurrency.DEFAULT_CURRENCY))
				mTimestamp = ((UnitCurrency)mQueryUnit).getTimeOfUpdate();
			else
				mTimestamp = ((UnitCurrency)mAnswerUnit).getTimeOfUpdate();
		}
	}

	private String formatDate(Long ld){
		String dateText = "";
		if(ld==0) return dateText;
		//format both today and date to format as 2014-330
		String nowDay = new SimpleDateFormat("y-D",Locale.US).format(new Date());
		String ldDay = new SimpleDateFormat("y-D",Locale.US).format(new Date(ld));
		//if the exact year and day of year are not same, just display month day
		if(nowDay.equals(ldDay))
			dateText = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(ld));
		else 
			dateText = new SimpleDateFormat("MMM d",Locale.US).format(new Date(ld));
		return dateText;
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
		return formatDate(mTimestamp);
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
