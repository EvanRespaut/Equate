package com.llamacorp.equate;

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
	private static final String JSON_QUERY_UNIT_TEXT = "query_unit_text";
	private static final String JSON_ANSWER_UNIT_TEXT = "answer_unit_text";
	private static final String JSON_QUERY_UNIT_TEXT_LONG = "query_unit_text_long";
	private static final String JSON_ANSWER_UNIT_TEXT_LONG = "answer_unit_text_long";
	private static final String JSON_UNIT_TYPE_POS = "unit_type_pos";
	private static final String JSON_CONTAINS_UNITS = "conatains_units";
	private static final String JSON_TIMESTAMP = "timestamp";


	private String mQuery;
	private String mAnswer;
	private String mQuerryUnitText;
	private String mAnswerUnitText;
	private String mQuerryUnitTextLong;
	private String mAnswerUnitTextLong;
	private Unit mQueryUnit;
	private Unit mAnswerUnit;
	private int mUnitTypePos;
	boolean mContainsUnits;
	private long mTimestamp;

	public Result(String query, String answer){
		setQuerryWithSep(query);
		setAnswerWithSep(answer);
		mQueryUnit = new UnitScalar();
		mAnswerUnit = new UnitScalar();
		mContainsUnits = false;
		mTimestamp = 0;
		mQuerryUnitText = "";
		mAnswerUnitText = "";
		mQuerryUnitTextLong = "";
		mAnswerUnitTextLong = "";
	}

	public Result(String query){
		this(query,"");
	}

	public Result(){
		this("","");
	}

	public Result(JSONObject json) throws JSONException {
		setQuerry(json.getString(JSON_QUERY));
		setAnswer(json.getString(JSON_ANSWER));
		mQueryUnit = Unit.getUnit(json.getJSONObject(JSON_QUERY_UNIT)); 
		mAnswerUnit = Unit.getUnit(json.getJSONObject(JSON_ANSWER_UNIT));
		mAnswerUnitText = json.getString(JSON_ANSWER_UNIT_TEXT);
		mAnswerUnitTextLong = json.getString(JSON_ANSWER_UNIT_TEXT_LONG);
		mQuerryUnitText = json.getString(JSON_QUERY_UNIT_TEXT);
		mQuerryUnitTextLong = json.getString(JSON_QUERY_UNIT_TEXT_LONG);
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
		json.put(JSON_QUERY_UNIT_TEXT, mQuerryUnitText);
		json.put(JSON_ANSWER_UNIT_TEXT, mAnswerUnitText);
		json.put(JSON_QUERY_UNIT_TEXT_LONG, mQuerryUnitTextLong);
		json.put(JSON_ANSWER_UNIT_TEXT_LONG, mAnswerUnitTextLong);
		json.put(JSON_UNIT_TYPE_POS, getUnitTypePos());
		json.put(JSON_CONTAINS_UNITS, containsUnits());
		json.put(JSON_TIMESTAMP, mTimestamp); 

		return json;
	}

	public String getQuerryWithoutSep(){
		return ExpSeperatorHandler.removeSep(getQuerry());
	}
	
	private String getQuerry() {
		return mQuery;
	}

	public void setQuerryWithSep(String querry){
		setQuerry(ExpSeperatorHandler.addSep(querry));
	}
	
	private void setQuerry(String querry) {
		mQuery = querry;
	}

	public String getAnswerWithoutSep(){
		return ExpSeperatorHandler.removeSep(getAnswer());
	}
	
	private String getAnswer() {
		return mAnswer;
	}

	public void setAnswerWithSep(String answer){
		setAnswer(ExpSeperatorHandler.addSep(answer));
	}
	
	private void setAnswer(String answer) {
		mAnswer = answer;
	}

	public Unit getQuerryUnit() {
		return mQueryUnit;
	}

	/** Set the query and answer units, and the overarching UnitType array 
	 * position
	 * @param queryUnit is the Unit to set for this query
	 * @param answerUnit is the Unit to set for this answer
	 * @param unitTypePos is the position in the UnitType array */
	public void setResultUnit(Unit queryUnit, Unit answerUnit, 
			int unitTypePos) {

		mAnswerUnit = answerUnit;
		mAnswerUnitText = answerUnit.toString();
		mAnswerUnitTextLong = answerUnit.getLowercaseLongName();

		mQueryUnit = queryUnit;
		//if we're dealing with the same historical currency, then the
		//years are most likely different
		if(queryUnit == answerUnit && queryUnit.isHistorical()){
			UnitHistCurrency uc = (UnitHistCurrency)queryUnit;
			mQuerryUnitText = uc.getPreviousShortName();
			mQuerryUnitTextLong = uc.getPreviousLowercaseLongName();			
		}
		else{
			mQuerryUnitText = queryUnit.toString();
			mQuerryUnitTextLong = queryUnit.getLowercaseLongName();
		}

		mUnitTypePos = unitTypePos;
		mContainsUnits = true;
		if(mAnswerUnit.isDynamic() && mQueryUnit.isDynamic()){
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
		String nowDay = new SimpleDateFormat("y-D",Locale.US).
				format(new Date());
		String ldDay = new SimpleDateFormat("y-D",Locale.US).
				format(new Date(ld));
		//if the exact year and day of year are not same, just display month day
		if(nowDay.equals(ldDay))
			dateText = DateFormat.getTimeInstance(DateFormat.SHORT).
			format(new Date(ld));
		else 
			dateText = new SimpleDateFormat("MMM d",Locale.US).
			format(new Date(ld));
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
			return mQuery + " " + mQuerryUnitText;
		else
			return mQuery;	
	}

	public String getTextAnswer() {
		if(mContainsUnits)
			return mAnswer + " " + mAnswerUnitText;
		else
			return mAnswer;
	}

	public String getQuerryUnitTextLong() {
		return mQuerryUnitTextLong;
	}

	public String getAnswerUnitTextLong() {
		return mAnswerUnitTextLong;
	}
}
