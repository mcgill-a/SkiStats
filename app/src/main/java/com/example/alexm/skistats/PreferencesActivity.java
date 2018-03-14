package com.example.alexm.skistats;

import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class PreferencesActivity extends PreferenceActivity {

    public final static String TAG = "SkiStats.Log.Prefs";

    public PreferencesActivity()
    {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new LocationFragment()).commit();

    }

    public static class LocationFragment extends PreferenceFragment {

        private final static String TAG = "SkiStats.Log";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG,"Location Fragment: onCreate");
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
