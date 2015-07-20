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
     * This inner class is used to pass information from the upper
     * class to other objects which exploit it.
     * More precisely, the stored information are related to the
     * pending activity (if present).
     */
    static public class StoredJobInfo {
        private long duration;
        private boolean run_or_pause;
        private boolean valid;

        public StoredJobInfo () {
            valid = false;
            run_or_pause = false;
            duration = 0;
        }

        public StoredJobInfo (long d, boolean rop) {
            duration = d;
            valid = true;
            run_or_pause = rop;
        }

        public boolean isValid () {
            return valid;
        }

        public long getDuration () {
            return duration;
        }

        public boolean getRun_or_pause () {
            return run_or_pause;
        }
    }

    /**
     * File to be used
     */
    static private final String FILENAME = "pendingJob";

    /**
     * Save the information permanently
     *
     * @param c Context
     * @param duration total elapsed time
     * @param actual_time actual time in milliseconds
     * @param run_or_pause TRUE if the tracking is running, FALSE if it is paused
     * @return TRUE if there is no error, FALSE otherwise
     */
    static public boolean setPendingJob(Context c, long duration, long actual_time, boolean run_or_pause) {
        FileOutputStream fos;
        DataOutputStream os;

        try {
            fos = c.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            if (fos == null)
                return false;
            os = new DataOutputStream(fos);
            os.writeLong(duration);
            os.writeLong(actual_time);
            os.writeBoolean(run_or_pause);
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
     * 
     * @param c context
     */
    static public void removePendingJob(Context c) {
        c.deleteFile(FILENAME);
    }

    /**
     * Retrieve the stored information
     * 
     * @param c context
     * @param actual_time current time used to update the timer
     * @return an object with all the retrieved information
     */
    static public StoredJobInfo getPendingJob(Context c, long actual_time) {
        FileInputStream fos;
        DataInputStream is;
        long stop_time;
        long duration;
        boolean run_or_pause;

        try {
            fos = c.openFileInput(FILENAME);
            if (fos == null)
                return new StoredJobInfo();
            is = new DataInputStream(fos);
            duration = is.readLong();
            stop_time = is.readLong();
            run_or_pause = is.readBoolean();
            is.close();
            fos.close();
        } catch (FileNotFoundException e) {
            return new StoredJobInfo();
        } catch (IOException e) {
            return new StoredJobInfo();
        } catch (NumberFormatException e) {
            return new StoredJobInfo();
        }
        if (run_or_pause)
            return new StoredJobInfo( (actual_time - stop_time) + duration, true);
        return new StoredJobInfo(duration, false);
    }

}
