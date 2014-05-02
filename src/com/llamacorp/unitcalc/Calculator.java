package com.llamacorp.unitcalc;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

import com.llamacorp.unitcalc.UnitType.OnConvertionListener;

public class Calculator implements OnConvertionListener{
	private static Calculator mCaculator;

	//private static Calculator mCaculator;
	//this will be used later with a serializer to save data to the system
	//private Context mAppContext; 

	//main expression
	private Expression mExpression;

	//string of previous expressions; this will be directly manipulated by ResultListFragment
	private List<PrevExpression> mPrevExpressions = new ArrayList<PrevExpression>();

	//stores the array of various types of units (length, area, volume, etc)
	private ArrayList<UnitType> mUnitTypeArray;
	//stores the current location in mUnitTypeArray
	private int mUnitTypePos;

	//we want the display precision to be a bit less than calculated
	private MathContext mMcOperate;

	//precision for all calculations
	public static final int intDisplayPrecision = 8;
	public static final int intCalcPrecision = intDisplayPrecision+2;

	//error messages
	private static final String strSyntaxError = "Syntax Error";
	private static final String strDivideZeroError = "Divide By Zero Error";
	private static final String strInfinityError = "Number Too Large";



	//------THIS IS FOR TESTING ONLY-----------------
	private Calculator(){
		mExpression=new Expression(new MathContext(intDisplayPrecision)); 		
		mMcOperate = new MathContext(intCalcPrecision); 
		mUnitTypePos=0;	
		initiateUnits();	
	}
	//------THIS IS FOR TESTING ONLY-----------------
	public static Calculator getTestCalculator(){ mCaculator=new Calculator(); return mCaculator; }


	/**
	 * Method turns calculator class into a singleton class (one instance allowed)
	 */
	private Calculator(Context appContext){
		//save our context
		//mAppContext = appContext;
		mExpression=new Expression(new MathContext(intDisplayPrecision));
		//load the calculating precision
		mMcOperate = new MathContext(intCalcPrecision);

		//set the unit type to length by default
		mUnitTypePos=0;
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

	/**
	 * Helper method used to initiate the array of various types of units
	 */	
	private void initiateUnits(){
		mUnitTypeArray = new ArrayList<UnitType>();

		UnitType unitsOfLength = new UnitType(this);
		unitsOfLength.addUnit("in", 0.0254);
		unitsOfLength.addUnit("ft", 0.3048);
		unitsOfLength.addUnit("yard", 0.9144);
		unitsOfLength.addUnit("mile", 1609.344);
		unitsOfLength.addUnit("km", 1000.0);

		unitsOfLength.addUnit("nm", 0.000000001);
		unitsOfLength.addUnit("um", 0.000001);
		unitsOfLength.addUnit("mm", 0.001);
		unitsOfLength.addUnit("cm", 0.01);
		unitsOfLength.addUnit("m", 1.0);
		mUnitTypeArray.add(unitsOfLength);	

		UnitType unitsOfArea = new UnitType(this);
		unitsOfArea.addUnit("in^2", 0.00064516);//0.0254^2
		unitsOfArea.addUnit("ft^2", 0.09290304);//0.3048^2
		unitsOfArea.addUnit("yd^2", 0.83612736);//0.3048^2*9
		unitsOfArea.addUnit("acre", 4046.8564224);//0.3048^2*9*4840
		unitsOfArea.addUnit("mi^2", 2589988.110336);//1609.344^2

		unitsOfArea.addUnit("mm^2", 0.000001);
		unitsOfArea.addUnit("cm^2", 0.0001);
		unitsOfArea.addUnit("m^2", 1.0);
		unitsOfArea.addUnit("km^2", 1000000.0);
		unitsOfArea.addUnit("ha", 10000.0);
		mUnitTypeArray.add(unitsOfArea);


		UnitType unitsOfVolume = new UnitType(this);
		unitsOfVolume.addUnit("tbsp", 0.000014786764765625);//gal/256
		unitsOfVolume.addUnit("cup",  0.00023658823625);//gal/16
		unitsOfVolume.addUnit("pint", 0.0004731764725);//gal/8
		unitsOfVolume.addUnit("qt",   0.000946352945);//gal/4
		unitsOfVolume.addUnit("gal",  0.00378541178);//found online; source of other conversions

		unitsOfVolume.addUnit("tsp", 4.92892158854166666667e-6);//gal/768
		unitsOfVolume.addUnit("fl oz", 0.00002957352953125);//gal/128
		unitsOfVolume.addUnit("ml", 1e-6);
		unitsOfVolume.addUnit("l", 0.001);
		unitsOfVolume.addUnit("m^3", 1);
		mUnitTypeArray.add(unitsOfVolume);
	}


	/**
	 * Function that is called after user hits the "=" key
	 * Called by calculator for solving current expression
	 * @param exp
	 * @return solved expression
	 */
	private void solveCurrentExpression(){
		String prevEx = solve(mExpression, mMcOperate);

		//save the expression temporarily, later save to prevExpression
		if(prevEx.equals(""))
			mPrevExpressions.add(new PrevExpression(prevEx));
	}


	/**
	 * Solves a given Expression
	 * Cleans off the expression, adds missing parentheses, then loads in more accurate result values if possible into expression
	 * Iterates over expression using PEMAS order of operations
	 * @param exp is the Expression to solve
	 * @param mcSolve is the rounding to use to solve
	 * @return the expression before conversion (potentially used for prev Expression)
	 */	
	static private String solve(Expression exp, MathContext mcSolve){
		//clean off any dangling operators and E's (not parentheses!!)
		exp.cleanDanglingOps();

		//if more open parentheses then close, add corresponding close para's
		exp.closeOpenPar();

		String prevExp = exp.toString();
		//if expression empty, don't need to solve anything
		if(exp.isEmpty())
			return "";

		//load in the precise result if possible
		exp.loadPreciseResult();

		//main calculation: first the P of PEMAS, this function then calls remaining EMAS 
		String tempExp = collapsePara(exp.toString(), mcSolve);
		//save solved expression away
		exp.setExpression(tempExp);

		//flag used to tell backspace and numbers to clear the expression when pressed
		exp.setSolved(true);

		return prevExp;
	}


	/**
	 * Recursively loop over all parentheses, invoke other operators in results found within
	 * @param str is the String to loop the parentheses solving over
	 */	
	private static String collapsePara(String str, MathContext mcSolve) {
		//find the first open parentheses
		int firstPara = str.indexOf("(");

		//if no open parentheses exists, move on
		if(firstPara!=-1){
			//loop over all parentheses
			int paraCount=0;
			int matchingPara=-1;
			for(int i = firstPara; i<str.length(); i++){
				if(str.charAt(i) == '(')
					paraCount++;
				else if (str.charAt(i) == ')'){
					paraCount--;
					if (paraCount==0){
						matchingPara=i;
						break;
					}
				}
			}

			//we didn't find the matching parentheses put up syntax error and quit
			if(matchingPara==-1){
				str = strSyntaxError;
				return str;
			}
			else{
				//this is the section before any parentheses, aka "25+" in "25+(9)", or just "" if "(9)"
				String firstSection = str.substring(0, firstPara);
				//this is the inside of the outermost parentheses set, recurse over inside to find more parentheses
				String middleSection = collapsePara(str.substring(firstPara+1, matchingPara), mcSolve);
				//this is after the close of the outermost found set, might be lots of operators/numbers or ""
				String endSection = str.substring(matchingPara+1, str.length());

				//all parentheses found, splice string back together
				str = collapsePara(firstSection + middleSection + endSection, mcSolve);
			}
		}
		//perform other operations in proper order of operations
		str = collapseOps(Expression.regexGroupedExponent, str, mcSolve);
		str = collapseOps(Expression.regexGroupedMultDiv, str, mcSolve);
		str = collapseOps(Expression.regexGroupedAddSub, str, mcSolve);
		return str;
	}



	/**
	 * Loop over/collapse down input str, solves for either +- or /*.  places result in expression
	 * @param regexOperatorType is the type of operators to look for in regex form
	 * @param str is the string to operate upon
	 */	
	private static String collapseOps(String regexOperatorType, String str, MathContext mcSolve){
		//find the first instance of operator in the str (we want left to right per order of operations)
		Pattern ptn = Pattern.compile(Expression.regexGroupedNumber + regexOperatorType + Expression.regexGroupedNumber);
		Matcher mat = ptn.matcher(str);
		BigDecimal result;

		//this loop will loop through each occurrence of the "# op #" sequence
		while (mat.find()) {
			BigDecimal operand1;
			BigDecimal operand2;
			String operator;

			//be sure string is formatted properly
			try{
				operand1 = new BigDecimal(mat.group(1));
				operand2 = new BigDecimal(mat.group(3));
				operator = mat.group(2);
			}
			catch (NumberFormatException e){
				//throw syntax error if we have a weirdly formatted string
				str = strSyntaxError;
				return str;
			}

			//perform actual operation on found operator and operands
			if(operator.equals("+"))
				result = operand1.add(operand2, mcSolve);
			else if(operator.equals("-"))
				result = operand1.subtract(operand2, mcSolve);
			else if(operator.equals("*"))
				result = operand1.multiply(operand2, mcSolve);
			else if(operator.equals("^")){
				//this is a temp hack, will most likely want to use a custom bigdecimal function to perform more accurate/bigger conversions
				double dResult=Math.pow(operand1.doubleValue(), operand2.doubleValue());
				//catch infinity errors could be neg or pos
				try{
					result = BigDecimal.valueOf(dResult);
				} catch (NumberFormatException ex){
					if (dResult==Double.POSITIVE_INFINITY || dResult==Double.NEGATIVE_INFINITY)
						str = strInfinityError;	
					//else case most likely shouldn't occur
					else 
						str = strSyntaxError;		
					return str;
				}
			}
			else if(operator.equals("/")){
				//catch divide by zero errors
				try{
					result = operand1.divide(operand2, mcSolve);
				} catch (ArithmeticException ex){
					str = strDivideZeroError;
					return str;
				}
			}
			else
				throw new IllegalArgumentException("In collapseOps, invalid operator...");
			//save cut out the old str and save in the result
			str = str.substring(0, mat.start()) + result + str.substring(mat.end());

			//reset the matcher with a our new str
			mat = ptn.matcher(str);
		}
		return str;
	}



	/**
	 * Function used to convert from one unit to another
	 * @param fromValue is standardized value of the unit being converted from
	 * @param toValue is standardized value of the unit being converted to
	 */		
	public void convertFromTo(Unit fromUnit, Unit toUnit){
		//if expression empty, or contains invalid chars, don't need to solve anything
		if(mExpression.equals("") || mExpression.matches(regexInvalidChars))
			return;

		//first solve the function
		boolean solveSuccess = solveCurrentExpression();
		//if there solve failed because there was nothing to solve, just leave (this way prevExpression isn't loaded)
		if (!solveSuccess)
			return;

		//this catches weird expressions like "-(-)-"
		try{
			//convert numbers into big decimals for more operation control over precision			
			BigDecimal bdToUnit   = new BigDecimal(toUnit.getValue(),mMcOperate);
			BigDecimal bdCurrUnit = new BigDecimal(fromUnit.getValue(),mMcOperate);			
			BigDecimal bdResult   = new BigDecimal(mExpression,mMcOperate);
			// perform actual unit conversion (result*currUnit/toUnit)
			mExpression = bdResult.multiply(bdCurrUnit.divide(bdToUnit, mMcOperate),mMcOperate).toString();
		}
		catch (NumberFormatException e){
			//System.out.println("e.getMessage()= " + e.getMessage());
			mExpression=strSyntaxError;
			return;
		}


		//rounding operation may throw NumberFormatException
		try{
			mExpression.roundAndCleanExpression();
		}
		catch (NumberFormatException e){
			mExpression=strSyntaxError;
			return;
		}

		//load units into prevExpression (this will also set contains unit flag
		mPrevExpressions.get(mPrevExpressions.size()-1).setQuerryUnit(fromUnit);
		mPrevExpressions.get(mPrevExpressions.size()-1).setAnswerUnit(toUnit);
		//load the final value into prevExpression
		mPrevExpressions.get(mPrevExpressions.size()-1).setAnswer(mExpression);
	}


	/**
	 * Passed a key from calculator (num/op/back/clear/eq) and distributes it to its proper function
	 * @param sKey is either single character (but still a String) or a string from prevExpression
	 */
	public void parseKeyPressed(String sKey){
		//if expression was displaying "Syntax Error" or similar (containing invalid chars) clear it
		if(mExpression.matches(regexInvalidChars))
			mExpression="";

		//check for equals key
		if(sKey.equals("=")){
			//first solve the function
			boolean solveSuccess = solveCurrentExpression();
			//if there solve failed because there was nothing to solve, just leave (this way prevExpression isn't loaded)
			if (!solveSuccess)
				return;
			//round and clean the result expression off
			roundAndCleanExpression();
			//load the final value into prevExpression
			mPrevExpressions.get(mPrevExpressions.size()-1).setAnswer(mExpression);
		}
		//check for backspace key
		else if(sKey.equals("b"))
			backspace();

		//check for clear key
		else if(sKey.equals("c"))
			clear();

		//else try all other potential numbers and operators, as well as prevExpression
		else
			addToExpression(sKey);
	}


	/**
	 * This function will try to add a number or operator, or entire prevExpression to the current expression
	 * Note that there is lots of error checking to be sure user can't entire an invalid operator/number
	 * @param sKey should only be single vaild number or operator character, or longer previous expressions
	 */
	private void addToExpression(String sKey){
		//for now, if we're adding a prev expression, just add it without error checking
		if(sKey.length()>1){
			if(mSolved) mExpression=sKey;
			else mExpression = mExpression + sKey;
			mSolved=false;
			return;
		}

		//check for invalid entries
		if(sKey.matches(regexInvalidChars))
			throw new IllegalArgumentException("In addToExpression, invalid sKey..."); 


		//don't start with [*/^E] when the expression string is empty or if we opened a para
		if(sKey.matches(regexInvalidStartChar) && (lastNumb().equals("") || mExpression.matches(".*\\($")))
			return;

		//if just hit equals, and we hit [.0-9(], then delete expression
		if(mSolved && sKey.matches("[.0-9(]")){
			//also will want to clear current unit type
			mUnitTypeArray.get(mUnitTypePos).clearUnitSelection();
			mExpression="";
		}
		//when adding (, if the previous character was any number or decimal, or close para, add mult
		if(sKey.equals("(") && mExpression.matches(".*[\\d).]$"))
			mExpression = mExpression + "*";

		//when adding # after ), add multiply
		if(sKey.matches("[0-9]") && mExpression.matches(".*\\)$"))
			mExpression = mExpression + "*";

		//add auto completion for close parentheses
		if(sKey.equals(")"))	
			if(numOpenPara() <= 0) //if more close than open, add an open
				mExpression = "(" + mExpression;

		//if we already have a decimal in the number, don't add another
		if(sKey.equals(".") && lastNumb().matches(".*[.E].*"))
			//lastNumb returns the last num; if expression="4.3+", returns "4.3"; if last key was an operator, allow decimals
			//if(expression.matches(".*[^-+/*]$"))
			if(!mExpression.matches(".*" + regexAnyValidOperator + "$"))
				return;

		//if we already have a E in the number, don't add another; also don't add E immediately after an operator
		//if(sKey.equals("E") && (lastNumb().contains("E")))
		if(sKey.equals("E") && (lastNumb().contains("E") || mExpression.matches(".*" + regexAnyValidOperator + "$")))
			return;		

		//if we E was last pressed, only allow [1-9+-(]
		if(lastNumb().matches(".*E$"))
			if(sKey.matches("[^\\d(-]"))
				return;

		//if last digit was only a decimal, don't add any operator or E
		if(sKey.matches(regexAnyOperatorOrE) && lastNumb().equals("."))
			return;	

		//don't allow "--" or "65E--"
		if(sKey.matches("[-]") && mExpression.matches(".*E?[-]"))
			return;	

		//if we have "84*-", replace both the * and the - with the operator
		if(sKey.matches(regexAnyValidOperator) && mExpression.matches(".*" + regexAnyValidOperator + regexAnyValidOperator + "$"))
			mExpression = mExpression.substring(0, mExpression.length()-2) + sKey;
		//if there's already an operator, replace it with the new operator, except for -, let that stack up
		else if(sKey.matches(regexInvalidStartChar) && mExpression.matches(".*" + regexAnyValidOperator + "$"))
			mExpression = mExpression.substring(0, mExpression.length()-1) + sKey;
		//otherwise load the new keypress
		else
			mExpression = mExpression + sKey;

		//we added a num/op, reset the solved flag
		mSolved=false;
	}


	/**
	 * Clear function for the calculator
	 */	
	private void clear(){
		//clear the immediate expression
		mExpression="";

		//reset solved flag
		mSolved=false;

		//reset current unit
		mUnitTypeArray.get(mUnitTypePos).clearUnitSelection();
	}   


	/**
	 * Backspace function for the calculator
	 */
	private void backspace(){
		//if we just solved expression, clear expression out
		if(mSolved || mExpression.equals("")){
			mUnitTypeArray.get(mUnitTypePos).clearUnitSelection();
			mExpression="";
			mSolved=false;
			//we're done. don't want to execute code below
			return;
		}

		//delete last of calcExp list so long as expression isn't already empty
		if(!mExpression.equals("")){
			mExpression=mExpression.substring(0, mExpression.length()-1);	
			//if we just cleared the expression out, also clear currUnit
			if(mExpression.equals(""))
				mUnitTypeArray.get(mUnitTypePos).clearUnitSelection();
		}	
	}

	public List<PrevExpression> getPrevExpressions() {
		return mPrevExpressions;
	}

	/** Used by the controller to determine if any convert keys should be selected */
	public boolean currUnitIsSet(){
		return mUnitTypeArray.get(mUnitTypePos).getIsUnitSelected();
	}

	public UnitType getUnitType(int pos) {
		return mUnitTypeArray.get(pos);
	}

	public UnitType getCurrUnitType() {
		return mUnitTypeArray.get(mUnitTypePos);
	}

	public int getUnitTypeSize() {
		return mUnitTypeArray.size();
	}

	public void setUnitTypePos(int pos){
		mUnitTypePos = pos;
	}

	@Override
	public String toString(){
		//needed for display updating
		return mExpression;
	}

}
