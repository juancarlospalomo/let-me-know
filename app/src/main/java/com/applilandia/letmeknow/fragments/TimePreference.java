package com.applilandia.letmeknow.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by JuanCarlos on 26/02/2015.
 * Class for Time Preference
 */
public class TimePreference extends DialogPreference {

    private OnPreferenceChangeListener mOnPreferenceChangeListener;

    private String mDefaultValue;
    private String mCurrentValue;
    private TimePicker mTimePicker;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Get default value stored in defaultValue attribute
     * @param a array with attributes
     * @param index index of defaultValue attribute
     * @return
     */
    @Override
    protected String onGetDefaultValue(TypedArray a, int index) {
        mDefaultValue = a.getString(index);
        return mDefaultValue;
    }

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        mOnPreferenceChangeListener = onPreferenceChangeListener;
    }

    /**
     * Called when the TimePreference is added to the screen
     * @param restorePersistedValue indicates if defaultValue has been already persisted
     * @param defaultValue default value to use if the preference is not persisted yet
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mCurrentValue = getPersistedString(mDefaultValue);
        } else {
            mCurrentValue = (String) defaultValue;
            persistString(mCurrentValue);
        }
    }

    /**
     * When any of the button of the DialogPreference is clicked on
     * @param dialog
     * @param which: -2: Cancel; -1: OK
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which==-1) {
            //We only change the preference if the user click on OK button
            Integer hour = mTimePicker.getCurrentHour();
            Integer minutes = mTimePicker.getCurrentMinute();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour.intValue());
            calendar.set(Calendar.MINUTE, minutes.intValue());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            mCurrentValue = simpleDateFormat.format(calendar.getTime());
            if (mOnPreferenceChangeListener!=null) {
                if (mOnPreferenceChangeListener.onPreferenceChange(this, mCurrentValue)) {
                    persistString(mCurrentValue);
                }
            }

        }
    }

    /**
     * Called to get the view to show when the preference is clicked on the preference screen
     * @return TimePicker
     */
    @Override
    protected View onCreateDialogView() {
        mTimePicker = new TimePicker(getContext());
        if (!TextUtils.isEmpty(mCurrentValue)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            try {
                Date date = simpleDateFormat.parse(mCurrentValue);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
                Integer minute = calendar.get(Calendar.MINUTE);
                mTimePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
                mTimePicker.setCurrentHour(hour.intValue());
                mTimePicker.setCurrentMinute(minute.intValue());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return mTimePicker;
    }
}
