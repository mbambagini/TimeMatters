package org.timematters.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.timematters.R;
import org.timematters.database.JobEntries;
import org.timematters.database.JobEntry;
import org.timematters.exceptions.JobNotCreated;
import org.timematters.misc.DateHandler;

import java.util.Date;

/*!
 * this Activity shows the result of the actual tracking and lets the user
 * insert additional information before permanently storing it
 */
public class SaveActivity extends ActionBarActivity {

    private String internal_date;
    private long duration = 0;

    /*!
     * read input and show it
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        TextView txt_when = (TextView)findViewById(R.id.txt_summary_day);
        TextView txt_duration = (TextView)findViewById(R.id.txt_summary_elapsed_time);

        Date actual_date = new Date();
        duration = getIntent().getExtras().getLong(getString(R.string.elapsed_time_id));
        internal_date = DateHandler.GetSQLDateFormat(actual_date);
        txt_when.setText(DateHandler.GetPreferenceDateFormat(actual_date));
        txt_duration.setText(DateHandler.GetElapsedTime(duration));
    }

    /*!
     * store information
     */
    public void onClickUpperButtons (View view) {
        JobEntry job;
        boolean created = true;

        if (view.getId()==R.id.btn_save) {
            job = new JobEntry();
            job.setDuration(duration);
            job.setStop(internal_date);
            job.setDescr(((TextView) findViewById(R.id.txt_summary_note)).getText().toString());
            JobEntries handler = new JobEntries(getApplicationContext());
            handler.open();
            try {
                handler.addJob(job);
            } catch (JobNotCreated e) {
                created = false;
            }
            if (created) {
                Toast.makeText(this, getString(R.string.tst_event_created), Toast.LENGTH_LONG).show();
                handler.close();
                finish();
            } else {
                Toast.makeText(this, getString(R.string.tst_event_not_created), Toast.LENGTH_LONG).show();
                handler.close();
            }
        }
    }

}
