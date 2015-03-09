package com.applilandia.letmeknow.cross;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.applilandia.letmeknow.R;

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

    private enum TypeDay {
        Yesterday,
        Today,
        Tomorrow,
        WithinWeek,
        WithinYear,
        OutOfYear
    }

    //Format will be yyyy-MM-dd
    private String mDate = null;
    //Format will be HH:mm in 24h format
    private String mTime = null;

    public LocalDate() {
        parse(new Date());
    }

    public LocalDate(LocalDate date) {
        mDate = date.mDate;
        mTime = date.mTime;
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
        mDate = String.format("%d-%02d-%02d", year, month, day);
    }

    public String getDate() {
        return mDate;
    }

    public void setTime(int hour, int minute) {
        mTime = String.format("%02d:%02d", hour, minute);
    }

    /**
     * Return the hour of day
     * @return hour
     */
    public int getHour() {
        if (!isTimeNull()) {
            String[] values = mTime.split(":");
            return Integer.parseInt(values[0]);
        }
        throw new RuntimeException("Time is not well formatted");
    }

    /**
     * Return the minute part
     * @return minutes of one hour
     */
    public int getMinute() {
        if (!isTimeNull()) {
            String[] values = mTime.split(":");
            return Integer.parseInt(values[1]);
        }
        throw new RuntimeException("Time is not well formatted");
    }

    public String getTime() {
        return mTime;
    }

    public Date getDateTime() {
        return getDate(toString());
    }

    private void parse(String date) throws ParseException {
        if (!TextUtils.isEmpty(date)) {
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

    /**
     * Remove the time from the date, setting time to null
     */
    public void removeTime() {
        mTime = null;
    }

    /**
     * Return if date & time parts are both null
     *
     * @return true or false
     */
    public boolean isNull() {
        return ((mDate == null) && (mTime == null));
    }

    /**
     * Return if time parts is null
     *
     * @return true or false
     */
    public boolean isTimeNull() {
        return (mTime == null);
    }

    /**
     * It calculates which period is the date inside
     *
     * @return TypeDay
     */
    private TypeDay when() {
        TypeDay result;

        if (mDate != null) {
            Calendar calendar = Calendar.getInstance();
            Date date = getDate(toString());
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            date = new Date();
            calendar.setTime(date);
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

            if ((year == currentYear) && (month == currentMonth) && (day == currentDay)) {
                result = TypeDay.Today;
            } else {
                if ((year == currentYear) && (month == currentMonth) && (day == currentDay - 1)) {
                    result = TypeDay.Yesterday;
                } else {
                    if ((year == currentYear) && (month == currentMonth) && (day == currentDay + 1)) {
                        result = TypeDay.Tomorrow;
                    } else {
                        if (year == currentYear) {
                            int firstDayOfWeek = calendar.getFirstDayOfWeek();
                            int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                            int firstDayOfWeekInTheMonth = currentDay - (currentDayOfWeek - firstDayOfWeek);
                            if ((firstDayOfWeekInTheMonth <= day) && (day <= firstDayOfWeekInTheMonth + 7)) {
                                result = TypeDay.WithinWeek;
                            } else {
                                result = TypeDay.WithinYear;
                            }
                        } else {
                            result = TypeDay.OutOfYear;
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("Date is null");
        }
        return result;
    }

    /**
     * Returns if the date is today
     *
     * @return
     */
    public boolean isToday() {
        return (when() == TypeDay.Today);
    }

    /**
     * Returns the week day name
     *
     * @return String with the name
     */
    private String getDisplayWeekDay(boolean shortFormat) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDateTime());
        if (shortFormat) {
            return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
        } else {
            return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        }
    }

    /**
     * Returns the month name for the date
     *
     * @return String with the Short month name
     */
    private String getDisplayMedium(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDateTime());
        String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        String time = DateFormat.getTimeFormat(context).format(getDateTime());
        if (isTimeNull()) time = "";
        if (Locale.getDefault().toString() == "en_US") {
            return String.format("%s, %d %s", monthName, calendar.get(Calendar.DAY_OF_MONTH), time);
        } else {
            return String.format("%s %d %s", monthName, calendar.get(Calendar.DAY_OF_MONTH), time);
        }
    }

    /**
     * Generates the date in a display format based on Material Design date formats
     *
     * @param context
     * @return the string with the formatted date
     */
    public String getDisplayFormat(Context context) {
        TypeDay typeDay = when();
        String result = "";

        if (typeDay == TypeDay.Today) {
            //Don't show the day, only time
            if (!isTimeNull()) {
                result = DateFormat.getTimeFormat(context).format(getDateTime());
            }
            return result;
        }
        if (typeDay == TypeDay.Tomorrow) {
            //Show tomorrow label and time if it has got
            result = context.getResources().getString(R.string.text_tomorrow);
            if (!isTimeNull()) {
                result += " " + DateFormat.getTimeFormat(context).format(getDateTime());
            }
            return result;
        }
        if (typeDay == TypeDay.Yesterday) {
            //Show yesterday label and time if it has got
            result = context.getResources().getString(R.string.text_yesterday);
            if (!isTimeNull()) {
                result += " " + DateFormat.getTimeFormat(context).format(getDateTime());
            }
            return result;
        }
        if (typeDay == TypeDay.WithinWeek) {
            if (!isTimeNull()) {
                result += getDisplayWeekDay(true) + " " + DateFormat.getTimeFormat(context).format(getDateTime());
            } else {
                result += getDisplayWeekDay(false);
            }
            return result;
        }
        if (typeDay == TypeDay.WithinYear) {
            //Show date in medium format removing the year
            return getDisplayMedium(context);
        }
        if (typeDay == TypeDay.OutOfYear) {
            java.text.DateFormat formatter;
            if (!isTimeNull()) {
                formatter = SimpleDateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,
                        java.text.DateFormat.SHORT, Locale.getDefault());
            } else {
                formatter = SimpleDateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault());
            }
            return formatter.format(getDateTime());
        }

        return result;
    }

    /**
     * Format a date as MEDIUM in the default locale
     * @return String containing the date formatted
     */
    public String getDisplayFormatDate() {
        java.text.DateFormat formatter = SimpleDateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault());
        return formatter.format(getDateTime());
    }

    public String getDisplayFormatTime(Context context) {
        return DateFormat.getTimeFormat(context).format(getDateTime());
    }

    /**
     * Convert to String format the date & time
     *
     * @return
     */
    @Override
    public String toString() {
        if (mTime != null) {
            return String.format("%s %s", mDate, mTime);
        } else {
            if (mDate != null) {
                return mDate;
            } else {
                return null;
            }
        }
    }


    /**
     * Compare the receiver to the specified {Date} to determine the relative
     * ordering.
     *
     * @param another a {@code Date} to compare against.
     * @return an {@code int < 0} if this {Date} is less than the specified {param Date}, {@code 0} if
     * they are equal, and an {@code int > 0} if this {Date} is greater.
     */
    @Override
    public int compareTo(LocalDate another) {
        Date thisDate = getDateTime();
        if (mTime == null) {
            another.mTime = null;
        }
        Date other = another.getDate(another.toString());
        return thisDate.compareTo(other);
    }

    /**
     * Is the local date equals to another one?
     *
     * @param another
     * @return true or false
     */
    @Override
    public boolean equals(Object another) {
        if (another instanceof LocalDate) {
            LocalDate other = (LocalDate) another;
            //Both Dates null means equals
            //Date null and other!=null, different
            //Date!=null and other==null, different
            //Date!=null and other!=null, check dates and if they are equal then check times
            boolean bothAreNull = ((mDate == null) && (other.mDate == null));
            if (bothAreNull) {
                return true;
            } else {
                boolean someIsNull = ((mDate == null) || (other.mDate == null));
                if (someIsNull) {
                    return false;
                } else {
                    if (mDate.equals(other.mDate)) {
                        bothAreNull = ((mTime == null) && (other.mTime == null));
                        if (bothAreNull) {
                            return true;
                        } else {
                            someIsNull = ((mTime == null) || (other.mTime == null));
                            if (someIsNull) {
                                return false;
                            } else {
                                if (mTime.equals((other.mTime))) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
    }

}







