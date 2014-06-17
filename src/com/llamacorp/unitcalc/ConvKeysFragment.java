package com.llamacorp.unitcalc;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class ConvKeysFragment extends Fragment {

	//this is for communication with the parent activity
	OnConvertKeySelectedListener mCallback;

	// Container Activity must implement this interface
	public interface OnConvertKeySelectedListener {
		public void updateScreen(boolean updateResult);
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


	private Toast mConvertToast;


	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		int pos = getArguments().getInt(EXTRA_UNIT_TYPE_POS);

		mUnitType = Calculator.getCalculator(getActivity()).getUnitType(pos);
	}

	@Override
	public void onResume(){
		super.onResume();
		colorSelectedButton();
	}

	public static ConvKeysFragment newInstance(int unitTypePos){
		Bundle args = new Bundle();
		args.putInt(EXTRA_UNIT_TYPE_POS, unitTypePos);

		ConvKeysFragment fragment = new ConvKeysFragment();
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

		return v;
	}


	/** Used by parent activity to select a unit within this fragment 
	 * @param unit the Unit selected */
	public void selectUnit(Unit unit) {
		int unitPos = mUnitType.findUnitPosition(unit);
		//unitPos will be -1 if it wasn't found
		if(unitPos != -1)
			clickUnitButton(unitPos);
	}

	/** Used to pass selected unit to the UnitType model class
	 * @param buttonPos the position in the list of buttons to select */
	private void clickUnitButton(int buttonPos){
		Unit oldUnit = mUnitType.getSelectedUnit();
		//Clear color from previously selected convert button
		clearButtonSelection();
		//Set select unit, also this will potentially call convert if we already have a selected unit
		boolean didConvert = mUnitType.selectUnit(buttonPos);
		
		Calculator calc = Calculator.getCalculator(getActivity());
		//for first time users
		if(!calc.mHints.isHasClickedUnit()){
			Builder dialog = new AlertDialog.Builder(getActivity())
			.setPositiveButton(android.R.string.ok,null);

			if(calc.isExpressionEmpty()){
//				dialog.setTitle(R.string.first_convert_title_no_numbers);
				dialog.setMessage(R.string.first_convert_message_no_numbers);
			}
			else
				dialog.setMessage(R.string.first_convert_message);
			dialog.show();
			calc.mHints.setHasClickedUnitTrue();
		}


		//if conversion performed, update screen
		if(didConvert){
			//cancel previous toast if it's there
			if(mConvertToast!=null) 
				mConvertToast.cancel();
			Unit newUnit = mUnitType.getSelectedUnit();
			String text;
			if(calc.isExpressionEmpty())
				text = getText(R.string.convert_toast_no_numbers).toString();
			else
				text = getText(R.string.convert_toast_converting) 
					+ " " + oldUnit.getLongName() 
					+ " " + getText(R.string.convert_toast_to) 
					+ " " + newUnit.getLongName();
			mConvertToast = Toast.makeText((Context)mCallback, text, Toast.LENGTH_SHORT);
			mConvertToast.show();
		}

		//always update screen to add/remove unit from expression
		mCallback.updateScreen(true);

		colorSelectedButton();
	}

	public void colorSelectedButton(){
		//is null when app's onResume calls it (convertkey's onCreate called after activity's onResume)
		if(mUnitType==null)
			return;

		//If new unit still selected, add color to newly selected convert button
		if(mUnitType.isUnitSelected())
			mConvButton.get(mUnitType.getCurrUnitPos()).setSelected(true);			
	}

	/** Clears the button unit selection */
	public void clearButtonSelection(){
		//function may be called before convert key array built, in which case, leave
		if(mConvButton == null) return;
		//Clear color from previously selected convert button
		Button prevSelected = mConvButton.get(mUnitType.getCurrUnitPos());
		prevSelected.setSelected(false);	
		//clear the button in the calc
		//	mUnitType.clearUnitSelection();
	}
}
