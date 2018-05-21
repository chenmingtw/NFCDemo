package com.chenming.nfcdemo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

public class ReaderSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsettings);

        if(getActionBar() != null) {
            ActionBar actionBar = getActionBar();
            actionBar.setTitle(R.string.readersettings_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.layout_rsettings, new PrefRSettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PrefRSettingsFragment extends PreferenceFragment {

        MultiSelectListPreference techList;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_rsettings_fragment);

            techList = (MultiSelectListPreference) findPreference(getString(R.string.readersettings_nfc_tech_list_key));
            techList.setSummary(techList.getValues().toString());
            techList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValues) {
                    preference.setSummary(newValues.toString());
                    return true;
                }
            });
        }
    }
}
