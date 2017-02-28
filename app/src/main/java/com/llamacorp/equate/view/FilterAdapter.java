package com.llamacorp.equate.view;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.llamacorp.equate.R;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Custom adapter class that implements the Filterable interface, allowing it
 * to filter items within it based on a filter string.
 */
class FilterAdapter extends BaseAdapter implements Filterable {
	private ArrayList<UnitSearchItem> mArrayList; // current values post filtering
	private ArrayList<UnitSearchItem> mOriginalValues; // values pre filtering
	private LayoutInflater inflater;

	FilterAdapter(Context context, ArrayList<UnitSearchItem> arrayList) {
		this.mArrayList = arrayList;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	public UnitSearchItem getUnitSearchItem(int position) {
		return mArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null){

			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.filter_dialog_list_row, parent, false);
			holder.mNameTextView = (TextView) convertView.findViewById(R.id.search_dialog_name_textView);
			holder.mAbbreviationTextView = (TextView) convertView.findViewById(R.id.search_dialog_abbreviation_textView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String s = mArrayList.get(position).getUnitName();
		holder.mNameTextView.setText(s);
		holder.mAbbreviationTextView.setText(mArrayList.get(position).getUnitAbbreviation());
		return convertView;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				mArrayList = (ArrayList<UnitSearchItem>) results.values; // has the filtered values
				notifyDataSetChanged();  // notifies the data with new filtered values
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values

				/* Create a hierarchy or order results separated by different sets
				* and then added together at the end.  Note HashSet used to guarantee
				* no duplicate results */
				LinkedHashSet<UnitSearchItem> list1 = new LinkedHashSet<>();
				LinkedHashSet<UnitSearchItem> list2 = new LinkedHashSet<>();
				LinkedHashSet<UnitSearchItem> list3 = new LinkedHashSet<>();
				LinkedHashSet<UnitSearchItem> list4 = new LinkedHashSet<>();
				LinkedHashSet<UnitSearchItem> list5 = new LinkedHashSet<>();

				if (mOriginalValues == null){
					mOriginalValues = new ArrayList<>(mArrayList); // saves the original data in mOriginalValues
				}

				// if constraint is too short:
				if (constraint == null || constraint.length() == 0){
					// set the Original result to return
					results.count = mOriginalValues.size();
					results.values = mOriginalValues;
					// constraint is long enough, proceed:
				} else {
					constraint = constraint.toString().toLowerCase();
					int mTextLength = constraint.length();

					searchItemLoop:
					for (UnitSearchItem item : mOriginalValues) {
						String abbrev = item.getUnitAbbreviation().toLowerCase();
						String longName = item.getUnitName().toLowerCase();
						String data = longName + " " + abbrev;

						// don't proceed if the filter is longer than the item
						if (mTextLength > data.length())
							continue;

						// check if the abbreviation is an exact match
						if (constraint.equals(abbrev)){
							list1.add(item);
							continue;
						}

						// check if the long name is an exact match
						if (constraint.equals(longName)){
							list2.add(item);
							continue;
						}

						// filter results that should appear start with constraint
						if (data.startsWith(constraint.toString())){
							list3.add(item);
							continue;
						}

						// next add results that have multiple words with non-first
						// starting letter
						String dataArray[] = data.split(" ");
						for (String part : dataArray) {
							// remove all brackets
							if (part.replaceAll("\\(|\\)", "")
									  .startsWith(constraint.toString())){
								list4.add(item);
								continue searchItemLoop;
							}
						}

						// finally find matches within words
						if (data.contains(constraint)){
							list5.add(item);
						}
					}

					// join lists preserving hierarchy and guaranteeing uniqueness
					list1.addAll(list2);
					list1.addAll(list3);
					list1.addAll(list4);
					list1.addAll(list5);

					results.count = list1.size();
					results.values = new ArrayList<>(list1);
				}
				return results;
			}
		};
	}


	private class ViewHolder {
		TextView mNameTextView;
		TextView mAbbreviationTextView;
	}
}