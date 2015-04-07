package com.applilandia.letmeknow.cross;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.applilandia.letmeknow.LetMeKnowApp;
import com.applilandia.letmeknow.R;

import java.io.File;

/**
 * Created by JuanCarlos on 20/02/2015.
 */
public class Settings {

    private final static String LOG_TAG = Settings.class.getSimpleName();
    private final static String DEFAULT_DAILY_TIME_VALUE = "09:00";

    public final static int DAILY_NOTIFICATION_ID = 0;



    /**
     * Get the value of default notification setting.
     * If this is true, one 5 minutes before notification has to be created
     * if th user didn't select any
     * @param context
     * @return
     */
    public static boolean getCreateDefaultNotificationValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getResources().getString(R.string.key_create_default_notification), true);
    }

    /**
     * Get the value of daily notification.
     * If this value is true, one notification has to be sent each day
     * with the task for the day
     * @param context
     * @return
     */
    public static boolean getDailyNotificationValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getResources().getString(R.string.key_trigger_daily_notification), true);
    }

    /**
     * Get the time when the daily notification has to be sent
     * @param context
     * @return
     */
    public static String getDailyTriggerNotificationValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getResources().getString(R.string.key_trigger_daily_time), DEFAULT_DAILY_TIME_VALUE);
    }

    /**
     * Return if the setting file exists
     * @param context
     * @return
     */
    public static boolean existPreferenceFile(Context context) {
        String preferenceFileName = LetMeKnowApp.class.getPackage().getName() + "_preferences.xml";
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(LetMeKnowApp.class.getPackage().getName(), PackageManager.PERMISSION_GRANTED);
            String filePathName = packageInfo.applicationInfo.dataDir + "/shared_prefs/" + preferenceFileName;
            File file = new File(filePathName);
            return file.exists();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Create the preference setting file if it doesn't exist
     * @param context
     */
    public static void createPreferenceFile(Context context) {
        if (!existPreferenceFile(context)) {
            String preferenceFileName = LetMeKnowApp.class.getPackage().getName() + "_preferences";
            SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //Init all settings
            editor.putBoolean(context.getResources().getString(R.string.key_create_default_notification), true); //default notifications
            editor.putBoolean(context.getResources().getString(R.string.key_trigger_daily_notification), true); //daily notification
            editor.putString(context.getResources().getString(R.string.key_trigger_daily_time), DEFAULT_DAILY_TIME_VALUE); //daily notification time
            editor.commit();
        }
    }

    /**
     * Return if a setting exists
     * @param context
     * @return
     */
    public static boolean existSetting(Context context, String name) {
        String preferenceFileName = LetMeKnowApp.class.getPackage().getName() + "_preferences";
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(name)) {
            return true;
        } else {
            return false;
        }
    }

}
