package com.llamacorp.equate.unit;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

/**
 * Helper class is used to update dynamic unit types such as currency by
 * calling all the necessary async tasks and such
 * Created by Evan on 10/2/2015.
 */
public class UnitTypeUpdater {
   //TODO should maybe make this not static
   public static int UPDATE_TIMEOUT_MIN = 30;
   private static Context mContext;

   public static void update(UnitType ut, Context c) {
      if(!ut.containsDynamicUnits()) return;
      mContext = c;

      //only do update if timeout period has passed
      if (isTimeoutReached(ut)) {
         //add "Updating" text
         ut.setUpdating(true);
         new UpdateYahooXMLAsyncTask(ut).execute();
      } else {
         toast("Timeout not reached");
      }
   }

   private static boolean isTimeoutReached(UnitType ut){
      Date now = new Date();
      if(ut.getLastUpdateTime() != null && (now.getTime() -
              ut.getLastUpdateTime().getTime())
              < (60*1000*UPDATE_TIMEOUT_MIN)){
         return false;
      }
      else
         return true;
   }

   /**
    * This class is used to create a background task that handles
    * the actual retrieval of the Yahoo XML file that contains the current rates
    */
   private static class UpdateYahooXMLAsyncTask extends AsyncTask<Void, Void, Boolean> {
      private UnitType mUnitType;

      public UpdateYahooXMLAsyncTask(UnitType unitType) {
         mUnitType = unitType;
      }

      //This method is called first
      @Override
      protected Boolean doInBackground(Void...voids) {
         return updateRatesWithXML(mUnitType);
      }

      // This method is called after doInBackground completes
      @Override
      protected void onPostExecute(Boolean successful) {
         if (successful) {
            mUnitType.setLastUpdateTime(new Date());
            toast("Updated " + mUnitType.getLastUpdateTime().toString());
         }

         //remove text "Updating"
         mUnitType.setUpdating(false);
//         if (!successful) {
//            final Toast toast = toast.maketext(mContext,
//                    "error updating yahoo xml currencies", toast.LENGTH_SHORT);
//            toast.show();
//            //todo might want to try old update strat here
//         } else {
//            final Toast toast = toast.maketext(mContext,
//                    "yahoo xml rates loaded!", toast.LENGTH_SHORT);
//            toast.show();
//         }
      }
   }


   private static boolean updateRatesWithXML(UnitType ut){
      //only try to update currencies if we have XML currency URL to work with
      if(ut.getXMLCurrencyURL() == null) return false;

      HashMap<String, YahooXmlParser.Entry> currRates = null;

      //grab the array of yahoo currency units
      try {
         currRates = getCurrRates(ut.getXMLCurrencyURL());
      } catch (XmlPullParserException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      //leave if we have not updated rates
      if(currRates == null) return false;

      //update each existing curr with new rates
      for (int i = 0; i < ut.size(); i++) {
         //skip unit support that don't support updating (not hist)
         if (!ut.getUnitAtOriginalPos(i).isDynamic()) continue;

         UnitCurrency u = ((UnitCurrency) ut.getUnitAtOriginalPos(i));
         YahooXmlParser.Entry entry = currRates.get(u.getName());
         if (entry != null) {
            u.setValue(entry.price);
            u.setUpdateTime(entry.date);
         }
         //this is for BTC
         else {
            //TODO should not do this this way
            //check to see if the update timeout has been reached
            if (u.isTimeoutReached(mContext))
               u.asyncRefresh(mContext);
         }
      }
      return true;
   }


   /**
    * Downloads XML file of current Yahoo finance currency rates
    * @param urlString URL of XML string
    * @return Array of currencies with updated values
    * @throws XmlPullParserException
    * @throws IOException
    */
   private static HashMap<String, YahooXmlParser.Entry> getCurrRates(String urlString)
           throws XmlPullParserException, IOException {
      InputStream stream = null;
      YahooXmlParser yahooXmlParser = new YahooXmlParser();
      HashMap<String, YahooXmlParser.Entry> currRates = null;

      try {
         stream = downloadUrl(urlString);
         currRates = yahooXmlParser.parse(stream);
         // Makes sure that the InputStream is closed after the app is
         // finished using it.
      } finally {
         if (stream != null) {
            stream.close();
         }
      }
      return currRates;
   }


   /**
    * Given a string representation of a URL, sets up a connection and gets
    * an input stream.
    */
   private static InputStream downloadUrl(String urlString) throws IOException {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setReadTimeout(10000 /* milliseconds */);
      conn.setConnectTimeout(15000 /* milliseconds */);
      conn.setRequestMethod("GET");
      conn.setDoInput(true);
      // Starts the query
      conn.connect();
      InputStream stream = conn.getInputStream();
      return stream;
   }



   private static void toast(String text) {
      final Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
      toast.show();
   }
}
