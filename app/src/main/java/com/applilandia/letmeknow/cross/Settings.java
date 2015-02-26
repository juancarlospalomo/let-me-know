package com.applilandia.letmeknow.cross;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.applilandia.letmeknow.R;

/**
 * Created by JuanCarlos on 20/02/2015.
 */
public class Settings {

    public static boolean getCreateDefaultNotificationValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getResources().getString(R.string.key_create_default_notification), true);
    }

    public static boolean getDailyNotificationValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getResources().getString(R.string.key_trigger_daily_notification), true);
    }

    public static boolean getDailyTriggerNotificationValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getResources().getString(R.string.key_trigger_daily_time), true);
    }
}
