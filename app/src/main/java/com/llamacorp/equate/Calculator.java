package com.llamacorp.equate;

import android.content.Context;

import com.llamacorp.equate.unit.Unit;
import com.llamacorp.equate.unit.UnitInitializer;
import com.llamacorp.equate.unit.UnitType;
import com.llamacorp.equate.view.ViewUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;


public class Calculator{
	private static final String FILENAME = "saved_data.json";
	private static final String JSON_RESULT_LIST = "result_list";
	private static final String JSON_UNIT_TYPE_ARRAY = "unit_type_array";
	private static final String JSON_EXPRESSION = "expression";
	private static final String JSON_UNIT_TYPE = "unit_type";
	private static final String JSON_HINTS = "hints";
	private static final int RESULT_LIST_MAX_SIZE = 100;
	private static final int UNIT_TYPE_DEFAULT_POS = 3;


	private static Calculator mCaculator;
	private Context mAppContext;

	//main expression
	private Expression mExpression;

	//object that handles all the math
	private Solver mSolver;

	//string of results; this will be directly manipulated by ResultListFragment
	private List<Result> mResultList;

	//stores the array of various types of units (length, area, volume, etc)
	private ArrayList<UnitType> mUnitTypeArray;
	//stores the current location in mUnitTypeArray
	private int mUnitTypePos;

	public Preferences mPreferences;

	//precision for all calculations
	public static final int intDisplayPrecision = 8;
	public static final int intCalcPrecision = intDisplayPrecision+2;


	private boolean mIsTestCalc = false;

	//------THIS IS FOR TESTING ONLY-----------------
	private Calculator(){
		mResultList = new ArrayList<>();
		mExpression=new Expression(intDisplayPrecision);
		//mMcOperate = new MathContext(intCalcPrecision);
		mSolver = new Solver(intCalcPrecision);
		mUnitTypePos=UNIT_TYPE_DEFAULT_POS;
		mUnitTypeArray = new ArrayList<>();
		mIsTestCalc=true;
		initiateUnits();
		mPreferences = new Preferences();
	}
	//------THIS IS FOR TESTING ONLY-----------------
	public static Calculator getTestCalculator(){ mCaculator=new Calculator(); return mCaculator; }


	/**
	 * Method turns calculator class into a singleton class
	 * (one instance allowed)
	 */
	private Calculator(Context appContext){
		//save our context
		mAppContext = appContext;

		mResultList = new ArrayList<>();
		mExpression = new Expression(intDisplayPrecision);
		//set the unit type to length by default
		mUnitTypePos=UNIT_TYPE_DEFAULT_POS;
		mPreferences = new Preferences();

		//load the calculating precision
		mSolver = new Solver(intCalcPrecision);

		mUnitTypeArray = new ArrayList<>();
		//call helper method to actually load in units
		initiateUnits();

		//over-right values above if this works
		try {
			loadState();
		}
		//might be from a JSON object not existing (app update)
		catch (JSONException JE){
			//delete the problem JSON file
			boolean del = mAppContext.deleteFile(FILENAME);
			String message = "Calculator reset due to JSONException. JSON file "
					+ (del ? "successfully" : "NOT") + " deleted.";
			toast(message);
			resetCalc(); //reset the calc and we should be good
		}
		catch (Exception e) {
			toast("Exception in Calculator.loadState():" + e.toString());
		}
	}

	/**
	 * Method turns calculator class into a singleton class (one instance allowed)
	 */
	public static Calculator getCalculator(Context c){
		if(mCaculator == null)
			mCaculator = new Calculator(c.getApplicationContext());
		return mCaculator;
	}


	private void toast(String msg){
		ViewUtils.toastLongCentered(msg, mAppContext);
	}


	/**
	 * Helper method used to initiate the array of various types of units
	 */
	private void initiateUnits(){
		mUnitTypeArray.clear();
		mUnitTypeArray = UnitInitializer.getDefaultUnitArray();
	}



	private void loadState() throws IOException, JSONException {
		BufferedReader reader = null;
		try {
			// open and read the file into a StringBuilder
			InputStream in = mAppContext.openFileInput(FILENAME);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				// line breaks are omitted and irrelevant
				jsonString.append(line);
			}

			// parse the JSON using JSONTokener
			JSONObject jObjState = (JSONObject) new JSONTokener(jsonString.toString()).nextValue();
			mUnitTypePos = jObjState.getInt(JSON_UNIT_TYPE);
			mExpression = new Expression(jObjState.getJSONObject(JSON_EXPRESSION), intDisplayPrecision);
			mPreferences = new Preferences(jObjState.getJSONObject(JSON_HINTS));

			JSONArray jResultArray = jObjState.getJSONArray(JSON_RESULT_LIST);
			// build the array of results from JSONObjects
			for (int i = 0; i < jResultArray.length(); i++) {
				mResultList.add(new Result(jResultArray.getJSONObject(i)));
			}

			int newSize = mUnitTypeArray.size();
			JSONArray jUnitTypeArray = jObjState.getJSONArray(JSON_UNIT_TYPE_ARRAY);
         //if we added another UnitType, use default everything
         //TODO idealy this should be smarter
			if(jUnitTypeArray.length() == newSize){
            //Load in user settings to already assembled UnitType array
            for (int i = 0; i < jUnitTypeArray.length(); i++) {
					mUnitTypeArray.get(i).loadJSON(jUnitTypeArray.getJSONObject(i));
				}
			}

		} catch (FileNotFoundException e) {
			// we will ignore this one, since it happens when we start fresh
		} finally {
			if (reader != null)
				reader.close();
      }
   }

   public void saveState() throws JSONException, IOException {
      JSONObject jObjState = new JSONObject();
      jObjState.put(JSON_EXPRESSION, mExpression.toJSON());
      jObjState.put(JSON_UNIT_TYPE, mUnitTypePos);
      jObjState.put(JSON_HINTS, mPreferences.toJSON());

      JSONArray jResultArray = new JSONArray();
      for (Result result : mResultList)
         jResultArray.put(result.toJSON());
      jObjState.put(JSON_RESULT_LIST, jResultArray);


      JSONArray jUnitTypeArray = new JSONArray();
      for (UnitType unitType : mUnitTypeArray)
         jUnitTypeArray.put(unitType.toJSON());
      jObjState.put(JSON_UNIT_TYPE_ARRAY, jUnitTypeArray);

      // write the file to disk
		Writer writer = null;
		try {
			OutputStream out = mAppContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(out);
			writer.write(jObjState.toString());
		} finally {
			if (writer != null)
				writer.close();
		}
	}


	/** Clears the result list, expression, and unit selection */
	public void resetCalc(){
		mResultList.clear();
		mExpression = new Expression(intDisplayPrecision);
		mPreferences = new Preferences();
		//set the unit type to length by default
		mUnitTypePos = UNIT_TYPE_DEFAULT_POS;

		//load the calculating precision
		mSolver = new Solver(intCalcPrecision);

		initiateUnits();
	}



	/**
	 * Passed a key from calculator (num/op/back/clear/eq) and distributes it to its proper function
	 * @param sKey is either single character (but still a String) or a string from result list
	 */
	public boolean parseKeyPressed(String sKey){
		//first clear any highlighted chars (and the animation)
		clearHighlighted();

		//if expression was displaying "Syntax Error" or similar (containing invalid chars) clear it
		if(isExpressionInvalid())
			mExpression.clearExpression();

		//if a convert was just done, main display will show "16 in", but no button will be colored
		//want this treated the same as 16 (as if no unit is actually selected)
		if(isSolved() && isUnitSelected())
			clearSelectedUnit();


		switch (sKey) {
			//check for equals key or for long press ='s key aka engineering form
			case "=":
			case "g":
				//if "Convert 3 in to..." is showing, help user out
				if (isUnitSelected() & !mExpression.containsOps()){
					//don't follow through with solve
					return false;
				}

				Result res = mSolver.tryToggleSciNote(mExpression, sKey.equals("g"));
				if (res != null){
					setSolved(true);
					loadResultToArray(res);
				} else
					//solve expression, load into result list if answer not empty
					solveAndLoadIntoResultList(sKey.equals("g"));
				return true;

			//check for plain text (want
			//check for backspace key
			case "b":
				backspace();
				break;

			//check for clear key
			case "c":
				clear();
				break;
			//else try all other potential numbers and operators, as well as result list
			default:
				//if just hit equals, and we hit [.0-9(], then clear current unit type
				if (mExpression.isSolved() && sKey.matches("[.0-9(]"))
					clearSelectedUnit();

				//if we hit an operator other than minus, load in the prev answer
				if (mExpression.isEmpty() && sKey.matches("[" + Expression.regexNonNegOperators + "]"))
					if (!mResultList.isEmpty())
						sKey = mResultList.get(mResultList.size() - 1).getAnswerWithoutSep() + sKey;

				boolean requestSolve = mExpression.keyPresses(sKey);
				if (requestSolve){
					solveAndLoadIntoResultList(false);
					return true;
				}
				break;
		}
		return false;
	}

	/**
	 * Function used to convert from one unit to another
	 * @param fromUnit is unit being converted from
	 * @param toUnit is unit being converted to
	 */
	public void convertFromTo(Unit fromUnit, Unit toUnit){
		//if expression was displaying "Syntax Error" or similar (containing invalid chars) clear it
		if(isExpressionInvalid()){
			mExpression.clearExpression();
			return;
		}
		//want to add a 1 if we just hit one unit and another
		if(isExpressionEmpty())
			parseKeyPressed("1");
		//first solve the function
		boolean solveSuccess = solveAndLoadIntoResultList(false);
		//if there solve failed because there was nothing to solve, just leave (this way result list isn't loaded)
		if (!solveSuccess)
			return;

		//next perform numerical unit conversion
		mSolver.convertFromTo(fromUnit, toUnit, mExpression);

		//TODO this saves current button position to result - NOT GOOD
		//TODO button positions can move at any time, so this doesn't at all
		//TODO guarantee that the correct unit will be recalled when result tapped
      int fromUnitPos = getCurrUnitType().findUnitPosInUnitArray(fromUnit);
      int toUnitPos = getCurrUnitType().findUnitPosInUnitArray(toUnit);

		//load units into result list (this will also set contains unit flag) (overrides that from solve)
		mResultList.get(mResultList.size()-1).setResultUnit(fromUnit, fromUnitPos,
              toUnit, toUnitPos, mUnitTypePos);
		//load the final value into the result list
		mResultList.get(mResultList.size()-1).setAnswerWithSep(mExpression.toString());
	}


	/**
	 * Function that is called after user hits the "=" key
	 * Called by calculator for solving current expression
	 * @return if solved expression
	 */
	private boolean solveAndLoadIntoResultList(boolean useEngineering){
		//the answer will be loaded into mExpression directly
		Result result = mSolver.solve(mExpression, useEngineering);
      return loadResultToArray(result);
	}

   /**
    * Add a result into the Result list array.  Method checks
    * @param result to add into the array
    */
   private boolean loadResultToArray(Result result){
      if(result == null)
         return false;

      //skip result list handling if no result was created
      mResultList.add(result);
      //if we hit size limit, remove oldest element
      if(mResultList.size() > RESULT_LIST_MAX_SIZE)
         mResultList.remove(0);
      //if result had an error, leave before setting units
      if(Expression.isInvalid(result.getAnswerWithoutSep()))
         return false;
      //also set result's unit if it's selected
      if(isUnitSelected()){
         //load units into result list (this will also set contains unit flag
         Unit toUnit = getCurrUnitType().getCurrUnit();
         int toUnitPos = getCurrUnitType().getCurrUnitButtonPos();
         mResultList.get(mResultList.size()-1).setResultUnit(toUnit, toUnitPos,
                 toUnit, toUnitPos, mUnitTypePos);
      }
      return true;
   }


	/** Clear function for the calculator */
	private void clear(){
		//clear the immediate expression
		mExpression.clearExpression();

		//reset current unit
		clearSelectedUnit();
	}


	/**
	 * Backspace function for the calculator
	 */
	private void backspace(){
		//clear out unit selection and expression if we just solved or if expression empty
		if(mExpression.isSolved() || mExpression.isEmpty()){
			clearSelectedUnit();
			mExpression.clearExpression();
			//we're done. don't want to execute code below
			return;
		}

		//since the expression isn't empty, delete last of calcExp list
		mExpression.backspaceAtSelection();
	}

	private void clearSelectedUnit(){
		mUnitTypeArray.get(mUnitTypePos).clearUnitSelection();
	}


	/**
	 * Update values of units that are not static (currency) via
	 * each unit's own HTTP/JSON API call. Note that this refresh
	 * is asynchronous and will only happen sometime in the future
	 * Internet connection permitting.
	 * @param forced should update be forced without waiting for time-out
	 */
	public void refreshAllDynamicUnits(boolean forced){
		//JUnit tests can't find AsynTask class, so skip it for test calc
		if(!mIsTestCalc)
			for(UnitType ut : mUnitTypeArray)
				ut.refreshDynamicUnits(mAppContext, forced);
	}

	/**
	 * @return if there are characters marked for highlighting
	 */
	public boolean isHighlighted(){
		return mExpression.isHighlighted();
	}

	public ArrayList<Integer> getHighlighted(){
		return mExpression.getHighlighted();
	}


	public void clearHighlighted() {
		mExpression.clearHighlightedList();
	}

	public List<Result> getResultList() {
		return mResultList;
	}

	/** Returns if a unit key in current UnitType is selected */
	public boolean isUnitSelected(){
		return mUnitTypeArray.get(mUnitTypePos).isUnitSelected();
	}

	public UnitType getUnitType(int pos) {
		return mUnitTypeArray.get(pos);
	}

	public UnitType getCurrUnitType() {
		return mUnitTypeArray.get(mUnitTypePos);
	}

	public String getUnitTypeName(int pos){
		return mUnitTypeArray.get(pos).getUnitTypeName();
	}

	public int getUnitTypeSize() {
		return mUnitTypeArray.size();
	}

	public void setUnitTypePos(int pos){
		mUnitTypePos = pos;
	}

	public int getUnitTypePos() {
		return mUnitTypePos;
	}

	public boolean isExpressionEmpty(){
		return mExpression.isEmpty();
	}

	public boolean isExpressionInvalid(){
		return mExpression.isInvalid();
	}

	public void pasteIntoExpression(String str){
		mExpression.pasteIntoExpression(str);
	}

	public void setSolved(boolean solved){
		mExpression.setSolved(solved);
	}

	/** Returns if current Expresion is solved (equals/conversion was last operation) */
	public boolean isSolved(){
		return mExpression.isSolved();
	}

	public int getSelectionEnd(){
		return mExpression.getSelectionEnd();
	}

	public int getSelectionStart(){
		return mExpression.getSelectionStart();
	}

	public Expression.NumFormat getNumberFormat() {
		return mExpression.getmNumFormat();
	}

	/**
	 * Set the EditText selection for expression
	 */
	public void setSelection(int selStart, int selEnd) {
		mExpression.setSelection(selStart, selEnd);
	}

	@Override
	public String toString(){
		//needed for display updating
		return mExpression.toString();
	}
}
