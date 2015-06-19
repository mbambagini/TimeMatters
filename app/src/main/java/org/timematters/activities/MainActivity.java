package org.timematters.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.timematters.R;
import org.timematters.adapter.JobAdapter;
import org.timematters.database.JobEntries;
import org.timematters.database.JobEntry;
import org.timematters.exceptions.JobNotCreated;
import org.timematters.exceptions.JobNotFound;
import org.timematters.misc.DateHandler;
import org.timematters.misc.JobStorage;
import org.timematters.misc.Layouts;
import org.timematters.misc.NotificationWrapper;
import org.timematters.misc.States;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This Activity implements the main features of the app: tracking,
 * listing and search.
 * A navigation drawer lets the user navigate through the facilities
 */
public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * time after which the tracking (elapsed time) is updated
     */
    static private final long period_1s = 1000;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * Used to store the actual layout to be shown
     */
    private Layouts internal_layout = Layouts.LAYOUT_TRACKING;

    /**
     * Reference to the actual frame layout: used when a new one is selected to
     * disable the previous
     */
    private FrameLayout actual_layout = null;

    /**
     * Current state of the tracking feature
     */
    private States internal_state = States.STATE_IDLE;

    /**
     * data used to filter activity result
     */
    private Date first_date = null;

    /**
     * data used to filter activity result
     */
    private Date second_date = null;
    
    /**
     * Object in charge of handling the notification bar when the activity becomes active/not active
     */
    private NotificationWrapper notifier = null;
    
    /**
     * Elapsed time of the actual tracking: relative interval
     */
    private long total_time = 0;
    
    /**
     * Elapsed time of the actual tracking: absolute time
     */
    private long elapsed_time = 0;
    
    /**
     * Timer which updates periodically (wrt period_1s) the tracking
     */
    private Timer timer = new Timer();

    private Runnable tick = new Runnable() {
        @Override
        public void run() {
            update_time();
        }
    };
    
    /**
     * Object which implements the Memento design pattern
     */
    private Memento mementoHandler;
    
    private JobEntries jobs;

    //action mode handler
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.dynamic_action_bar_job_list, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after
        // onCreateActionMode, but may be called multiple times if the mode is invalidated.
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        // Called when the user selects a contextual menu item
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menuItemDelete:
                    deleteJobs();
                    return true;
            }
            return false;
        }

        // Called when the user exits the action mode
        public void onDestroyActionMode(ActionMode mode) {
            mementoHandler.destroyMode();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        //check if the tracking must be resumed after the application/activity is closed/suspended
        if (savedInstanceState == null) {
            long val = JobStorage.getPendingJob(this, System.currentTimeMillis(), -1);
            if (val != -1) {
                internal_state = States.STATE_RUNNING;
                internal_layout = Layouts.LAYOUT_TRACKING;
                start_counter(val);
                JobStorage.removePendingJob(getApplication());
                Toast.makeText(this, getString(R.string.tst_job_resumed), Toast.LENGTH_LONG).show();
            }
        }

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        //setup memento
        jobs = new JobEntries(getApplicationContext());
        mementoHandler = new Memento();
        ListView lst = (ListView) findViewById(R.id.lstJobs);
        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                if (mementoHandler.isSelectionMode()) {
                    TextView txt = (TextView)view.findViewById(R.id.txt_job_id);
                    if (txt!=null)
                        mementoHandler.toggleJob(view, Long.parseLong(txt.getText().toString()));
                }
            }
        });
        lst.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txt = (TextView)view.findViewById(R.id.txt_job_id);
                if (txt!=null)
                    mementoHandler.toggleJob(view, Long.parseLong(txt.getText().toString()));
                return true;
            }
        });

        //show layout
        setLayout();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        switch (position) {
            case 0: //home - tracking
                internal_layout = Layouts.LAYOUT_TRACKING;
                break;
            case 1: //complete list
                internal_layout = Layouts.LAYOUT_LIST;
                first_date = null;
                second_date = DateHandler.GetActualDate();
                fillList(first_date, second_date);
                break;
            case 2: //this week list
                internal_layout = Layouts.LAYOUT_LIST;
                first_date = DateHandler.GetLastSunday();
                second_date = DateHandler.GetActualDate();
                fillList(first_date, second_date);
                break;
            case 3: //this month list
                internal_layout = Layouts.LAYOUT_LIST;
                first_date = DateHandler.GetMonthStart();
                second_date = DateHandler.GetActualDate();
                fillList(first_date, second_date);
                break;
            case 4: //search
                internal_layout = Layouts.LAYOUT_SEARCH;
                break;
            case 5: //about
                internal_layout = Layouts.LAYOUT_ABOUT;
                break;
        }
        setLayout();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    /**
     * Inflate the correct menu according to the actual layout and only iff
     * the drawer is closed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            switch (internal_layout) {
                case LAYOUT_LIST:
                    getMenuInflater().inflate(R.menu.main, menu);
                    break;
                case LAYOUT_SEARCH:
                case LAYOUT_TRACKING:
                case LAYOUT_ABOUT:
                default:
                    getMenuInflater().inflate(R.menu.main_reduced, menu);
                    break;
            }
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Since this Activity may be resumed after a new element has been
     * inserted, the list must be updated (if the layout was LAYOUT_LIST)
     */
    @Override
    public void onResume() {
        super.onResume();
        setLayout();
        if (internal_layout == Layouts.LAYOUT_LIST)
            fillList(first_date, second_date);
        if (notifier != null) {
            notifier.destroy(this);
            notifier = null;
        }
    }

    /**
     * Callback invoked when an item in the menu is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_share:
                String msg = buildString();
                if (msg != null) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, buildString());
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.action_share)));
                } else {
                    Toast.makeText(this, getString(R.string.tst_no_share), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_new:
            case R.id.action_new_reduced:
                Intent intent = new Intent(this, NewActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * When the Activity is stopped,  if a tracking was running, its state will be
     * saved to be resumed
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (internal_state == States.STATE_RUNNING) {
            JobStorage.setPendingJob(this, total_time, System.currentTimeMillis());
            notifier = new NotificationWrapper();
            notifier.create(this);
        } else {
            JobStorage.removePendingJob(this);
        }
    }

    /**
     * This function is called when a button is pressed
     */
    public void onClickUpperButtons(View v) {
        switch (v.getId()) {
            case R.id.btn_tap_to_start: //start the tracking
                internal_state = States.STATE_RUNNING;
                internal_layout = Layouts.LAYOUT_TRACKING;
                start_counter(0L);
                setLayout();
                break;
            case R.id.btn_stop: //stop the tracking
                internal_state = States.STATE_IDLE;
                internal_layout = Layouts.LAYOUT_TRACKING;
                Intent intent = new Intent(this, SaveActivity.class);
                intent.putExtra(getString(R.string.elapsed_time_id), total_time);
                startActivity(intent);
                break;
            case R.id.btn_pause: //pause the tracking
                internal_state = States.STATE_BLOCKED;
                internal_layout = Layouts.LAYOUT_TRACKING;
                setLayout();
                break;
            case R.id.btn_tap_to_resume: //resume the tracking
                internal_state = States.STATE_RUNNING;
                internal_layout = Layouts.LAYOUT_TRACKING;
                elapsed_time = System.currentTimeMillis();
                setLayout();
                break;
            case R.id.btn_search: //start a search
                DatePicker datePickerFrom = (DatePicker) findViewById(R.id.datePickerSearchFrom);
                DatePicker datePickerTo = (DatePicker) findViewById(R.id.datePickerSearchUntil);
                if ((datePickerFrom != null) && (datePickerTo != null)) {

                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, datePickerFrom.getYear());
                    cal.set(Calendar.MONTH, datePickerFrom.getMonth());
                    cal.set(Calendar.DAY_OF_MONTH, datePickerFrom.getDayOfMonth());
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.HOUR, 0);
                    first_date = cal.getTime();
                    cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, datePickerTo.getYear());
                    cal.set(Calendar.MONTH, datePickerTo.getMonth());
                    cal.set(Calendar.DAY_OF_MONTH, datePickerTo.getDayOfMonth());
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.HOUR, 0);
                    second_date = cal.getTime();
                    if (second_date.compareTo(first_date) >= 0) {
                        internal_layout = Layouts.LAYOUT_LIST;
                        setLayout();
                        fillList(first_date, second_date);
                    } else {
                        Toast.makeText(this, getString(R.string.tst_error_date), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    /**
     * Manage the elements in the result list
     */
    private JobAdapter adapter = null;

    /**
     * show the result list according to first_date and second_date
     */
    private void fillList(Date start, Date stop) {
        JobEntries jobs = new JobEntries(this);
        jobs.open();
        List<JobEntry> list = jobs.getJobs(start, stop);
        jobs.close();
        ListView lst = (ListView) findViewById(R.id.lstJobs);
        LinearLayout details = (LinearLayout) findViewById(R.id.lst_info);
        TextView txt = (TextView) findViewById(R.id.txt_empty);
        long total = 0;
        for (int i = 0; (list != null) && (i < list.size()); i++)
            total = total + list.get(i).getDuration();
        ((TextView) findViewById(R.id.txt_hours)).setText(DateHandler.GetElapsedTime(total));
        if (list != null && list.size() > 0) {
            lst.setVisibility(View.VISIBLE);
            txt.setVisibility(View.GONE);
            details.setVisibility(View.VISIBLE);
            adapter = new JobAdapter(this, android.R.layout.simple_list_item_1, list);
            lst.setAdapter(adapter);
        } else {
            lst.setVisibility(View.GONE);
            txt.setVisibility(View.VISIBLE);
            details.setVisibility(View.GONE);
        }
    }

    /*
    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_home);
                break;
            case 2:
                mTitle = getString(R.string.title_activities);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }
    */

    /**
     * Start the tracking: set values, make the timer start and set the callback
     */
    private void start_counter(long offset) {
        total_time = offset;
        elapsed_time = System.currentTimeMillis();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ticker();
            }
        }, 0, period_1s);
    }

    /**
     * Periodic update of the tracking
     */
    private void update_time() {
        if (internal_state == States.STATE_RUNNING) {
            long curr = System.currentTimeMillis();
            total_time += curr - elapsed_time;
            TextView txt = (TextView) findViewById(R.id.txt_time);
            txt.setText(DateHandler.GetElapsedTime(total_time));
            elapsed_time = System.currentTimeMillis();
            if (notifier != null)
                notifier.update(this, total_time);
        }
    }

    private void ticker() {
        this.runOnUiThread(tick);
    }

    /**
     * Set the appropriate layout (fragment to show) wrt internal_layout
     */
    private void setLayout() {
        FrameLayout tmp = null;
        switch (internal_layout) {
            case LAYOUT_TRACKING:
                switch (internal_state) {
                    case STATE_RUNNING:
                        tmp = (FrameLayout) findViewById(R.id.container_layout_running);
                        break;
                    case STATE_BLOCKED:
                        tmp = (FrameLayout) findViewById(R.id.container_layout_blocked);
                        break;
                    case STATE_IDLE:
                        tmp = (FrameLayout) findViewById(R.id.container_layout_idle);
                        break;
                }
                break;
            case LAYOUT_SEARCH:
                tmp = (FrameLayout) findViewById(R.id.container_layout_search);
                DatePicker datePickerFrom = (DatePicker) findViewById(R.id.datePickerSearchFrom);
                DatePicker datePickerTo = (DatePicker) findViewById(R.id.datePickerSearchUntil);
                if ((datePickerFrom != null) && (datePickerTo != null)) {
                    datePickerFrom.setMaxDate(System.currentTimeMillis());
                    datePickerTo.setMaxDate(System.currentTimeMillis());
                }
                break;
            case LAYOUT_LIST:
                tmp = (FrameLayout) findViewById(R.id.container_layout_list);
                break;
            case LAYOUT_ABOUT:
                tmp = (FrameLayout) findViewById(R.id.container_layout_about);
                break;
        }
        if (tmp != null) {
            if (actual_layout != null) {
                actual_layout.setVisibility(View.GONE);
            }
            actual_layout = tmp;
            actual_layout.setVisibility(View.VISIBLE);
        }
        /* to be optimized: do not change the layout when it is the same */
    }

    /**
     * Delete the selected activities and activate mememto
     */
    private void deleteJobs() {
        mementoHandler.deleteSelectedJobs();
        jobs.close();
        fillList(first_date, second_date);
        showUndo();
    }

    /**
     * Show the undo bar after a delection and implement the following behaviors:
     * - time expires: confirm delection
     * - undo pressed: restore activities
     */
    private void showUndo() {
        final View view = findViewById(R.id.undobar);
        view.setVisibility(View.VISIBLE);
        view.setAlpha(1);
        TextView txt = (TextView) view.findViewById(R.id.txtSumDelete);
        if (txt != null)
            txt.setText(getResources().getString(R.string.msg_deleted) + " " +
                            String.valueOf(mementoHandler.getDeletedJobs()) + " " +
                            (mementoHandler.getDeletedJobs() == 1 ?
                                    getResources().getString(R.string.msg_activity) :
                                    getResources().getString(R.string.msg_activities)
                            )
            );
        fillList(first_date, second_date);
        view.animate().alpha(0.4f).setDuration(5000).withEndAction(new Runnable() {
            @Override
            synchronized public void run() {
                view.setVisibility(View.GONE);
                fillList(first_date, second_date);
                mementoHandler.discardData();
            }
        });
    }

    /**
     * Restore deleted activities
     */
    public void undoDelection(View view) {
        findViewById(R.id.undobar).setVisibility(View.GONE);
        if (mementoHandler.undo()) {
            fillList(first_date, second_date);
        } else
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.msg_undo_not_possible),
                    Toast.LENGTH_SHORT).show();
    }

    /**
     * Build the string which is gonna be shared
     */
    private String buildString() {
        if (second_date == null)
            return null;

        String body = null;
        long total = 0;
        JobEntries jobs = new JobEntries(this);
        jobs.open();
        List<JobEntry> list = jobs.getJobs(first_date, second_date);
        jobs.close();
        if (list == null || list.size() == 0) {
            body = getString(R.string.str_no_activities);
        } else {
            for (int i = 0; i < list.size(); i++) {
                JobEntry entry = list.get(i);
                if (entry != null) {
                    body = DateHandler.GetPreferenceDateFormat(entry.getStop()) + ":  " +
                            DateHandler.GetElapsedTime(entry.getDuration()) + "\n";
                    total += entry.getDuration();
                } else
                    body = "";
            }
        }

        String header;
        if (first_date == null) {
            header = getString(R.string.str_all_activities) + " " +
                        getString(R.string.text_until) + " " +
                        DateHandler.GetPreferenceDateFormat(second_date) + " (" +
                        getString(R.string.text_hours) + DateHandler.GetElapsedTime(total) + "):\n";
        } else {
            header = getString(R.string.str_all_activities) + " " +
                        getString(R.string.text_from) + " " +
                        DateHandler.GetPreferenceDateFormat(first_date) + " " +
                        getString(R.string.text_to) + " " +
                        DateHandler.GetPreferenceDateFormat(second_date) + " (" +
                        getString(R.string.text_hours) + DateHandler.GetElapsedTime(total) + "):\n";
        }

        if (body == null)
            return header;
        return header + body;
    }

    /**
     * Memento pattern implementation
     */
    private class Memento {
        private ActionMode mActionMode = null;

        private ArrayList<Long> selectedJobs = null;

        private ArrayList<JobEntry> memento = null;
        private int deletedCount = 0;

        synchronized public boolean isSelectionMode() {
            return mActionMode != null;
        }

        synchronized public void toggleJob(View v, Long id) {
            if (memento != null)
                return;
            if (mActionMode == null) {
                mActionMode = MainActivity.this.startActionMode(actionModeCallback);
                selectedJobs = null;
            }
            if (selectedJobs!=null && selectedJobs.contains(id))
                deselectJob(v, id);
            else
                selectJob(v, id);
            if (selectedJobs != null)
                mActionMode.setTitle(Integer.toString(selectedJobs.size()));
        }

        synchronized private void selectJob(View v, Long id) {
            if (selectedJobs == null)
                selectedJobs = new ArrayList<>();
            if (!selectedJobs.contains(id))
                selectedJobs.add(id);
            if (adapter!=null)
                adapter.setSelection(id, true);
            v.setSelected(true);
            v.setBackgroundColor(getResources().getColor(R.color.selected_job));
        }

        synchronized private void deselectJob(View v, Long id) {
            if (selectedJobs != null)
                selectedJobs.remove(id);
            v.setBackgroundColor(Color.TRANSPARENT);
            v.setSelected(false);
            if (adapter!=null)
                adapter.setSelection(id, false);
            if (selectedJobs == null || selectedJobs.size() == 0)
                mActionMode.finish();
        }

        synchronized public int getDeletedJobs() {
            return deletedCount;
        }

        private void deleteJobs() {
            deletedCount = 0;
            if (memento == null)
                memento = new ArrayList<>();
            else
                memento.clear();
            jobs.open();
            for (Long i : selectedJobs) {
                try {
                    JobEntry j = jobs.getJob(i);
                    memento.add(j);
                    jobs.deleteJob(j);
                    deletedCount++;
                } catch (JobNotFound e) {
                    //skip
                }
            }
            jobs.close();
        }

        synchronized public void deleteSelectedJobs() {
            if (selectedJobs != null) {
                deletedCount = 0;
                deleteJobs();
                mActionMode.finish();
            }
        }

        synchronized public void discardData() {
            memento = null;
        }

        synchronized public void destroyMode() {
            adapter.clean();
            selectedJobs = null;
            mActionMode = null;
        }

        /*
                synchronized public void ignoreAction () {
                    mActionMode.finish();
                }
        */
        synchronized public boolean undo() {
            boolean ret = true;
            if (memento == null)
                return false;
            jobs.open();
            for (JobEntry j : memento) {
                try {
                    jobs.restoreJob(j);
                } catch (JobNotCreated exp) {
                    ret = false;
                }
            }
            jobs.close();
            memento = null;
            return ret;
        }
    }
}
