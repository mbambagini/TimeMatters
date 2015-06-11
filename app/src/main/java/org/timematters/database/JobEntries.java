package org.timematters.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.timematters.utils.DateConverter;
import org.timematters.exceptions.JobNotCreated;
import org.timematters.exceptions.JobNotFound;

public class JobEntries {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    private String[] allJobColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_STOP,
            DBHelper.COLUMN_DURATION,
            DBHelper.COLUMN_DESCR
    };

    public JobEntries (Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void deleteJob (JobEntry job) throws JobNotFound {
        int i = db.delete(DBHelper.TABLE_JOBS, DBHelper.COLUMN_ID + " = " + job.getId() + "", null);
        if (i!=1)
            throw new JobNotFound();
    }

    public List<JobEntry> getJobs(Date start, Date stop) {
        String orderByClause = new String (DBHelper.COLUMN_STOP+" DESC");
        String whereClause = new String (DBHelper.COLUMN_STOP + "<= \'" +
                                                       DateConverter.GetSQLDateFormat(stop) + "\'");
        if (start!=null)
            whereClause += new String (" AND " + DBHelper.COLUMN_STOP + ">= \'" +
                                                      DateConverter.GetSQLDateFormat(start) + "\'");
        Cursor cursor = db.query(DBHelper.TABLE_JOBS, allJobColumns, whereClause, null, null, null,
                                                                                     orderByClause);
        if (cursor.getCount()==0)
            return null;
        cursor.moveToFirst();
        List<JobEntry> jobs = new ArrayList<JobEntry>();
        while (!cursor.isAfterLast()) {
            JobEntry job = cursorToJob(cursor);
            jobs.add(job);
            cursor.moveToNext();
        }
        Collections.sort(jobs);
        cursor.close();
        return jobs;
    }

    public JobEntry getJob (long id) throws JobNotFound {
        String whereClause = new String (DBHelper.COLUMN_ID +"=" + id);
        Cursor cursor = db.query(DBHelper.TABLE_JOBS, allJobColumns,  whereClause,
                                                                            null, null, null, null);
        if (cursor.getCount()==0)
            return null;
        cursor.moveToFirst();
        JobEntry job = cursorToJob(cursor);
        return job;
    }

    public void addJob (JobEntry job) throws JobNotCreated {
        ContentValues values = new ContentValues();
//        values.put(DBHelper.COLUMN_ID, job.getId());
        values.put(DBHelper.COLUMN_STOP, job.getStop().toString());
        values.put(DBHelper.COLUMN_DURATION, job.getDuration());
        values.put(DBHelper.COLUMN_DESCR, job.getDescr());
        System.out.println("ADDED: "+job.getStop().toString()+" - "+job.getDuration());
        try {
            db.insert(DBHelper.TABLE_JOBS, null, values);
        } catch (Exception e) {
            throw new JobNotCreated();
        }
    }

    public void restoreJob (JobEntry job) throws JobNotCreated {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_ID, job.getId());
        values.put(DBHelper.COLUMN_STOP, job.getStop().toString());
        values.put(DBHelper.COLUMN_DURATION, job.getDuration());
        values.put(DBHelper.COLUMN_DESCR, job.getDescr());
        try {
            db.insert(DBHelper.TABLE_JOBS, null, values);
        } catch (Exception e) {
            throw new JobNotCreated();
        }
    }

    private JobEntry cursorToJob (Cursor cursor) {
        JobEntry job = new JobEntry ();

        job.setId(cursor.getLong(0));
        job.setStop(cursor.getString(1));
        job.setDuration(cursor.getLong(2));
        job.setDescr(cursor.getString(3));
        return job;
    }
}
