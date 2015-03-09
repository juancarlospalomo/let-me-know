package com.applilandia.letmeknow.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.applilandia.letmeknow.cross.LocalDate;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by JuanCarlos on 26/02/2015.
 */
public class DateDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    /**
     * Interface for Events
     */
    public interface OnDateDialogListener {
        public void onOk(LocalDate date);

        public void onCancel();
    }

    //DatePicker Dialog to show
    private DatePickerDialog mDatePickerDialog;
    //Call back handler to manage OK and Cancel buttons
    private OnDateDialogListener mDateDialogListener;
    //Requested date to init the Dialog
    private LocalDate mInitialDate = null;

    /**
     * Allows set the initial selected date for the dialog
     *
     * @param date
     */
    public void setInitialDate(LocalDate date) {
        mInitialDate = date;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        LocalDate date = new LocalDate(year, monthOfYear + 1, dayOfMonth);
        if (mDateDialogListener != null) {
            mDateDialogListener.onOk(date);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Calendar calendar = Calendar.getInstance();
        if (mInitialDate != null) {
            calendar.setTime(mInitialDate.getDateTime());
        }
        mDatePickerDialog = new DatePickerDialog(getActivity(), this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.getDatePicker().setMinDate(new Date().getTime());
        return mDatePickerDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDateDialogListener != null) {
            mDateDialogListener.onCancel();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mDateDialogListener != null) {
            mDateDialogListener.onCancel();
        }
    }

    /**
     * Set the handler object to the caller
     *
     * @param l
     */
    public void setOnDateDialogListener(OnDateDialogListener l) {
        mDateDialogListener = l;
    }
}
