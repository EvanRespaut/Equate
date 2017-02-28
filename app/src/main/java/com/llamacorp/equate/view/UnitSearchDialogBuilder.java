package com.llamacorp.equate.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.llamacorp.equate.R;
import com.llamacorp.equate.unit.Unit;
import com.llamacorp.equate.unit.UnitType;
import com.llamacorp.equate.unit.UnitTypeList;
import com.llamacorp.equate.view.IdlingResource.SimpleIdlingResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Helper class used to build a dialog list of units that is searchable
 */
public class UnitSearchDialogBuilder {
	private AlertDialog mAlertDialog;
	private final ArrayList<UnitSearchItem> mOriginalList;
	private FilterAdapter mArrayAdapter;

	/**
	 * Constructor for the unit search dialog builder
	 * @param unitTypeList is the collection of Unit Types and units to search
	 *                     over.
	 */
	public UnitSearchDialogBuilder(UnitTypeList unitTypeList){
		ArrayList<UnitSearchItem> items = new ArrayList<>();

		for (Map.Entry<String, UnitType> entry :
				  unitTypeList.getUnitTypeArray().entrySet()) {
			String unitTypeKey = entry.getKey();
			UnitType unitType = entry.getValue();
			for (int i = 0; i < unitType.size(); i++) {
				Unit unit = unitType.getUnitPosInUnitArray(i);
				// some units are just dummies to help position other units
				if (unit.toString().equals("") || unit.getLongName().equals("")){
					continue;
				}
				items.add(new UnitSearchItem(unitTypeKey, unit.getLongName(),
						  unit.getAbbreviation(), i));
			}
			Collections.sort(items, new Comparator<UnitSearchItem>() {
				@Override
				public int compare(UnitSearchItem o1, UnitSearchItem o2) {
					return o1.getUnitName().compareTo(o2.getUnitName());
				}
			});
		}
		mOriginalList = items;
	}

	/**
	 * Method to create the actual search dialog.
	 * @param context of the application used to create the dialog
	 * @param listener is called back when a item in the dialog list is clicked
	 */
	public void buildDialog(Context context,
									@Nullable final SimpleIdlingResource idlingResource,
									AdapterView.OnItemClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		final EditText filterEditText = new EditText(context);
		final ListView listView = new ListView(context);

		filterEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_white, 0, 0, 0);
		filterEditText.setInputType(InputType.TYPE_CLASS_TEXT);

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(filterEditText);
		layout.addView(listView);
		builder.setView(layout);

		mArrayAdapter = new FilterAdapter(context, mOriginalList);
		mArrayAdapter.registerDataSetObserver(new DataSetObserver() {
			/**
			 * Call in UI thread once filter action has finished
			 */
			@Override
			public void onChanged() {
				super.onChanged();
				// The IdlingResource is null in production.
				if (idlingResource != null) {
					idlingResource.setIdleState(true);
				}
			}
		});
		listView.setAdapter(mArrayAdapter);
		listView.setOnItemClickListener(listener);

		filterEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s,
													int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// The IdlingResource is null in production.
				if (idlingResource != null) {
					idlingResource.setIdleState(false);
				}
				// use Filter to filter results so filtering actions don't
				// operate on the UI thread
				mArrayAdapter.getFilter().filter(s.toString());
			}
		});

		builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		mAlertDialog = builder.create();

		// put dialog at top of screen so it doesn't move while filtering
		Window window = mAlertDialog.getWindow();
		if (window != null){
			WindowManager.LayoutParams layoutParams = window.getAttributes();
			layoutParams.gravity = Gravity.TOP;
			window.setAttributes(layoutParams);
		}

		// show the keyboard by default
		mAlertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		mAlertDialog.show();
	}

	public UnitSearchItem getItem(int position) {
		return mArrayAdapter.getUnitSearchItem(position);
	}

	/**
	 * Clears the dialog if one exists.
	 */
	public void cancelDialog(){
		if (mAlertDialog != null){
			mAlertDialog.cancel();
		}
	}
}
