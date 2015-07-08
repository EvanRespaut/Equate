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
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses XML feeds from stackoverflow.com.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class YahooFinanceXmlParser {
   // We don't use namespaces
   private static final String ns = null;


   public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
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

   private List<Entry> findResources(XmlPullParser parser) throws XmlPullParserException, IOException {
      List<Entry> entries = new ArrayList<Entry>();

      parser.require(XmlPullParser.START_TAG, ns, "list");
      while (parser.next() != XmlPullParser.END_TAG) {
         if (parser.getEventType() != XmlPullParser.START_TAG) {
            continue;
         }
         String name = parser.getName();
         // Starts by looking for the entry tag
         if (name.equals("resources")) {
            entries = readList(parser);
         } else {
            skip(parser);
         }
      }
      return entries;
   }


   private List<Entry> readList(XmlPullParser parser) throws XmlPullParserException, IOException {
      List<Entry> entries = new ArrayList<Entry>();

      parser.require(XmlPullParser.START_TAG, ns, "resources");
      while (parser.next() != XmlPullParser.END_TAG) {
         if (parser.getEventType() != XmlPullParser.START_TAG) {
            continue;
         }
         String name = parser.getName();
         // Starts by looking for the entry tag
         if (name.equals("resource")) {
            entries.add(readResource(parser));
         } else {
            skip(parser);
         }
      }
      return entries;
   }

   // This class represents a single entry (post) in the XML feed.
   // It includes the data members "price," "symbol," and "summary."
   public static class Entry {
      public final String price;
      public final String symbol;

      private Entry(String price, String symbol) {
         this.price = price;
         this.symbol = symbol;
      }
   }

   // Parses the contents of an entry. If it encounters a price, summary, or symbol tag, hands them
   // off
   // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
   private Entry readResource(XmlPullParser parser) throws XmlPullParserException, IOException {
      parser.require(XmlPullParser.START_TAG, ns, "resource");
      String price = null;
      String symbol = null;
      while (parser.next() != XmlPullParser.END_TAG) {
         if (parser.getEventType() != XmlPullParser.START_TAG) {
            continue;
         }
         //read in name attribute (ie in  <field name="symbol">XAG=X</field>
         //this will be "symbol")
         String name = parser.getAttributeValue(null,"name");
         if (name.equals("price")) {
            price = readPrice(parser);
         } else if (name.equals("symbol")) {
            symbol = readSymbol(parser);
         } else {
            skip(parser);
         }
      }
      return new Entry(price, symbol);
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
      if (parser.next() == XmlPullParser.TEXT) {
         result = parser.getText();
         parser.nextTag();
      }
      return result;
   }

   // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
   // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
   // finds the matching END_TAG (as indicated by the value of "depth" being 0).
   private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
      if (parser.getEventType() != XmlPullParser.START_TAG) {
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
