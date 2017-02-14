package com.llamacorp.equate.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.util.ArrayList;

/**
 * Helper class designed to build a dialog that can filter on a given list.
 */
public class FilterDialogBuilder {
	private AlertDialog mAlertDialog = null;


	public FilterDialogBuilder(Context context, ArrayList arrayToFilter,
										AdapterView.OnItemClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		final EditText filterEditText = new EditText(context);
		final ListView listview = new ListView(context);

		filterEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_white, 0, 0, 0);
		filterEditText.setInputType(InputType.TYPE_CLASS_TEXT);

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(filterEditText);
		layout.addView(listview);
		builder.setView(layout);

		final FilterAdapter arrayAdapter = new FilterAdapter(context, arrayToFilter);

		listview.setAdapter(arrayAdapter);
		listview.setOnItemClickListener(listener);

		filterEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s,
													int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// use Filter to filter results so filtering actions don't
				// operate on the UI thread
				arrayAdapter.getFilter().filter(s.toString());
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
}
