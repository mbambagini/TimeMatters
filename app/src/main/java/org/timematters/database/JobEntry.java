package org.timematters.database;

import android.support.annotation.Nullable;

import java.sql.Date;

/**
 * Database tuple
 */
public class JobEntry implements Comparable<JobEntry> {

    private long id = 0;
    private Date stop = null;
    private long duration = 0;
    private String descr = null;

    /**
     * Retrieve the tuple identifier
     * 
     * @return ID
     */
    public long getId() {
        return id;
    }

    /**
     * Set the tuple identifier
     * 
     * @param i new ID
     */
    public void setId(long i) {
        id = i;
    }

    /**
     * Retrieve when the activity was saved
     *
     * @return the saving date
     */
    public Date getStop() {
        return stop;
    }

    /**
     * Set the date when the activity has been done
     * 
     * @param s string of the date to save (compliant with the DBMS)
     */
    public void setStop(String s) {
        if (s == null)
            stop = null;
        else
            stop = Date.valueOf(s);
    }

    /**
     * Retrieve total elapsed time
     * 
     * @return worked interval
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Set elapsed time
     * 
     * @param i worked interval
     */
    public void setDuration(long i) {
        duration = i;
    }

    /**
     * Retrieve description
     * 
     * @return description
     */
    public String getDescr() {
        return descr;
    }

    /**
     * Assign a note to the current tuple
     * 
     * @param s description
     */
    public void setDescr(String s) {
        if (s == null)
            descr = null;
        else
            descr = s;
    }

    @Override
    public int compareTo(@Nullable JobEntry another) {
        if (another == null)
            return 0;
        return another.stop.compareTo(stop);
    }

    @Override
    public String toString() {
        return descr;
    }

}
