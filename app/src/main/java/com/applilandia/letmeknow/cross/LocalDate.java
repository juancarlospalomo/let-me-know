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

public class LocalDate implements Comparable<LocalDate> {

    private static final String DATE_DATABASE_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm";

    //Format will be yyyy-MM-dd
    private String mDate = null;
    //Format will be HH:mm in 24h format
    private String mTime = null;

    public LocalDate() {
        parse(new Date());
    }

    public LocalDate(Date date) {
        parse(date);
    }

    public LocalDate(String date) throws ParseException {
        parse(date);
    }

    public LocalDate(int year, int month, int day) {
        setDate(year, month, day);
    }

    public LocalDate(int year, int month, int day, int hour, int minute) {
        setDate(year, month, day);
        setTime(hour, minute);
    }

    /**
     * Add a number of days to the current date
     *
     * @param number number of days to add (if it is negative, they will be subtracted)
     */
    public LocalDate addDays(int number) {
        Calendar calendar = Calendar.getInstance();
        Date date = getDate(toString());
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, number);
        setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        return this;
    }

    /**
     * Add a number of hours to the current date
     *
     * @param number number of hours to add (if it is negative, they will be subtracted)
     */
    public LocalDate addHours(int number) {
        if (mTime == null) {
            throw new IllegalStateException("time is empty");
        } else {
            Calendar calendar = Calendar.getInstance();
            Date date = getDate(toString());
            calendar.setTime(date);
            calendar.add(Calendar.HOUR, number);
            setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        }
        return this;
    }

    /**
     * Add a number of minutes to the current date
     *
     * @param number number of minutes to add (if it is negative, they will be subtracted)
     */
    public LocalDate addMinutes(int number) {
        if (mTime == null) {
            throw new IllegalStateException("time is empty");
        } else {
            Calendar calendar = Calendar.getInstance();
            Date date = getDate(toString());
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, number);
            setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        }
        return this;
    }

    public void setDate(int year, int month, int day) {
        mDate = String.format("%s-%s-%s", year, month, day);
    }

    public String getDate() {
        return mDate;
    }

    public void setTime(int hour, int minute) {
        mTime = String.format("%d:%02d", hour, minute);
    }

    public String getTime() {
        return mTime;
    }

    public Date getDateTime() {
        return getDate(toString());
    }

    private void parse(String date) throws ParseException {
        String[] dateTimeParts = date.split(" ");
        if (dateTimeParts.length > 0) {
            String[] dateParts = dateTimeParts[0].split("-");
            if (dateParts.length != 3) {
                throw new ParseException("date format is wrong", 0);
            } else {
                setDate(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]),
                        Integer.parseInt(dateParts[2]));
            }
            if (dateTimeParts.length == 2) {
                String[] timeParts = dateTimeParts[1].split(":");
                if (timeParts.length != 2) {
                    throw new ParseException("time format is wrong", 0);
                } else {
                    setTime(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
                }
            }
        } else {
            throw new ParseException("date-time format is wrong", 0);
        }
    }

    private void parse(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        mDate = simpleDateFormat.format(date);
        simpleDateFormat.applyPattern(TIME_FORMAT);
        mTime = simpleDateFormat.format(date);
    }

    /**
     * Get a Date object from a String date formatted as DATE_DATABASE_FORMAT
     *
     * @param formattedDate date formatted as DATE_DATABASE_FORMAT
     * @return date object
     */
    private Date getDate(String formattedDate) {
        Date date;
        SimpleDateFormat dateFormat;
        if (mTime != null) {
            dateFormat = new SimpleDateFormat(DATE_DATABASE_FORMAT, Locale.getDefault());
        } else {
            dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        }
        try {
            date = dateFormat.parse(formattedDate);
        } catch (NullPointerException | ParseException e) {
            e.printStackTrace(); //TODO
            date = null;
        }
        return date;
    }

    @Override
    public String toString() {
        if (mTime != null) {
            return String.format("%s %s", mDate, mTime);
        } else {
            return mDate;
        }
    }

    @Override
    public int compareTo(LocalDate another) {
        Date thisDate = getDateTime();
        Date other = another.getDate(another.toString());
        return thisDate.compareTo(other);
    }

    @Override
    public boolean equals(Object another) {
        if (another instanceof LocalDate) {
            return ((mDate.equals(((LocalDate) another).mDate)) &&
                    mTime.equals(((LocalDate) another).mTime));
        } else {
            return false;
        }
    }

}







