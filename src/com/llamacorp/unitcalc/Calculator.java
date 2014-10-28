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

	public Hints mHints;

	//precision for all calculations
	public static final int intDisplayPrecision = 8;
	public static final int intCalcPrecision = intDisplayPrecision+2;


	private boolean mIsTestCalc = false;

	//------THIS IS FOR TESTING ONLY-----------------
	private Calculator(){
		mResultList = new ArrayList<Result>();
		mExpression=new Expression(intDisplayPrecision); 		
		//mMcOperate = new MathContext(intCalcPrecision); 
		mSolver = new Solver(intCalcPrecision);
		mUnitTypePos=UNIT_TYPE_DEFAULT_POS;	
		mUnitTypeArray = new ArrayList<UnitType>();
		mIsTestCalc=true;
		initiateUnits();
		mHints = new Hints();
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

		mResultList = new ArrayList<Result>();
		mExpression = new Expression(intDisplayPrecision);
		//set the unit type to length by default
		mUnitTypePos=UNIT_TYPE_DEFAULT_POS;
		mHints = new Hints();

		//load the calculating precision
		mSolver = new Solver(intCalcPrecision);

		mUnitTypeArray = new ArrayList<UnitType>();
		//call helper method to actually load in units
		initiateUnits();

		//over-right values above if this works
		try {
			loadState();
		} catch (Exception e) {}
	}

	/**
	 * Method turns calculator class into a singleton class (one instance allowed)
	 */
	public static Calculator getCalculator(Context c){
		if(mCaculator == null)
			mCaculator = new Calculator(c.getApplicationContext());
		return mCaculator;
	}



	/**
	 * Helper method used to initiate the array of various types of units
	 */	
	private void initiateUnits(){
		mUnitTypeArray.clear();

		UnitType unitsOfCurrency = new UnitType("Currency");
		unitsOfCurrency.addUnit(new UnitCurrency("USD", "Dollars", 1));
		unitsOfCurrency.addUnit(new UnitCurrency("EUR", "Euros", 0.76)); 
		unitsOfCurrency.addUnit(new UnitCurrency("CAD", "Canadian Dollars", 1.08)); 
		unitsOfCurrency.addUnit(new UnitCurrency("GBP", "Pounds", 0.6)); 
		unitsOfCurrency.addUnit(new UnitCurrency("JPY", "Yen", 103.9)); 

		unitsOfCurrency.addUnit(new UnitCurrency("CHF", "Swiss Francs", 0.91)); 
		unitsOfCurrency.addUnit(new UnitCurrency("AUD", "Australian Dollars", 1.07)); 
		unitsOfCurrency.addUnit(new UnitCurrency("HKD", "Hong Kong Dollars", 7.75)); 
		unitsOfCurrency.addUnit(new UnitCurrency("SGD", "Singapore Dollars", 1.25)); 
		unitsOfCurrency.addUnit(new UnitCurrency("CNY", "Chinese Yuans", 6.15)); 
//		unitsOfCurrency.addUnit(new UnitCurrency()); 
//		unitsOfCurrency.addUnit(new UnitCurrency()); 
//		unitsOfCurrency.addUnit(new UnitCurrency()); 
//
//		unitsOfCurrency.addUnit(new UnitCurrency()); 
//		unitsOfCurrency.addUnit(new UnitCurrency()); 
//		unitsOfCurrency.addUnit(new UnitCurrency()); 
//		unitsOfCurrency.addUnit(new UnitCurrency()); 
		
		unitsOfCurrency.addUnit(new UnitCurrency("BTC", "Bitcoins", 0.003, 
												"http://blockchain.info/tobtc?currency=USD&value=1")); 
		unitsOfCurrency.addUnit(new UnitCurrency("RUB", "Russian Rubles", 39.7)); 
		mUnitTypeArray.add(unitsOfCurrency);

		refreshAllDynamicUnits();		

		UnitType unitsOfTemp = new UnitType("Temp");
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature("\u00B0F", "Fahrenheit", UnitTemperature.FAHRENHEIT));

		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature("\u00B0K", "Kelvin", UnitTemperature.KELVIN));
		unitsOfTemp.addUnit(new UnitTemperature("\u00B0C", "Celsius", UnitTemperature.CELSIUS));
		mUnitTypeArray.add(unitsOfTemp);


		UnitType unitsOfWeight = new UnitType("Weight");
		unitsOfWeight.addUnit(new UnitScalar("oz", "Ounces", 1/0.0283495));
		unitsOfWeight.addUnit(new UnitScalar("lb", "Pounds", 1/0.453592));
		unitsOfWeight.addUnit(new UnitScalar("ton us", "Short Tons", 1/907.184));
		unitsOfWeight.addUnit(new UnitScalar("ton uk", "Long Tons", 1/1016.04608));
		unitsOfWeight.addUnit(new UnitScalar("st", "Stones", 1/6.350288));

		unitsOfWeight.addUnit(new UnitScalar("\u00B5g", "Micrograms", 1/1e-9));
		unitsOfWeight.addUnit(new UnitScalar("mg", "Milligrams", 1/1e-6));
		unitsOfWeight.addUnit(new UnitScalar("g", "Grams", 1/0.001));
		unitsOfWeight.addUnit(new UnitScalar("kg", "Kilograms", 1));
		unitsOfWeight.addUnit(new UnitScalar("ton", "Metric Tons", 1/1e3));

		unitsOfWeight.addUnit(new UnitScalar("oz t", "Troy Ounces", 1/0.0311034768)); //exact
		unitsOfWeight.addUnit(new UnitScalar("gr", "Grains", 1/6.479891E-5)); //exact
		unitsOfWeight.addUnit(new UnitScalar("dwt", "Pennyweight", 20/0.0311034768)); //exact, 1/20 troy oz
		unitsOfWeight.addUnit(new UnitScalar("CD", "Carat", 5000)); // =200mg
		mUnitTypeArray.add(unitsOfWeight);


		UnitType unitsOfLength = new UnitType("Length");
		unitsOfLength.addUnit(new UnitScalar("in", "Inches", 1/0.0254));//exact
		unitsOfLength.addUnit(new UnitScalar("ft", "Feet", 1/0.3048));//exact: in*12
		unitsOfLength.addUnit(new UnitScalar("yd", "Yards", 1/0.9144));//exact: in*12*3
		unitsOfLength.addUnit(new UnitScalar("mi", "Miles", 1/1609.344));//exact: in*12*5280
		unitsOfLength.addUnit(new UnitScalar("km", "Kilometers", 1/1000.0));

		unitsOfLength.addUnit(new UnitScalar("nm", "Nanometers", 1E9));
		unitsOfLength.addUnit(new UnitScalar("\u00B5m", "Micrometers", 1E6));
		unitsOfLength.addUnit(new UnitScalar("mm", "Millimeters", 1000));
		unitsOfLength.addUnit(new UnitScalar("cm", "Centimeters", 100));
		unitsOfLength.addUnit(new UnitScalar("m", "Meters", 1));

		unitsOfLength.addUnit(new UnitScalar("Å", "Ångströms", 1E10));
		unitsOfLength.addUnit(new UnitScalar("mil", "Thousandths of an Inch", 1/2.54E-5));
		unitsOfLength.addUnit(new UnitScalar("fur", "Furlongs", 0.00497096954));
		unitsOfLength.addUnit(new UnitScalar("nmi", "Nautical Miles", 1/1852));
		unitsOfLength.addUnit(new UnitScalar("ly", "Light Years", 1/9.4607E15));
		unitsOfLength.addUnit(new UnitScalar("pc", "Parsecs", 3.24078E-17));
		mUnitTypeArray.add(unitsOfLength);	


		UnitType unitsOfArea = new UnitType("Area");
		unitsOfArea.addUnit(new UnitScalar("in\u00B2", "Square Inches", 1/0.00064516));//exact: 0.0254^2
		unitsOfArea.addUnit(new UnitScalar("ft\u00B2", "Square Feet", 1/0.09290304));//0.3048^2
		unitsOfArea.addUnit(new UnitScalar("yd\u00B2", "Square Yards", 1/0.83612736));//0.3048^2*9
		unitsOfArea.addUnit(new UnitScalar("acre", "Acres", 1/4046.8564224));//0.3048^2*9*4840
		unitsOfArea.addUnit(new UnitScalar("mi\u00B2", "Square Miles", 1/2589988.110336));//1609.344^2

		unitsOfArea.addUnit(new UnitScalar("mm\u00B2", "Square Millimeters", 1/0.000001));
		unitsOfArea.addUnit(new UnitScalar("cm\u00B2", "Square Centimeters", 1/0.0001));
		unitsOfArea.addUnit(new UnitScalar("m\u00B2", "Square Meters", 1));
		unitsOfArea.addUnit(new UnitScalar("km\u00B2", "Square Kilometers", 1/1000000.0));
		unitsOfArea.addUnit(new UnitScalar("ha", "Hectares", 1/10000.0));

		unitsOfArea.addUnit(new UnitScalar("a", "Ares", 0.01));
		unitsOfArea.addUnit(new UnitScalar("cir mil", "Circular Mils", 1/5.067E-10));
		mUnitTypeArray.add(unitsOfArea);


		UnitType unitsOfVolume = new UnitType("Volume");
		unitsOfVolume.addUnit(new UnitScalar("tbsp", "Tablespoons", 1/0.00001478676478125));//exact: gal/256
		unitsOfVolume.addUnit(new UnitScalar("cup", "Cups", 1/0.0002365882365));//exact: gal/16
		unitsOfVolume.addUnit(new UnitScalar("pt", "Pints (US)", 1/0.000473176473));//exact: gal/8
		unitsOfVolume.addUnit(new UnitScalar("qt", "Quarts (US)", 1/0.000946352946));//exact: gal/4
		unitsOfVolume.addUnit(new UnitScalar("gal", "Gallons (US)", 1/0.003785411784));//exact: according to wiki

		unitsOfVolume.addUnit(new UnitScalar("tsp", "Teaspoons", 1/0.00000492892159375));//exact: gal/768
		unitsOfVolume.addUnit(new UnitScalar("fl oz", "Fluid Ounces (US)", 1/0.0000295735295625));//exact: gal/128
		unitsOfVolume.addUnit(new UnitScalar("mL", "Milliliters", 1E6)); 
		unitsOfVolume.addUnit(new UnitScalar("L", "Liters", 1000));
		unitsOfVolume.addUnit(new UnitScalar("m^3", "Cubic Meters", 1));

		unitsOfVolume.addUnit(new UnitScalar("in\u00B3", "Cubic Inches", 1/0.000016387064));//exact: gal/231
		unitsOfVolume.addUnit(new UnitScalar("ft\u00B3", "Cubic Feet", 1/0.028316846592));//exact: gal/231*12^3
		unitsOfVolume.addUnit(new UnitScalar("yd\u00B3", "Cubic Yards", 1/0.764554857984));//exact: 3^3 ft^3
		unitsOfVolume.addUnit(new UnitScalar("cm\u00B3", "Cubic Centimeters", 1E6));
		unitsOfVolume.addUnit(new UnitScalar("cm\u00B3", "Cubic Millimeters", 1E9));
		unitsOfVolume.addUnit(new UnitScalar("cL", "Centiliter", 1E5));
		unitsOfVolume.addUnit(new UnitScalar("dL", "Deciliters", 1E4));
		unitsOfVolume.addUnit(new UnitScalar("gal uk", "Gallons (UK)", 1000/4.54609));//exact: 4.54609L/gal uk
		unitsOfVolume.addUnit(new UnitScalar("qt uk", "Quart (UK)", 1000/1.1365225));//exact: gal uk/4
		unitsOfVolume.addUnit(new UnitScalar("pt uk", "Pints (UK)", 1000/0.56826125));//exact: gal uk/8
		unitsOfVolume.addUnit(new UnitScalar("fl oz uk", "Fluid Ounces (UK)", 1000/0.0284130625));//exact: gal uk/160
		unitsOfVolume.addUnit(new UnitScalar("shot", "Shots (US)", 1/0.00004436029434375));//exact for 1.5 fl oz

		mUnitTypeArray.add(unitsOfVolume);


		UnitType unitsOfSpeed = new UnitType("Speed");
		unitsOfSpeed.addUnit(new UnitScalar("mi/min","Miles per minute", 1/26.8224));
		unitsOfSpeed.addUnit(new UnitScalar("min/mi","Minute miles", 1/26.8224, true));
		unitsOfSpeed.addUnit(new UnitScalar("ft/s", "Feet per Second", 1/0.3048));
		unitsOfSpeed.addUnit(new UnitScalar("mph", "Miles per Hour", 1/0.44704));
		unitsOfSpeed.addUnit(new UnitScalar("knot", "Knots", 1/0.514444));

		unitsOfSpeed.addUnit(new UnitScalar("", 0));
		unitsOfSpeed.addUnit(new UnitScalar("", 0));
		unitsOfSpeed.addUnit(new UnitScalar("", 0));
		unitsOfSpeed.addUnit(new UnitScalar("m/s", "Meters per Second", 1));
		unitsOfSpeed.addUnit(new UnitScalar("kph", "Kilometers per Hour", 3.6));
		mUnitTypeArray.add(unitsOfSpeed);


		UnitType unitsOfPower = new UnitType("Power");
		unitsOfPower.addUnit(new UnitScalar("", 0));
		unitsOfPower.addUnit(new UnitScalar("", 0));
		unitsOfPower.addUnit(new UnitScalar("W", "Watts", 1));
		unitsOfPower.addUnit(new UnitScalar("kW", "Kilowatts", 1E-3));
		unitsOfPower.addUnit(new UnitScalar("hp", "Horsepower", 1/745.699872)); //don't think it's exact

		unitsOfPower.addUnit(new UnitScalar("", 0));
		unitsOfPower.addUnit(new UnitScalar("Btu/hr", "Btus/Hour", 3.412141632)); //approx
		unitsOfPower.addUnit(new UnitScalar("Btu/min", "Btus/Minute", 0.0568690272)); //approx
		unitsOfPower.addUnit(new UnitScalar("ft-lb/min", "Foot-Pounds/Minute", 44.2537289)); //most likely approx
		unitsOfPower.addUnit(new UnitScalar("ft-lb/sec", "Foot-Pounds/Second", 0.73756215)); //most likely approx
		mUnitTypeArray.add(unitsOfPower);


		UnitType unitsOfEnergy = new UnitType("Energy");
		unitsOfEnergy.addUnit(new UnitScalar("cal", "Calories", 0.239005736)); //approx
		unitsOfEnergy.addUnit(new UnitScalar("kCal", "Kilocalories", 0.239005736/1E3)); //approx, but exact comp to cal
		unitsOfEnergy.addUnit(new UnitScalar("BTU", "British Thermal Units", 0.00094781712)); //approx
		unitsOfEnergy.addUnit(new UnitScalar("ft-lb", "Foot-pounds", 1/1.3558179483314)); //approx
		unitsOfEnergy.addUnit(new UnitScalar("in-lb", "Inch-pounds", 12/1.3558179483314)); //approx

		unitsOfEnergy.addUnit(new UnitScalar("kJ", "Kilojoules", 0.001));
		unitsOfEnergy.addUnit(new UnitScalar("J", "Joules", 1));
		unitsOfEnergy.addUnit(new UnitScalar("Wh", "Watt-Hours", 1/3.6E3)); //exact
		unitsOfEnergy.addUnit(new UnitScalar("kWh", "Kilowatt-Hours", 1/3.6E6)); //exact
		unitsOfEnergy.addUnit(new UnitScalar("Nm", "Newton-Meters", 1));

		unitsOfEnergy.addUnit(new UnitScalar("MJ", "Megajoules", 1E-6));
		unitsOfEnergy.addUnit(new UnitScalar("eV", "Electronvolts", 6.241509E18));
		mUnitTypeArray.add(unitsOfEnergy);


		UnitType unitsOfTorque = new UnitType("Torque");
		unitsOfTorque.addUnit(new UnitScalar("Nm", "Newton Meters", 1)); 
		unitsOfTorque.addUnit(new UnitScalar("Ncm", "Newton Centimeters", 100)); 
		unitsOfTorque.addUnit(new UnitScalar("kgf m", "Kilogram-Force Meters", 1/9.80665)); //exact
		unitsOfTorque.addUnit(new UnitScalar("kgf cm", "Kilogram-Force Centimeters", 100/9.80665)); //exact
		unitsOfTorque.addUnit(new UnitScalar("dyn m", "Dyne Meters", 1E5)); 

		unitsOfTorque.addUnit(new UnitScalar("lbf in", "Pound-Force Inches", 12/1.3558179483314004));  //exact
		unitsOfTorque.addUnit(new UnitScalar("lbf ft", "Pound-Force Feet", 1/1.3558179483314004));  //exact
		unitsOfTorque.addUnit(new UnitScalar("ozf in", "Ounce-Force Inches", 192/1.3558179483314004));  //exact
		unitsOfTorque.addUnit(new UnitScalar("ozf ft", "Ounce-Force Feet", 16/1.3558179483314004));  //exact
		unitsOfTorque.addUnit(new UnitScalar("dyn cm", "Dyne Centimeters", 1));
		mUnitTypeArray.add(unitsOfTorque);

		//note the use of singular tense, not sure what is the best
		UnitType unitsOfPressure = new UnitType("Pressure");
		unitsOfPressure.addUnit(new UnitScalar("N/m\u00B2", "Newton/Square Meter", 1));
		unitsOfPressure.addUnit(new UnitScalar("lb/ft\u00B2", "Pounds/Square Foot", 144/6894.757293168));  //approx
		unitsOfPressure.addUnit(new UnitScalar("psi", "Pounds/Square Inch", 1/6894.757293168)); //approx
		unitsOfPressure.addUnit(new UnitScalar("atm", "Atmospheres", 1/101325.0));  //exact
		unitsOfPressure.addUnit(new UnitScalar("bar", "Bars", 0.00001));  //exact

		unitsOfPressure.addUnit(new UnitScalar("kg/m\u00B2", "Kilogram/Square Meter", 1/9.80665)); //approx?
		unitsOfPressure.addUnit(new UnitScalar("kPa", "Kilopascals", 0.001)); //exact
		unitsOfPressure.addUnit(new UnitScalar("Pa", "Pascal", 1)); //exact
		unitsOfPressure.addUnit(new UnitScalar("inHg", "Inches of Mercury", 1/3386.388640341)); //exact using cmHg - 1/(1333.22387415*2.54)
		unitsOfPressure.addUnit(new UnitScalar("cmHg", "Centimeters of Mercury", 1/1333.22387415)); //exact

		unitsOfPressure.addUnit(new UnitScalar("mmHg", "Millimeters of Mercury", 1/133.322387415)); //exact
		unitsOfPressure.addUnit(new UnitScalar("N/cm\u00B2", "Newton/Square Centimeters", 1E-4));
		unitsOfPressure.addUnit(new UnitScalar("N/mm\u00B2", "Newton/Square Millimeters", 1E-6));
		unitsOfPressure.addUnit(new UnitScalar("kg/cm\u00B2", "Kilogram/Square Centimeter", 1/98066.5)); //approx?
		mUnitTypeArray.add(unitsOfPressure);

	}



	private void loadState() throws IOException, JSONException {
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
			mUnitTypePos = jObjState.getInt(JSON_UNIT_TYPE);
			mExpression = new Expression(jObjState.getJSONObject(JSON_EXPRESSION), intDisplayPrecision);
			mHints = new Hints(jObjState.getJSONObject(JSON_HINTS));


			JSONArray jResultArray = jObjState.getJSONArray(JSON_RESULT_LIST);
			// build the array of results from JSONObjects
			for (int i = 0; i < jResultArray.length(); i++) {
				mResultList.add(new Result(jResultArray.getJSONObject(i)));
			}

			int newSize = mUnitTypeArray.size();	
			JSONArray jUnitTypeArray = jObjState.getJSONArray(JSON_UNIT_TYPE_ARRAY);
			//only load in saved unit types only if same size as current
			if(jUnitTypeArray.length()==newSize){
				mUnitTypeArray.clear();
				// build the array of results from JSONObjects
				for (int i = 0; i < jUnitTypeArray.length(); i++) {
					mUnitTypeArray.add(new UnitType(jUnitTypeArray.getJSONObject(i)));
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
		jObjState.put(JSON_HINTS, mHints.toJSON());

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
		mHints = new Hints();
		//set the unit type to length by default
		mUnitTypePos=UNIT_TYPE_DEFAULT_POS;

		//load the calculating precision
		mSolver = new Solver(intCalcPrecision);

		mUnitTypeArray.clear();
		initiateUnits();
	}



	/**
	 * Passed a key from calculator (num/op/back/clear/eq) and distributes it to its proper function
	 * @param sKey is either single character (but still a String) or a string from result list
	 */
	public void parseKeyPressed(String sKey){
		//if expression was displaying "Syntax Error" or similar (containing invalid chars) clear it
		if(isExpressionInvalid())
			mExpression.clearExpression();

		//if a convert was just done, main display will show "16 in", but no button will be colored
		//want this treated the same as 16 (as if no unit is actually selected)
		if(isSolved() && isUnitIsSet())
			clearSelectedUnit();

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
		else if(sKey.equals("c")){
			clear();
		}

		//else try all other potential numbers and operators, as well as result list
		else{
			//if just hit equals, and we hit [.0-9(], then clear current unit type
			if(mExpression.isSolved() && sKey.matches("[.0-9(]"))
				clearSelectedUnit();

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
		if(isExpressionInvalid()){
			mExpression.clearExpression();
			return;
		}
		//want to add a 1 if we just hit one unit and another
		if(isExpressionEmpty())
			parseKeyPressed("1");
		//first solve the function
		boolean solveSuccess = solveAndLoadIntoResultList();
		//if there solve failed because there was nothing to solve, just leave (this way result list isn't loaded)
		if (!solveSuccess)
			return;

		//next perform numerical unit conversion
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
			//if result had an error, leave before setting units
			if(Expression.isInvalid(result.getAnswer()))
				return false;
			//also set result's unit if it's selected
			if(isUnitIsSet()){
				//load units into result list (this will also set contains unit flag
				Unit toUnit = getCurrUnitType().getCurrUnit();
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

		//delete last of calcExp list so long as expression isn't already empty
		if(!mExpression.isEmpty()){
			mExpression.backspaceAtSelection();
			//if we just cleared the expression out, also clear currUnit
			if(mExpression.isEmpty())
				clearSelectedUnit();
		}	
	}

	private void clearSelectedUnit(){
		mUnitTypeArray.get(mUnitTypePos).clearUnitSelection();
	}


	/**
	 * Update values of units that are not static (currency) via
	 * each unit's own HTTP/JSON API call. Note that this refresh
	 * is asynchronous and will only happen sometime in the future 
	 * Internet connection permitting.
	 */		
	public void refreshAllDynamicUnits(){
		//JUnit tests can't find AsynTask class, so skip it for test calc
		if(!mIsTestCalc)
			for(UnitType ut : mUnitTypeArray)
				ut.refreshDynamicUnits(mAppContext);
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
