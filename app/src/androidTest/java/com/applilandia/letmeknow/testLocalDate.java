package com.applilandia.letmeknow;

import android.test.AndroidTestCase;

import com.applilandia.letmeknow.cross.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        String today = "2015-2-23";
        try {
            LocalDate date = new LocalDate(today);
            assertEquals(today, date.toString());

            today = "2015-2-27";
            date.setDate(2015, 2, 27);
            assertEquals(today, date.toString());

            date.setTime(10, 3);
            assertNotSame(today, date.toString());

            today = "2015-2-27 10:03";
            assertEquals(today, date.toString());

            LocalDate other = new LocalDate(today);
            assertEquals(0, date.compareTo(other));

            other = new LocalDate("2015-2-23 10:30");
            date = new LocalDate("2015-2-24 10:30");
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
            assertEquals(0, "2015-2-23 13:10".compareTo(date.toString()));
            date.setTime(13, 15);

            date.addHours(-1);
            assertEquals(0, "2015-2-23 12:15".compareTo(date.toString()));
            date.setTime(13, 15);

            date.addDays(-1);
            assertEquals(0, "2015-2-22 13:15".compareTo(date.toString()));
            date.setDate(2015, 2, 23);
            date.setTime(13, 15);

            date.addDays(-7);
            assertEquals(0, "2015-2-16 13:15".compareTo(date.toString()));


        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
