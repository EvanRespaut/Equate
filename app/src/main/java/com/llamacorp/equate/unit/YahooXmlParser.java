/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.llamacorp.equate.unit;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * This class parses XML feeds from
 * http://finance.yahoo.com/webservice/v1/symbols/allcurrencies/quote
 */
public class YahooXmlParser {
	// We don't use namespaces
	private static final String ns = null;


	public HashMap<String, Entry> parse(InputStream in) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return findResources(parser);
		} finally {
			in.close();
		}
	}

	/**
	 * Iterates over the top level XML branch called list, looking for the first
	 * "resources" tag. Note that we only expect to find one.
	 *
	 * @param parser XML parser from Yahoo
	 * @return a HashMap of entries
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private HashMap<String, Entry> findResources(XmlPullParser parser) throws XmlPullParserException, IOException {
		HashMap<String, Entry> entries = new HashMap<>();

		parser.require(XmlPullParser.START_TAG, ns, "list");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the resources tag
			if (name.equals("resources")){
				entries = readListOfResources(parser);
			} else {
				skip(parser);
			}
		}
		return entries;
	}


	/**
	 * Iterates through each "resource" tag in the "resources" parent. Each
	 * currency should have it's own "resource" tag.
	 */
	private HashMap<String, Entry> readListOfResources(XmlPullParser parser)
			  throws XmlPullParserException, IOException {
		HashMap<String, Entry> entries = new HashMap<>();

		parser.require(XmlPullParser.START_TAG, ns, "resources");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the resource tag
			if (name.equals("resource")){
				if (parser.isEmptyElementTag()){
					skip(parser);
				}
				Entry ent = readResource(parser);
				if (ent != null){
					entries.put(ent.symbol, ent);
				}
			} else {
				skip(parser);
			}
		}
		return entries;
	}

	/**
	 * Parses the contents of a currency entry. If it encounters a price,
	 * summary, or symbol tag, hands them off to their respective methods for
	 * processing. Otherwise, skips the tag.
	 *
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private Entry readResource(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "resource");
		String price = null;
		String symbol = null;
		Date date = null;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			//read in name attribute (ie in <field name="symbol">XAG=X</field>
			//this will be "symbol")
			String name = parser.getAttributeValue(null, "name");
			switch (name) {
				case "price":
					price = readPrice(parser);
					break;
				case "symbol":
					symbol = readSymbol(parser);

					break;
				case "utctime":
					SimpleDateFormat sdf =
							  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);

					try {
						date = sdf.parse(readSymbol(parser));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					break;
				default:
					skip(parser);
					break;
			}
		}
		if (price == null || symbol == null || date == null){
			return null;
		}
		return new Entry(Double.parseDouble(price), symbol, date);
	}

	// Processes price tags in the feed.
	private String readPrice(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "field");
		String price = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "field");
		return price;
	}

	// Processes summary tags in the feed.
	private String readSymbol(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "field");
		String symbol = readText(parser);
		//symbol seems to have "=X" after, cut this off
		symbol = symbol.replace("=X", "");
		parser.require(XmlPullParser.END_TAG, ns, "field");
		return symbol;
	}

	// For the tags price and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT){
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	// Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
	// if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
	// finds the matching END_TAG (as indicated by the value of "depth" being 0).
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG){
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}

	static class Entry {
		final double price;
		final String symbol;
		final Date date;

		private Entry(double price, String symbol, Date date) {
			this.price = price;
			this.symbol = symbol;
			this.date = date;
		}
	}
}
