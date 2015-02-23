package com.applilandia.letmeknow.cross;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by JuanCarlos on 20/02/2015.
 */
public class Settings {

    public final static String KEY_CREATE_DEFAULT_NOTIFICATION = "prefDefNot";
    public final static String KEY_TRIGGER_DAILY_NOTIFICATION = "prefDaily";
    public final static String KEY_TRIGGER_DAILY_TIME = "prefDailyTime";

    public static boolean getCreateDefaultNotificationValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(KEY_CREATE_DEFAULT_NOTIFICATION, true);
    }

    public static boolean getDailyNotificationValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(KEY_TRIGGER_DAILY_NOTIFICATION, true);
    }

    public static boolean getDailyTriggerNotificationValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(KEY_TRIGGER_DAILY_TIME, true);
    }
}
