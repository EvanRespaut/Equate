package com.llamacorp.equate;

import android.content.res.Resources;

import java.util.ArrayList;

/**
 * Created by Evan on 1/24/2017.
 */

public class ResourceArrayParser {
	private final static int UNIT_TYPE_KEY_POS = 0;
	private final static int UNIT_TYPE_KEY_NAME = 1;
	private final static int UNIT_TYPE_KEY_TAB_NAME = 2;

	/**
	 * Used to translate between full unit type names ("Temperature") to names
	 * that appear in the Unit Type tab view ("Temp").
	 * @param toTranslate is a list of full unit type names to translate
	 * @return a list of the translated tab names
    */
	public static ArrayList<String> getTabNamesFromNames(ArrayList<String> toTranslate, Resources resources) {
		ArrayList<String> returnList = new ArrayList<>();
		ArrayList<String> tabNames = getUnitTypeTabNameArrayList(resources);
		ArrayList<String> unitNames = getUnitTypeNameArrayList(resources);

		for (String s : toTranslate) {
			returnList.add(tabNames.get(unitNames.indexOf(s)));
		}
		return returnList;
	}

	public static String[] getUnitTypeKeyArray(Resources resources) {
		ArrayList<String> al = getUnitTypeKeyArrayList(resources);
		return al.toArray(new String[al.size()]);
	}


	public static String[] getUnitTypeNameArray(Resources resources) {
		ArrayList<String> al = getUnitTypeNameArrayList(resources);
		return al.toArray(new String[al.size()]);
	}


	public static ArrayList<String> getUnitTypeKeyArrayList(Resources resources) {
		String[] stringArray = resources.getStringArray(R.array.unit_type_array_combined);
		return getUnitArray(stringArray, UNIT_TYPE_KEY_POS);
	}


	public static ArrayList<String> getUnitTypeNameArrayList(Resources resources) {
		String[] stringArray = resources.getStringArray(R.array.unit_type_array_combined);
		return getUnitArray(stringArray, UNIT_TYPE_KEY_NAME);
	}

	public static ArrayList<String> getUnitTypeTabNameArrayList(Resources resources) {
		String[] stringArray = resources.getStringArray(R.array.unit_type_array_combined);
		return getUnitArray(stringArray, UNIT_TYPE_KEY_TAB_NAME);
	}

	private static ArrayList<String> getUnitArray(String[] stringArray, int dataIndex) {
		ArrayList<String> outList = new ArrayList<>();
		for (String s : stringArray) {
			String[] splitStr = s.split("\\|");
			outList.add(splitStr[dataIndex].trim());
		}
		return outList;
	}
}
