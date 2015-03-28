package me.champeau.gr8confagenda.app

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import groovy.transform.CompileStatic
import android.widget.Filter.FilterResults
import me.champeau.gr8confagenda.app.client.Session

@CompileStatic
class SessionListAdapter extends ArrayAdapter<Session> {

    private LayoutInflater inflater

    private Filter filter

    public static class SessionFilter extends Filter {
        String trackName
        Long speakerId

        private final SessionListAdapter adapter

        SessionFilter(SessionListAdapter adapter) {
            this.adapter = adapter
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults()
            def values = Application.instance.sessions.findAll {
                it.slot.date == constraint
            }
            if (trackName) {
                values = values.findAll {
                    it.slot.trackName == trackName
                }
            }
            if (speakerId) {
                values = values.findAll {
                    it.speakerId == speakerId
                }
            }
            results.values = values
            results.count = values.size()
            results
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.clear()
            adapter.addAll((List<Session>) results.values)
            adapter.notifyDataSetChanged()
        }
    }

    SessionListAdapter(Context context) {
        super(context,android.R.layout.simple_list_item_activated_1,
                android.R.id.text1, [])
        notifyOnChange = false
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
    }

    @Override
    Filter getFilter() {
        if (filter==null) {
            filter = new SessionFilter(this)
        }
        filter
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

        def starElem = (ImageView) view.findViewById(R.id.starred_session)
        starElem.setVisibility(Application.instance.favorites.contains(session.id)?ImageView.VISIBLE:ImageView.GONE)

        view
    }
}