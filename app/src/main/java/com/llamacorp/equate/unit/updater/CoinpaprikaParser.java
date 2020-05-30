package com.llamacorp.equate.unit.updater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * This class parses JSON stream from Coinmarketcap of the top 100 crypto
 * currencies and outputs a HashMap of symbols with their current exchange rate
 */
public class CoinpaprikaParser extends CurrencyURLParser {
	private static final String COINMARKETCAP_API_URL =
			  "https://api.coinpaprika.com/v1/tickers";

	public CoinpaprikaParser() {
		super(COINMARKETCAP_API_URL);
	}

	/**
	 * Parse a stream of JSON data and extract crypto currency rates and times.
	 * @param stream input JSON stream to parse
	 * @return a HashMap with filled with up to date currencies, with the symbol
	 * as the key and Entries as the values
	 */
	@Override
	HashMap<String, Entry> parse(InputStream stream) throws CurrencyParseException {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						  stream, Charset.forName("UTF-8")));

				String jsonText = readAll(reader);
				JSONArray jArray = new JSONArray(jsonText);
				return extractEntries(jArray);
			} catch (JSONException | IOException e) {
				throw new CurrencyParseException(e.getMessage());
			}
	}



	private HashMap<String, Entry> extractEntries(JSONArray jArray) throws JSONException {
		HashMap<String, Entry> result = new HashMap<>();

		for (int i = 0; i < jArray.length(); i++) {
			Object obj = jArray.get(i);

			if (obj instanceof JSONObject) {
				JSONObject jObj = (JSONObject) obj;
				String symbol = jObj.getString("symbol");
				String last_updated = jObj.getString("last_updated");

				JSONObject jObjUSD = jObj.getJSONObject("quotes").getJSONObject("USD");

				// invert the price, want currencies in 1/$'s
				double price = 1.0 / jObjUSD.getDouble("price");

				Date date = null;
				SimpleDateFormat sdf =
						  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US);
				try {
					date = sdf.parse(last_updated);
				} catch (java.text.ParseException e) {
					e.printStackTrace();
				}

				Entry entry = new Entry(price, symbol, date);
				result.put(symbol, entry);
			}
		}
		return result;
	}



	/**
	 * Takes a BufferReader and parses it into a String
	 * @param reader input BufferReader to read
	 * @return One long string from the buffer, with new lines separated with "\n"
	 * @throws IOException if this reader is closed or some other I/O error occurs.
	 */
	private static String readAll(BufferedReader reader) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line).append("\n");
		}
		return stringBuilder.toString();
	}
}
