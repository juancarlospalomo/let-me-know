package com.applilandia.letmeknow.cross;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by JuanCarlos on 18/02/2015.
 * Class to manage the different dates format
 */
public final class Dates {

    public static final String DATE_DATABASE_FORMAT = "yyyy-MM-dd HH:mm";

    /**
     * Get a Date object from a String date formatted as DATE_DATABASE_FORMAT
     * @param formattedDate date formatted as DATE_DATABASE_FORMAT
     * @return date object
     */
    public static Date getDate(String formattedDate) {
        Date date;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_DATABASE_FORMAT,
                Locale.getDefault());
        try {
            date = dateFormat.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace(); //TODO
            date = null;
        }
        return date;
    }

    /**
     * Get the current date & time
     * @return The date got in a DATE_DATABASE_FORMAT string
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                DATE_DATABASE_FORMAT, Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Add a number of days to a given date
     * @param date date
     * @param number number of days to add
     * @return date object
     */
    public static Date addDays(Date date, int number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, number);
        return calendar.getTime();
    }

    /**
     * Convert a date object to a string in format DATE_DATABASE_FORMAT
     * @param date date to cast
     * @return the formatted string date
     */
    public static String castToDatabaseFormat(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_DATABASE_FORMAT, Locale.getDefault());
        return simpleDateFormat.format(date);
    }

}
