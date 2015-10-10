package com.llamacorp.equate.view;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.llamacorp.equate.Calculator;
import com.llamacorp.equate.Result;
import com.llamacorp.equate.R;

public class ResultListFragment extends ListFragment {
	//this is for communication with the parent activity
	private OnResultSelectedListener mCallback;
	private List<Result> mResultArray;

	// Container Activity must implement this interface
	public interface OnResultSelectedListener {
		public void updateScreen(boolean updateResult);
		public void selectUnitAtUnitArrayPos(int unitPos, int unitTypePos);
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
				convertView = getActivity().getLayoutInflater().
					inflate(R.layout.list_item_result, parent, false);

			// Configure the view for this result
			Result result = getItem(position);

			TextView textViewUnitDesc = (TextView)convertView.
					findViewById(R.id.list_item_result_convertUnitDesc);
			TextView textViewUnitTimestamp = (TextView)convertView.
					findViewById(R.id.list_item_result_currencyTimestamp);
			textViewUnitTimestamp.setVisibility(View.GONE);
			if(result.containsUnits()){
				String text = getResources().getString(R.string.word_Converting) +
						" " + result.getQueryUnitTextLong() +
						" " + getResources().getString(R.string.word_to) +
						" " + result.getAnswerUnitTextLong() + ":";
				textViewUnitDesc.setText(Html.fromHtml("<i>" + text + "</i>"));
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

			TextView textViewQuery = (TextView)convertView.
					findViewById(R.id.list_item_result_textPrevQuery);
			setUpResultTextView(textViewQuery, result.getTextQuery());

			TextView textViewAnswer = (TextView)convertView.
					findViewById(R.id.list_item_result_textPrevAnswer);
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
               //error case
               if(view == null){
                  Toast toast = Toast.makeText(getActivity(), "ERROR: onClick parameter view is null", Toast.LENGTH_LONG);
                  toast.show();
                  return;
               }

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
						textPassBack = thisResult.getQueryWithoutSep();
					if (viewID==R.id.list_item_result_textPrevAnswer)
						textPassBack = thisResult.getAnswerWithoutSep();

					calc.parseKeyPressed(textPassBack);

					//if unit not selected in calc, and result has unit, set that unit
					if(!calc.isUnitSelected() && thisResult.containsUnits()){
						int unitPosPassBack;
						if (viewID==R.id.list_item_result_textPrevQuery)
                     unitPosPassBack = thisResult.getQueryUnitPos();
						else
                     unitPosPassBack = thisResult.getAnswerUnitPos();

						//if the selection was a success (and we weren't in the wrong unitType), then set the color
						//int selectedUnitPos = calc.getCurrUnitType().selectUnitAtUnitArrayPos(unitPassBack);
						//if(selectedUnitPos != -1)
						mCallback.selectUnitAtUnitArrayPos(unitPosPassBack, thisResult.getUnitTypePos());
					}

					mCallback.updateScreen(false);
				}
			});

			textView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
               Toast toast = Toast.makeText(view.getContext(), "Result Deleted", Toast.LENGTH_SHORT);
               toast.setGravity(Gravity.CENTER, 0, 0);
               toast.show();

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
	 * Update the listView and scroll to the bottom
	 * @param instaScroll if false, use animated scroll to bottom,
	 * otherwise use scroll instantly
	 */
	public void refresh(boolean instaScroll) {
		//notify the adapter that the listview needs to be updated
		((ResultAdapter)getListAdapter()).notifyDataSetChanged();

		//scroll to the bottom of the list
		if(instaScroll){
			//post a runnable for setSelection otherwise it won't be called
			getListView().post(new Runnable() {
		        @Override
		        public void run() {
                     getListView().setSelection(getListAdapter().getCount()-1);
                 }
		    });
		}
		else
			getListView().smoothScrollToPosition(getListAdapter().getCount()-1);
	}
}
