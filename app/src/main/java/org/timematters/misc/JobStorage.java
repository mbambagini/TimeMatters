package org.timematters.misc;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class JobStorage {

    static private String FILENAME = "pendingJob";

    static public boolean setPendingJob (Context c, long duration, long actual_time) {
        FileOutputStream fos;
        DataOutputStream os;

        try {
            fos = c.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            if (fos==null)
                return false;
            os = new DataOutputStream(fos);
            os.writeLong(duration);
            os.writeLong(actual_time);
            System.out.println("WRITE: " + actual_time + " " + duration);
            fos.close();
            os.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    static public void removePendingJob(Context c) {
        System.out.println("DELETE: ");
        c.deleteFile(FILENAME);
    }

    static public long getPendingJob (Context c, long actual_time, long def) {
        FileInputStream fos;
        DataInputStream is;
        long stop_time;
        long duration;

        try {
            fos = c.openFileInput(FILENAME);
            if (fos==null)
                return def;
            is = new DataInputStream(fos);
            duration = is.readLong();
            stop_time = is.readLong();
            is.close();
            fos.close();
        } catch (FileNotFoundException e) {
            return def;
        } catch (IOException e) {
            return def;
        } catch (NumberFormatException e) {
            return def;
        }
        System.out.println("DURATION "+duration + " start "+stop_time+ " act "+actual_time);
        return (actual_time-stop_time)+duration;
    }

}
