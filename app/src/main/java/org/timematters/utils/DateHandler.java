package org.timematters.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateHandler {

    static public String GetElapsedTime (long duration) {
        DecimalFormat formatter = new DecimalFormat("00");
        DecimalFormat formatterHours = new DecimalFormat("0000");
        duration = duration/ 1000;
        long seconds = duration % 60;
        duration = duration / 60;
        long minutes = duration % 60;
        duration = duration / 60;
        long hours = duration;

        if (hours>99)
            return formatterHours.format(hours)+":" +formatter.format(minutes) + ":" + formatter.format(seconds);
        return formatter.format(hours)+":" +formatter.format(minutes) + ":" + formatter.format(seconds);
    }

    static public String GetSQLDateFormat (Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    static public String GetPreferenceDateFormat (Date date) {
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

    static public Date GetMonthStart () {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    static public Date GetActualDate () {
        return new Date();
    }

    static public Date GetLastSunday () {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_WEEK, Calendar.SUNDAY - day);
        return cal.getTime();
    }
}
