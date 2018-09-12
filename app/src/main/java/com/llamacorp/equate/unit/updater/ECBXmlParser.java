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

package com.llamacorp.equate.unit.updater;

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
import java.util.Map;

/**
 * This class parses XML feeds from
 * https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
 */
public class ECBXmlParser extends CurrencyURLParser{
	private static final String ECB_API_URL =
			  "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
	private static final String ns = null; // We don't use namespaces

	private static final String TOP_ELEMENT = "gesmes:Envelope";
	private static final String SECOND_ELEMENT = "Cube";
	private static final String THIRD_ELEMENT = "Cube";
	private static final String FOURTH_ELEMENT = "Cube";
	private static final String THIRD_ELEMENT_ATTRIBUTE_TIME = "time";
	private static final String FOURTH_ELEMENT_ATTRIBUTE_CURR = "currency";
	private static final String FOURTH_ELEMENT_ATTRIBUTE_RATE = "rate";

	/** Constructor that initializes the base method's URL */
	ECBXmlParser() {
		super(ECB_API_URL);
	}


	/**
	 * Parse a stream of XML data and extract currency rates and times.
	 * @param stream input XML stream to parse
	 * @return a HashMap of all the symbols and prices from the XML stream
	 * @throws CurrencyParseException if there is any problem parsing the stream
	 */
	@Override
	protected HashMap<String, Entry> parse(InputStream stream) throws CurrencyParseException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(stream, null);
			parser.nextTag();
			HashMap<String, Entry> euroBased =  findSecondElement(parser);
			return convertEuroToDollar(euroBased);
		} catch (IOException | XmlPullParserException e) {
			throw new CurrencyParseException(e.getMessage());
		}
	}

	/**
	 * Converters all the conversion rates that are referenced to the Euro to the
	 * dollar.  This is effectively achieved by dividing every currency price by
	 * the price of the dollar
	 * @param euroBased
	 * @return
	 */
	private HashMap<String,Entry> convertEuroToDollar(HashMap<String, Entry> euroBased) {
		HashMap<String, Entry> dollarBased = new HashMap<>();
		double dollarPrice = euroBased.get("USD").price;
		for (Map.Entry<String, Entry> e : euroBased.entrySet()) {
			String key = e.getKey();
			Entry value = e.getValue();
			double newPrice = value.price/dollarPrice;
			dollarBased.put(key, new Entry(newPrice, value.symbol, value.date));
		}

		//remove the USD entry and replace with EUR
		Date date = dollarBased.get("USD").date;
		dollarBased.remove("USD");
		dollarBased.put("EUR", new Entry(1/dollarPrice, "EUR", date));
		return dollarBased;
	}

	/**
	 * Iterates over the top level XML branch, looking for the first occurrence
	 * of a tag with the name specified by SECOND_ELEMENT. Note that we only
	 * expect to find one.
	 * @param parser XML parser from already downloaded from a url
	 * @return a HashMap of entries
	 */
	private HashMap<String, Entry> findSecondElement(XmlPullParser parser)
			  throws XmlPullParserException, IOException {
		HashMap<String, Entry> entries = new HashMap<>();

		parser.require(XmlPullParser.START_TAG, ns, TOP_ELEMENT);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the resources tag
			if (name.equals(SECOND_ELEMENT)){
				entries = findThirdElement(parser);
			} else {
				skip(parser);
			}
		}
		return entries;
	}


	/**
	 * Iterates over the second top most XML branch, looking for the first occurrence
	 * of a tag with the name specified by THIRD_ELEMENT. Note that we only
	 * expect to find one.
	 * @param parser XML parser from already downloaded from a url
	 * @return a HashMap of entries
	 */
	private HashMap<String, Entry> findThirdElement(XmlPullParser parser)
			  throws XmlPullParserException, IOException {
		HashMap<String, Entry> entries = new HashMap<>();

		parser.require(XmlPullParser.START_TAG, ns, SECOND_ELEMENT);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the resources tag
			if (name.equals(THIRD_ELEMENT)){
				String rawDate = parser.getAttributeValue(null, THIRD_ELEMENT_ATTRIBUTE_TIME);

				SimpleDateFormat dateFormatter =
						  new SimpleDateFormat("yyyy-MM-dd", Locale.US);

				Date date = null;
				try {
					date = dateFormatter.parse(rawDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				entries = readFourthElements(parser, date);
			} else {
				skip(parser);
			}
		}
		return entries;
	}


	/**
	 * Iterates through each 4th element tag that contains the currencies.
	 */
	private HashMap<String, Entry> readFourthElements(XmlPullParser parser,
																	  Date date)
			  throws XmlPullParserException, IOException {
		HashMap<String, Entry> entries = new HashMap<>();

		parser.require(XmlPullParser.START_TAG, ns, THIRD_ELEMENT);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the resource tag
			if (name.equals(FOURTH_ELEMENT)){
				Entry ent = readResource(parser, date);
				parser.next();
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
	private Entry readResource(XmlPullParser parser, Date date) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, FOURTH_ELEMENT);
		String price = null;
		String symbol = null;

		//read in name attribute (ie in <field name="symbol">XAG=X</field>
		//this will be "symbol")

		symbol = readSymbol(parser);
		price = readPrice(parser);

		if (price == null || symbol == null || date == null){
			return null;
		}
		return new Entry(Double.parseDouble(price), symbol, date);
	}


	// Processes price tags in the feed.
	private String readSymbol(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, FOURTH_ELEMENT);
		String symbol = parser.getAttributeValue(null, FOURTH_ELEMENT_ATTRIBUTE_CURR);
		return symbol;
	}

	// Processes price tags in the feed.
	private String readPrice(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, FOURTH_ELEMENT);
		String price = parser.getAttributeValue(null, FOURTH_ELEMENT_ATTRIBUTE_RATE);
		return price;
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
}
