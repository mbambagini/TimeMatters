package org.timematters.misc;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class provides a set of functions related to the
 * management of date
 */
public class DateHandler {

    /**
     * Convert a duration in milliseconds into a string
     */
    static public String GetElapsedTime(long duration) {
        DecimalFormat formatter = new DecimalFormat("00");
        DecimalFormat formatterHours = new DecimalFormat();
        duration = duration / 1000;
        long seconds = duration % 60;
        duration = duration / 60;
        long minutes = duration % 60;
        duration = duration / 60;
        long hours = duration;

        if (hours > 10)
            return formatterHours.format(hours) + ":" + formatter.format(minutes) + ":" + formatter.format(seconds);
        return formatter.format(hours) + ":" + formatter.format(minutes) + ":" + formatter.format(seconds);
    }

    /**
     * Convert a date into a string which can be stored within the
     * internal database
     */
    static public String GetSQLDateFormat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * Convert a date into a string according to the pre-defined format
     */
    static public String GetPreferenceDateFormat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("E dd/MM/yyyy");
        return sdf.format(date);
    }

    /*
    static public String GetTimeFormat (Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        return sdf.format(date);
    }

    static public String GetTimeFormat (int hour, int minute) {
        DecimalFormat formatter = new DecimalFormat("00");
        return formatter.format(hour)+":"+formatter.format(minute);
    }
    */

    /**
     * Return the first day of the actual month
     */
    static public Date GetMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /**
     * Return the actual date
     */
    static public Date GetActualDate() {
        return new Date();
    }

    /**
     * Return the date of the last sunday
     */
    static public Date GetLastSunday() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_WEEK, Calendar.SUNDAY - day);
        return cal.getTime();
    }
}
