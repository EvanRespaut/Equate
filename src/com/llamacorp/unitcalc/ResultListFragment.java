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
	OnResultSelectedListener mCallback;

	// Container Activity must implement this interface
	public interface OnResultSelectedListener {
		public void updateScreen(boolean updatePrev);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnResultSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnResultSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ResultAdapter adapter = new ResultAdapter(Calculator.getCalculator(getActivity()).getPrevExpressions());
		setListAdapter(adapter);	
	}

	/*
	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		String result = ((ResultAdapter)getListAdapter()).getItem(position);
		System.out.println("position=" + position);
		System.out.println("result= " + result);
		System.out.println("id= " + id);

		String [] splitPrevExp;
		splitPrevExp = result.split("\\s=\\s");

		if(splitPrevExp.length != 2)
			throw new IllegalArgumentException("In resultlistfragment, splitPrevExpression is improperly formatted..."); 

		String query =  splitPrevExp[0];	
		System.out.println("query= " + query);

		Calculator.getCalculator(getActivity()).parseKeyPressed(query);
		mCallback.updateScreen(false);
	}
	*/
	 

	private class ResultAdapter extends ArrayAdapter<String> {
		public ResultAdapter(List<String> prevTest){
			super(getActivity(), 0, prevTest);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			// If we weren't given a view, inflate one
			if (convertView == null)
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_result, null);

			// Configure the view for this result
			String prevExpression = getItem(position);

			TextView textViewQuerry = (TextView)convertView.findViewById(R.id.list_item_result_textPrevQuerry);	
			setUpResultTextView(textViewQuerry, prevExpression, true);

			TextView textViewAnswer = (TextView)convertView.findViewById(R.id.list_item_result_textPrevAnswer);
			setUpResultTextView(textViewAnswer, prevExpression, false);

			return convertView; 
		}

		/**
		 * Helper function to reduce repeated code. Sets up the query and answer textViews
		 * @param textView the TextView to setup
		 * @param prevExpression the previous query and answer string
		 */
		private void setUpResultTextView(TextView textView, String prevExpression, boolean isQuery){
			textView.setClickable(true);
			textView.setText(splitPrevExpression(prevExpression, isQuery));
			
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					String text = (String) ((TextView)view).getText();
					//((TextView)view).setBackgroundColor(getResources().getColor(R.color.num_button_pressed));
					//pass the textView's value to the calculator
					Calculator.getCalculator(getActivity()).parseKeyPressed(text);
					mCallback.updateScreen(false);				
					}

			});
		}

		/**
		 * Function that splits up the prevExpression string
		 * @param prevExpression is string to split, ie "1+2 = 3"
		 * @param wantQuery is 0 for query, ie "1+2", 1 for answer, ie "3"
		 */
		private String splitPrevExpression(String prevExpression, boolean wantQuery){
			String [] splitPrevExp;
			splitPrevExp = prevExpression.split("\\s=\\s");

			if(splitPrevExp.length != 2)
				throw new IllegalArgumentException("In resultlistfragment, splitPrevExpression is improperly formatted..."); 

			if(wantQuery)
				return splitPrevExp[0];
			else
				return splitPrevExp[1];			
		}
	}

	/**
	 * Gets called by the activity
	 */
	public void refresh() {
		//notify the adapter that the listview needs to be updated
		((ResultAdapter)getListAdapter()).notifyDataSetChanged();	
		//scroll to the bottom of the list
		getListView().smoothScrollToPosition(getListAdapter().getCount()-1);
		//getListView().setSelection(getListAdapter().getCount()-1);
	}
}
