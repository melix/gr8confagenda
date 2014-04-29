package me.champeau.gr8confagenda.app

import android.content.Context
import android.text.TextUtils
import android.util.Log
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
    }

    @Override
    Filter getFilter() {
        return new DateFilter(this)
    }

    @Override
    View getView(int position, View convertView, ViewGroup parent) {
        def view = super.getView(position, convertView, parent)
        if (view instanceof TextView) {
            view.singleLine = true
            view.ellipsize = TextUtils.TruncateAt.MARQUEE
            view.marqueeRepeatLimit = -1
            def session = getItem(position)
            view.setText(session.title)
        }
        view
    }
}