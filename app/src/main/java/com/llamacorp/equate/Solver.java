package com.llamacorp.equate;

import com.llamacorp.equate.unit.Unit;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Solver {
	//error messages
	public static final String strSyntaxError = "Syntax Error";
	public static final String strDivideZeroError = "Divide By Zero Error";
	public static final String strInfinityError = "Number Too Large";


	//we want the display precision to be a bit less than calculated
	private MathContext mMcOperate;
	
	
	public Solver(int solvePrecision){
		if(solvePrecision>0)
			mMcOperate = new MathContext(solvePrecision);
	}


   public Result tryToggleSciNote(Expression exp, boolean forceEngineering){
      if(exp.isInvalid() || exp.isEmpty() || exp.containsOps()
              || exp.containsParens())
         return null;

      String cleanedQuery = exp.toString();

      //if we want engineering, just convert regardless if we have E already
      if(forceEngineering){
			exp.roundAndCleanExpression(Expression.NumFormat.ENGINEERING);
		}
         //determine if we are are in sci notation already
      else if(exp.toString().matches(".*E.*"))
         exp.roundAndCleanExpression(Expression.NumFormat.PLAIN);
      else
         exp.roundAndCleanExpression(Expression.NumFormat.SCINOTE);

      return new Result(cleanedQuery, exp.toString());
   }


	/**
	 * Solves a given Expression
	 * Cleans off the expression, adds missing parentheses, then loads in more
    * accurate result values if possible into expression.
	 * Iterates over expression using PEMAS order of operations
	 * @param exp is the Expression to solve
	 * @return the expression before conversion (potentially used for result list)
	 */	
	public Result solve(Expression exp, boolean useEngineering){
		//clean off any dangling operators and E's (not parentheses!!)
		exp.cleanDanglingOps();

		//if more open parentheses then close, add corresponding close para's
		exp.closeOpenPar();

		//save away query before we start manipulating it
		String cleanedQuery = exp.toString();
		//add implied multiplies for display purposes
		cleanedQuery = Expression.addImpliedParMult(cleanedQuery);
		//if expression empty|invalid, don't need to solve anything
		if(exp.isEmpty())
			return null;

		//load in the precise result if possible
		exp.loadPreciseResult();

		//deal with percent operators
		String strExp = Expression.replacePercentOps(exp.toString());
		
		//put parenthesis around ^ expressions; -(1)^2 to -((1)^2)
		strExp = Expression.groupPowerOperands(strExp);

		//add implied multiples for parenthesis
		strExp = Expression.addImpliedParMult(strExp);
		
		//main calculation: first the P of PEMAS, this function then calls remaining EMAS 
		strExp = collapsePara(strExp);
		//save solved expression away
		exp.replaceExpression(strExp);

		roundAndClean(exp, useEngineering);
		
		//flag used to tell backspace and numbers to clear the expression when pressed
		exp.setSolved(true);
		return new Result(cleanedQuery, exp.toString());
	}

	

	/**
	 * Function used to convert from one unit to another
	 * @param fromUnit is unit being converted from
	 * @param toUnit is unit being converted to
	 */		
	public void convertFromTo(Unit fromUnit, Unit toUnit, Expression exp){
		String toSolve = fromUnit.convertTo(toUnit, exp.getPreciseResult());
		exp.replaceExpression(toSolve);

		solve(exp, false);
	}

	/**
	 * Recursively loop over all parentheses, invoke other operators in results found within
	 * @param str is the String to loop the parentheses solving over
	 */	
	private String collapsePara(String str) {
		//find the first open parentheses
		int firstPara = str.indexOf("(");

		//if no open parentheses exists, move on
		if(firstPara!=-1){
			//loop over all parentheses
			int matchingPara = Expression.findMatchingClosePara(str, firstPara);

			//we didn't find the matching parentheses put up syntax error and quit
			if(matchingPara==-1){
				str = strSyntaxError;
				return str;
			}
			else{
				//this is the section before any parentheses, aka "25+" in "25+(9)", or just "" if "(9)"
				String firstSection = str.substring(0, firstPara);
				//this is the inside of the outermost parentheses set, recurse over inside to find more parentheses
				String middleSection = collapsePara(str.substring(firstPara+1, matchingPara));
				//this is after the close of the outermost found set, might be lots of operators/numbers or ""
				String endSection = str.substring(matchingPara+1, str.length());

				//all parentheses found, splice string back together
				str = collapsePara(firstSection + middleSection + endSection);
			}
		}
		//perform other operations in proper order of operations
		str = collapseOps(Expression.regexGroupedExponent, str);
		str = collapseOps(Expression.regexGroupedMultDiv, str);
		str = collapseOps(Expression.regexGroupedAddSub, str);
		return str;
	}



	/**
	 * Loop over/collapse down input str, solves for either +- or /*.  places result in expression
	 * @param regexOperatorType is the type of operators to look for in regex form
	 * @param str is the string to operate upon
	 */	
	private String collapseOps(String regexOperatorType, String str){
		//find the first instance of operator in the str (we want left to right per order of operations)
		Pattern ptn = Pattern.compile(Expression.regexGroupedNonNegNumber + regexOperatorType + Expression.regexGroupedNumber);
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
				operand2 = new BigDecimal(mat.group(Expression.numGroupsInregexGroupedNumber+2));
				operator = mat.group(Expression.numGroupsInregexGroupedNumber+1);
			}
			catch (NumberFormatException e){
				//throw syntax error if we have a weirdly formatted string
				str = strSyntaxError;
				return str;
			}
			
			//perform actual operation on found operator and operands
			if(operator.equals("+")){
				//crude fix for 1E999999+1, which hangs the app. Could be handled better with real infinity...
				if(operand1.scale() < -9000 || operand2.scale() < -9000)
					return strInfinityError;	
				result = operand1.add(operand2, mMcOperate);
			}
			else if(operator.equals("-")){
				if(operand1.scale() < -9000 || operand2.scale() < -9000)
					return strInfinityError;	
				result = operand1.subtract(operand2, mMcOperate);
			}
			else if(operator.equals("*"))
				result = operand1.multiply(operand2, mMcOperate);
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
					result = operand1.divide(operand2, mMcOperate);
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

	private void roundAndClean(Expression exp, boolean useEngineering){
		//rounding operation may throw NumberFormatException
		try{
         if(useEngineering)
            exp.roundAndCleanExpression(Expression.NumFormat.ENGINEERING);
         else
            exp.roundAndCleanExpression(Expression.NumFormat.NORMAL);
		}
		catch (NumberFormatException e){
			exp.replaceExpression(strSyntaxError);
		}
	}

}
