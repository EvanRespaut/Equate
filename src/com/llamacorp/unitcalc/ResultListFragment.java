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


	private class ResultAdapter extends ArrayAdapter<PrevExpression> {
		public ResultAdapter(List<PrevExpression> prevTest){
			super(getActivity(), 0, prevTest);
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			// If we weren't given a view, inflate one
			if (convertView == null)
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_result, null);

			// Configure the view for this result
			PrevExpression prevExp = getItem(position);

			TextView textViewQuerry = (TextView)convertView.findViewById(R.id.list_item_result_textPrevQuerry);	
			setUpResultTextView(textViewQuerry, prevExp.getTextQuerry());

			TextView textViewAnswer = (TextView)convertView.findViewById(R.id.list_item_result_textPrevAnswer);
			setUpResultTextView(textViewAnswer, prevExp.getTextAnswer());

			return convertView; 
		}

		/**
		 * Helper function to reduce repeated code. Sets up the query and answer textViews
		 * @param textView the TextView to setup
		 * @param text the previous query or answer String
		 */
		private void setUpResultTextView(TextView textView, String text){
			textView.setClickable(true);
			textView.setText(text);

			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					//get the listView position of this answer/query
					int position = getListView().getPositionForView((View)view.getParent());
					//grab the associated previous expression
					PrevExpression thisPrevExp = Calculator.getCalculator(getActivity()).getPrevExpressions().get(position);
					//get text to pass back to calc
					String passBack="";
					if (view.getId()==R.id.list_item_result_textPrevQuerry)
						passBack = thisPrevExp.getQuerry();
					if (view.getId()==R.id.list_item_result_textPrevAnswer)
						passBack = thisPrevExp.getAnswer();
					Calculator.getCalculator(getActivity()).parseKeyPressed(passBack);
					
					//now set up the proper unit
					//TODO
					
					mCallback.updateScreen(false);				
				}

			});
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
