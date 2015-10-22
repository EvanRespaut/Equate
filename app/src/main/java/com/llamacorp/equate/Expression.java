package com.llamacorp.equate;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class Expression {
	private static final String JSON_EXPRESSION = "expression";
	private static final String JSON_PRECISE = "precise";
	private static final String JSON_START = "sel_start";
	private static final String JSON_END = "sel_end";
	private static final String JSON_SOLVED = "sel_end";

	public enum NumFormat {NORMAL, PLAIN, SCINOTE, ENGINEERING}

	//the main expression string
	private String mExpression;
	//this string stores the more precise result after solving
	private String mPreciseResult;
	private MathContext mMcDisp;
	private int mIntDisplayPrecision;

	//highlighted text selection
	private int mSelectionStart;
	private int mSelectionEnd;

	//stores whether or not this expression was just solved
	private boolean mSolved;

	private NumFormat mNumFormat = NumFormat.NORMAL;

	//list of indexes of characters to highlight
	private ArrayList<Integer> mHighlightedCharList;

	public static final String regexGroupedExponent = "(\\^)";
	public static final String regexGroupedMultDiv = "([/*])";
	public static final String regexGroupedAddSub = "([+-])";

	//note that in []'s only ^, -, and ] need escapes. - doen't need one if invalid
	public static final String regexNonNegOperators = "+/*^%";
	private static final String regexOperators = regexNonNegOperators + "-";
	private static final String regexInvalidChars = "[^0-9()E." + regexOperators + "]";
	private static final String regexHasInvalidChars = ".*" + regexInvalidChars + ".*";
	private static final String regexInvalidStartChar = "[E" + regexNonNegOperators + "]";
	private static final String regexAnyValidOperator = "[" + regexOperators + "]";
	private static final String regexAnyOpExceptPercent = regexAnyValidOperator.replace("%","");
	private static final String regexAnyOperatorOrE = "[E" + regexOperators + "]";
	public static final String regexGroupedNumber = "([-]?\\d*[.]?\\d+[.]?(?:E[+-]?\\d+)?)";
	public static final String regexGroupedNonNegNumber = "((?:(?<=^)[-])?(?:(?<=[*+(/-])[-])?\\d*[.]?\\d+[.]?(?:E[+-]?\\d+)?)";
	//this isn't really needed anymore, if want non capturing group, use ?:"
	public static final int numGroupsInregexGroupedNumber = 1;

	//the following is a more concise version, but doesn't work with NP++ due to the | in the lookbehind
	//public static final String regexGroupedNonNegNumber = "((?:(?<=^|[*+/])\\-)?\\d*\\.?\\d+\\.?(?:E[\\-\\+]?\\d+)?)";

	private String[][] substituteChars = new String[][]{{"[\u00f7\u00B7]", "/"}, //alt-246
			  {"[x\u00d7]", "*"},//alt-0215,249,250
			  {"[\u0096\u0097]", "-"}}; //alt-0151,0150
	//TODO add in all these characters

	public Expression(int dispPrecision){
		clearExpression();
		mPreciseResult="";
		setSelection(0, 0);
		mHighlightedCharList = new ArrayList<>();
		clearHighlightedList();
		//skip precise unit usage if precision is set to 0
		if(dispPrecision>0){
			mIntDisplayPrecision = dispPrecision;
			mMcDisp = new MathContext(mIntDisplayPrecision);
		}
	}

	public Expression(JSONObject json, int displayPrecision) throws JSONException {
		this(displayPrecision);
		replaceExpression(json.getString(JSON_EXPRESSION));
		mPreciseResult = json.getString(JSON_PRECISE);
		//TODO this is breaking
		//		mSelectionStart = json.getInt(JSON_START);
		//		mSelectionEnd = json.getInt(JSON_END);

		setSolved(json.getBoolean(JSON_SOLVED));
	}

	public JSONObject toJSON()  throws JSONException {
		JSONObject json = new JSONObject();

		json.put(JSON_EXPRESSION, toString());
		json.put(JSON_PRECISE, mPreciseResult);
		json.put(JSON_START, getSelectionStart());
		json.put(JSON_END, getSelectionEnd());
		json.put(JSON_SOLVED, isSolved());

		return json;
	}


	/**
	 * This function will try to add a number or operator, or entire result list to the current expression
	 * Note that there is lots of error checking to be sure user can't entire an invalid operator/number
	 * @param sKey should only be single valid number or operator character, or longer previous results
	 * @return returns if a solve should be performed
	 */
	public boolean keyPresses(String sKey){
		//for now, if we're adding a previous result, just add it without error checking
		if(sKey.length() > 1){
			//don't load in errors
			if(isInvalid(sKey)) return false;
			if(isSolved()) replaceExpression(sKey);
			else insertAtSelection(sKey);
			setSolved(false);
			return false;
		}

		//if we have negation, instantly perform and return
		if(sKey.equals("n")){
			negateLastNumber();
			return false;
		}

		//if we have inversion, instantly perform and return
		if(sKey.equals("i")){
			int[] toHighlight = invertLastNumber();
			//highlight the newly added invert symbols if not immediately solving
			if(!isSolved())
				markHighlighted(toHighlight);
			return isSolved();
		}

		//add auto completion for close parentheses
		if(sKey.equals("]"))
			if(numOpenPara() <= 0){ //if more close than open, add an open
				insertAt("(", 0);
				sKey = ")";
			}

		if(sKey.equals("[")){
			insertAt(")", length());
			sKey = "(";
		}

		//if just hit equals, and we hit [.0-9(], then clear expression
		if(isSolved() && sKey.matches("[.0-9(]"))
			clearExpression();

		if(!isEntryValid(sKey))
			return false;

		//add implicit * before ( if previous character was #, decimal, or )
		if(sKey.equals("(") && expressionToSelection().matches(".*[\\d).]$")){
			sKey = "*" + sKey;
			markHighlighted(expressionToSelection().length());
		}

		//when adding # after ), add multiply
		if(sKey.matches("[.0-9]") && expressionToSelection().matches(".*[)]$")){
			sKey = "*" + sKey;
			markHighlighted(expressionToSelection().length());
		}

		//if we have "84*-", replace both the * and the - with the operator
		if(sKey.matches(regexAnyValidOperator) && expressionToSelection().matches(".*" + regexAnyOpExceptPercent + regexAnyValidOperator + "$")){
			//if we have something highlighted, delete it first
			if(getSelectionEnd()>getSelectionStart()) backspaceAtSelection();
			backspaceAtSelection();
			backspaceAtSelection();
		}
		//if there's already an operator, replace it with the new operator, except for -, let that stack up
		else if(sKey.matches(regexInvalidStartChar) && expressionToSelection().matches(".*" + regexAnyOpExceptPercent + "$")){
			//if we have something highlighted, delete it first
			if(getSelectionEnd()>getSelectionStart()) backspaceAtSelection();
			backspaceAtSelection();
		}
		//otherwise load the new keypress
		insertAtSelection(sKey);

		//try to highlight matching set of para if we just closed or opened one
		highlightMatchingPara(sKey);

		//we added a num/op, reset the solved flag
		setSolved(false);
		return false;
	}


	private boolean isEntryValid(String sKey){
		//check for invalid entries
		if(sKey.matches(regexHasInvalidChars))
			throw new IllegalArgumentException("In addToExpression, invalid sKey...");

		//don't start with [*/^E] when the expression string is empty or if we opened a para
		if(sKey.matches(regexInvalidStartChar) && expressionToSelection().matches("(^[-]?$)|(.*[(]$)"))
			return false;

		//if we already have a decimal or E in the number, don't add a decimal
		if(sKey.equals(".") && getLastPartialNumb().matches(".*[.E].*"))
			//lastNumb returns the last num; if expression="4.3+", returns "4.3"; if last key was an operator, allow decimals
			if(!expressionToSelection().matches(".*" + regexAnyValidOperator + "$"))
				return false;

		//if we already have a E in the number, don't add another; also don't add E immediately after an operator
		if(sKey.equals("E") && (getLastPartialNumb().contains("E") || expressionToSelection().matches(".*" + regexAnyValidOperator + "$")))
			return false;

		//if "E" or "E-" was last pressed, only allow [0-9(-]
		if(getLastPartialNumb().matches(".*E[-]?$"))
			if(sKey.matches("[^\\d(-]"))
				return false;

		//if last digit was only a decimal, don't add any operator or E
		if(sKey.matches(regexAnyOperatorOrE) && getLastPartialNumb().equals("."))
			return false;

		//don't allow "--" or "65E--"
		if(sKey.matches("[-]") && expressionToSelection().matches(".*E?[-]"))
			return false;

		//don't allow two %'s in a row, or "5%*" then another "%"
		if(sKey.matches("%") && expressionToSelection().matches(".*%" + regexAnyValidOperator + "?$"))
			return false;

		//no problems, return valid entry
		return true;
	}


	/**
	 * This function takes text pasted by user, formats it and loads it into the expression
	 * @param str text to clean and load into expression
	 */
	public void pasteIntoExpression(String str){
		//first replace all substitutable characters
		for (String[] substituteChar : substituteChars)
			str = str.replaceAll(substituteChar[0], substituteChar[1]);
		//next remove all invalid chars
		str = str.replaceAll(regexInvalidChars,"");
		//then just blindly insert text without case checking
		insertAtSelection(str);
		//likely not necessary, since the click on EditText should've overwritten solved
		setSolved(false);
	}


	/**
	 * Rounds expression down by a MathContext mcDisp, formats it in sci notation
	 * or not, and replaces the current expression with the result
	 * @throws NumberFormatException if Expression not formatted correctly
	 */
	public void roundAndCleanExpression(NumFormat numFormat) throws NumberFormatException {
		//if expression was displaying error (with invalid chars) leave
		if(isInvalid() || isEmpty())
			return;

		//save current numformat (used to tell if we want to show eng prefix)
		setmNumFormat(numFormat);

		//if formatting messed ("-", "(())"), or number too big, throw error
		BigDecimal bd = new BigDecimal(getExpression(), mMcDisp);

		//only after getExpression() was successfully converted to BigDecimal
		//save the original to precise result for potential later use
		mPreciseResult = getExpression();

		String formatStr;
		switch(numFormat){
			case NORMAL:
				//determine if exponent (number after E) is small enough for non-engineering style print, otherwise do regular style
				if (lastNumbExponent() < mIntDisplayPrecision)
					formatStr = bd.toPlainString();
				else
					formatStr = bd.toString();
				break;
			case PLAIN:
				formatStr = bd.toPlainString();
				break;
			case SCINOTE:
				formatStr = getSciNotation(bd, mIntDisplayPrecision, false);
				break;
			case ENGINEERING:
				formatStr = getSciNotation(bd, mIntDisplayPrecision, true);
				break;
			default:
				throw new NumberFormatException("Invalid number format");
		}

		//finally clean the result off and set it in mExpression
		replaceExpression(cleanFormatting(formatStr));
	}

	/**
	 * Takes a BigDecimal and returns a string formatted in scientific notation.
	 * Has option of returning string in engineering scientific notation, where
	 * the exponent is always a multiple of 3 (eg 53E3 or 360E-9)
	 * @param bd is the number to convert into a string in sci notation
	 * @param digits number of digits of precision (eg 5 for 1.2345E8)
	 * @param engStr flag for regular scientific notation, or engineering style.
	 * @return String representation of the number in scientific notation
	 */
	private static String getSciNotation(BigDecimal bd, int digits, boolean engStr) {
		String format = engStr ? "##0.0E0" : "#.0E0";
		DecimalFormat formatter = new DecimalFormat(format);
		//formatter.setRoundingMode(RoundingMode.HALF_UP);
		formatter.setMinimumFractionDigits(digits);
		return formatter.format(bd);
	}

	/** Adds parenthesis around power operations */
	public static String groupPowerOperands(String str){
		//search to find "^" (start at 1 so no NPE later; "^3" invalid anyway)
		for(int i = 1; i < str.length(); i++){
			if(str.charAt(i) == '^'){
				int openPareIndex = 0;
				int closePareIndex = 0;
				//first find where to add (
				//first case is (###)^
				if(str.charAt(i - 1) == ')'){
					for(int k = i - 2; k > 0; k--){
						if(numOpenPara(str.substring(k, i))==0){
							openPareIndex = k;
							break;
						}
					}
				}
				//second case is just #^
				else {
					String lastNumb = getLastNumb(str.substring(0, i));
					//make nonneg (^ is beginning of, not power operator)
					lastNumb = lastNumb.replaceAll("^\\-", "");
					openPareIndex = i - lastNumb.length();
				}

				//next find where to add )
				//first case is ^(###)
				if(str.charAt(i+1) == '('){
					for(int k=i+2;k<=str.length();k++){
						if(numOpenPara(str.substring(i, k))==0){
							closePareIndex = k;
							break;
						}
					}
				}
				//second case is just #^
				else {
					String firstNum = getFirstNumb(str.substring(i+1,str.length()));
					closePareIndex = i + 1 + firstNum.length();
				}

				//actually add in pares
				str = str.substring(0, openPareIndex)
						  + "(" + str.substring(openPareIndex, closePareIndex) + ")"
						  +  str.substring(closePareIndex, str.length());
				//advanced index beyond ^#)
				i=closePareIndex;
			}
		}
		return str;
	}


	/** Replaces % operators their respective operators */
	public static String replacePercentOps(String str) {
		String subStr;
		String strAfter;
		for(int i=0;i<str.length();i++){
			if(str.charAt(i)=='%'){
				//save beginning portion of string before the %
				subStr = str.substring(0, i);
				//get the number that was right before the %
				String lastNum = getLastNumb(subStr);
				//trim off the last number
				subStr = subStr.substring(0,subStr.length()-lastNum.length());
				//as long as the string isn't empty, find last operator
				strAfter = str.substring(i+1, str.length());
				if(!subStr.equals("")){
					String lastOp = subStr.substring(subStr.length()-1,subStr.length());
					//trim off the last operator
					subStr = subStr.substring(0,subStr.length()-1);
					//case similar to 2+5% or 2-5%, but no 2+5%3 or 2+5%*3
					if(lastOp.matches(regexGroupedAddSub) && !subStr.equals("") &&
							  (strAfter.equals("") || strAfter.matches(regexGroupedAddSub + ".*$")))
						subStr = "(" + subStr + ")*(1" + lastOp + lastNum + "*0.01)";
						//cases like 2*3% or 3% or similar
					else
						subStr = subStr + lastOp + "(" + lastNum + "*0.01)";
				}
				else
					subStr = "(" + lastNum + "*0.01)";
				//replace the old contents with the new expression
				str = subStr + strAfter;
				//move up i by the diff between the new and old str
				i = subStr.length();
			}
		}
		return str;
	}


	/** Adds implied multiples for parenthesis */
	public static String addImpliedParMult(String str){
		//first replace all )( with )*(
		str = str.replaceAll("\\)\\(", "\\)\\*\\(");

		//replace all #( with #*(
		str = str.replaceAll("([\\d\\.])\\(", "$1\\*\\(");

		//replace all )# with )*#
		str = str.replaceAll("\\)([\\d\\.])", "\\)\\*$1");
		return str;
	}

	/**
	 * Searches forward in string for associated close parenthesis given the
	 * index of an open
	 * @param str String to look search over
	 * @param firstOpenIndex Index in str of the open parenthesis requiring a mate
	 * @return The index of the associated close parenthesis give the supplied open
	 * returns -1 if no such associated parenthesis was found
	 */
	public static int findMatchingClosePara(String str, int firstOpenIndex) {
		if(!str.equals("") && firstOpenIndex != -1){
			int paraCount=0;
			for(int i = firstOpenIndex; i<str.length(); i++){
				if(str.charAt(i) == '(')
					paraCount++;
				else if (str.charAt(i) == ')'){
					paraCount--;
					if (paraCount==0){
						return i;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Searches forward in string for associated close parenthesis given the
	 * index of an open
	 * @param str String to look search over
	 * @param lastCloseIndex Index in str of the close parenthesis requiring a mate
	 * @return The index of the associated open parenthesis give the supplied close
	 * returns -1 if no such associated parenthesis was found
	 */
	public static int findMatchingOpenPara(String str, int lastCloseIndex) {
		if(!str.equals("") && lastCloseIndex != -1){
			int paraCount=0;
			for(int i = lastCloseIndex; i >= 0; i--){
				if(str.charAt(i) == ')')
					paraCount++;
				else if (str.charAt(i) == '('){
					paraCount--;
					if (paraCount==0){
						return i;
					}
				}
			}
		}
		return -1;
	}


	/** Close any open parentheses in this expression */
	public void closeOpenPar(){
		//if more open parentheses then close, add corresponding close para's
		int numCloseParaToAdd = numOpenPara();
		for(int i=0; i<numCloseParaToAdd; i++){
			setExpression(getExpression() + ")");
		}
	}


	/**
	 * Load in more precise result if possible
	 */
	public void loadPreciseResult(){
		//make sure we have valid precise result and rounding Mathcontext first
		if(mPreciseResult.equals("") || mMcDisp == null)
			return;

		//make the precise string not precise temporarily for comparison
		BigDecimal formallyPrec = new BigDecimal(mPreciseResult, mMcDisp);
		String formallyPrecCleaned = cleanFormatting(formallyPrec.toString());

		//find out if expression's first term matches first part of the precise result, if so replace with more precise term
		if(getFirstNumb().equals(formallyPrecCleaned)){
			replaceExpression(getExpression().replaceFirst(regexGroupedNumber, mPreciseResult));
		}
	}


	/** Clean off any dangling operators and E's (not parentheses!!) at the END ONLY */
	public void cleanDanglingOps(){
		//don't want to trim off %'s so long as there's at least one char before it
		if(getExpression().matches(".+%$")) return;
		replaceExpression(getExpression().replaceAll(regexAnyOperatorOrE + "+$", ""));
	}


	public void setSelection(int selectionStart, int selectionEnd ) {
		if(selectionEnd > length() || selectionStart > length())
			throw new IllegalArgumentException("In Expression.setSelection, selection end or start > expression length");
		//this occurs if the use drags the end selector before the start selector; need the following code so expression doesn't get confused
		if(selectionEnd < selectionStart) {
			int temp = selectionEnd;
			selectionEnd = selectionStart;
			selectionStart = temp;
		}

		mSelectionStart = selectionStart;
		mSelectionEnd = selectionEnd;
	}


	/** Clears entire expression. Note selection will move to 0,0 */
	public void clearExpression(){
		replaceExpression("");
		setSolved(false);
	}


	/** Replaces entire expression. Selection moves to end of the expression.
	 * @param tempExp String to replace expression with*/
	public void replaceExpression(String tempExp) {
		setExpression(tempExp);
		setSelectionToEnd();
	}


	public void backspaceAtSelection(){
		int selStart = getSelectionStart();
		int selEnd = getSelectionEnd();

		//return if nothing to delete or selection at beginning of expression
		if(isEmpty() || selEnd==0)
			return;

		//if something is highlighted, delete highlighted part (replace with "")
		if(selStart != selEnd)
			insertAtSelection("");
		else {
			setExpression(getExpression().substring(0, selStart - 1) + expressionAfterSelectionStart());
			setSelection(selStart-1, selStart-1);
		}
	}

	/** Returns if this expression is empty */
	public boolean isEmpty(){
		return getExpression().equals("");
	}


	/** Returns if this expression is has invalid characters */
	public static boolean isInvalid(String str){
		return str.matches(regexHasInvalidChars);
	}


	/** Returns if this expression is has invalid characters */
	public boolean isInvalid(){
		return isInvalid(getExpression());
	}

	/**
	 * Tell whether or not this expression contains operators
	 * @return false if empty or only contains a number ("", "3E1", or "-4"),
	 * true for a result such as "2-3" "2^4" etc
	 */
	public boolean containsOps(){
		return getLastNumb(getExpression()).length() != length();
	}

	/**
	 * Returns true if this expression contains either open or close parenthesis
	 * such as "(53" or "((1))"
	 */
	public boolean containsParens(){
		return getExpression().matches(".*[()].*");
	}

	/** Returns the post rounded result */
	public String getPreciseResult(){
		return mPreciseResult;
	}

	/**
	 * @return if there are characters marked for highlighting
	 * in the current expression
	 */
	public boolean isHighlighted(){
		return mHighlightedCharList.size() != 0;
	}


	public ArrayList<Integer> getHighlighted(){
		return mHighlightedCharList;
	}


	public void clearHighlightedList() {
		mHighlightedCharList.clear();
	}


	public int getSelectionStart() {
		return mSelectionStart;
	}


	public int getSelectionEnd() {
		return mSelectionEnd;
	}


	public void setSelectionToEnd() {
		setSelection(length(), length());
	}


	public NumFormat getmNumFormat() {
		return mNumFormat;
	}

	public void setmNumFormat(NumFormat mNumFormat) {
		this.mNumFormat = mNumFormat;
	}

	public boolean isSolved() {
		return mSolved;
	}


	public void setSolved(boolean solved) {
		mSolved = solved;
	}


	/**
	 * Returns the length of the current expression
	 * @return length of expression
	 */
	public int length(){
		return getExpression().length();
	}


	/**
	 * Returns the current expression in expressed as a String
	 */
	@Override
	public String toString(){
		return getExpression();
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

		//remove 0's before E ei 6.1000E4 to 6.1E4; or 6.000E4 to 6.1E4; but leave
		// 0E8 and 100E4 as themselves
		sToClean = sToClean.replaceAll("(\\.\\d*?)0+E", "$1E");

		//remove those pesky decimal points before an E (like 1.E8)
		sToClean = sToClean.replaceAll("\\.E", "E");

		return sToClean;
	}


	/**
	 * This function will negate the last number before the selection
	 * If expression is empty, add a minus;
	 */
	private void negateLastNumber(){
		String str = expressionToSelection();
		String lastNum = getLastNumb(str);

		int frontLen = str.length() - lastNum.length();
		String endStr = str.substring(0,frontLen);

		//if we had 5+-6, remove the - before the 6
		if(lastNum.matches("[-].*")){
			endStr = endStr + lastNum.substring(1,lastNum.length());
		}
		//add in a minus and the shorten unnecessary signs
		else {
			endStr = endStr + "-" + lastNum;
			endStr = endStr.replace("+-", "-").replace("--", "+");
			markHighlighted(endStr.length() - 1 - lastNum.length());
		}
		setExpression(endStr + expressionAfterSelectionStart());
		setSelection(endStr.length(), endStr.length());
	}



	/**
	 * This function will add a "1/(" before the last number before the selection
	 * If the expression is solved, also solve again
	 * @return an array of indexes that were added
	 */
	private int[] invertLastNumber(){
		String str = expressionToSelection();
		String lastNum = getLastNumb(str);

		int frontLen = str.length() - lastNum.length();
		String inv = "1/(";
		insertAt(inv, frontLen);

		frontLen = frontLen + inv.length();
		String expEnd = getExpression().substring(frontLen, length());
		int lenFirstNum = getFirstNumb(expEnd).length();

		insertAt(")", frontLen + lenFirstNum);

		//move cursor back into the parenthesis if no number was inverted
		if(lenFirstNum == 0)
			setSelection(getSelectionStart()-1, getSelectionEnd()-1);

		//mark indexes of elements added
		return new int[]{frontLen-3, frontLen-2, frontLen-1, frontLen + lenFirstNum};
	}


	/**
	 * Counts the number of open vs. number of closed parentheses in expressionToSelection()
	 * @return 0 if equal num of open/close para, positive # if more open, neg # if more close
	 */
	private int numOpenPara() {
		return numOpenPara(expressionToSelection());
	}

	/**
	 * Counts the number of open vs. number of closed parentheses in the given string
	 * @param str is String containing parenthesis to count
	 * @return 0 if equal num of open/close para, positive # if more open, neg # if more close
	 */
	private static int numOpenPara(String str) {
		int numOpen = 0;
		int numClose = 0;
		for(int i = 0; i < str.length(); i++){
			if (str.charAt(i) == '(')
				numOpen++;
			if (str.charAt(i) == ')')
				numClose++;
		}

		return numOpen - numClose;
	}


	private void highlightMatchingPara(String sKey){
		String open = "(";
		String close = ")";
		int associatedIndex;
		if(sKey.equals(close)){
			//search for backwards for the matching open
			associatedIndex = findMatchingOpenPara(expressionToSelection(),
					  expressionToSelection().lastIndexOf(close));
		}
		else if(sKey.equals(open) || sKey.equals("*" + open)){
			//search for forwards from selection for the matching close
			associatedIndex = findMatchingClosePara(getExpression(),
					  getSelectionStart()-1);
		}
		else
			return;
		if(associatedIndex != -1){
			int[] tmpArray = {getSelectionStart()-1, associatedIndex};
			markHighlighted(tmpArray);
		}
	}

	/**
	 * Mark one character in the expression for highlighting
	 * during the next screen update.
	 * @param index is 0 indexed, so in "74" to highlight 7, pass 0
	 */
	private void markHighlighted(int index){
		int[] tmpArray = {index};
		markHighlighted(tmpArray);
	}

	/**
	 * Mark two characters in the expression for highlighting
	 * during the next screen update.
	 * @param indexArray is 0 indexed, so in "74" to highlight 7, pass 0
	 */
	private void markHighlighted(int[] indexArray){
		clearHighlightedList();

		for(int index : indexArray){
			mHighlightedCharList.add(index);
		}

		Collections.sort(mHighlightedCharList);
	}


	private void setExpression(String tempExp){
		mExpression = tempExp;
	}

	private String getExpression(){
		return mExpression;
	}

	private String expressionToSelection(){
		return getExpression().substring(0, getSelectionStart());
	}

	private String expressionAfterSelectionStart(){
		return getExpression().substring(getSelectionStart(), length());
	}

	/**
	 * Add a String to this expression at the correct selection point
	 * Note if something is highlighted (selStart != selEnd), this
	 * selection will be replaced
	 * @param toAdd the String to add
	 */
	private void insertAtSelection(String toAdd){
		//delete the current highlighted selection (if it exists)
		if(getSelectionStart() != getSelectionEnd()){
			setExpression(getExpression().substring(0, getSelectionStart())
					  + getExpression().substring(getSelectionEnd(), length()));
			//update the selections to reflected deleted highlighted selection
			setSelection(getSelectionStart(), getSelectionStart());
		}
		insertAt(toAdd, getSelectionStart());
	}


	/**
	 * Inserts a string at the insert location and preserves the user's selection
	 * @param toAdd the string to insert
	 * @param insertLocation the location to insert the string. In "42+3", 0 would
	 * specify before the 4, 1 would be before the 2, etc.
	 */
	private void insertAt(String toAdd, int insertLocation){
		//return if we're trying to insert at invalid location
		if(insertLocation > length() | insertLocation < 0)
			return;
		//actually insert text into the expression
		setExpression(getExpression().substring(0, insertLocation) + toAdd
				  + getExpression().substring(insertLocation, length()));
		//move up the selection start if necessary
		int selStart = getSelectionStart();
		int selEnd = getSelectionEnd();
		if(selStart == selEnd && insertLocation <= selStart){
			selStart = selStart + toAdd.length();
			selEnd = selStart;
		}
		else{
			if(insertLocation <= selStart)
				selStart = selStart + toAdd.length();
			if(insertLocation < selEnd)
				selEnd = selEnd + toAdd.length();
		}
		setSelection(selStart, selEnd);
	}


	/** Gets the number after the E in expression (not including + and -) */
	private int lastNumbExponent(){
		//func returns "" if expression empty, and expression if doesn't contain E[+-]?
		if(getExpression().contains("E")){
			String [] strA = getExpression().split("E[+-]?");
			return Integer.parseInt(strA[strA.length - 1]);
		}
		else
			//need to be bigger than intDisplayPrecision so calling func uses toString instead of toPlainString
			return mIntDisplayPrecision + 2;
	}


	/**
	 * Gets the first number (returned as a String) of string
	 * @param str is String in which to find first number
	 * @return anything before the first valid operator, or "" if expression empty,
	 * or entire expression if doesn't contain regexAnyValidOperator (for "-3E-4-5" returns "-3E-4")
	 */
	private static String getFirstNumb(String str){
		String [] strA = str.split("(?<!^|E)" + regexAnyValidOperator);
		return strA[0];
	}

	/**
	 * Gets the first number (returned as a String) at selection in current expression
	 * @return anything before the first valid operator, or "" if expression empty,
	 * or entire expression if doesn't contain regexAnyValidOperator
	 */
	private String getFirstNumb(){
		return getFirstNumb(expressionToSelection());
	}

	/**
	 * Gets the last partial number (returned as a String) at selection in current expression
	 * @return last partial number, invalid or not, or "" if expression empty, or entire expression
	 * if doesn't contain regexAnyValidOperator. Note if expression is "1+-5*" it will return "-5"
	 * This number might be valid (eg "1E3") or invalid, (eg "1E", "1.E-" or "34)")
	 */
	private String getLastPartialNumb(){
		String [] strA = expressionToSelection().split("(?<!^|[E*^/%+])" + regexAnyValidOperator);
		if(strA.length == 0)
			return "";
		else
			return strA[strA.length - 1];
	}


	/**
	 * Gets the last number (returned as a String) for input string
	 * @param str is String expression to find last number of
	 * @return Last valid number in expression, "" If expression empty, or entire
	 * expression if doesn't contain regexAnyValidOperator. For expStr = "1+-5",
	 * return "-5". If expStr = "1-5", return "5"; for "(-45", return "-45", for
	 * "(53)" return ""
	 */
	private static String getLastNumb(String str){
		if (str.matches(".*" + regexGroupedNonNegNumber))
			return str.replaceAll(".*?" + regexGroupedNonNegNumber + "$", "$1");
		return "";
	}
}
