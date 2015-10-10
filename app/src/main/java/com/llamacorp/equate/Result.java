package com.llamacorp.equate;

import com.llamacorp.equate.unit.Unit;
import com.llamacorp.equate.unit.UnitCurrency;
import com.llamacorp.equate.unit.UnitHistCurrency;

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
	private static final String JSON_CONTAINS_UNITS = "contains_units";
	private static final String JSON_TIMESTAMP = "timestamp";


	private String mQuery;
	private String mAnswer;
	private String mQueryUnitText;
	private String mAnswerUnitText;
	private String mQueryUnitTextLong;
	private String mAnswerUnitTextLong;
	private int mQueryUnitPosInUnitArray;
	private int mAnswerUnitPosInUnitArray;
	private int mUnitTypePos;
	boolean mContainsUnits;
	private long mTimestamp;

	public Result(String query, String answer){
		setQueryWithSep(query);
		setAnswerWithSep(answer);
		mQueryUnitPosInUnitArray = -1;
		mAnswerUnitPosInUnitArray = -1;
		mContainsUnits = false;
		mTimestamp = 0;
		mQueryUnitText = "";
		mAnswerUnitText = "";
		mQueryUnitTextLong = "";
		mAnswerUnitTextLong = "";
	}

	public Result(JSONObject json) throws JSONException {
		setQuery(json.getString(JSON_QUERY));
		setAnswer(json.getString(JSON_ANSWER));
		mQueryUnitPosInUnitArray = json.getInt(JSON_QUERY_UNIT);
		mAnswerUnitPosInUnitArray = json.getInt(JSON_ANSWER_UNIT);
		mAnswerUnitText = json.getString(JSON_ANSWER_UNIT_TEXT);
		mAnswerUnitTextLong = json.getString(JSON_ANSWER_UNIT_TEXT_LONG);
		mQueryUnitText = json.getString(JSON_QUERY_UNIT_TEXT);
		mQueryUnitTextLong = json.getString(JSON_QUERY_UNIT_TEXT_LONG);
		mUnitTypePos = json.getInt(JSON_UNIT_TYPE_POS);
		mContainsUnits = json.getBoolean(JSON_CONTAINS_UNITS);
		mTimestamp = json.getLong(JSON_TIMESTAMP);
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();

		json.put(JSON_QUERY, getQuery());
		json.put(JSON_ANSWER, getAnswer());
		json.put(JSON_QUERY_UNIT, mQueryUnitPosInUnitArray);
		json.put(JSON_ANSWER_UNIT, mAnswerUnitPosInUnitArray);
		json.put(JSON_QUERY_UNIT_TEXT, mQueryUnitText);
		json.put(JSON_ANSWER_UNIT_TEXT, mAnswerUnitText);
		json.put(JSON_QUERY_UNIT_TEXT_LONG, mQueryUnitTextLong);
		json.put(JSON_ANSWER_UNIT_TEXT_LONG, mAnswerUnitTextLong);
		json.put(JSON_UNIT_TYPE_POS, getUnitTypePos());
		json.put(JSON_CONTAINS_UNITS, containsUnits());
		json.put(JSON_TIMESTAMP, mTimestamp); 

		return json;
	}

	public String getQueryWithoutSep(){
		return ExpSeparatorHandler.removeSep(getQuery());
	}
	
	/** Returns query with separators */ 
	private String getQuery() {
		return mQuery;
	}

	public void setQueryWithSep(String query){
		setQuery(ExpSeparatorHandler.addSep(query));
	}
	
	private void setQuery(String query) {
		mQuery = query;
	}

	public String getAnswerWithoutSep(){
		return ExpSeparatorHandler.removeSep(getAnswer());
	}
	
	/** Returns answer with separators */ 
	private String getAnswer() {
		return mAnswer;
	}

	public void setAnswerWithSep(String answer){
		setAnswer(ExpSeparatorHandler.addSep(answer));
	}
	
	private void setAnswer(String answer) {
		mAnswer = answer;
	}


	/** Set the query and answer units, and the overarching UnitType array 
	 * position
	 * @param queryUnit is the Unit to set for this query
	 * @param answerUnit is the Unit to set for this answer
	 * @param unitTypePos is the position in the UnitType array */
	public void setResultUnit(Unit queryUnit, int queryUnitPos, Unit answerUnit,
			int answerUnitPos, int unitTypePos) {


		mAnswerUnitPosInUnitArray = answerUnitPos;
		mAnswerUnitText = answerUnit.toString();
		mAnswerUnitTextLong = answerUnit.getLowercaseLongName();

		mQueryUnitPosInUnitArray = queryUnitPos;
		//if we're dealing with the same historical currency, then the
		//years are most likely different
		if(queryUnit == answerUnit && queryUnit.isHistorical()){
			UnitHistCurrency uc = (UnitHistCurrency)queryUnit;
			mQueryUnitText = uc.getPreviousShortName();
			mQueryUnitTextLong = uc.getPreviousLowercaseLongName();
		}
		else{
			mQueryUnitText = queryUnit.toString();
			mQueryUnitTextLong = queryUnit.getLowercaseLongName();
		}

		mUnitTypePos = unitTypePos;
		mContainsUnits = true;
		if(answerUnit.isDynamic() && queryUnit.isDynamic()){
			//the default unit (USD) doesn't get updated
			if(answerUnit.toString().equals(UnitCurrency.DEFAULT_CURRENCY))
				mTimestamp = ((UnitCurrency)queryUnit).getTimeOfUpdate();
			else
				mTimestamp = ((UnitCurrency)answerUnit).getTimeOfUpdate();
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

	public int getAnswerUnitPos() {
		return mAnswerUnitPosInUnitArray;
	}

   public int getQueryUnitPos() {
      return mQueryUnitPosInUnitArray;
   }

	public boolean containsUnits() {
		return mContainsUnits;
	}

	public String getTimestamp(){
		return formatDate(mTimestamp);
	}

	public String getTextQuery() {
		if(mContainsUnits)
			return mQuery + " " + mQueryUnitText;
		else
			return mQuery;	
	}

	public String getTextAnswer() {
		if(mContainsUnits)
			return mAnswer + " " + mAnswerUnitText;
		else
			return mAnswer;
	}

	public String getQueryUnitTextLong() {
		return mQueryUnitTextLong;
	}

	public String getAnswerUnitTextLong() {
		return mAnswerUnitTextLong;
	}
}
