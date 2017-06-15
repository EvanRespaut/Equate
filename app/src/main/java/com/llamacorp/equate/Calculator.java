package com.llamacorp.equate;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spanned;

import com.llamacorp.equate.unit.Unit;
import com.llamacorp.equate.unit.UnitType;
import com.llamacorp.equate.unit.UnitTypeList;
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
import java.util.Set;


public class Calculator {
	private static final String FILENAME = "saved_data.json";
	private static final String JSON_RESULT_LIST = "result_list";
	private static final String JSON_UNIT_TYPE_LIST = "unit_type_array";
	private static final String JSON_EXPRESSION = "expression";
	private static final String JSON_HINTS = "hints";
	private static final int RESULT_LIST_MAX_SIZE = 100;

	//TODO fix warning below by removing reference to mAppContext in calc class
	private static Calculator mCalculator;
	private Context mAppContext;

	//main expression
	private Expression mExpression;

	//object that handles all the math
	private Solver mSolver;

	//string of results; this will be directly manipulated by ResultListFragment
	private List<Result> mResultList;

	// stores the array of various types of units (length, area, volume, etc)
	// as well as current unit type position
	private UnitTypeList mUnitTypeList;

	public Preferences mPreferences;

	//precision for all calculations
	public static final int DISPLAY_PRECISION = 15;
	public static final int intCalcPrecision = DISPLAY_PRECISION + 2;


	private boolean mIsTestCalc = false;
	private Preview mPreview;

	//------THIS IS FOR TESTING ONLY-----------------
	private Calculator(Resources mockResources) {
		mResultList = new ArrayList<>();
		mExpression = new Expression(DISPLAY_PRECISION);
		//mMcOperate = new MathContext(intCalcPrecision);
		mSolver = new Solver(intCalcPrecision);

		// try passing a dummy context, make sure we don't actually use Unit Type
		// in test
		mUnitTypeList = new UnitTypeList(mockResources);
		mIsTestCalc = true;
		mPreferences = new Preferences();

		//load the calculating precision
		mSolver = new Solver(intCalcPrecision);
		mPreview = new Preview(mSolver);
	}

	//------THIS IS FOR TESTING ONLY-----------------
	static Calculator getTestCalculator(Resources mockResources) {
		mCalculator = new Calculator(mockResources);
		return mCalculator;
	}


	/**
	 * Method turns calculator class into a singleton class
	 * (one instance allowed)
	 */
	private Calculator(Context appContext) {
		//save our context
		mAppContext = appContext.getApplicationContext();

		mResultList = new ArrayList<>();
		mExpression = new Expression(DISPLAY_PRECISION);
		//set the unit type to length by default
		mPreferences = new Preferences();

		//load the calculating precision
		mSolver = new Solver(intCalcPrecision);


		mPreview = new Preview(mSolver);

		mUnitTypeList = new UnitTypeList(appContext.getResources());


		//over-right values above if this works
		try {
			loadState();
		}
		//might be from a JSON object not existing (app update)
		catch (JSONException JE) {
			//delete the problem JSON file
			boolean del = mAppContext.deleteFile(FILENAME);
			String message = "Calculator reset due to JSONException. JSON file "
					  + (del ? "successfully" : "NOT") + " deleted.";
			toast(message);
			resetCalc(); //reset the calc and we should be good
		} catch (Exception e) {
			toast("Exception in Calculator.loadState():" + e.toString());
		}
	}

	/**
	 * Method turns calculator class into a singleton class (one instance allowed)
	 */
	public static Calculator getCalculator(Context c) {
		if (mCalculator == null)
			mCalculator = new Calculator(c.getApplicationContext());
		return mCalculator;
	}


	private void toast(String msg) {
		ViewUtils.toastLongCentered(msg, mAppContext);
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
			mExpression = new Expression(jObjState.getJSONObject(JSON_EXPRESSION), DISPLAY_PRECISION);
			mPreferences = new Preferences(jObjState.getJSONObject(JSON_HINTS));

			JSONArray jResultArray = jObjState.getJSONArray(JSON_RESULT_LIST);
			// build the array of results from JSONObjects
			for (int i = 0; i < jResultArray.length(); i++) {
				mResultList.add(new Result(jResultArray.getJSONObject(i)));
			}

			mUnitTypeList = new UnitTypeList(mAppContext.getResources(),
					  jObjState.getJSONObject(JSON_UNIT_TYPE_LIST));

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
		jObjState.put(JSON_HINTS, mPreferences.toJSON());

		JSONArray jResultArray = new JSONArray();
		for (Result result : mResultList)
			jResultArray.put(result.toJSON());
		jObjState.put(JSON_RESULT_LIST, jResultArray);

		jObjState.put(JSON_UNIT_TYPE_LIST, mUnitTypeList.toJSON());

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


	/**
	 * Clears the result list, expression, and unit selection. Remember that this
	 * is only backend data changes, the screen will not have been updated to
	 * reflect any changes.
	 */
	public void resetCalc() {
		mResultList.clear();
		mExpression = new Expression(DISPLAY_PRECISION);
		mPreferences = new Preferences();

		//load the calculating precision
		mSolver = new Solver(intCalcPrecision);

		mUnitTypeList.initialize();
	}

	/**
	 * Used to store some booleans used by CalculatorActivity after the
	 * Calculator class handled the key-press
	 */
	public class CalculatorResultFlags {
		//has a solve been performed (used to determine if result list update is necessary)
		public boolean performedSolve = false;
		//has a unit been selected and then equals been pressed (give user feedback
		//that the user needs to select another unit
		public boolean createDiffUnitDialog = false;
		//used to determine if the instant result should be displayed
		public boolean displayInstantResult = false;
	}


	/**
	 * Passed a key from calculator (num/op/back/clear/eq) and distributes it to its proper function
	 *
	 * @param sKey is either single character (but still a String) or a string from result list
	 */
	public CalculatorResultFlags parseKeyPressed(String sKey) {
		//create a return object
		CalculatorResultFlags resultFlags = new CalculatorResultFlags();

		//first clear any highlighted chars (and the animation)
		clearHighlighted();

		//if expression was displaying "Syntax Error" or similar (containing invalid chars) clear it
		if (isExpressionInvalid())
			mExpression.clearExpression();

		//if a convert was just done, main display will show "16 in", but no button will be colored
		//want this treated the same as 16 (as if no unit is actually selected)
		if (isSolved() && isUnitSelected())
			clearSelectedUnit();


		switch (sKey) {
			//check for equals
			case "=":
				// Help dialog: if "Convert 3 in to..." is showing, help user out
				if (isUnitSelected() & !mExpression.containsOps()){
					//don't follow through with solve
					resultFlags.createDiffUnitDialog = true;
					return resultFlags;
				}
				// Display sci/engineering notation if expression is just a number
				boolean togglePerformed = mSolver.tryToggleSciNote(mExpression, false);

				if (togglePerformed){
					// in case this toggle was performed after operation, remove
					// solve flag to show the preview
					setSolved(false);
					mPreview.set(new Expression(mExpression), Expression.NumFormat.ENGINEERING);
				}
				else {
					//solve expression, load into result list if answer not empty
					solveAndLoadIntoResultList();
					resultFlags.performedSolve = isSolved();
				}

				return resultFlags;
//			//check for long hold equals key
//			case "g":
//				//toggle between SI notation and not:
//				//if (mPreview.isNumFormatEngineering())
//				//	mPreview.set(new Expression(mExpression), Expression.NumFormat.NORMAL);
//				//else
//				mPreview.set(new Expression(mExpression), Expression.NumFormat.ENGINEERING);
//				if (mExpression.isOnlyValidNumber()){
//					if (mExpression.isSciNotation())
//						//mPreview.set(mExpression, Expression.NumFormat.PLAIN);
//						mExpression.roundAndCleanExpression(Expression.NumFormat.PLAIN);
//					else
//						//mPreview.set(mExpression, Expression.NumFormat.SCI_NOTE);
//						mExpression.roundAndCleanExpression(Expression.NumFormat.SCI_NOTE);
//					setSolved(false);
//				}
//				return resultFlags;
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

				//deal with all other cases in expression
				boolean requestSolve = mExpression.keyPresses(sKey);

				//used when inverter key used after expression is solved
				if (requestSolve){
					solveAndLoadIntoResultList();
					resultFlags.performedSolve = isSolved();
					return resultFlags;
				}
				break;
		}
		//want to make a copy of expression so original doesn't change
		mPreview.set(new Expression(mExpression), Expression.NumFormat.NORMAL);

		return resultFlags;
	}

	/**
	 * Function used to convert from one unit to another
	 *
	 * @param fromUnit is unit being converted from
	 * @param toUnit   is unit being converted to
	 */
	public void convertFromTo(Unit fromUnit, Unit toUnit) {
		//if expression was displaying "Syntax Error" or similar (containing
		// invalid chars) clear it
		if (isExpressionInvalid()){
			mExpression.clearExpression();
			return;
		}
		//want to add a 1 if we just hit one unit and another
		if (isExpressionEmpty())
			parseKeyPressed("1");
		//first solve the function
		boolean solveSuccess = solveAndLoadIntoResultList();
		//if there solve failed because there was nothing to solve, just leave
		// (this way result list isn't loaded)
		if (!solveSuccess)
			return;

		//next perform numerical unit conversion
		mSolver.convertFromTo(fromUnit, toUnit, mExpression);

		int fromUnitPos = getCurrUnitType().findUnitPosInUnitArray(fromUnit);
		int toUnitPos = getCurrUnitType().findUnitPosInUnitArray(toUnit);

		//load units into result list (this will also set contains unit flag)
		// (overrides that from solve)
		mResultList.get(mResultList.size() - 1).setResultUnit(fromUnit, fromUnitPos,
				  toUnit, toUnitPos, mUnitTypeList.getCurrentKey());
		//load the final value into the result list
		mResultList.get(mResultList.size() - 1).setAnswerWithSep(mExpression.toString());
	}


	/**
	 * Function that is called after user hits the "=" key
	 * Called by calculator for solving current expression
	 *
	 * @return if solved expression
	 */
	private boolean solveAndLoadIntoResultList() {
		//the answer will be loaded into mExpression directly
		Result result = mSolver.solve(mExpression, Expression.NumFormat.NORMAL);
		return loadResultToArray(result);
	}

	/**
	 * Add a result into the Result list array.  Method checks
	 *
	 * @param result to add into the array
	 */
	private boolean loadResultToArray(Result result) {
		if (result == null)
			return false;

		//skip result list handling if no result was created
		mResultList.add(result);
		//if we hit size limit, remove oldest element
		if (mResultList.size() > RESULT_LIST_MAX_SIZE)
			mResultList.remove(0);
		//if result had an error, leave before setting units
		if (Expression.isInvalid(result.getAnswerWithoutSep()))
			return false;
		//also set result's unit if it's selected
		if (isUnitSelected()){
			//load units into result list (this will also set contains unit flag
			Unit toUnit = getCurrUnitType().getCurrUnit();
			int toUnitPos = getCurrUnitType().getCurrUnitButtonPos();
			mResultList.get(mResultList.size() - 1).setResultUnit(toUnit, toUnitPos,
					  toUnit, toUnitPos, mUnitTypeList.getCurrentKey());
		}
		return true;
	}


	/**
	 * Clear function for the calculator
	 */
	private void clear() {
		//clear the immediate expression
		mExpression.clearExpression();

		//reset current unit
		clearSelectedUnit();
	}


	/**
	 * Backspace function for the calculator
	 */
	private void backspace() {
		//clear out unit selection and expression if we just solved or if expression empty
		if (mExpression.isSolved() || mExpression.isEmpty()){
			clearSelectedUnit();
			mExpression.clearExpression();
			//we're done. don't want to execute code below
			return;
		}

		//since the expression isn't empty, delete last of calcExp list
		mExpression.backspaceAtSelection();
	}

	private void clearSelectedUnit() {
		mUnitTypeList.getCurrent().clearUnitSelection();
	}


	/**
	 * Update values of units that are not static (currency) via
	 * each unit's own HTTP/JSON API call. Note that this refresh
	 * is asynchronous and will only happen sometime in the future
	 * Internet connection permitting.
	 *
	 * @param forced should update be forced without waiting for time-out
	 */
	public void refreshAllDynamicUnits(boolean forced) {
		//JUnit tests can't find AsynTask class, so skip it for test calc
		if (!mIsTestCalc)
			mUnitTypeList.refreshDynamicUnits(mAppContext, forced);
	}

	/**
	 * @return if there are characters marked for highlighting
	 */
	public boolean isHighlighted() {
		return mExpression.isHighlighted();
	}

	public ArrayList<Integer> getHighlighted() {
		return mExpression.getHighlighted();
	}

	/**
	 * Clear highlighted character (those that are turned red, for example the
	 * open bracket when close bracket is held down.
	 */
	public void clearHighlighted() {
		mExpression.clearHighlightedList();
	}

	public List<Result> getResultList() {
		return mResultList;
	}

	public UnitTypeList getUnitTypeList() {
		return mUnitTypeList;
	}

	/**
	 * Returns if a unit key in current UnitType is selected
	 */
	public boolean isUnitSelected() {
		return mUnitTypeList.getCurrent().isUnitSelected();
	}

	public UnitType getUnitType(int pos) {
		return mUnitTypeList.get(pos);
	}

	public UnitType getCurrUnitType() {
		return mUnitTypeList.getCurrent();
	}

	public String getUnitTypeName(int index) {
		return mUnitTypeList.get(index).getUnitTypeName();
	}

	public int getUnitTypeSize() {
		return mUnitTypeList.numberVisible();
	}

	public void setCurrentUnitTypePos(int index) {
		mUnitTypeList.setCurrent(index);
	}

	public int getUnitTypePos() {
		return mUnitTypeList.getCurrentIndex();
	}

	/**
	 * Gets the visible Unit Type index supplied key, if able.  If the key does
	 * not exist in the visible Unit Types, returns -1
	 */
	public int getUnitTypeIndex(String key) {
		return mUnitTypeList.getIndex(key);
	}

	public boolean isExpressionEmpty() {
		return mExpression.isEmpty();
	}

	public boolean isExpressionInvalid() {
		return mExpression.isInvalid();
	}

	public void pasteIntoExpression(String str) {
		mExpression.pasteIntoExpression(str);
	}

	public void setSolved(boolean solved) {
		mExpression.setSolved(solved);
	}

	/**
	 * Returns if current Expression is solved (equals/conversion was last operation)
	 */
	public boolean isSolved() {
		return mExpression.isSolved();
	}

	public int getSelectionEnd() {
		return mExpression.getSelectionEnd();
	}

	public int getSelectionStart() {
		return mExpression.getSelectionStart();
	}

	public Expression.NumFormat getNumberFormat() {
		return mExpression.getNumFormat();
	}

	public boolean isPreviewEmpty() {
		return mPreview.isEmpty();
	}

	public Spanned getPreviewText(int suffixColor) {
		return mPreview.getText(suffixColor);
	}

	public void setSelectedUnitTypes(Set<String> set) {
		mUnitTypeList.setOrdered(set);
	}

	/**
	 * Set the EditText selection for expression
	 */
	public void setSelection(int selStart, int selEnd) {
		mExpression.setSelection(selStart, selEnd);
	}

	@Override
	public String toString() {
		//needed for display updating
		return mExpression.toString();
	}
}
