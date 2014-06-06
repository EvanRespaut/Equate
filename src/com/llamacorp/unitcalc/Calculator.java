package com.llamacorp.unitcalc;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;

import com.llamacorp.unitcalc.UnitType.OnConvertionListener;

public class Calculator implements OnConvertionListener{
	private static final String FILENAME = "saved_data.json";
	private static final String JSON_RESULT_LIST = "result_list";
	private static final String JSON_EXPRESSION = "expression";
	private static final String JSON_UNIT_TYPE = "unit_type";
	private static final int RESULT_LIST_MAX_SIZE = 100;
	

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


	//precision for all calculations
	public static final int intDisplayPrecision = 8;
	public static final int intCalcPrecision = intDisplayPrecision+2;


	//------THIS IS FOR TESTING ONLY-----------------
	private Calculator(){
		mResultList = new ArrayList<Result>();
		mExpression=new Expression(intDisplayPrecision); 		
		//mMcOperate = new MathContext(intCalcPrecision); 
		mSolver = new Solver(intCalcPrecision);
		mUnitTypePos=2;	
		initiateUnits();	
	}
	//------THIS IS FOR TESTING ONLY-----------------
	public static Calculator getTestCalculator(){ mCaculator=new Calculator(); return mCaculator; }


	/**
	 * Method turns calculator class into a singleton class (one instance allowed)
	 */
	private Calculator(Context appContext){
		//save our context
		mAppContext = appContext;
		
		mResultList = new ArrayList<Result>();
		mExpression = new Expression(intDisplayPrecision);
		//set the unit type to length by default
		mUnitTypePos=2;
		
		//load the calculating precision
		mSolver = new Solver(intCalcPrecision);
			
		try {
			loadState();
		} catch (Exception e) {	
		}

		//call helper method to actually load in units
		initiateUnits();
	}

	/**
	 * Method turns calculator class into a singleton class (one instance allowed)
	 */
	public static Calculator getCalculator(Context c){
		if(mCaculator == null)
			mCaculator = new Calculator(c.getApplicationContext());
		return mCaculator;
	}


	private void loadState() throws IOException, JSONException {
		ArrayList<Result> results = new ArrayList<Result>();
		BufferedReader reader = null;
		try {
			// open and read the file into a StringBuilder
			InputStream in = mAppContext.openFileInput(FILENAME);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				// line breaks are omitted and irrelevant
				jsonString.append(line);
			}

			// parse the JSON using JSONTokener
			JSONObject jObjState = (JSONObject) new JSONTokener(jsonString.toString()).nextValue();
			mExpression = new Expression(jObjState.getJSONObject(JSON_EXPRESSION), intDisplayPrecision);
			mUnitTypePos = jObjState.getInt(JSON_UNIT_TYPE);

			JSONArray jResultArray = jObjState.getJSONArray(JSON_RESULT_LIST);

			// build the array of results from JSONObjects
			for (int i = 0; i < jResultArray.length(); i++) {
				results.add(new Result(jResultArray.getJSONObject(i)));
			}
			mResultList = results;
		} catch (FileNotFoundException e) {
			// we will ignore this one, since it happens when we start fresh
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public void saveState() throws JSONException, IOException {
		//Main JSON object 
		JSONObject jObjState = new JSONObject();
		jObjState.put(JSON_EXPRESSION, mExpression.toJSON());
		jObjState.put(JSON_UNIT_TYPE, mUnitTypePos);
		
		JSONArray jResultArray = new JSONArray();
		for (Result result : mResultList)
			jResultArray.put(result.toJSON());

		jObjState.put(JSON_RESULT_LIST, jResultArray);

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
		//set the unit type to length by default
		mUnitTypePos=2;
	}


	/**
	 * Helper method used to initiate the array of various types of units
	 */	
	private void initiateUnits(){
		mUnitTypeArray = new ArrayList<UnitType>();

		UnitType unitsOfTemp = new UnitType(this,"Temp");
		unitsOfTemp.addUnit("",  0, 0);
		unitsOfTemp.addUnit("",  0, 0);
		unitsOfTemp.addUnit("",  0, 0);
		unitsOfTemp.addUnit("",  0, 0);
		unitsOfTemp.addUnit("°F",  1.8, -17.77777777777777778);

		unitsOfTemp.addUnit("",  0, 0);
		unitsOfTemp.addUnit("",  0, 0);
		unitsOfTemp.addUnit("",  0, 0);
		unitsOfTemp.addUnit("",  0, 0);
		unitsOfTemp.addUnit("°C", 1, 32);
		mUnitTypeArray.add(unitsOfTemp);


		UnitType unitsOfWeight = new UnitType(this,"Weight");
		unitsOfWeight.addUnit("oz", 1/0.0283495);
		unitsOfWeight.addUnit("lb", 1/0.453592);
		unitsOfWeight.addUnit("short ton", 1/907.184);
		unitsOfWeight.addUnit("long ton", 1/1016.04608);
		unitsOfWeight.addUnit("stone", 1/6.350288);

		unitsOfWeight.addUnit("µg", 1/1e-9);
		unitsOfWeight.addUnit("mg", 1/1e-6);
		unitsOfWeight.addUnit("g", 1/0.001);
		unitsOfWeight.addUnit("kg", 1);
		unitsOfWeight.addUnit("metric ton", 1/1e3);
		mUnitTypeArray.add(unitsOfWeight);


		UnitType unitsOfLength = new UnitType(this,"Length");
		unitsOfLength.addUnit("in", 1/0.0254);
		unitsOfLength.addUnit("ft", 1/0.3048);
		unitsOfLength.addUnit("yard", 1/0.9144);
		unitsOfLength.addUnit("mile", 1/1609.344);
		unitsOfLength.addUnit("km", 1/1000.0);

		unitsOfLength.addUnit("nm", 1/0.000000001);
		unitsOfLength.addUnit("µm", 1/0.000001);
		unitsOfLength.addUnit("mm", 1/0.001);
		unitsOfLength.addUnit("cm", 1/0.01);
		unitsOfLength.addUnit("m", 1.0);
		mUnitTypeArray.add(unitsOfLength);	


		UnitType unitsOfArea = new UnitType(this,"Area");
		unitsOfArea.addUnit("in^2", 1/0.00064516);//0.0254^2
		unitsOfArea.addUnit("ft^2", 1/0.09290304);//0.3048^2
		unitsOfArea.addUnit("yd^2", 1/0.83612736);//0.3048^2*9
		unitsOfArea.addUnit("acre", 1/4046.8564224);//0.3048^2*9*4840
		unitsOfArea.addUnit("mi^2", 1/2589988.110336);//1609.344^2

		unitsOfArea.addUnit("mm^2", 1/0.000001);
		unitsOfArea.addUnit("cm^2", 1/0.0001);
		unitsOfArea.addUnit("m^2", 1.0);
		unitsOfArea.addUnit("km^2", 1/1000000.0);
		unitsOfArea.addUnit("ha", 1/10000.0);
		mUnitTypeArray.add(unitsOfArea);


		UnitType unitsOfVolume = new UnitType(this,"Volume");
		unitsOfVolume.addUnit("tbsp", 1/0.000014786764765625);//gal/256
		unitsOfVolume.addUnit("cup", 1/0.00023658823625);//gal/16
		unitsOfVolume.addUnit("pint", 1/0.0004731764725);//gal/8
		unitsOfVolume.addUnit("qt", 1/0.000946352945);//gal/4
		unitsOfVolume.addUnit("gal", 1/0.00378541178);//found online; source of other conversions

		unitsOfVolume.addUnit("tsp", 1/4.92892158854166666667e-6);//gal/768
		unitsOfVolume.addUnit("fl oz", 1/0.00002957352953125);//gal/128
		unitsOfVolume.addUnit("ml", 1/1e-6);
		unitsOfVolume.addUnit("l", 1/0.001);
		unitsOfVolume.addUnit("m^3", 1);
		mUnitTypeArray.add(unitsOfVolume);
		/*
		UnitType unitsOfPower = new UnitType(this,"Power");
		unitsOfPower.addUnit("tbsp", 1/0.000014786764765625);
		unitsOfPower.addUnit("cup", 1/0.00023658823625);
		unitsOfPower.addUnit("pint", 1/0.0004731764725);
		unitsOfPower.addUnit("qt", 1/0.000946352945);
		unitsOfPower.addUnit("gal", 1/0.00378541178);

		unitsOfPower.addUnit("tsp", 1/4.92892158854166666667e-6);
		unitsOfPower.addUnit("fl oz", 1/0.00002957352953125);
		unitsOfPower.addUnit("ml", 1/1e-6);
		unitsOfPower.addUnit("l", 1/0.001);
		unitsOfPower.addUnit("m^3", 1);
		mUnitTypeArray.add(unitsOfPower);
		
		UnitType unitsOfSpeed = new UnitType(this,"Speed");
		unitsOfSpeed.addUnit("tbsp", 1/0.000014786764765625);
		unitsOfSpeed.addUnit("cup", 1/0.00023658823625);
		unitsOfSpeed.addUnit("pint", 1/0.0004731764725);
		unitsOfSpeed.addUnit("qt", 1/0.000946352945);
		unitsOfSpeed.addUnit("gal", 1/0.00378541178);

		unitsOfSpeed.addUnit("tsp", 1/4.92892158854166666667e-6);
		unitsOfSpeed.addUnit("fl oz", 1/0.00002957352953125);
		unitsOfSpeed.addUnit("ml", 1/1e-6);
		unitsOfSpeed.addUnit("l", 1/0.001);
		unitsOfSpeed.addUnit("m^3", 1);
		mUnitTypeArray.add(unitsOfSpeed);
		
		UnitType unitsOfasdfsdf = new UnitType(this,"Speed");
		unitsOfasdfsdf.addUnit("tbsp", 1/0.000014786764765625);
		unitsOfasdfsdf.addUnit("cup", 1/0.00023658823625);
		unitsOfasdfsdf.addUnit("pint", 1/0.0004731764725);
		unitsOfasdfsdf.addUnit("qt", 1/0.000946352945);
		unitsOfasdfsdf.addUnit("gal", 1/0.00378541178);

		unitsOfasdfsdf.addUnit("tsp", 1/4.92892158854166666667e-6);
		unitsOfasdfsdf.addUnit("fl oz", 1/0.00002957352953125);
		unitsOfasdfsdf.addUnit("ml", 1/1e-6);
		unitsOfasdfsdf.addUnit("l", 1/0.001);
		unitsOfasdfsdf.addUnit("m^3", 1);
		mUnitTypeArray.add(unitsOfasdfsdf);
		
		UnitType unitsOfasds = new UnitType(this,"Speed");
		unitsOfasds.addUnit("tbsp", 1/0.000014786764765625);
		unitsOfasds.addUnit("cup", 1/0.00023658823625);
		unitsOfasds.addUnit("pint", 1/0.0004731764725);
		unitsOfasds.addUnit("qt", 1/0.000946352945);
		unitsOfasds.addUnit("gal", 1/0.00378541178);

		unitsOfasds.addUnit("tsp", 1/4.92892158854166666667e-6);
		unitsOfasds.addUnit("fl oz", 1/0.00002957352953125);
		unitsOfasds.addUnit("ml", 1/1e-6);
		unitsOfasds.addUnit("l", 1/0.001);
		unitsOfasds.addUnit("m^3", 1);
		mUnitTypeArray.add(unitsOfasds);
		
		UnitType unitsOfdds = new UnitType(this,"Speed");
		unitsOfdds.addUnit("tbsp", 1/0.000014786764765625);
		unitsOfdds.addUnit("cup", 1/0.00023658823625);
		unitsOfdds.addUnit("pint", 1/0.0004731764725);
		unitsOfdds.addUnit("qt", 1/0.000946352945);
		unitsOfdds.addUnit("gal", 1/0.00378541178);

		unitsOfdds.addUnit("tsp", 1/4.92892158854166666667e-6);
		unitsOfdds.addUnit("fl oz", 1/0.00002957352953125);
		unitsOfdds.addUnit("ml", 1/1e-6);
		unitsOfdds.addUnit("l", 1/0.001);
		unitsOfdds.addUnit("m^3", 1);
		mUnitTypeArray.add(unitsOfdds);
		*/
	}



	/**
	 * Passed a key from calculator (num/op/back/clear/eq) and distributes it to its proper function
	 * @param sKey is either single character (but still a String) or a string from result list
	 */
	public void parseKeyPressed(String sKey){
		//if expression was displaying "Syntax Error" or similar (containing invalid chars) clear it
		if(isExpressionInvalid())
			mExpression.clearExpression();

		//check for equals key
		if(sKey.equals("=")){
			//solve expression, load into result list if answer not empty
			solveAndLoadIntoResultList();
		}
		//check for plain text (want 
		//check for backspace key
		else if(sKey.equals("b"))
			backspace();

		//check for clear key
		else if(sKey.equals("c"))
			clear();

		//else try all other potential numbers and operators, as well as result list
		else{
			//if just hit equals, and we hit [.0-9(], then clear current unit type
			if(mExpression.isSolved() && sKey.matches("[.0-9(]"))
				mUnitTypeArray.get(mUnitTypePos).clearUnitSelection();

			mExpression.keyPresses(sKey);
		}
	}
	
	/**
	 * Function used to convert from one unit to another
	 * @param fromValue is standardized value of the unit being converted from
	 * @param toValue is standardized value of the unit being converted to
	 */		
	public void convertFromTo(Unit fromUnit, Unit toUnit){
		//if expression was displaying "Syntax Error" or similar (containing invalid chars) clear it
		if(isExpressionInvalid())
			mExpression.clearExpression();
		//first solve the function
		boolean solveSuccess = solveAndLoadIntoResultList();
		//if there solve failed because there was nothing to solve, just leave (this way result list isn't loaded)
		if (!solveSuccess)
			return;
		
		mSolver.convertFromTo(fromUnit, toUnit, mExpression);

		//load units into result list (this will also set contains unit flag) (overrides that from solve)
		mResultList.get(mResultList.size()-1).setResultUnit(fromUnit, toUnit, mUnitTypePos);
		//load the final value into the result list
		mResultList.get(mResultList.size()-1).setAnswer(mExpression.toString());
	}
	
	
	/**
	 * Function that is called after user hits the "=" key
	 * Called by calculator for solving current expression
	 * @param exp
	 * @return solved expression
	 */
	private boolean solveAndLoadIntoResultList(){
		//the answer will be loaded into mExpression directly
		Result result = mSolver.solve(mExpression);
		//skip result list handling if no result was created
		if(result != null){
			mResultList.add(result);
			//if we hit size limit, remove oldest element 
			if(mResultList.size() > RESULT_LIST_MAX_SIZE)
				mResultList.remove(0);
			//also set result's unit if it's selected
			if(isUnitIsSet()){
				//load units into result list (this will also set contains unit flag
				Unit toUnit = getCurrUnitType().getSelectedUnit();
				mResultList.get(mResultList.size()-1).setResultUnit(toUnit, toUnit, mUnitTypePos);
			}
			return true;
		}
		else 
			return false;
	}

	/**
	 * Clear function for the calculator
	 */	
	private void clear(){
		//clear the immediate expression
		mExpression.clearExpression();

		//reset current unit
		mUnitTypeArray.get(mUnitTypePos).clearUnitSelection();
	}   


	/**
	 * Backspace function for the calculator
	 */
	private void backspace(){
		//clear out unit selection and expression if we just solved or if expression empty
		if(mExpression.isSolved() || mExpression.isEmpty()){
			mUnitTypeArray.get(mUnitTypePos).clearUnitSelection();
			mExpression.clearExpression();
			//we're done. don't want to execute code below
			return;
		}

		//delete last of calcExp list so long as expression isn't already empty
		if(!mExpression.isEmpty()){
			mExpression.backspaceAtSelection();
			//if we just cleared the expression out, also clear currUnit
			if(mExpression.isEmpty())
				mUnitTypeArray.get(mUnitTypePos).clearUnitSelection();
		}	
	}

	public List<Result> getResultList() {
		return mResultList;
	}

	/** Used by the controller to determine if any convert keys should be selected */
	public boolean isUnitIsSet(){
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

	public int getSelectionEnd(){
		return mExpression.getSelectionEnd();
	}	

	public int getSelectionStart(){
		return mExpression.getSelectionStart();
	}

	/**
	 * Set the EditText selection for expression
	 * @param selStart
	 * @param selEnd
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
