package com.paydayme.spatialguide.ui.preferences;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SGPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SGPreferencesFragment())
                .commit();
    }
}
