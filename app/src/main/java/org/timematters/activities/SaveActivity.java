package org.timematters.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.timematters.R;
import org.timematters.database.JobEntries;
import org.timematters.database.JobEntry;
import org.timematters.exceptions.JobNotCreated;
import org.timematters.utils.DateConverter;

import java.util.Date;

public class SaveActivity extends ActionBarActivity {

    private String external_date;
    private String internal_date;
    private long duration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        TextView txt_when = (TextView)findViewById(R.id.txt_summary_day);
        TextView txt_duration = (TextView)findViewById(R.id.txt_summary_elapsed_time);

        Date actual_date = new Date();
        duration = getIntent().getExtras().getLong(getString(R.string.elapsed_time_id));
        external_date = DateConverter.GetPreferenceDateFormat(actual_date);
        internal_date = DateConverter.GetSQLDateFormat(actual_date);
        txt_when.setText(external_date);
        txt_duration.setText(DateConverter.GetElapsedTime(duration));
    }

    public void onClickUpperButtons (View view) {
        JobEntry job;
        boolean created = true;
        switch (view.getId()) {
            case R.id.btn_save:
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
                break;
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
