package com.llamacorp.equate.unit;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.GregorianCalendar;

public class UnitCurrency extends Unit {
	private static final String JSON_LAST_UPDATE = "updated";
	public static final String DEFAULT_CURRENCY = "USD";

	private Date mTimeLastUpdated;
	//	private String mURLPrefix = "http://rate-exchange.appspot.com/currency?from=USD&to=";
	private String mURLPrefix = "http://rate-exchange.herokuapp.com/fetchRate?from=" 
			+ DEFAULT_CURRENCY + "&to=";

	//this is for communication with fragment hosting convert keys
//	OnConvertKeyUpdateFinishedListener mCallback;
	Context mContext;
//
//	public interface OnConvertKeyUpdateFinishedListener {
//		public void updateDynamicUnitButtons(String text);
//	}

	//used to tell parent classes if the asyncRefresh is currently running
	private boolean mUpdating = false;

	public UnitCurrency(String name, String longName, double value){
		super(name, longName, value);
      //Note that Jan = 0 in the Gregorian Calendar constructor below
		mTimeLastUpdated = new GregorianCalendar(2015,3,1,1,11).getTime();
	}

	public UnitCurrency(String name, String longName, double value, String URL){
		this(name, longName, value);
		mURLPrefix = URL;
	}	

	/** Load in the update time */
   @Override
	public boolean loadJSON(JSONObject json) throws JSONException {
		boolean success = super.loadJSON(json);
      //only load in the time if the JSON object matches this UNIT
		if(success)
			setUpdateTime(new Date(json.getLong(JSON_LAST_UPDATE)));
      return success;
	}

	/** Save the update time */
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put(JSON_LAST_UPDATE, mTimeLastUpdated.getTime());
		return json;
	}

	public void setUpdateTime(Date date) {
		mTimeLastUpdated = date;
	}

	public long getTimeOfUpdate(){
		if(mTimeLastUpdated != null)
			return mTimeLastUpdated.getTime();
		else
			return 0;
	}

	public boolean isUpdating(){ 
		return mUpdating;
	}

//
//	public void setCallback(OnConvertKeyUpdateFinishedListener callback) {
//		mCallback = callback;
//	}

	@Override
	public String convertTo(Unit toUnit, String expressionToConv) {
		return expressionToConv + "*" + toUnit.getValue() + "/" + getValue();
	}


	public boolean isTimeoutReached(Context c){
		mContext = c;
		Date now = new Date();
		return !(mTimeLastUpdated != null && (now.getTime() - mTimeLastUpdated.getTime())
				  < (60 * 1000 * UnitTypeUpdater.UPDATE_TIMEOUT_MIN));
	}

	/**
	 * Asynchronously try to update the currency rate by fetching the
	 * value via an HTTP JSON API call.  Note that this call will take 
	 * some time to update the value, since it runs in the background.
	 * Also note that the value may or may not even be updated, dependent
	 * on Internet connection.  
	 */
	public void asyncRefresh(Context c){
		if(getName().equals("") || getName().equals(DEFAULT_CURRENCY)) return;
		if(mUpdating) return;

		mContext = c;

		mUpdating = true;
		//		if(mCallback != null)
		//			mCallback.updateDynamicUnitButtons("Updating");
		new HttpAsyncTask().execute(getURL());
	}

	private String getURL(){
		//crude method, want something better later
		if(getName().equals("BTC"))
			return mURLPrefix;
		else
			return mURLPrefix + toString();
	}

	/**
	 * This class is used to create a background task that handles 
	 * the actual HTTP getting and JSON parsing
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		//This method is called first
		@Override
		protected String doInBackground(String... urls) {
			return GET(urls[0]);
		}

		// This method is called after doInBackground completes
		@Override
		protected void onPostExecute(String result) {

			boolean updateSuccess = false;

			//this is crude, but works until we have more URLs each with unique formats
			if(getName().equals("BTC"))
				try{
					setValue(Double.parseDouble(result));
					updateSuccess = true;
				} catch (Exception e){
					//System.out.println("Parsing Exception for BTC, result = " + result);
				}
			else
				updateSuccess = parseRateFromJSONString(result);

			//record time of update only if we actually updated
			if(updateSuccess){
				setUpdateTime(new Date());
			}

			//updating is complete
			mUpdating = false;
//			if(mCallback != null)
//				mCallback.updateDynamicUnitButtons("Updating");
		}

		/**
		 * Attempt to take a JSON string, parse it, and set the value
		 * of this current Currency Unit
		 */		
		private boolean parseRateFromJSONString(String result){
			boolean updateSuccess = false;
			try {
				//System.out.println("Trying to parse \"" + result + "\"");
				JSONObject json = new JSONObject(result);
				final String JSON_URL_RATE_TAG = "Rate";
				double rate = json.getDouble(JSON_URL_RATE_TAG);
				setValue(rate);
				updateSuccess = true;
			} catch (JSONException e) {
//				if(result.contains("Over Quota")){
//					System.out.println("Over quota for " + getName());
//				}
//				else
//					System.out.println("Result didn't parse, result = " + result);
			}
			return updateSuccess;
		}

		/** Helper function for above method*/
		private String GET(String url){
			InputStream inputStream;
			String result = "";
			try {
				// create HttpClient
				HttpClient httpclient = new DefaultHttpClient();

				// make GET request to the given URL
				HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

				// receive response as inputStream
				inputStream = httpResponse.getEntity().getContent();

				// convert inputstream to string
				if(inputStream != null)
					result = convertInputStreamToString(inputStream);
				else
					result = "Did not work!";

			} catch (Exception e) {
				Log.d("InputStream", e.getLocalizedMessage());
			}

			return result;
		}

		/** Helper function for above method*/
		private String convertInputStreamToString(InputStream inputStream) throws IOException{
			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
			String line;
			String result = "";
			while((line = bufferedReader.readLine()) != null)
				result += line;

			inputStream.close();
			return result;

		}
	}

}

