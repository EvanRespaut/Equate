package com.llamacorp.equate.unit;

import android.content.Context;
import android.os.AsyncTask;

import com.llamacorp.equate.R;
import com.llamacorp.equate.view.ViewUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Helper class is used to update dynamic unit types such as currency by
 * calling all the necessary async tasks and such
 * Created by Evan on 10/2/2015.
 */
public class UnitTypeUpdater {
   public static int UPDATE_TIMEOUT_MIN = 90;
   private Context mContext;
   private ArrayList<Integer> mUnitsToUpdate;

   public UnitTypeUpdater(Context mContext) {
      this.mContext = mContext;
      mUnitsToUpdate = new ArrayList<>();
   }

   public void update(UnitType ut, boolean forced) {
      if(!ut.containsDynamicUnits()) return;

      //only do update if timeout period has passed
      if (isTimeoutReached(ut) || forced) {
         //add "Updating" text
         ut.setUpdating(true);
         new UpdateYahooXMLAsyncTask(ut, forced).execute();
      } else {
         ViewUtils.toast(mContext.getText(R.string.words_units_up_to_date)
                 .toString(), mContext);
      }
   }

   private boolean isTimeoutReached(UnitType ut){
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
   private class UpdateYahooXMLAsyncTask extends AsyncTask<Void, Void, Boolean> {
      private UnitType mUnitType;
      private Boolean mForced;

      public UpdateYahooXMLAsyncTask(UnitType unitType, Boolean forced) {
         mUnitType = unitType;
         mForced = forced;
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
            String text = "Updated at ";
            if(mForced) text = "FORCED update at ";
            ViewUtils.toastLong(text + mUnitType.getLastUpdateTime().toString(),
                    mContext);
         }

         //update the remaining units that got missed by yahoo xml
         if (mUnitsToUpdate != null) {
            for (int i = 0; i < mUnitsToUpdate.size(); i++) {
               UnitCurrency u = (UnitCurrency) mUnitType.getUnitPosInUnitArray(mUnitsToUpdate.get(i));
               if (u.isTimeoutReached(mContext))
                  u.asyncRefresh(mContext);
            }
         }

         //remove text "Updating"
         mUnitType.setUpdating(false);
      }
   }


   private boolean updateRatesWithXML(UnitType ut){
      //only try to update currencies if we have XML currency URL to work with
      if(ut.getXMLCurrencyURL() == null) return false;

      HashMap<String, YahooXmlParser.Entry> currRates = null;

      //grab the array of yahoo currency units
      try {
         currRates = getCurrRates(ut.getXMLCurrencyURL());
      } catch (XmlPullParserException | IOException e) {
         e.printStackTrace();
      }

      //leave if we have not updated rates
      if(currRates == null) return false;

      //update each existing curr with new rates
      for (int i = 0; i < ut.size(); i++) {
         //skip unit support that don't support updating (not hist)
         if (!ut.getUnitPosInUnitArray(i).isDynamic()) continue;

         UnitCurrency u = ((UnitCurrency) ut.getUnitPosInUnitArray(i));
         YahooXmlParser.Entry entry = currRates.get(u.getName());
         if (entry != null) {
            u.setValue(entry.price);
            u.setUpdateTime(entry.date);
         }
         else {
            //this is for BTC and other units that don't get updated by yahoo
            mUnitsToUpdate.add(i);
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
   private HashMap<String, YahooXmlParser.Entry> getCurrRates(String urlString)
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
}
