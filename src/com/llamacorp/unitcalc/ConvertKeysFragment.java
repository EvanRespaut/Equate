package com.llamacorp.unitcalc;

import java.util.ArrayList;

import com.llamacorp.unitcalc.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView.BufferType;

public class ConvertKeysFragment extends Fragment {
	//this is for communication with the parent activity
	OnConvertKeySelectedListener mCallback;

	// Container Activity must implement this interface
	public interface OnConvertKeySelectedListener {
		public void updateScreen(boolean updatePrev);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnConvertKeySelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnConvertKeySelectedListener");
		}
	}

	//used for the extra
	private static final String EXTRA_UNIT_TYPE_POS = "com.llamacorp.unitcalc.unit_type_pos";
	//holds UnitType for this fragment aka series of convert buttons
	private UnitType mUnitType;
	private ArrayList<Button> mConvButton;

	private int[] convertButtonIds = {	
			R.id.convert_button1,
			R.id.convert_button2,
			R.id.convert_button3,
			R.id.convert_button4,
			R.id.convert_button5,
			R.id.convert_button6,
			R.id.convert_button7,
			R.id.convert_button8,
			R.id.convert_button9,
			R.id.convert_button10};


	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		int pos = getArguments().getInt(EXTRA_UNIT_TYPE_POS);

		mUnitType = Calculator.getCalculator(getActivity()).getUnitType(pos);
	}

	public static ConvertKeysFragment newInstance(int unitTypePos){
		Bundle args = new Bundle();
		args.putInt(EXTRA_UNIT_TYPE_POS, unitTypePos);

		ConvertKeysFragment fragment = new ConvertKeysFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_convert_keys, parent, false);

		mConvButton = new ArrayList<Button>();

		for(int i = 0; i < mUnitType.size(); i++) {
			Button button = (Button)v.findViewById(convertButtonIds[i]);

			//add to our list of conv buttons
			mConvButton.add(button);			

			
			String displayText = mUnitType.getUnitDisplayName(i);
			
			//if button is empty, don't create OnClickListener for it
			if(displayText.equals(""))
				continue;
			
			//want to superscript text after a "^" character
			String [] splitArray = displayText.split("\\^");
			//only upper-case text if it exists
			if(splitArray.length>1){
				//cut out the "^"
				SpannableString spanText = new SpannableString(splitArray[0] + splitArray[1]);   
				//superscript the portion after the "^"
				spanText.setSpan(new SuperscriptSpan(), splitArray[0].length(), spanText.length(), 0);  
				button.setText(spanText, BufferType.SPANNABLE);   
			}
			//otherwise just set it normally
			else
				button.setText(displayText);


			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					int viewId = view.getId();
					for (int i=0; i<convertButtonIds.length; i++){
						if(convertButtonIds[i] == viewId){
							//select key
							clickUnitButton(i);
							//don't continue looking through the button array
							break;
						}
					}
				}
			});
		}
		//this is called to refreshed the selected button after app closes and comes back
		colorSelectedButton();

		return v;
	}


	/** Used by parent activity to select a unit within this fragment 
	 * @param unit the Unit selected */
	public void selectUnit(Unit unit) {
		int unitPos = mUnitType.getUnitPosition(unit);
		//unitPos will be -1 if it wasn't found
		if(unitPos != -1)
			clickUnitButton(unitPos);
	}

	/** Used to pass selected unit to the UnitType model class
	 * @param buttonPos the position in the list of buttons to select */
	private void clickUnitButton(int buttonPos){
		//Clear color from previously selected convert button
		clearButtonSelection();
		//Set select unit, also this will potentially call convert if we already have a selected unit
		boolean didConvert = mUnitType.selectUnit(buttonPos);

		//if conversion performed, update screen
		if(didConvert)
			mCallback.updateScreen(true);
		
		colorSelectedButton();
	}
	
	public void colorSelectedButton(){
		//is null when app's onResume calls it (convertkey's onCreate called after activity's onResume)
		if(mUnitType==null){
			System.out.println("mUnitType is null");
			return;
		}
		//If new unit still selected, add color to newly selected convert button
		if(mUnitType.isUnitSelected())
			mConvButton.get(mUnitType.getCurrUnitPos()).setSelected(true);			
	}

	public void clearButtonSelection(){
		//Clear color from previously selected convert button
		Button prevSelected = mConvButton.get(mUnitType.getCurrUnitPos());
		prevSelected.setSelected(false);	
	}
}
