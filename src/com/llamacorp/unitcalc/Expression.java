package com.llamacorp.unitcalc;

import java.math.BigDecimal;
import java.math.MathContext;

public class Expression {
	//the main expression string
	private String mExpression;
	//this string stores the more precise result after solving
	private String mPreciseResult;
	private MathContext mMcDisp;
	//stores whether or not this expression was just solved
	private boolean mSolved;


	public static final String regexGroupedExponent = "(\\^)";
	public static final String regexGroupedMultDiv = "([/*])";
	public static final String regexGroupedAddSub = "([+-])";

	//note that in []'s only ^, -, and ] need escapes. - doen't need one if invalid
	public static final String regexInvalidChars = ".*[^0-9()E.+*^/-].*";
	public static final String regexOperators = "+/*^-";
	public static final String regexInvalidStartChar = "[E*^/+]";
	public static final String regexAnyValidOperator = "[" + regexOperators + "]";
	public static final String regexAnyOperatorOrE = "[E" + regexOperators + "]";
	public static final String regexGroupedNumber = "(\\-?\\d*\\.?\\d+\\.?(?:E[\\-\\+]?\\d+)?)";


	public Expression(MathContext mcDisp){
		mExpression="";
		mPreciseResult="";
		setSolved(false);
		mMcDisp = mcDisp;
	}

	public Expression(){
		this(null);
	}


	/** Close any open parentheses in this expression */
	public void closeOpenPar(){
		//if more open parentheses then close, add corresponding close para's
		int numCloseParaToAdd = numOpenPara();
		for(int i=0; i<numCloseParaToAdd; i++){
			mExpression = mExpression + ")";
		}
	}

	/**
	 * Load in more precise result if possible
	 * @param mMcDisp is the amount to round
	 */		
	public void loadPreciseResult(){
		//make sure we have valid precise result and rounding Mathcontext first
		if(!mPreciseResult.equals("") || mMcDisp != null){
			//make the precise string not precise temporarily for comparison 
			BigDecimal formallyPrec = new BigDecimal(mPreciseResult, mMcDisp);
			String formallyPrecCleaned = cleanFormatting(formallyPrec.toString());

			//find out if expression's first term matches first part of the precise result, if so replace with more precise term
			if(firstNumb().equals(formallyPrecCleaned)){
				mExpression=mExpression.replaceFirst(regexGroupedNumber, mPreciseResult.toString());
			}
		}
	}
	
	/** Clean off any dangling operators and E's (not parentheses!!) at the END ONLY */
	public void cleanDanglingOps(){
		mExpression = mExpression.replaceAll(regexAnyOperatorOrE + "+$", "");
	}
	

	
	
	/** Returns if this expression is empty */
	public boolean isEmpty(){
		if(mExpression.equals("")) 
			return true;
		else 
			return false;
	}


	
	
	/** Returns the post rounded result */
	public String getPreciseResult(){
		return mPreciseResult;
	}
	

	public boolean isSolved() {
		return mSolved;
	}

	public void setSolved(boolean solved) {
		mSolved = solved;
	}

	public void setExpression(String tempExp) {
		mExpression=tempExp;
	}	
	
	@Override
	public String toString(){
		return mExpression;
	}

	

	/**
	 * Clean up a string's formatting 
	 * @param sToClean is the string that will be cleaned
	 */		
	static private String cleanFormatting(String sToClean){
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
	 * Counts the number of open vs number of closed parentheses in the given 
	 * @return 0 if equal num of open/close para, positive # if more open, neg # if more close
	 */
	private int numOpenPara() {
		int numOpen = 0;
		int numClose = 0;
		for(int i=0; i<mExpression.length(); i++){
			if (mExpression.charAt(i) == '(')
				numOpen++;
			if (mExpression.charAt(i) == ')')
				numClose++;
		}

		return numOpen - numClose;
	}

	
	
	/**
	 * Gets the first number (returned as a String) in the current expression
	 * @return anything before the first valid operator, or "" if expression empty, or entire expression if doesn't contain regexAnyValidOperator
	 */
	private String firstNumb(){
		String [] strA = mExpression.split(regexAnyValidOperator);
		return strA[0];
	}

	/**
	 * Gets the last double (returned as a String) in the current expression
	 * @return anything after last valid operator, or "" if expression empty, or entire expression if doesn't contain regexAnyValidOperator
	 */
	private String lastNumb(){
		String [] strA = mExpression.split(regexAnyValidOperator);
		if(strA.length==0) return "";
		else return strA[strA.length-1];
	}

	
}
