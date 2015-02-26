package com.applilandia.letmeknow.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.applilandia.letmeknow.R;

/**
 * Created by JuanCarlos on 26/02/2015.
 */
public class SettingFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Load the preferences from res/xml/preferences.xml
        addPreferencesFromResource(R.xml.preferences);
    }

}
