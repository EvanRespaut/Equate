package com.llamacorp.equate.unit;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Abstract class, note that child class must implement a function to do raw
 * number conversion
 */
public class UnitType {
	private static final String JSON_NAME = "name";
	private static final String JSON_UNIT_DISP_ORDER = "unit_disp_order";
	private static final String JSON_CURR_UNIT_POS_IN_ARRAY = "pos";
	private static final String JSON_IS_SELECTED = "selected";
	private static final String JSON_UNIT_ARRAY = "unit_array";
	private static final String JSON_UPDATE_TIME = "update_time";

	private String mName;
	private ArrayList<Unit> mUnitArray;
	private Unit mPrevUnit;
	private Unit mCurrUnit;
	private boolean mIsUnitSelected;
	private boolean mContainsDynamicUnits = false;
   //Order to display units (based on mUnitArray index
   private ArrayList<Integer> mUnitDisplayOrder;

	private String mXMLCurrencyURL;
	private boolean mUpdating = false;
	private Date mLastUpdateTime;


	//this is for communication with fragment hosting convert keys
	OnConvertKeyUpdateFinishedListener mCallback;

	public interface OnConvertKeyUpdateFinishedListener {
		void refreshAllButtonsText();
	}


	/** Default constructor used by UnitType Initializer   */
	public UnitType(String name){
		mName = name;
		mUnitArray = new ArrayList<>();
      mUnitDisplayOrder = new ArrayList<>();
		mIsUnitSelected = false;
		mUpdating = false;
		mLastUpdateTime = new GregorianCalendar(2015,3,1,1,11).getTime();
	}

	public UnitType(String name, String URL) {
		this(name);
		mXMLCurrencyURL = URL;
	}

   /**
    * Takes JSON object and loads out user saved info, such as currently
    * selected unit and unit display order
    * @param json is the JSON object that contains save data of UnitType
    *             to load.
    */
	public void loadJSON(JSONObject json) throws JSONException {
		//Check to make we have the right saved JSON, else leave
      if(!getUnitTypeName().equals(json.getString(JSON_NAME)))
         return;

		//load in saved data from Units (currency values and update times)
		//note that no other type of unit is loaded
      if(containsDynamicUnits()) {
         JSONArray jUnitArray = json.getJSONArray(JSON_UNIT_ARRAY);
         for (int i = 0; i < jUnitArray.length(); i++) {
            getUnit(i).loadJSON(jUnitArray.getJSONObject(i));
         }
      }

		mCurrUnit = getUnitPosInUnitArray(json.getInt(JSON_CURR_UNIT_POS_IN_ARRAY));
		mIsUnitSelected = json.getBoolean(JSON_IS_SELECTED);
		setLastUpdateTime(new Date(json.getLong(JSON_UPDATE_TIME)));

      JSONArray jUnitDisOrder = json.getJSONArray(JSON_UNIT_DISP_ORDER);
      mUnitDisplayOrder.clear();
      for (int i = 0; i < jUnitDisOrder.length(); i++) {
         mUnitDisplayOrder.add(jUnitDisOrder.getInt(i));
      }
      //fill in the remaining if missing (if we added a unit)
      fillUnitDisplayOrder();
	}

   /**
    * Save the state of this UnitType into a JSON object for later use
    * @return JSON object that contains this object
    */
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();

      //should only be saving data from Currency unit type
      if(containsDynamicUnits()) {
         JSONArray jUnitArray = new JSONArray();
         for (Unit unit : mUnitArray)
            jUnitArray.put(unit.toJSON());
         json.put(JSON_UNIT_ARRAY, jUnitArray);
      }

      //used to identify this UnitType from others
		json.put(JSON_NAME, mName);
		json.put(JSON_CURR_UNIT_POS_IN_ARRAY, findUnitPosInUnitArray(mCurrUnit));
		json.put(JSON_IS_SELECTED, mIsUnitSelected);
		json.put(JSON_UPDATE_TIME, getLastUpdateTime().getTime());


		JSONArray jUnitDisOrder = new JSONArray();
		for (Integer i : mUnitDisplayOrder)
			jUnitDisOrder.put(i);
		json.put(JSON_UNIT_DISP_ORDER, jUnitDisOrder);

      return json;
	}


	/** Used to build a UnitType after it has been created */
	public void addUnit(Unit u){
		mUnitArray.add(u);
      //0th element is 0, 1st is 1, etc
      mUnitDisplayOrder.add(mUnitDisplayOrder.size());
		//if there was already a dynamic unit or this one is, UnitType still contains dynamic units
		if(mContainsDynamicUnits || u.isDynamic()) mContainsDynamicUnits = true;
	}

	/** Swap positions of units */
   public void swapUnits(int pos1, int pos2){
		Collections.swap(mUnitDisplayOrder, pos1, pos2);
	}

	/**
	 * Rotates the units in the display order such that the unit at toIndex - 1
	 * now has position fromIndex, the unit at fromIndex has position fromIndex +
	 * 1, etc.
	 * @param fromIndex is inclusive
	 * @param toIndex is exclusive
	 */
	public void rotateUnitSublist(int fromIndex, int toIndex){
		Collections.rotate(mUnitDisplayOrder.subList(fromIndex, toIndex), 1);
	}


	/**
	 * Sorts a sublist of units in display order by name
	 * @param fromIndex is inclusive
	 * @param toIndex is exclusive
	 */
	public void sortUnitSublist(int fromIndex, int toIndex){
		Collections.sort(mUnitDisplayOrder.subList(fromIndex, toIndex),
				  new Comparator<Integer>() {
					  public int compare(Integer s1, Integer s2) {
						  return mUnitArray.get(s1).getLongName()
									 .compareTo(mUnitArray.get(s2).getLongName());
					  }
				  });
	}

	/**
	 * Find the position of the unit in the current unit button order
	 * @return -1 if selection failed, otherwise the position of the unit
	 */
	public int findUnitButtonPosition(Unit unit){
		if (unit == null)
			return - 1;
		for(int i=0;i<size();i++){
			if(unit.equals(getUnit(i)))
				return i; //found the unit
		}
		return -1;  //if we didn't find the unit
	}


	public int findUnitPosInUnitArray(Unit unit){
		if(unit == null) return -1;
		for(int i=0;i<size();i++){
			if(unit.equals(getUnitPosInUnitArray(i)))
				return i; //found the unit
		}
		return -1;  //if we didn't find the unit
	}

	public int findButtonPositionforUnitArrayPos(int pos) {
		return mUnitDisplayOrder.indexOf(new Integer(pos));
	}

	/**
	 * If mCurrUnit not set, set mCurrUnit
	 * If mCurrUnit already set, call functions to perform a convert
	 * @return returns if a conversion is requested
	 */
	public boolean selectUnit(int clickedButPos) {
		Unit unitPressed = getUnit(clickedButPos);

		//used to tell caller if we needed to do a conversion
		boolean requestConvert = false;
		//If we've already selected a unit, do conversion
		if (mIsUnitSelected){
			//if the unit is the same as before, de-select it
			if (getCurrUnitButtonPos() == clickedButPos){
				//if historical unit, allow selection again (for a different year)
				if (!unitPressed.isHistorical()){
					mIsUnitSelected = false;
					return false;
				}
			}
			mPrevUnit = mCurrUnit;
			requestConvert = true;
		}

		//Select new unit regardless
		mCurrUnit = unitPressed;
		//Engage set flag
		mIsUnitSelected = true;
		return requestConvert;
	}

	/**
	 * Update values of units that are not static (currency) via
	 * each unit's own HTTP/JSON api call. Note that this refresh
	 * is asynchronous and will only happen sometime in the future
	 * Internet connection permitting.
	 */
	public void refreshDynamicUnits(Context c, boolean forced){
		if(containsDynamicUnits()){
			UnitTypeUpdater utp = new UnitTypeUpdater(c);
			utp.update(this, forced);
		}
	}



	/**
	 * Check to see if this UnitType holds any units that have values that
	 * need to be refreshed via the Internet
	 */
	public boolean containsDynamicUnits(){
		return mContainsDynamicUnits;
	}

//	/** Check to see if unit at position pos is currently updating */
//	public boolean isUnitUpdating(int pos){
//		//TODO have this return true if isUpdating is true, otherwise do the following
//		//TODO also make conv keys reflect this change (so all will show updating)
//		//TODO but once yahoo xml update is finished, individuals will show updating
//		if(getUnit(pos).isDynamic())
//			return ((UnitCurrency)getUnit(pos)).isUpdating();
//		else
//			return false;
//	}

	/** Check to see if unit at position pos is dynamic */
	public boolean isUnitDynamic(int pos){
		return getUnit(pos).isDynamic();
	}

	public void setDynamicUnitCallback(OnConvertKeyUpdateFinishedListener callback) {
		if(containsDynamicUnits()) {
			mCallback = callback;
//			for (int i = 0; i < size(); i++)
//				if (getUnit(i).isDynamic())
//					((UnitCurrency) getUnit(i)).setCallback(mCallback);
		}
	}

	/** Check to see if unit at position pos is dynamic */
	public boolean isUnitHistorical(int pos){
		return getUnit(pos).isHistorical();
	}


	/** Resets mIsUnitSelected flag */
	public void clearUnitSelection(){
		mIsUnitSelected = false;
	}

	public boolean isUnitSelected(){
		return mIsUnitSelected;
	}

	public String getUnitTypeName(){
		return mName;
	}

	/**
	 * @param pos is index of Unit in the mUnitArray list
	 * @return String name to be displayed on convert button
	 */
	public String getUnitDisplayName(int pos){
		return getUnit(pos).toString();
	}

	public String getLowercaseGenericLongName(int pos){
		return getUnit(pos).getLowercaseGenericLongName();
	}

	/** Method builds charSequence array of long names of undisplayed units
	 * @param numDispUnits is Array of long names of units not being displayed
	 * @return Number of units being displayed, used to find undisplayed units
	 */
	public CharSequence[] getUndisplayedUnitNames(int numDispUnits){
		//ArrayList<Unit> subList = mUnitArray.subList(numDispUnits, size());
		//return subList.toArray(new CharSequence[subLists.size()]);
		int arraySize = size() - numDispUnits;
		CharSequence[] cs = new CharSequence[arraySize];
		for(int i=0;i<arraySize;i++){
			cs[i] = getUnit(numDispUnits+i).getGenericLongName();
		}
		return cs;
	}

   /**
    * Used to get the Unit at a given position.  Note that the position is the
    * user defined order for buttons. Uses a mask to convert displayed position
    * into real array position.
    * @param buttonPos Position of unit to retrieve (user defined order)
    * @return Unit at the given position
    */
	public Unit getUnit(int buttonPos){
      //If a unit is added
      if(mUnitDisplayOrder.size() < size())
         fillUnitDisplayOrder();
      //if somehow there are more UnitDisplayOrder (unit deleted), don't want
      //to address nonexistent element
      if(mUnitDisplayOrder.size() > size())
         resetUnitDipplayOrder();

      return mUnitArray.get(mUnitDisplayOrder.get(buttonPos));
	}

	/**
	 * Get the unit given a position of the original mUnitArray.  This does not
	 * use the mUnitDisplayOrder like getUnit uses. Will return null if pos is
	 * invalid (less than 0 or greater than mUnitArray size)
	 * @param pos position of unit in mUnitArray
	 * @return unit from mUnitArray
	 */
	public Unit getUnitPosInUnitArray(int pos) {
		if(pos < 0 || pos >= mUnitArray.size()) return null;
		return mUnitArray.get(pos);
	}

   /**
    * Populate the UnitDisplayOrder array.  Will fill if empty or top off if
    * it has less elements than UnitArray
    */
   private void fillUnitDisplayOrder(){
      for(int i = mUnitDisplayOrder.size(); i < size(); i++)
         mUnitDisplayOrder.add(mUnitDisplayOrder.size());
   }

   private void resetUnitDipplayOrder(){
      mUnitDisplayOrder.clear();
      fillUnitDisplayOrder();
   }


	public String getXMLCurrencyURL() {
		return mXMLCurrencyURL;
	}


	public Unit getPrevUnit(){
		return mPrevUnit;
	}

	public Unit getCurrUnit(){
		return mCurrUnit;
	}

   /**
    * Get the number of Units in this UnitType
    * @return integer of number of Units in this UnitType
    */
	public int size() {
		return mUnitArray.size();
	}

	public int getCurrUnitButtonPos() {
		return findUnitButtonPosition(mCurrUnit);
	}

	public void setUpdating(boolean updating) {
		mUpdating = updating;
		//refresh text
		if(mCallback != null)
			mCallback.refreshAllButtonsText();
	}

	public boolean isUpdating() {
		return mUpdating;
	}


	public Date getLastUpdateTime() {
		return mLastUpdateTime;
	}

	public void setLastUpdateTime(Date mLastUpdateTime) {
		this.mLastUpdateTime = mLastUpdateTime;
	}
}