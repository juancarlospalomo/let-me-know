package com.applilandia.letmeknow.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CalendarView;
import android.widget.DatePicker;

import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.cross.LocalDate;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by JuanCarlos on 26/02/2015.
 */
public class DateDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private final static String LOG_TAG = DateDialogFragment.class.getSimpleName();

    /**
     * Interface for Events
     */
    public interface OnDateDialogListener {
        public void onOk(LocalDate date);

        public void onCancel();
    }

    //Call back handler to manage OK and Cancel buttons
    private OnDateDialogListener mDateDialogListener;
    //Requested date to init the Dialog
    private LocalDate mInitialDate = null;
    //Selected date in the view
    private LocalDate mSelectedDate = null;
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
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            return createDatePicker();
        } else {
            return createCalendarView();
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
     * Create a Date Picker Dialog
     * @return DatePickerDialog
     */
    private DatePickerDialog createDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (mInitialDate != null) {
            calendar.setTime(mInitialDate.getDateTime());
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(new Date().getTime());
        return datePickerDialog;
    }

    /**
     * Create a Calendar View as the Dialog
     * @return
     */
    private AlertDialog createCalendarView() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_calendar, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        CalendarView calendarView = (CalendarView) view.findViewById(R.id.calendarDate);
        Calendar calendar = Calendar.getInstance();
        mSelectedDate = new LocalDate(calendar.getTime());
        mSelectedDate.removeTime();
        if (mInitialDate != null) {
            mSelectedDate = mInitialDate;
            calendar.setTime(mInitialDate.getDateTime());
            calendarView.setDate(calendar.getTime().getTime());
        }
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
               mSelectedDate = new LocalDate(year, month + 1, dayOfMonth);
            }
        });
        builder.setNegativeButton(R.string.calendar_view_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDateDialogListener != null) {
                    mDateDialogListener.onCancel();
                }
            }
        });
        builder.setPositiveButton(R.string.calendar_view_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDateDialogListener != null) {
                    mDateDialogListener.onOk(mSelectedDate);
                }
            }
        });

        return builder.create();
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
