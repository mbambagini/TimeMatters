package org.timematters.utils;

import java.util.Calendar;
import java.util.Date;

public class DateRetriever {

    static public Date GetActualDate () {
        return new Date();
    }

    static public Date GetLastSunday () {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_WEEK, Calendar.SUNDAY - day);
        return cal.getTime();
    }

    /*
    static public Date GetLastWeekSunday () {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_WEEK, Calendar.SUNDAY - day - 7);
        return cal.getTime();
    }
    */
    static public Date GetMonthStart () {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /*
    static public Date GetLastMonthStart () {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.MONTH);
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }
    */
}
