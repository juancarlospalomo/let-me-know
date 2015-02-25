package com.applilandia.letmeknow;

import android.content.Context;
import android.test.AndroidTestCase;
import android.text.format.DateFormat;

import com.applilandia.letmeknow.cross.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by JuanCarlos on 23/02/2015.
 */
public class testLocalDate extends AndroidTestCase {

    public void testShouldCreateNewDate() {
        Date today = new Date();
        LocalDate date = new LocalDate(today);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String dbFormatDate = simpleDateFormat.format(today);

        assertEquals(dbFormatDate, date.toString());
    }

    public void testCheckCases() {
        String today = "2015-02-23";
        try {
            LocalDate date = new LocalDate(today);
            assertEquals(today, date.toString());

            today = "2015-02-27";
            date.setDate(2015, 2, 27);
            assertEquals(today, date.toString());

            date.setTime(10, 3);
            assertNotSame(today, date.toString());

            today = "2015-02-27 10:03";
            assertEquals(today, date.toString());

            LocalDate other = new LocalDate(today);
            assertEquals(0, date.compareTo(other));

            other = new LocalDate("2015-02-23 10:30");
            date = new LocalDate("2015-02-24 10:30");
            assertEquals(1, date.compareTo(other));

            other = new LocalDate(2015, 2, 28);
            date = new LocalDate(2015, 3, 1);
            assertEquals(1, date.compareTo(other));

            date.addDays(-1);
            assertEquals(0, date.compareTo(other));

            date.setTime(11, 5);
            assertEquals(1, date.compareTo(other));

            date = new LocalDate(2015, 2, 23, 13, 15);
            date.addMinutes(-5);
            assertEquals(0, "2015-02-23 13:10".compareTo(date.toString()));
            date.setTime(13, 15);

            date.addHours(-1);
            assertEquals(0, "2015-02-23 12:15".compareTo(date.toString()));
            date.setTime(13, 15);

            date.addDays(-1);
            assertEquals(0, "2015-02-22 13:15".compareTo(date.toString()));
            date.setDate(2015, 2, 23);
            date.setTime(13, 15);

            date.addDays(-7);
            assertEquals(0, "2015-02-16 13:15".compareTo(date.toString()));


        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getTimeString(LocalDate date) {
        SimpleDateFormat simpleDateFormat;
        String expectedResult;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date.getDateTime());

        if (DateFormat.is24HourFormat(mContext)) {
            simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        } else {
            if (calendar.get(Calendar.AM_PM)==1) {
                //PM
                simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            } else {
                //AM
                simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            }
        }
        expectedResult = simpleDateFormat.format(date.getDateTime());
        return expectedResult;
    }

    private String getDisplayWeekDay(LocalDate date, boolean shortFormat) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date.getDateTime());
        if (shortFormat) {
            return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
        } else {
            return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        }
    }

    private String getDisplayMedium(Context context, LocalDate date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date.getDateTime());
        String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        String time = DateFormat.getTimeFormat(context).format(date.getDateTime());
        if (date.isTimeNull()) time = "";
        if (Locale.getDefault().toString() == "en_US") {
            return String.format("%s, %d %s", monthName, calendar.get(Calendar.DAY_OF_MONTH), time);
        } else {
            return String.format("%s %d %s", monthName, calendar.get(Calendar.DAY_OF_MONTH), time);
        }
    }

    public void testDisplayFormat() {
        String expectedResult = "";
        LocalDate date = new LocalDate();

        //Change Locale
        Locale.setDefault(Locale.US);

        //Today with time: It must show only time
        expectedResult = getTimeString(date);
        assertEquals(expectedResult, date.getDisplayFormat(mContext));

        //Today without time: It must be empty
        date.removeTime();
        assertEquals("", date.getDisplayFormat(mContext));

        //Yesterday with time: It must return "Yesterday Time"
        date = new LocalDate();
        date.addDays(-1);
        expectedResult = mContext.getResources().getString(R.string.text_yesterday) + " " + getTimeString(date);
        assertEquals(expectedResult, date.getDisplayFormat(mContext));

        //Yesterday without time: It must returns "Yesterday"
        date.removeTime();
        expectedResult = mContext.getResources().getString(R.string.text_yesterday);
        assertEquals(expectedResult, date.getDisplayFormat(mContext));

        //Tomorrow with time: It must return "Tomorrow Time"
        date = new LocalDate();
        date.addDays(1);
        expectedResult = mContext.getResources().getString(R.string.text_tomorrow) + " " + getTimeString(date);
        assertEquals(expectedResult, date.getDisplayFormat(mContext));

        //Tomorrow without time: It must returns "Yesterday"
        date.removeTime();
        expectedResult = mContext.getResources().getString(R.string.text_tomorrow);
        assertEquals(expectedResult, date.getDisplayFormat(mContext));

        //Within current week: It must returns "Weekday Time"
        date = new LocalDate();
        date.addDays(2);
        expectedResult = getDisplayWeekDay(date, true) + " " + getTimeString(date);
        assertEquals(expectedResult, date.getDisplayFormat(mContext));

        //Within current week: It must returns "Weekday"
        date = new LocalDate();
        date.addDays(2);
        date.removeTime();
        expectedResult = getDisplayWeekDay(date, false);
        assertEquals(expectedResult, date.getDisplayFormat(mContext));

        //Within current year with time: It must not show the year
        date = new LocalDate();
        date.addDays(8);
        expectedResult = getDisplayMedium(mContext, date);
        assertEquals(expectedResult, date.getDisplayFormat(mContext));

        //Within current year without time
        date = new LocalDate();
        date.addDays(8);
        date.removeTime();
        expectedResult = getDisplayMedium(mContext, date);
        assertEquals(expectedResult, date.getDisplayFormat(mContext));

        //Out of the current year with time
        date = new LocalDate();
        date.addDays(366);
        java.text.DateFormat formatter = SimpleDateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,
                java.text.DateFormat.SHORT, Locale.getDefault());
        expectedResult = formatter.format(date.getDateTime());
        assertEquals(expectedResult, date.getDisplayFormat(mContext));

        //Out of the current year without time
        date = new LocalDate();
        date.addDays(366);
        date.removeTime();
        formatter = SimpleDateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault());
        expectedResult = formatter.format(date.getDateTime());
        assertEquals(expectedResult, date.getDisplayFormat(mContext));
    }

}
