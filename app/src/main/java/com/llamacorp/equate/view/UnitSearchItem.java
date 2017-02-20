package com.llamacorp.equate.view;


/**
 * Class used to contain in backend info for a single row of the searchable
 * list dialog.
 */
public class UnitSearchItem {
	private String mUnitTypeKey;
	private String mUnitName;
	private String mUnitAbbreviation;
	private int mUnitPosition;

	public UnitSearchItem(String unitTypeKey, String unitName,
								 String unitAbbreviation, int unitPosition) {
		this.mUnitTypeKey = unitTypeKey;
		this.mUnitName = unitName;
		this.mUnitAbbreviation = unitAbbreviation;
		this.mUnitPosition = unitPosition;
	}

	public String getUnitTypeKey() {
		return mUnitTypeKey;
	}

	public String getUnitName() {
		return mUnitName;
	}

	public String getUnitAbbreviation() {
		return mUnitAbbreviation;
	}

	public int getUnitPosition() {
		return mUnitPosition;
	}
}