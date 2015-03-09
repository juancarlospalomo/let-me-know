package com.applilandia.letmeknow.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by JuanCarlos on 03/03/2015.
 */
public class TimeDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    /**
     * Interface for Events
     */
    public interface OnTimeDialogListener {
        public void onOk(int hour, int minute);

        public void onCancel();
    }

    private TimePickerDialog mTimePickerDialog;
    //Listener
    private OnTimeDialogListener mTimeDialogListener;
    //Initial time to start in picker
    private Integer mInitialHour = null;
    private Integer mInitialMinute = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Calendar calendar = Calendar.getInstance();
        if ((mInitialHour != null) && (mInitialMinute != null)) {
            mTimePickerDialog = new TimePickerDialog(getActivity(), this,
                    mInitialHour.intValue(),
                    mInitialMinute.intValue(),
                    android.text.format.DateFormat.is24HourFormat(getActivity()));
        } else {
            mTimePickerDialog = new TimePickerDialog(getActivity(), this,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    android.text.format.DateFormat.is24HourFormat(getActivity()));
        }
        return mTimePickerDialog;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (mTimeDialogListener != null) {
            mTimeDialogListener.onOk(hourOfDay, minute);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mTimeDialogListener != null) {
            mTimeDialogListener.onCancel();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mTimeDialogListener != null) {
            mTimeDialogListener.onCancel();
        }
    }

    /**
     * Set the initial time
     * @param hour
     * @param minute
     */
    public void setInitialTime(int hour, int minute) {
        mInitialHour = hour;
        mInitialMinute = minute;
    }

    /**
     * Set the handler object to the caller
     *
     * @param l
     */
    public void setOnDateDialogListener(OnTimeDialogListener l) {
        mTimeDialogListener = l;
    }

}
