package com.llamacorp.equate.view;


import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.llamacorp.equate.R;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.llamacorp.equate.ResourceArrayParser.getUnitTypeKeyArray;
import static com.llamacorp.equate.ResourceArrayParser.getUnitTypeNameArray;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {
   private final static String UNIT_TYPE_PREF_KEY = "unit_type_prefs";

   /**
    * A preference value change listener that updates the preference's summary
    * to reflect its new value.
    */
   private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
           new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object value) {
         String stringValue = value.toString();

         if (preference instanceof ListPreference){
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

         } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
         }
         return true;
      }
   };


   /**
    * Binds a preference's summary to its value. More specifically, when the
    * preference's value is changed, its summary (line of text below the
    * preference title) is updated to reflect the value. The summary is also
    * immediately updated upon calling this method. The exact display format is
    * dependent on the type of preference.
    *
    * @see #sBindPreferenceSummaryToValueListener
    */
   private static void bindPreferenceSummaryToValue(Preference preference) {
      // Set the listener to watch for value changes.
      preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

      // Trigger the listener immediately with the preference's
      // current value.
      sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
              PreferenceManager
                      .getDefaultSharedPreferences(preference.getContext())
                      .getString(preference.getKey(), ""));
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment()).commit();
      setupActionBar();
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();

      if (id == android.R.id.home){
         finish(); //closes settings activity
         return true;
      }
      return super.onOptionsItemSelected(item);
   }


   /**
    * Set up the {@link android.app.ActionBar}, if the API is available.
    */
   private void setupActionBar() {
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null){
         // Show the Up button in the action bar.
         actionBar.setDisplayHomeAsUpEnabled(true);
      }
   }



   public static class PrefsFragment extends PreferenceFragment {
      @Override
      public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
         //setHasOptionsMenu(true);

         setUpUnitTypePrefs();

         // Bind the summaries of EditText/List/Dialog/Ringtone preferences
         // to their values. When their values change, their summaries are
         // updated to reflect the new value, per the Android Design
         // guidelines.
         bindPreferenceSummaryToValue(findPreference("example_text"));
         bindPreferenceSummaryToValue(findPreference("example_list"));
      }

		/**
       * Helper Class to setup the default Unit Type preference list in code
       */
      private void setUpUnitTypePrefs() {
         PreferenceScreen screen = getPreferenceScreen();
         MultiSelectListPreference listPref = new MultiSelectListPreference(super.getActivity());
         listPref.setOrder(0);
         listPref.setDialogTitle(R.string.unit_select_title);
         listPref.setKey(UNIT_TYPE_PREF_KEY);
         listPref.setSummary(R.string.unit_select_summary);
         listPref.setTitle(R.string.unit_select_title);
         listPref.setEntries(getUnitTypeNameArray(getResources()));

         String[] keyArray = getUnitTypeKeyArray(getResources());
         listPref.setEntryValues(keyArray);

         final Set<String> result = new HashSet<>();
         Collections.addAll(result, keyArray);

         listPref.setDefaultValue(result);

         screen.addPreference(listPref);
      }

      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
         int id = item.getItemId();
         ViewUtils.toast("other", getActivity());

         if (id == android.R.id.home){
            ViewUtils.toast("Home", getActivity());
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
         }
         return super.onOptionsItemSelected(item);
      }
   }
}
