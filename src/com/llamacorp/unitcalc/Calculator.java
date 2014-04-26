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

	//string to hold expressions
	private String expression;
	//this string stores the more precise result after solving
	private String precResult="";

	private List<PrevExpression> prevExpressions = new ArrayList<PrevExpression>();

	private ArrayList<UnitType> mUnitTypeArray;
	private int UnitTypePos;

	//stores whether or not we just hit equals
	private boolean solved=false;

	//we want the display precision to be a bit less than calculated
	private MathContext mcOperate = new MathContext(intCalcPrecision);
	private MathContext mcDisp = new MathContext(intDisplayPrecision);

	//precision for all calculations
	public static final int intDisplayPrecision = 8;
	public static final int intCalcPrecision = intDisplayPrecision+2;

	//Error messages
	private static final String strSyntaxError = "Syntax Error";
	private static final String strDivideZeroError = "Divide By Zero Error";
	private static final String strInfinityError = "Number Too Large";


	//note that in []'s only ^, -, and ] need escapes. - doen't need one if invalid
	private static final String regexInvalidChars = ".*[^0-9()E.+*^/-].*";
	private static final String regexOperators = "+/*^-";
	private static final String regexInvalidStartChar = "[E*^/]";
	private static final String regexAnyValidOperator = "[" + regexOperators + "]";
	private static final String regexAnyOperatorOrE = "[E" + regexOperators + "]";
	private static final String regexGroupedNumber = "(\\-?\\d*\\.?\\d+\\.?(?:E[\\-\\+]?\\d+)?)";

	private static final String regexGroupedExponent = "(\\^)";
	private static final String regexGroupedMultDiv = "([/*])";
	private static final String regexGroupedAddSub = "([+-])";
	

	//this is for testing only
	private Calculator(){
		expression="";
		//used to tell what type of unit is being converted to from
		UnitTypePos=0;
		initiateUnits();
	}
	//this is for testing only (note that it overwrites the old one)
	public static Calculator getTestCalculator(){
		mCaculator = new Calculator();
		return mCaculator;
	}

	/**
	 * Function turns calculator class into a singleton class (one instance allowed)
	 */
	private Calculator(Context appContext){
		//save our context
		//mAppContext = appContext;
		expression="";

		//holds the current location of UnitType
		UnitTypePos=0;
		initiateUnits();
	}

	/**
	 * Function turns calculator class into a singleton class (one instance allowed)
	 */
	public static Calculator getCalculator(Context c){
		if(mCaculator == null)
			mCaculator = new Calculator(c.getApplicationContext());
		return mCaculator;
	}

	/**
	 * Function used to initiate the array of various types of units
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
	 * Cleans off the expression, adds missing parentheses, then loads in more accurate result values if possible into expression
	 * Iterates over expression using PEMAS order of operations, then sets solved flag to true
	 * @return the cleaned previous expression for loading into prevExpression
	 */	
	private boolean solve(){
		//clean off any dangling operators and E's (not para!!)
		expression = expression.replaceAll(regexAnyOperatorOrE + "+$", "");

		//if expression empty, don't need to solve anything
		if(expression.equals(""))
			return false;

		//if more open para then close, add corresponding close para's
		int numCloseParaToAdd = numOpenPara();
		for(int i=0; i<numCloseParaToAdd; i++){
			expression = expression + ")";
		}

		//save the expression temporarily, later save to prevExpression
		prevExpressions.add(new PrevExpression(expression));

		//load in more precise result if possible
		if(!precResult.equals("")){
			//make the precise string not precise temporarily
			BigDecimal lessPrec = new BigDecimal(precResult,mcDisp);
			String lessPrecCleaned = cleanNum(lessPrec.toString());

			//find out if expression's first term matches first part of the precise result, if so replace with more precise term
			if(firstNumb().equals(lessPrecCleaned)){
				expression=expression.replaceFirst(regexGroupedNumber, precResult.toString());
			}
		}

		//The P operator of PEMAS, this function will call the remaining EMAS 
		expression = collapsePara(expression);

		//flag used to tell backspace and numbers to clear the expression when pressed
		solved=true;
		
		//solve was a success (might be able to replace with solved
		return true;
		//String prevAns = expression;
		//save the entire unsolved expression in a list
		//prevExpressions.add(prevExp + " = " + prevAns);
	}


	/**
	 * Clean up a string's formatting 
	 * @param sToClean is the string that will be cleaned
	 */		
	private String cleanNum(String sToClean){
		//clean off any dangling .'s and .0's 
		sToClean = sToClean.replaceAll("\\.0*$", "");	

		//clean off 0's after decimal
		sToClean = sToClean.replaceAll("(\\.\\d*[1-9])0+$", "$1");	

		//remove +'s from #E+#
		sToClean = sToClean.replaceAll("E\\+", "E");	

		//remove 0's before E ei 6.1000E4 to 6.1E4; or 6.000E4 to 6.1E4; but leave 0E8 as itself
		sToClean = sToClean.replaceAll("([\\d.]+?)0+E", "$1E");

		return sToClean;
	}

	/**
	 * Recursively loop over all parentheses, invoke other operators in results found within
	 * @param s is the String to loop the parentheses solving over
	 */	
	private String collapsePara(String s) {
		//find the first open para
		int firstPara = s.indexOf("(");

		//if no open para exists, move on
		if(firstPara!=-1){
			//loop over all parentheses
			int paraCount=0;
			int matchingPara=-1;
			for(int i = firstPara; i<s.length(); i++){
				if(s.charAt(i) == '(')
					paraCount++;
				else if (s.charAt(i) == ')'){
					paraCount--;
					if (paraCount==0){
						matchingPara=i;
						break;
					}
				}
			}

			//we didn't find the matching para put up syntax error
			if(matchingPara==-1)
				expression = strSyntaxError;
			else{
				//this is the section before any para, aka "25+" in "25+(9)", or just "" if "(9)"
				String firstSection = s.substring(0, firstPara);
				//this is the inside of the outermost parentheses set, recurse over inside to find more parentheses
				String middleSection = collapsePara(s.substring(firstPara+1, matchingPara));
				//this is after the close of the outermost found set, might be lots of operators/numbers or ""
				String endSection = s.substring(matchingPara+1, s.length());

				//all parentheses found, splice string back together
				s = collapsePara(firstSection + middleSection + endSection);
			}
		}
		//perform other operations in proper order of operations
		s = collapseOps(regexGroupedExponent, s);
		s = collapseOps(regexGroupedMultDiv, s);
		s = collapseOps(regexGroupedAddSub, s);
		return s;
	}



	/**
	 * Loop over/collapse down str, solves for either +- or /*.  places result in expression
	 * @param regexOperatorType is the type of operators to look for in regex form
	 * @param str is the string to operate upon
	 */	
	private String collapseOps(String regexOperatorType, String str){
		//find the first instance of operator in the str (we want left to right per order of operations)
		Pattern ptn = Pattern.compile(regexGroupedNumber + regexOperatorType + regexGroupedNumber);
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
				str=strSyntaxError;
				return str;
			}

			//perform actual operation on found operator and operands
			if(operator.equals("+"))
				result=operand1.add(operand2,mcOperate);
			else if(operator.equals("-"))
				result=operand1.subtract(operand2,mcOperate);
			else if(operator.equals("*"))
				result=operand1.multiply(operand2,mcOperate);
			else if(operator.equals("^")){
				//this is a temp hack, will most likely want to use a custom bigdecimal function to perform more accurate/bigger conversions
				double dResult=Math.pow(operand1.doubleValue(), operand2.doubleValue());
				//catch infinity errors could be neg or pos
				try{
					result = BigDecimal.valueOf(dResult);
				} catch (NumberFormatException ex){
					if (dResult==Double.POSITIVE_INFINITY || dResult==Double.NEGATIVE_INFINITY)
						str=strInfinityError;	
					//else case most likely shouldn't occur
					else 
						str=strSyntaxError;		
					return str;
				}
			}
			else if(operator.equals("/")){
				//catch divide by zero errors
				try{
					result=operand1.divide(operand2,mcOperate);
				} catch (ArithmeticException ex){
					str=strDivideZeroError;
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
	 * Gets the first number (returned as a String) in the current expression
	 * @return anything before the first valid operator, or "" if expression empty, or entire expression if doesn't contain regexAnyValidOperator
	 */
	private String firstNumb(){
		String [] strA = expression.split(regexAnyValidOperator);
		return strA[0];
	}

	/**
	 * Gets the last double (returned as a String) in the current expression
	 * @return anything after last valid operator, or "" if expression empty, or entire expression if doesn't contain regexAnyValidOperator
	 */
	private String lastNumb(){
		String [] strA = expression.split(regexAnyValidOperator);
		if(strA.length==0) return "";
		else return strA[strA.length-1];
	}


	/** Gets the number after the E in expression (not including + and -) */	
	private int lastNumbExponent(){
		//func returns "" if expression empty, and expression if doesn't contain E[+-]?
		if(expression.contains("E")){
			String [] strA = expression.split("E[+-]?");
			return Integer.parseInt(strA[strA.length-1]);
		}
		else 
			//need to be bigger than intDisplayPrecision so calling func uses toString instead of toPlainString
			return intDisplayPrecision+2;
	}	

	/**
	 * Counts the number of open vs number of closed para in the current expression
	 * @return 0 if equal num of open/close para, positive # if more open, neg # if more close
	 */
	private int numOpenPara() {
		int numOpen = 0;
		int numClose = 0;
		for(int i=0; i<expression.length(); i++){
			if (expression.charAt(i) == '(')
				numOpen++;
			if (expression.charAt(i) == ')')
				numClose++;
		}

		return numOpen - numClose;
	}

	/**
	 * Rounds expression down by a mathcontext mcDisp
	 */	
	private void roundAndCleanExpression() {
		//if expression was displaying error (with invalid chars) leave
		if(expression.matches(regexInvalidChars) || expression.equals(""))
			return;

		//if there's any messed formatting, or if number is too big, throw syntax error
		BigDecimal bd;
		try{
			//round the answer for viewer's pleasure
			bd = new BigDecimal(expression,mcDisp);
		}
		catch (NumberFormatException e){
			expression=strSyntaxError;
			return;
		}

		//save the original to precise result for potential later use
		precResult = expression;

		//determine if exponent (number after E) is small enough for non-engineering style print, otherwise do regular style
		if(lastNumbExponent()<intDisplayPrecision)
			expression = bd.toPlainString();
		else
			expression = bd.toString();
		
		//finally clean the result off
		expression=cleanNum(expression);
	}


	/**
	 * Function used to convert from one unit to another
	 * @param fromValue is standardized value of the unit being converted from
	 * @param toValue is standardized value of the unit being converted to
	 */		
	public void convertFromTo(Unit fromUnit, Unit toUnit){
		//if expression empty, or contains invalid chars, don't need to solve anything
		if(expression.equals("") || expression.matches(regexInvalidChars))
			return;

		//first solve the function
		boolean solveSuccess = solve();
		//if there solve failed because there was nothing to solve, just leave (this way prevExpression isn't loaded)
		if (!solveSuccess)
			return;

		//this catches weird expressions like "-(-)-"
		try{
			//convert numbers into big decimals for more operation control over precision			
			BigDecimal bdToUnit   = new BigDecimal(toUnit.getValue(),mcOperate);
			BigDecimal bdCurrUnit = new BigDecimal(fromUnit.getValue(),mcOperate);			
			BigDecimal bdResult   = new BigDecimal(expression,mcOperate);
			// perform actual unit conversion (result*currUnit/toUnit)
			expression = bdResult.multiply(bdCurrUnit.divide(bdToUnit, mcOperate),mcOperate).toString();
		}
		catch (NumberFormatException e){
			//System.out.println("e.getMessage()= " + e.getMessage());
			expression=strSyntaxError;
			return;
		}

		//round and clean the result expression off
		roundAndCleanExpression();
		
		//load units into prevExpression (this will also set contains unit flag
		prevExpressions.get(prevExpressions.size()-1).setQuerryUnit(fromUnit);
		prevExpressions.get(prevExpressions.size()-1).setAnswerUnit(toUnit);
		//load the final value into prevExpression
		prevExpressions.get(prevExpressions.size()-1).setAnswer(expression);
	}


	/**
	 * Passed a key from calculator (num/op/back/clear/eq) and distributes it to its proper function
	 * @param sKey is either single character (but still a String) or a string from prevExpression
	 */
	public void parseKeyPressed(String sKey){
		//if expression was displaying "Syntax Error" or similar (containing invalid chars) clear it
		if(expression.matches(regexInvalidChars))
			expression="";

		//check for equals key
		if(sKey.equals("=")){
			//first solve the function
			boolean solveSuccess = solve();
			//if there solve failed because there was nothing to solve, just leave (this way prevExpression isn't loaded)
			if (!solveSuccess)
				return;
			//round and clean the result expression off
			roundAndCleanExpression();
			//load the final value into prevExpression
			prevExpressions.get(prevExpressions.size()-1).setAnswer(expression);
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
			if(solved) expression=sKey;
			else expression = expression + sKey;
			solved=false;
			return;
		}

		//check for invalid entries
		if(sKey.matches(regexInvalidChars))
			throw new IllegalArgumentException("In addToExpression, invalid sKey..."); 


		//don't start with [*/^E] when the expression string is empty or if we opened a para
		if(sKey.matches(regexInvalidStartChar) && (lastNumb().equals("") || expression.matches(".*\\($")))
			return;

		//if just hit equals, and we hit [.0-9(], then delete expression
		if(solved && sKey.matches("[.0-9(]")){
			//also will want to clear current unit type
			mUnitTypeArray.get(UnitTypePos).clearUnitSelection();
			expression="";
		}
		//when adding (, if the previous character was any number or decimal, or close para, add mult
		if(sKey.equals("(") && expression.matches(".*[\\d).]$"))
			expression = expression + "*";

		//when adding # after ), add multiply
		if(sKey.matches("[0-9]") && expression.matches(".*\\)$"))
			expression = expression + "*";

		//add auto completion for close parentheses
		if(sKey.equals(")"))	
			if(numOpenPara() <= 0) //if more close than open, add an open
				expression = "(" + expression;

		//if we already have a decimal in the number, don't add another
		if(sKey.equals(".") && lastNumb().matches(".*[.E].*"))
			//lastNumb returns the last num; if expression="4.3+", returns "4.3"; if last key was an operator, allow decimals
			//if(expression.matches(".*[^-+/*]$"))
			if(!expression.matches(".*" + regexAnyValidOperator + "$"))
				return;

		//if we already have a E in the number, don't add another; also don't add E immediately after an operator
		//if(sKey.equals("E") && (lastNumb().contains("E")))
		if(sKey.equals("E") && (lastNumb().contains("E") || expression.matches(".*" + regexAnyValidOperator + "$")))
			return;		

		//if we E was last pressed, only allow [1-9+-(]
		if(lastNumb().matches(".*E$"))
			if(sKey.matches("[^\\d(+-]"))
				return;

		//if last digit was only a decimal, don't add any operator or E
		if(sKey.matches(regexAnyOperatorOrE) && lastNumb().equals("."))
			return;				

		//if we're adding an operator and we already have one, replace it
		if(sKey.matches(regexAnyValidOperator) && expression.matches(".*" + regexAnyValidOperator + "$"))
			expression = expression.substring(0, expression.length()-1) + sKey;
		//otherwise load the new string
		else
			expression = expression + sKey;

		//we added a num/op, reset the solved flag
		solved=false;
	}


	/**
	 * Clear function for the calculator
	 */	
	private void clear(){
		//clear the immediate expression
		expression="";

		//reset solved flag
		solved=false;

		//reset current unit
		mUnitTypeArray.get(UnitTypePos).clearUnitSelection();
	}   


	/**
	 * Backspace function for the calculator
	 */
	private void backspace(){
		//if we just solved expression, clear expression out
		if(solved || expression.equals("")){
			mUnitTypeArray.get(UnitTypePos).clearUnitSelection();
			expression="";
			solved=false;
			//we're done. don't want to execute code below
			return;
		}

		//delete last of calcExp list so long as expression isn't already empty
		if(!expression.equals("")){
			expression=expression.substring(0, expression.length()-1);	
			//if we just cleared the expression out, also clear currUnit
			if(expression.equals(""))
				mUnitTypeArray.get(UnitTypePos).clearUnitSelection();
		}	
	}

	public List<PrevExpression> getPrevExpressions() {
		return prevExpressions;
	}

	/** Used by the controller to determine if any convert keys should be selected */
	public boolean currUnitIsSet(){
		return mUnitTypeArray.get(UnitTypePos).getIsUnitSelected();
	}

	public UnitType getUnitType(int pos) {
		return mUnitTypeArray.get(pos);
	}

	public int getUnitTypeSize() {
		return mUnitTypeArray.size();
	}

	public void setUnitTypePos(int pos){
		UnitTypePos = pos;
	}
	
	///** returns the string of the previous expression */
	//public String toStringLastExpression(){
	//	if(prevExpressions.size()==0)
	//		return "";
	//	else 
	//		return prevExpressions.get(prevExpressions.size()-1).toString();
	//}

	@Override
	public String toString(){
		//needed for display updating
		return expression;
	}

}
