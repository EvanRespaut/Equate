package com.llamacorp.equate.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.llamacorp.equate.Calculator;
import com.llamacorp.equate.R;
import com.llamacorp.equate.unit.UnitHistCurrency;
import com.llamacorp.equate.unit.UnitType;
import com.llamacorp.equate.unit.UnitType.OnConvertKeyUpdateFinishedListener;

import java.util.ArrayList;

public class ConvKeysFragment extends Fragment implements OnConvertKeyUpdateFinishedListener {

	//this is for communication with the parent activity
	OnConvertKeySelectedListener mCallback;

	// Container Activity must implement this interface
	public interface OnConvertKeySelectedListener {
		void updateScreen(boolean updateResult);
		void setEqualButtonColor(boolean unitSet);
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
	//TODO use getPackageName() instead
	private static final String EXTRA_UNIT_TYPE_POS = "com.llamacorp.equate.unit_type_pos";
	private static final int NUM_MORE_FAVORITES = 3;
	private static final int NUM_UNITS_REQUIRED_FOR_FAVORITES = 25;

	//holds UnitType for this fragment aka series of convert buttons
	private UnitType mUnitType;
	private ArrayList<Button> mConvButton;
	
	private Button mMoreButton;

	private int mNumConvButtons;

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
		mUnitType.setDynamicUnitCallback(this);
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

		mConvButton = new ArrayList<>();

		mNumConvButtons =  convertButtonIds.length;

		//if we have more than 10 unit buttons, replace last convert button with a more button
		if(mUnitType.size() > convertButtonIds.length){
			mNumConvButtons =  mNumConvButtons - 1;

			mMoreButton = (Button)v.findViewById(convertButtonIds[mNumConvButtons]);

			mMoreButton.setText(getText(R.string.more_button));
			//button.setTypeface(null, Typeface.ITALIC);
			
			mMoreButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					createMoreUnitsDialog(getText(R.string.select_unit), 
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int item) {
							clickUnitButton(item + mNumConvButtons);
							updateFavorites(item + mNumConvButtons);
						}
					});
				}
			});
		}


		for(int i=0; i < mNumConvButtons; i++) {
			Button button = (Button)v.findViewById(convertButtonIds[i]);

			//add ellipses for long press
			if(mUnitType.size() > mNumConvButtons)
				((SecondaryTextButton)button).setSecondaryText((String) getText(R.string.ellipsis));

			//add to our list of conv buttons
			mConvButton.add(button);			

			//if button is empty, don't create OnClickListener for it
			if(mUnitType.getUnitDisplayName(i).equals(""))
				continue;

			refreshButtonText(i);

			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					int viewId = view.getId();
					for (int i=0; i < mNumConvButtons; i++){
						if(convertButtonIds[i] == viewId){
							//select key
							clickUnitButton(i);
							//don't continue looking through the button array
							break;
						}
					}

				}
			});

			final int buttonPos = i;
			button.setOnLongClickListener(new View.OnLongClickListener() {
				@Override 
				public boolean onLongClick(View view) {
					//if there are less units to display than slots, move on
					if(mUnitType.size() <= mNumConvButtons)
						return true;

					String title = getText(R.string.word_Change) 
							+ " " + mUnitType.getLowercaseGenericLongName(buttonPos) 
							+ " " + getText(R.string.word_to) + ":";

					//pass the title and on item click listener
					createMoreUnitsDialog(title, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int item) {
							mUnitType.swapUnits(buttonPos, item + mNumConvButtons);
							refreshButtonText(buttonPos);
						}
					});
					return false;
				}
			});
		}
		return v;
	}


	/** Used by parent activity to select a unit within this fragment 
	 * @param unitPos the postion of Unit selected */
	public void selectUnitAtUnitArrayPos(int unitPos) {
		//unitPos will be -1 if it wasn't found
		if(unitPos != -1)
			clickUnitButton(mUnitType.findButtonPositionforUnitArrayPos(unitPos));
	}


	/**
	 * Helper function to build a dialog box that list overflow units not shown
	 * on the screen.  Dialog lists has a cancel button.
	 * @param title to display at top of dialog box
	 * @param itemClickListener OnClickListener for when the user selects one of the
	 * units in the dialog list
	 */
	private void createMoreUnitsDialog(CharSequence title, DialogInterface.OnClickListener itemClickListener){
		AlertDialog.Builder builder = new AlertDialog.
				Builder(getActivity());
		builder.setTitle(title);
		builder.setItems(mUnitType.getUndisplayedUnitNames(mNumConvButtons), itemClickListener);
		//null seems to do the same as canceling the dialog
		builder.setNegativeButton(android.R.string.cancel,null);
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void refreshAllButtonsText() {
		for (int i = 0; i < mConvButton.size(); i++) {
			refreshButtonText(i);
		}
	}

	private void refreshButtonText(int buttonPos){
		refreshButtonText("", buttonPos);
	}

	private void refreshButtonText(String textPrefix, int buttonPos){
		//if trying to update historical curr text and button not on screen, move on
		if(buttonPos >= mNumConvButtons)
			return;
		
		String displayText = mUnitType.getUnitDisplayName(buttonPos);
		if(displayText.equals(""))
			return;
		displayText = textPrefix + displayText;

		if(mUnitType.containsDynamicUnits() && mUnitType.isUpdating())
			if(mUnitType.isUnitDynamic(buttonPos) && isAdded())
				displayText = (String) getText(R.string.word_updating);

		Button button = mConvButton.get(buttonPos);

		button.setText(displayText);
		
		//accent the text color of the button
		button.setHovered(!textPrefix.equals(""));
		
		//TODO crude method here, since this is called 10x times and only needs
		//to be called once
		if(mMoreButton != null)
			mMoreButton.setHovered(!textPrefix.equals(""));
	}

	/** Used to pass selected unit to the UnitType model class
	 * @param buttonPos the position in the list of buttons to select */
	private void clickUnitButton(final int buttonPos){
		//pop open selection dialog for historical units
		if(mUnitType.isUnitHistorical(buttonPos)){
			UnitHistCurrency uhc = (UnitHistCurrency) mUnitType.getUnit(buttonPos);
			AlertDialog.Builder builder = new AlertDialog.
					Builder(getActivity());
			builder.setTitle(getText(R.string.historical_dialog_title));
			builder.setSingleChoiceItems(uhc.getPossibleYearsReversed(), uhc.getReversedYearIndex(), 
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					dialog.dismiss();
					UnitHistCurrency uhc = (UnitHistCurrency) mUnitType.getUnit(buttonPos);
					uhc.setYearIndexReversed(item);
					refreshButtonText(buttonPos);
					tryConvert(buttonPos);
				}
			});
			//null seems to do the same as canceling the dialog
			builder.setNegativeButton(android.R.string.cancel,null);
			AlertDialog alert = builder.create();
			alert.show();
		}
		//for historical units, only perform after dialog is gone
		else
			tryConvert(buttonPos);
	}


	private void tryConvert(int buttonPos){
		//Clear color and arrows from previously selected convert buttons
		clearButtonSelection();

		//Set select unit, also this will potentially call convert if we already have a selected unit
		boolean requestConvert = mUnitType.selectUnit(buttonPos);

		Calculator calc = Calculator.getCalculator(getActivity());
		//unit previously selected, we pressed another, do a convert
		if(requestConvert){
			calc.convertFromTo(mUnitType.getPrevUnit(), mUnitType.getCurrUnit());
			clearButtonSelection();
		}
		//unit not previously selected, now select one
		else if(mUnitType.isUnitSelected()){
			//if expression was blank, add a highlighted "1"
			if(calc.isExpressionEmpty()){
				//add in a 1 for user's convenience 
				calc.parseKeyPressed("1");
				//highlight it
				calc.setSelection(0, calc.toString().length());
			}
			colorSelectedButton();
		}

		//always update screen to add/remove unit from expression
		mCallback.updateScreen(true);
	}

	private void colorSelectedButton(){
		//is null when app's onResume calls it (convertkey's onCreate called after activity's onResume)
		if(mUnitType==null)
			return;

		if(mUnitType.isUnitSelected()){
			for(int i=0;i<mConvButton.size();i++){
				if(i != mUnitType.getCurrUnitButtonPos()){
					refreshButtonText(getResources().getString(R.string.convert_arrow), i);
				}
			}
			setSelectedButtonHighlight(true);
		}			
	}

	private void updateFavorites(int clickedButtonPos){
		//only populate a favorite units if we have a min numb items in the dialog
		if(NUM_UNITS_REQUIRED_FOR_FAVORITES > (mUnitType.size()))
			return;

		int indexLastMoreFav = mNumConvButtons + NUM_MORE_FAVORITES - 1;

		mUnitType.swapUnits(clickedButtonPos, indexLastMoreFav);
		mUnitType.rotateUnitSublist(mNumConvButtons, indexLastMoreFav + 1);
		mUnitType.sortUnitSublist(indexLastMoreFav + 1, mUnitType.size());
	}

	/** Clears the button unit selection */
	public void clearButtonSelection(){
		//function may be called before convert key array built, in which case, leave
		if(mConvButton == null) return;

		//remove arrows
		for(int i = 0; i < mConvButton.size(); i++){
			refreshButtonText(i);
		}

		//Clear color from previously selected convert button
		setSelectedButtonHighlight(false);
		//clear the button in the calc
		//	mUnitType.clearUnitSelection();
	}
	
	
	private void setSelectedButtonHighlight(boolean highlighted){
		mCallback.setEqualButtonColor(highlighted);
		//Don't color if "More" button was selected
		if(mUnitType.getCurrUnitButtonPos() < mNumConvButtons){
			int currButtonPos = mUnitType.getCurrUnitButtonPos();
			//if no button is selected, return
			if(currButtonPos == -1)
				return;
			//set the current button to highlighted or not
			mConvButton.get(currButtonPos).setSelected(highlighted);
		}
	}
}
