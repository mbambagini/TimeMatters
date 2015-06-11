package org.timematters.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import org.timematters.utils.DateConverter;

import java.util.Date;

public class NewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        TimePicker timePicker = (TimePicker)findViewById(R.id.timePickerTotalTime);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(8);
        timePicker.setCurrentMinute(0);
        DatePicker datePicker = (DatePicker)findViewById(R.id.datePickerNewActivity);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save_new) {
            if (saveActivity())
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickUpperButtons(View v) {
        if (v.getId() == R.id.btn_save_new) {
            if (saveActivity())
                finish();
        }
    }

    public boolean saveActivity () {
        DatePicker datePicker = (DatePicker)findViewById(R.id.datePickerNewActivity);
        TimePicker timePicker = (TimePicker)findViewById(R.id.timePickerTotalTime);
        EditText note = (EditText)findViewById(R.id.txt_new_note);
        if ((datePicker == null) || (timePicker == null) || (note == null))
            return false;

        long duration = (timePicker.getCurrentHour()*3600+timePicker.getCurrentMinute()*60)*1000;

        JobEntries db = new JobEntries(this);
        db.open();
        JobEntry job = new JobEntry();
        if (note.getText().toString()!=null)
            job.setDescr(note.getText().toString());
        System.out.println("DATE: "+datePicker.getYear()+" "+datePicker.getMonth()+" "+datePicker.getDayOfMonth());
        System.out.println("DURATION: "+duration);
        Date date = new Date();
        date.setYear(datePicker.getYear()-1900);
        date.setMonth(datePicker.getMonth());
        date.setDate(datePicker.getDayOfMonth());
        date.setMinutes(0);
        date.setHours(0);
        job.setStop(DateConverter.GetSQLDateFormat(date));
        job.setDuration(duration);
        System.out.println(date.getTime());
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
