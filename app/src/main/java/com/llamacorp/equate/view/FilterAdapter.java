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
import java.util.List;

/**
 * Custom adapter class that implements the Filterable interface, allowing it
 * to filter items within it based on a filter string.
 */
class FilterAdapter extends BaseAdapter implements Filterable {
	private List<String> arrayList; // current values post filtering
	private List<String> mOriginalValues; // values pre filtering
	private LayoutInflater inflater;

	FilterAdapter(Context context, List<String> arrayList) {
		this.arrayList = arrayList;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return arrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
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
			holder.textView = (TextView) convertView.findViewById(R.id.country_name_textView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.textView.setText(arrayList.get(position));
		return convertView;
	}

	public List<String> getArrayList() {
		return arrayList;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				arrayList = (List<String>) results.values; // has the filtered values
				notifyDataSetChanged();  // notifies the data with new filtered values
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values

				/* Create a hierarchy or order results separated by different sets
				* and then added together at the end.  Note HashSet used to guarantee
				* no duplicate results */
				LinkedHashSet<String> mainList = new LinkedHashSet<>();
				LinkedHashSet<String> secondaryList = new LinkedHashSet<>();
				LinkedHashSet<String> tertiaryList = new LinkedHashSet<>();

				if (mOriginalValues == null){
					mOriginalValues = new ArrayList<>(arrayList); // saves the original data in mOriginalValues
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

					mainLoop:
					for (String data : mOriginalValues) {
						if (mTextLength > data.length())
							continue;
						// first results that should appear start with the filter
						if (data.toLowerCase().startsWith(constraint.toString())){
							mainList.add(data);
							continue;
						}
						// next add results that have multiple words with non-first
						// starting letter
						if (data.contains(" ")){
							String dataArray[] = data.toLowerCase().split(" ");
							for (String part : dataArray) {
								if (part.replaceAll("\\(|\\)", "")
										  .startsWith(constraint.toString())){
									secondaryList.add(data);
									continue mainLoop;
								}
							}
						}
						// finally find matches within words
						if (data.toLowerCase().contains(constraint)){
							tertiaryList.add(data);
						}
					}

					// join lists preserving hierarchy and guaranteeing uniqueness
					mainList.addAll(secondaryList);
					mainList.addAll(tertiaryList);

					results.count = mainList.size();
					results.values = new ArrayList<>(mainList);
				}
				return results;
			}
		};
	}

	private class ViewHolder {
		TextView textView;
	}
}