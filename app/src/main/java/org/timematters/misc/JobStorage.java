package org.timematters.misc;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class provides facilities to save permanently information.
 * This is used to restore a tracking when the application is put
 * in the background or even closed.
 * The implementation relies on a local file
 */
public class JobStorage {

    /**
     * File to be used
     */
    static private final String FILENAME = "pendingJob";

    /**
     * Save the information permanently
     */
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
            fos.close();
            os.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Delete the stored information
     */
    static public void removePendingJob(Context c) {
        c.deleteFile(FILENAME);
    }

    /**
     * Retrieve the stored information
     */
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
