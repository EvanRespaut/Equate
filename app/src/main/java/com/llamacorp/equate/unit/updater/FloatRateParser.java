package com.llamacorp.equate.unit.updater;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * This class parses JSON feed from
 * http://www.floatrates.com/daily/usd.json
 */
public class FloatRateParser extends CurrencyURLParser {
	private static final String FLOAT_RATE_API_URL =
			  "http://www.floatrates.com/daily/usd.json";

	public FloatRateParser() {
		super(FLOAT_RATE_API_URL);
	}

	@Override
	HashMap<String, Entry> parse(InputStream stream) throws CurrencyParseException {

		// convert InputStream into a Reader for easy conversion
		JSONObject jsonObj;
		try {
			jsonObj = streamToJSON(stream);
			return jsonToHashMap(jsonObj);
		} catch (IOException | JSONException e) {
			throw new CurrencyParseException(e.getMessage());
		}
	}


	/**
	 * Converts InputStream into a Reader and then into a JSON object.
	 * See https://stackoverflow.com/questions/6511880/how-to-parse-a-json-input-stream
	 * for source.
	 * @param stream is InputStream to convert into JSON object
	 * @return a JSON object
	 * @throws JSONException if errors occur while reading JSON data
	 */
	private JSONObject streamToJSON(InputStream stream) throws IOException, JSONException {
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		StringBuilder responseStrBuilder = new StringBuilder();

		String inputStr;
		while ((inputStr = streamReader.readLine()) != null)
			responseStrBuilder.append(inputStr);
		return new JSONObject(responseStrBuilder.toString());
	}


	/**
	 * Takes the complete JSON object of currency data and extracts each currency
	 * field, price and date, and puts them into a HashMap of Entries to return
	 * @param rawJson is the complete JSON object of currency data
	 * @return a HashMap of Entries
	 * @throws JSONException if JSON object's key doesn't return valid data
	 */
	private HashMap<String, Entry> jsonToHashMap(JSONObject rawJson) throws JSONException {
		HashMap<String, Entry> entries = new HashMap<>();

		for (Iterator<String> it = rawJson.keys(); it.hasNext(); ) {
			String key = it.next();
			JSONObject currency = rawJson.getJSONObject(key);
			String name = currency.getString("code");
			double rate = currency.getDouble("rate");
			Date date = parseDate(currency.getString("date"));

			Entry entry = new Entry(rate, name, date);
			entries.put(name, entry);
		}
		return entries;
	}

	/**
	 * Attemps to parse the date from a String into a Date object. If this
	 * parsing is unsuccessful, the current date will be used.
	 * @param rawDate date in a format such as "Thu, 23 May 2019 00:00:01 GMT"
	 * @return a Date object with parsed date or current date if parsing error
	 */
	private Date parseDate(String rawDate) {
		SimpleDateFormat dateFormatter =
				  new SimpleDateFormat("EEE, dd LLL yyyy HH:mm:ss", Locale.US);

		// attempt to parse the date, if there's an issue, just use the current date
		Date date;
		try {
			date = dateFormatter.parse(rawDate);
		} catch (ParseException e) {
			date = new Date();
		}
		return date;
	}
}
