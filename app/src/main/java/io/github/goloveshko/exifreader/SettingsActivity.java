package io.github.goloveshko.exifreader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends PreferenceActivity {
    public static final String PREFERENCE_SHOW_EXIF_COLUMNS = "preference_show_exif_columns";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            SettingsFragment preferencesFragment = new SettingsFragment();
            preferencesFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(android.R.id.content, preferencesFragment).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            Bundle bundle = getArguments();
            final HashMap<Integer, String> argumentEntries = (HashMap<Integer, String>)bundle.getSerializable(ExifReaderActivity.EXIF_TAG_NAMES_ENTRIES);

            MultiSelectListPreference multiPref = (MultiSelectListPreference)findPreference("preference_show_exif_columns");

            Set<Integer> keySet = argumentEntries.keySet();
            HashSet<String> entryValuesStr = new HashSet<>(keySet.size());
            keySet.forEach(i -> entryValuesStr.add(i.toString()));

            CharSequence[] entries = argumentEntries.values().toArray(new CharSequence[0]);
            CharSequence[] entryValues = entryValuesStr.toArray(new CharSequence[0]);

            multiPref.setEntries(entries);
            multiPref.setEntryValues(entryValues);
            multiPref.setDefaultValue(entries);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(!sharedPrefs.contains(PREFERENCE_SHOW_EXIF_COLUMNS)) {
                multiPref.setValues(entryValuesStr);
            }

            multiPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    MultiSelectListPreference mpreference = (MultiSelectListPreference) preference;
                    mpreference.setSummary(newValue.toString());
                    mpreference.setValues((Set<String>) newValue);
                    return true;
                }
            });
        }
    }
}
