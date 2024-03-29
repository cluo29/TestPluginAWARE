package com.aware.plugin.pluginfortesting;

/**
 * Created by Comet on 26/01/16.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.aware.Aware;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    //Plugin settings in XML @xml/preferences
    public static final String STATUS_PLUGIN_PLUGINFORTESTING = "status_plugin_pluginfortesting";

    //Plugin settings UI elements
    private static CheckBoxPreference status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();

        status = (CheckBoxPreference) findPreference(STATUS_PLUGIN_PLUGINFORTESTING);
        if( Aware.getSetting(this, STATUS_PLUGIN_PLUGINFORTESTING).length() == 0 ) {
            Aware.setSetting( this, STATUS_PLUGIN_PLUGINFORTESTING, true ); //by default, the setting is true on install
        }
        status.setChecked(Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_PLUGINFORTESTING).equals("true"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference setting = findPreference(key);

        if( setting.getKey().equals(STATUS_PLUGIN_PLUGINFORTESTING) ) {
            boolean is_active = sharedPreferences.getBoolean(key, false);
            Aware.setSetting(this, key, is_active);
            if( is_active ) {
                Aware.startPlugin(getApplicationContext(), "com.aware.plugin.pluginfortesting");
            } else {
                Aware.stopPlugin(getApplicationContext(), "com.aware.plugin.pluginfortesting");
            }
            status.setChecked(is_active);
        }
    }

}
