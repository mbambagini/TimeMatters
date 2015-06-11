package org.timematters.database;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Created by mario on 02/02/15.
 */
public class JobEntry implements Comparable<JobEntry>  {

    private long id = 0;
    private Date stop = null;
    private long duration = 0;
    private String descr = null;

    public long getId () {
        return id;
    }

    public Date getStop () {
        return stop;
    }

    public long getDuration () {
        return duration;
    }

    public String getDescr () {
        return descr;
    }

    public void setId (long i) {
        id = i;
    }

    public void setStop (String s) {
        if (s==null)
            stop = null;
        else
            stop = Date.valueOf(s);
    }

    public void setDuration (long i) {
        duration = i;
    }

    public void setDescr (String s) {
        if (s==null)
            descr = null;
        else
            descr = new String(s);
    }

    @Override
    public int compareTo(JobEntry another) {
        return 0;
    }

    @Override
    public String toString() {
        return descr;
    }

}
