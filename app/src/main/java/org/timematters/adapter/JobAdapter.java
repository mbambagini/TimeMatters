package org.timematters.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.timematters.R;
import org.timematters.database.JobEntry;
import org.timematters.misc.DateHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * JobEntry adapter
 */
public class JobAdapter extends ArrayAdapter<JobEntry> {

    private final Activity context;
    private final List<JobEntry> jobs;

    private final ArrayList<Long> selections = new ArrayList<>();

    public JobAdapter(Activity context, int textViewResourceId, List<JobEntry> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        jobs = values;
    }

    static class ViewHolder {
        public TextView when;
        public TextView time;
        public TextView note;
        public TextView id;
    }

    public void setSelection (Long id, boolean b) {
        if (b == false) {
            selections.remove(id);
        } else {
            if (selections.contains(id) == false)
                selections.add(id);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) { //reuse view
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.job_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.when = (TextView)view.findViewById(R.id.txt_job_when);
            viewHolder.time = (TextView)view.findViewById(R.id.txt_job_duration);
            viewHolder.note = (TextView)view.findViewById(R.id.txt_job_note);
            viewHolder.id = (TextView)view.findViewById(R.id.txt_job_id);
            view.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.when.setText(DateHandler.GetPreferenceDateFormat(jobs.get(position).getStop()));
        holder.time.setText(DateHandler.GetElapsedTime(jobs.get(position).getDuration()));
        holder.note.setText(jobs.get(position).getDescr());
        holder.id.setText(Long.toString(jobs.get(position).getId()));
        if (selections.contains(jobs.get(position).getId()))
            view.setBackgroundColor(getContext().getResources().getColor(R.color.selected_job));
        else
            view.setBackgroundColor(Color.TRANSPARENT);
        return view;
    }

    public void clean () {
        selections.clear();
    }
}
