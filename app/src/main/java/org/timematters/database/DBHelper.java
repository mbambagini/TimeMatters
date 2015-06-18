package org.timematters.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database handler
 * The database contains only a table called "Jobs" whose columns are
 * _id: auto-incremental variable used as primary key
 * _stop: the day the activity is computed
 * _duration: elapsed time associated to the activity
 * _descr: notes typed by the user
 */
public class DBHelper extends SQLiteOpenHelper {

    //databases
    public static final String TABLE_JOBS = "Jobs";
    //columns
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_STOP = "_stop";
    public static final String COLUMN_DURATION = "_duration";
    public static final String COLUMN_DESCR = "_descr";
    //creation queries
    private static final String DATABASE_CREATE_JOBS = "create table "
            + TABLE_JOBS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_STOP + " date,"
            + COLUMN_DURATION + " integer,"
            + COLUMN_DESCR + " text);";
    //version
    private static final int DATABASE_VERSION = 1;
    //file
    private static final String DATABASE_NAME = "jobs.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_JOBS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOBS);
        onCreate(db);
    }
}
