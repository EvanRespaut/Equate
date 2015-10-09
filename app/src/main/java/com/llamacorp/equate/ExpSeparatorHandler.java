package com.llamacorp.equate;

import java.util.ArrayList;


public  class ExpSeparatorHandler {
	private static String THOUS_SEP = ",";

	public ArrayList<Integer> mSepIndexes;


	public ExpSeparatorHandler(){
		mSepIndexes = new ArrayList<>();
	}

	/**
	 * Inserts separators between thousands places in a String representation
	 * of a expression composed of numbers and other characters.  Ignores 
	 * numbers after a decimal point or "E".
	 * @param str String representation of expression to add separators to
	 * @return String with separators added: eg 1000 returns 1,000; 1420.2425+53
	 * returns 1,420.2425+53; 
	 */
	static public String addSep(String str){
		return getSepTextHelper(str, null);
	}
	
	/**
	 * Static method that just removes separators from a given string
	 * @param str is the string with separators
	 * @return the str with separators removed
	 */
	static public String removeSep(String str){
		return str.replace(THOUS_SEP, "");
	}
	
	
	/**
	 * Inserts separators between thousands places in a String representation
	 * of a expression composed of numbers and other characters.  Ignores 
	 * numbers after a decimal point or "E".  Keeps track of each added 
	 * seperator's index.
	 * @param str String representation of expression to add separators to
	 * @return String with separators added: eg 1000 returns 1,000; 1420.2425+53
	 * returns 1,420.2425+53; 
	 */
	public String getSepText(String str){
		return getSepTextHelper(str, mSepIndexes);
	}
	

	static private String getSepTextHelper(String str, ArrayList<Integer> indList){
		if(indList != null) indList.clear();
		final String regNum = "\\d";
		final String regSkipNumsAfter = "[E.]";
		int numCount = 0;

		for(int i = 0; i <= str.length(); i++){
			//check to see if this isn't the last try and we have a number
			if(i < str.length() && str.substring(i,i+1).matches(regNum))
				numCount++;  //we have a number, increment counter
			else {
				//insert commas 
				if(numCount > 3){
					for(int j = numCount%3; j < numCount; j = j + 3){
						if(j == 0) continue;
						int comPos = i - numCount + j;
						str = str.substring(0,comPos) + THOUS_SEP 
								+ str.substring(comPos, str.length());
						if(indList != null) indList.add(comPos);
						i++; //offset for added commas
					}
				}
				//we're at the end of the string and finished inserted commas, leave
				if(i == str.length()) break; 
				//skip over numbers directly following decimals (or E)
				if (str.substring(i,i+1).matches(regSkipNumsAfter)) {
					do {
						i++;         
					} while(i != str.length() && str.substring(i,i+1).matches(regNum));
				}
				numCount = 0;
			}				
		}
		return str;
	}


	/**
	 * Same as translateToSepIndex, except for list of indexes
	 */
	public ArrayList<Integer> translateIndexListToSep(ArrayList<Integer> inList){
		ArrayList<Integer> outList = new ArrayList<>();
		for(int i = 0; i < inList.size(); i++)
			outList.add(i, translateToSepIndex(inList.get(i)));
		return outList;

	}

	/**
	 * Takes and index in the current sequence of numbers (the last call to 
	 * getSepText sets the sequence) without separators and moves it to the 
	 * same location relative to the number's digits (ignoring the 
	 * separators. Eg given index=2 for exp=1,234; outputs 3
	 */
	public int translateToSepIndex(int index){
		if(mSepIndexes == null)
			return index;
		else
			for(int i = 0; i < mSepIndexes.size(); i++)
				if(mSepIndexes.get(i) < index) index++;
		return index;
	}


	/**
	 * Takes and index in the current sequence of numbers (the last call to 
	 * getSepText sets the sequence) with separators and moves it to the 
	 * same location relative to the number's digits (ignoring the 
	 * separators. Eg given index=3 for exp=1,234; outputs 2
	 */
	public int translateFromSepIndex(int index){
		if(mSepIndexes == null)
			return index;

		int numSmaller = 0;
		for(int i = 0; i < mSepIndexes.size(); i++)
			if(mSepIndexes.get(i) < index) numSmaller++;
		return index - numSmaller;
	}

	/**
	 * Take an index and shift it over if it's in an invalid location relative
	 * to the separator. Eg if index=2 in 1,234, move it to 1
	 */
	public int makeIndexValid(int index){
		if(mSepIndexes != null)
			for(int i = 0; i < mSepIndexes.size(); i++)
				if(mSepIndexes.get(i) + 1 == index) index--;
		return index;
	}

}
