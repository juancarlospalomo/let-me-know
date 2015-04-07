package com.applilandia.letmeknow.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.cross.Settings;
import com.applilandia.letmeknow.data.NotificationSet;

/**
 * Created by JuanCarlos on 26/02/2015.
 */
public class SettingFragment extends PreferenceFragment {

    private final static String LOG_TAG = SettingFragment.class.getSimpleName();

    private CheckBoxPreference mDailyPreference;
    private TimePreference mTimePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Load the preferences from res/xml/preferences.xml
        addPreferencesFromResource(R.xml.preferences);
        //Inflate views
        inflatePreferenceViews();
        //Set the initial state for the preference views
        setInitialState();
        //Init listeners for preferences
        initPreferencesHandler();
    }

    /**
     * Inflate the preferences into the View objects
     */
    private void inflatePreferenceViews() {
        mDailyPreference = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.key_trigger_daily_notification));
        mTimePreference = (TimePreference) getPreferenceManager().findPreference(getString(R.string.key_trigger_daily_time));
    }

    /**
     * Set the initial state for the preference views
     */
    private void setInitialState() {
       if (!Settings.getDailyNotificationValue(this.getActivity())) {
           mTimePreference.setEnabled(false);
       }
    }

    /**
     * Create all needed handlers for preferences
     */
    private void initPreferencesHandler() {
        initDailyPreferenceListener();
        initDailyTimePreferenceListener();
    }

    /**
     * Set the daily preference listener
     */
    private void initDailyPreferenceListener() {
        mDailyPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    mTimePreference.setEnabled(true);
                    createAlarm(null);
                } else {
                    mTimePreference.setEnabled(false);
                    removeAlarm();
                }
                return true;
            }
        });
    }

    /**
     * Set the listener for the time preference in daily alarms
     */
    private void initDailyTimePreferenceListener() {
        mTimePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                removeAlarm();
                createAlarm((String) newValue);
                return true;
            }
        });
    }

    /**
     * Create the alarm for the daily notification
     */
    private void createAlarm(String time) {
        if (TextUtils.isEmpty(time)) {
            time = Settings.getDailyTriggerNotificationValue(getActivity());
        }
        NotificationSet.Alarm alarm = new NotificationSet(getActivity()).new Alarm();
        alarm.create(time);
    }

    /**
     * Remove the alarm for the daily notification
     */
    private void removeAlarm() {
        NotificationSet.Alarm alarm = new NotificationSet(getActivity()).new Alarm();
        alarm.remove(Settings.DAILY_NOTIFICATION_ID);
    }

}
