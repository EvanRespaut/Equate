package com.llamacorp.unitcalc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;


public class CalcJSONSerializer {
	private static final String JSON_RESULT_LIST = "result_list";

	private Context mContext;
	private String mFilename;

	public CalcJSONSerializer(Context c, String f) {
		mContext = c;
		mFilename = f;
	}

	public List<Result> loadResult() throws IOException, JSONException {
		ArrayList<Result> results = new ArrayList<Result>();
		BufferedReader reader = null;
		try {
			// open and read the file into a StringBuilder
			InputStream in = mContext.openFileInput(mFilename);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				// line breaks are omitted and irrelevant
				jsonString.append(line);
			}
			
			// parse the JSON using JSONTokener
			JSONObject jObject = (JSONObject) new JSONTokener(jsonString.toString()).nextValue();
			JSONArray array = jObject.getJSONArray(JSON_RESULT_LIST);
			
			// build the array of results from JSONObjects
			for (int i = 0; i < array.length(); i++) {
				results.add(new Result(array.getJSONObject(i)));
			}
		} catch (FileNotFoundException e) {
			// we will ignore this one, since it happens when we start fresh
		} finally {
			if (reader != null)
				reader.close();
		}
		return results;
	}

	public void saveCalcState(List<Result> resultList) throws JSONException, IOException {
		// build an array in JSON
		JSONArray array = new JSONArray();
		for (Result r : resultList)
			array.put(r.toJSON());

		JSONObject jObject = new JSONObject();
		jObject.put(JSON_RESULT_LIST, array);
		
		// write the file to disk
		Writer writer = null;
		try {
			OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(out);
			writer.write(jObject.toString());
		} finally {
			if (writer != null)
				writer.close();
		}
	}
}
