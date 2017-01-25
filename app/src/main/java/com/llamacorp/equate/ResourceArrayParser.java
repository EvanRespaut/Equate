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


	public static ArrayList<String> getUnitTypeKeyArray(Resources resources) {
		String[] stringArray = resources.getStringArray(R.array.unit_type_array_combined);
		return getUnitArray(stringArray, UNIT_TYPE_KEY_POS);
	}

	public static ArrayList<String> getUnitTypeNameArray(Resources resources) {
		String[] stringArray = resources.getStringArray(R.array.unit_type_array_combined);
		return getUnitArray(stringArray, UNIT_TYPE_KEY_NAME);
	}

	public static ArrayList<String> getUnitTypeTabNameArray(Resources resources) {
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
