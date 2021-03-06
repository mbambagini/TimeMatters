package org.timematters.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import org.timematters.R;
import org.timematters.database.JobEntries;
import org.timematters.database.JobEntry;
import org.timematters.exceptions.JobNotCreated;
import org.timematters.misc.DateHandler;

import java.util.Calendar;
import java.util.Date;

/**
 * This Activity lets the user insert manually an activity
 */
public class NewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        TimePicker timePicker = (TimePicker) findViewById(R.id.timePickerTotalTime);
        if (timePicker != null) {
            timePicker.setIs24HourView(true);
            timePicker.setCurrentHour(8);
            timePicker.setCurrentMinute(0);
        }
        DatePicker datePicker = (DatePicker) findViewById(R.id.datePickerNewActivity);
        if (datePicker != null)
            datePicker.setMaxDate(System.currentTimeMillis());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if ((id == R.id.action_save_new) && saveActivity())
            finish();

        return super.onOptionsItemSelected(item);
    }

    public void onClickUpperButtons(View v) {
        if ((v.getId() == R.id.btn_save_new) && saveActivity())
            finish();
    }

    /**
     * An activity is stored permanently: values are read from user inputs
     */
    private boolean saveActivity() {
        DatePicker datePicker = (DatePicker) findViewById(R.id.datePickerNewActivity);
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePickerTotalTime);
        EditText note = (EditText) findViewById(R.id.txt_new_note);
        if ((datePicker == null) || (timePicker == null) || (note == null))
            return false;

        long duration = (timePicker.getCurrentHour() * 3600 + timePicker.getCurrentMinute() * 60) * 1000;

        JobEntries db = new JobEntries(this);
        db.open();
        JobEntry job = new JobEntry();
        if (note.getText().toString() != null)
            job.setDescr(note.getText().toString());
        Calendar cal = Calendar.getInstance();
        //cal.setTime(DateHandler.GetActualDate());
        cal.set(Calendar.YEAR, datePicker.getYear());
        cal.set(Calendar.MONTH, datePicker.getMonth());
        cal.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        Date date = cal.getTime();
        job.setStop(DateHandler.GetSQLDateFormat(date));
        job.setDuration(duration);
        try {
            db.addJob(job);
        } catch (JobNotCreated jobNotCreated) {
            Toast.makeText(this, getString(R.string.msg_error_saving), Toast.LENGTH_LONG).show();
            db.close();
            return false;
        }
        db.close();
        Toast.makeText(this, getString(R.string.msg_save_ok), Toast.LENGTH_LONG).show();
        return true;
    }

}
