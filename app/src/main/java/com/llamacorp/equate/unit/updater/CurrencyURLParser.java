package com.llamacorp.equate.unit.updater;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

/**
 * Generic class used to carry common methods used to download a collection of
 * currencies or similar from a URL, parse the contents, and load them into a
 * form that can be used to update the various currency units.
 */
public abstract class CurrencyURLParser {
	private final String mURL;


	public CurrencyURLParser(String url) {
		mURL = url;
	}


	/**
	 * Download a collection of currencies at a given URL and pass it on
	 * to a child for parsing.
	 * @throws IOException if there are problems finding URL. Deal with this
	 * downstream with proper error handling. Throws {@link CurrencyParseException}
	 * if there is a problem with the parsing function.
	 */
	public HashMap<String, Entry> downloadAndParse()
			  throws IOException, CurrencyParseException {
		InputStream stream = null;

		try {
			stream = downloadUrl(mURL);
			return parse(stream);
		} finally {
			if (stream != null){
				stream.close();
			}
		}
	}


	/**
	 * Method that is implemented by children in different ways, used to parse
	 * the stream that is downloaded from the URL.
	 * @return a HashMap with filled with up to date currencies, with the symbol
	 * as the key and Entries as the values
	 */
	abstract HashMap<String, Entry> parse(InputStream stream) throws CurrencyParseException;


	/**
	 * Given a string representation of a URL, sets up a connection and gets
	 * an input stream.
	 */
	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		return conn.getInputStream();
	}


	/** Generic exception used when there are parsing errors */
	static class CurrencyParseException extends Exception {
		public CurrencyParseException(String message) {
			super(message);
		}
	}

	static class Entry {
		final double price;
		final String symbol;
		final Date date;

		Entry(double price, String symbol, Date date) {
			this.price = price;
			this.symbol = symbol;
			this.date = date;
		}
	}
}
