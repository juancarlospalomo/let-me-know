package com.applilandia.letmeknow;

import android.app.Activity;
import android.os.Bundle;

import com.applilandia.letmeknow.fragments.SettingFragment;


public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingFragment())
                .commit();
    }

}
