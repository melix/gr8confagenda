package me.champeau.gr8confagenda.app

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filter.FilterResults
import android.widget.TextView
import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.client.Session

@CompileStatic
class SessionListAdapter extends ArrayAdapter<Session> {

    private LayoutInflater inflater

    private static class DateFilter extends Filter {
        private final SessionListAdapter adapter

        DateFilter(SessionListAdapter adapter) {
            this.adapter = adapter
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults()
            if (!constraint) {
                results.values = Application.instance.sessions
                results.count = adapter.count
            } else {
                def values = Application.instance.sessions.findAll {
                    it.slot.date == constraint
                }
                results.values = values
                results.count = values.size()
            }
            results
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.clear()
            adapter.addAll((List<Session>)results.values)
            adapter.notifyDataSetChanged()
        }
    }

    SessionListAdapter(Context context, int resource, int textViewResourceId, List<Session> objects) {
        super(context, resource, textViewResourceId, objects)
        notifyOnChange = false
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
    }

    @Override
    Filter getFilter() {
        return new DateFilter(this)
    }

    @Override
    View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView
        if (!view) {
            view = inflater.inflate(R.layout.session_list_row, null)
        }
        Session session = getItem(position)

        def titleElem = (TextView) view.findViewById(R.id.session_list_title)
        titleElem.setText(session.title)
        def trackElem = (TextView) view.findViewById(R.id.session_list_track)
        trackElem.setText(session.slot.trackName)
        trackElem.setBackgroundColor(Color.parseColor(session.slot.trackColor))
        def timeElem = (TextView) view.findViewById(R.id.session_list_time)
        String timeText = "${session.slot.startTime}\n${session.slot.endTime}"
        timeElem.setText(timeText)
        view
    }
}