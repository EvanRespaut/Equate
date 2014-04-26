package com.llamacorp.unitcalc;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
			button.setText(mUnitType.getUnitDisplayName(i));

			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					for (int i=0; i<convertButtonIds.length; i++){
						if(convertButtonIds[i] == view.getId()){
							//Clear color from previously selected convert button
							Button prevSelected = mConvButton.get(mUnitType.getCurrUnitPos());
							prevSelected.setSelected(false);	
							//Set select unit, also this will potentially call convert if we already have a selected unit
							boolean didConvert = mUnitType.selectUnit(i);

							//if conversion performed, update screen
							if(didConvert)
								mCallback.updateScreen(true);
							
							//Add color to newly selected convert button
							view.setSelected(true);
							break;
						}
					}
				}
			});
			//add to our list of conv buttons
			mConvButton.add(button);			
		}

		return v;
	}

	
	public void clearKeySelection(){
		//Clear color from previously selected convert button
		Button prevSelected = mConvButton.get(mUnitType.getCurrUnitPos());
		prevSelected.setSelected(false);	
	}

}
