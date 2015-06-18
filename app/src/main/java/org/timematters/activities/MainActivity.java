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
    private Date first_date = null;
    private Date second_date = null;
    private NotificationWrapper notifier = null;
    private long total_time = 0;
    private long elapsed_time = 0;
    private Timer timer = new Timer();
    private Runnable tick = new Runnable() {
        @Override
        public void run() {
            update_time();
        }
    };
    private Memento mementoHandler;
    private JobEntries jobs;
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.dynamic_action_bar_job_list, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after
        // onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
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

/* UNTIL HERE */

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
                if (mementoHandler.isSelectionMode())
                    mementoHandler.toggleJob(view);
            }
        });
        lst.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mementoHandler.toggleJob(view);
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
                    first_date = new Date();
                    first_date.setYear(datePickerFrom.getYear() - 1900);
                    first_date.setMonth(datePickerFrom.getMonth());
                    first_date.setDate(datePickerFrom.getDayOfMonth());
                    first_date.setMinutes(0);
                    first_date.setHours(0);
                    second_date = new Date();
                    second_date.setYear(datePickerTo.getYear() - 1900);
                    second_date.setMonth(datePickerTo.getMonth());
                    second_date.setDate(datePickerTo.getDayOfMonth());
                    second_date.setMinutes(0);
                    second_date.setHours(0);
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

    /* show list */
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
            JobAdapter adapter = new JobAdapter(this, android.R.layout.simple_list_item_1, list);
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

    private void deleteJobs() {
        mementoHandler.deleteSelectedJobs();
        jobs.close();
        fillList(null, DateHandler.GetActualDate());
        showUndo();
    }

    private void showUndo() {
        //findViewById(R.id.sample_content_fragment).setVisibility(View.GONE);
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
//                if (internal_layout==Layouts.LAYOUT_LIST)
//                    findViewById(R.id.sample_content_fragment).setVisibility(View.VISIBLE);
                mementoHandler.discardData();
            }
        });
    }

    public void undoDelection(View view) {
        findViewById(R.id.undobar).setVisibility(View.GONE);
//        findViewById(R.id.sample_content_fragment).setVisibility(View.VISIBLE);
        if (mementoHandler.undo()) {
            fillList(first_date, second_date);
        } else
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.msg_undo_not_possible),
                    Toast.LENGTH_SHORT).show();
    }

    private String buildString() {
        if (first_date == null && second_date == null)
            return null;

        String body = null;
        long total = 0;
        JobEntries jobs = new JobEntries(this);
        jobs.open();
        List<JobEntry> list = jobs.getJobs(first_date, second_date);
        jobs.close();
        if (list == null || list.size() == 0) {
            body = "No activity stored";
        } else {
            for (int i = 0; i < list.size(); i++) {
                JobEntry entry = list.get(i);
                if (entry != null) {
                    body = DateHandler.GetPreferenceDateFormat(entry.getStop()) + ":  " + DateHandler.GetElapsedTime(entry.getDuration()) + "\n";
                    total += entry.getDuration();
                } else
                    body = "";
            }
        }

        String header;
        if (first_date == null) {
            header = "All activities until " + DateHandler.GetPreferenceDateFormat(second_date) + " (total time " + DateHandler.GetElapsedTime(total) + "):\n";
        } else {
            header = "All activities from " + DateHandler.GetPreferenceDateFormat(first_date) + " to " + DateHandler.GetPreferenceDateFormat(second_date) + " (total time " + DateHandler.GetElapsedTime(total) + "):\n";
        }

        if (body == null)
            return header;
        return header + body;
    }

    private class Memento {
        private ActionMode mActionMode = null;

        private ArrayList<View> selectedViews = null;
        private ArrayList<Integer> selectedJobs = null;

        private ArrayList<JobEntry> memento = null;
        private int deletedCount = 0;

        synchronized public boolean isSelectionMode() {
            return mActionMode != null;
        }

        synchronized public void toggleJob(View v) {
            if (memento != null)
                return;
            if (mActionMode == null) {
                mActionMode = MainActivity.this.startActionMode(actionModeCallback);
                selectedJobs = null;
                selectedViews = null;
            }
            if (selectedViews != null && selectedViews.indexOf(v) != -1)
                deselectJob(v);
            else
                selectJob(v);
            if (selectedViews != null)
                mActionMode.setTitle(Integer.toString(selectedViews.size()));
        }

        synchronized private void selectJob(View v) {
            TextView txt = (TextView) v.findViewById(R.id.txt_job_id);
            if (txt == null)
                return;
            if (selectedJobs == null)
                selectedJobs = new ArrayList<>();
            if (selectedViews == null)
                selectedViews = new ArrayList<>();
            selectedJobs.add(Integer.parseInt(txt.getText().toString()));
            selectedViews.add(v);
            v.setSelected(true);
            v.setBackgroundColor(getResources().getColor(R.color.selected_job));
        }

        synchronized private void deselectJob(View v) {
            TextView txt = (TextView) v.findViewById(R.id.txt_job_id);
            if (txt == null) {
                mActionMode.finish();
                return;
            }
            if (selectedJobs != null) {
                Integer val = Integer.parseInt(txt.getText().toString());
                for (int i = 0; i < selectedJobs.size(); i++)
                    if (selectedJobs.get(i).compareTo(val) == 0) {
                        selectedJobs.remove(i);
                        break;
                    }
            }
            if (selectedViews != null)
                selectedViews.remove(v);
            v.setBackgroundColor(Color.TRANSPARENT);
            v.setSelected(false);
            if (selectedJobs != null && selectedJobs.size() == 0)
                mActionMode.finish();
        }

        synchronized public int getDeletedJobs() {
            return deletedCount;
        }

        /*
                synchronized public int getSelectedJobs () {
                    if (selectedJobs!=null)
                        return selectedJobs.size();
                    return 0;
                }
        */
        private void deleteJobs() {
            deletedCount = 0;
            if (memento == null)
                memento = new ArrayList<>();
            else
                memento.clear();
            jobs.open();
            for (Integer i : selectedJobs) {
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
            if (selectedViews != null)
                for (View v : selectedViews) {
                    if (v != null) {
                        v.setBackgroundColor(Color.TRANSPARENT);
                        v.setSelected(false);
                    }
                }
            selectedViews = null;
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
