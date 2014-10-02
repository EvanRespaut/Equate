package com.llamacorp.unitcalc;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ResultListFragment extends ListFragment {
	//this is for communication with the parent activity
	private OnResultSelectedListener mCallback;
	private List<Result> mResultArray;

	// Container Activity must implement this interface
	public interface OnResultSelectedListener {
		public void updateScreen(boolean updateResult);
		public void selectUnit(Unit unit, int unitTypePos);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		//Make sure container implements callback interface; else, throw exception
		try {
			mCallback = (OnResultSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnResultSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mResultArray = Calculator.getCalculator(getActivity()).getResultList();
		ResultAdapter adapter = new ResultAdapter(mResultArray);
		setListAdapter(adapter);	
	}


	private class ResultAdapter extends ArrayAdapter<Result> {
		public ResultAdapter(List<Result> prevTest){
			super(getActivity(), 0, prevTest);
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			// If we weren't given a view, inflate one
			if (convertView == null)
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_result, null);

			// Configure the view for this result
			Result result = getItem(position);
			
			TextView textViewUnitDesc = (TextView)convertView.findViewById(R.id.list_item_result_convertUnitDesc);	
			TextView textViewUnitTimestamp = (TextView)convertView.findViewById(R.id.list_item_result_currencyTimestamp);
			textViewUnitTimestamp.setVisibility(View.GONE);
			if(result.containsUnits()){
				String text = getResources().getString(R.string.word_Converting) + 
						" " + result.getQuerryUnit().getLowercaseLongName() +
						" " + getResources().getString(R.string.word_to) + 
						" " + result.getAnswerUnit().getLowercaseLongName() + ":";
				textViewUnitDesc.setText(text);
				//ListView reuses old textViewUnitDesc sometimes; make sure old one isn't still invisible
				textViewUnitDesc.setVisibility(View.VISIBLE);
				
				//see if the result was dynamic and therefore has a timestamp to display
				String timestamp = result.getTimestamp();
				if(!timestamp.equals("")){
					textViewUnitTimestamp.setText(timestamp);
					textViewUnitTimestamp.setVisibility(View.VISIBLE);
				}
			}
			else {
				textViewUnitDesc.setVisibility(View.GONE);
			}	

			TextView textViewQuerry = (TextView)convertView.findViewById(R.id.list_item_result_textPrevQuery);	
			setUpResultTextView(textViewQuerry, result.getTextQuerry());

			TextView textViewAnswer = (TextView)convertView.findViewById(R.id.list_item_result_textPrevAnswer);
			setUpResultTextView(textViewAnswer, result.getTextAnswer());

			return convertView; 
		}

		/**
		 * Helper function to reduce repeated code. Sets up the query and answer textViews
		 * @param textView the TextView to setup
		 * @param text the previous query or answer String
		 */
		private void setUpResultTextView(TextView textView, String text){
			//textView.setClickable(true);
			/*
			//want to superscript text after a "^" character
			String [] splitArray = text.split("\\^");
			//only upper-case text if it exists
			if(splitArray.length>1){
				//cut out the "^"
				SpannableString spanText = new SpannableString(splitArray[0] + splitArray[1]);   
				//superscript the portion after the "^"
				spanText.setSpan(new SuperscriptSpan(), splitArray[0].length(), spanText.length(), 0);  
				textView.setText(spanText, BufferType.SPANNABLE);   
			}

			//otherwise just set it normally
			else
			*/
			textView.setText(text);

			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					//get the listView position of this answer/query
					int position = getListView().getPositionForView((View)view.getParent());
					//grab the calc
					Calculator calc = Calculator.getCalculator(getActivity());
					//grab the associated previous expression
					Result thisResult = mResultArray.get(position);


					//get text to pass back to calc
					String textPassBack="";
					int viewID = view.getId();
					if (viewID==R.id.list_item_result_textPrevQuery)
						textPassBack = thisResult.getQuerry();
					if (viewID==R.id.list_item_result_textPrevAnswer)
						textPassBack = thisResult.getAnswer();

					//if unit not selected in calc, and result has unit, set that unit
					if(!calc.isUnitIsSet() && thisResult.containsUnits()){
						Unit unitPassBack;
						if (viewID==R.id.list_item_result_textPrevQuery)
							unitPassBack = thisResult.getQuerryUnit();
						else
							unitPassBack = thisResult.getAnswerUnit();

						//if the selection was a success (and we weren't in the wrong unitType), then set the color
						//int selectedUnitPos = calc.getCurrUnitType().selectUnit(unitPassBack);
						//if(selectedUnitPos != -1)
						mCallback.selectUnit(unitPassBack, thisResult.getUnitTypePos());
					}

					calc.parseKeyPressed(textPassBack);
					mCallback.updateScreen(false);				
				}
			});
			
			textView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override 
				public boolean onLongClick(View view) {
					//get the listView position of this answer/query
					int position = getListView().getPositionForView((View)view.getParent());
					//delete associated previous expression
					mResultArray.remove(position);
					mCallback.updateScreen(true);				
					
					return false;
				}
			});
		}
	}

	

	/**
	 * Gets called by the activity
	 * @param instaScroll 
	 */
	public void refresh(boolean instaScroll) {
		//notify the adapter that the listview needs to be updated
		((ResultAdapter)getListAdapter()).notifyDataSetChanged();	
		//scroll to the bottom of the list
		if(instaScroll)
			getListView().setSelection(getListAdapter().getCount()-1);
		else
			getListView().smoothScrollToPosition(getListAdapter().getCount()-1);
	}
}
