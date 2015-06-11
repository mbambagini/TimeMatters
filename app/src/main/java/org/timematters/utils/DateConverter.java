package org.timematters.utils;

import android.widget.TextView;

import org.timematters.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {


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
            return new String(formatterHours.format(hours)+":" +formatter.format(minutes) + ":" +
                            formatter.format(seconds));
        return new String(formatter.format(hours)+":" +formatter.format(minutes) + ":" +
                formatter.format(seconds));
    }

    static public String GetSQLDateFormat (Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    static public String GetPreferenceDateFormat (Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("E dd/MM/yyyy");
        return sdf.format(date);
    }

    static public String GetTimeFormat (Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        return sdf.format(date);
    }

    static public String GetTimeFormat (int hour, int minute) {
        DecimalFormat formatter = new DecimalFormat("00");
        return new String(formatter.format(hour)+":"+formatter.format(minute));
    }
}
