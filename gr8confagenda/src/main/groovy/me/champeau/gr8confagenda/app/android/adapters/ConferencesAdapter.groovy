package me.champeau.gr8confagenda.app.android.adapters

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.R
import me.champeau.gr8confagenda.app.android.persistence.UserDefaults
import me.champeau.gr8confagenda.app.client.Conference

@CompileStatic
class ConferencesAdapter extends RecyclerView.Adapter<ConferencesAdapter.ConferenceViewHolder> {

    private static int ROW_TYPE_CONFERENCES = 1

    private Context context
    IConferenceAdapterDelegate adapterDelegate
    private List<Conference> conferences

    void setConferences(List<Conference> conferences) {
        this.conferences = conferences
        notifyDataSetChanged()
    }

    ConferencesAdapter(Context context, IConferenceAdapterDelegate adapterDelegate) {
        this.context = context
        this.adapterDelegate = adapterDelegate
    }


    @Override
    ConferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int resource = R.layout.conference_list_item
        View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false)
        new ConferenceViewHolder(view)
    }

    @Override
    int getItemViewType(int position) {
        ROW_TYPE_CONFERENCES
    }

    @Override
    void onBindViewHolder(ConferenceViewHolder holder, int position) {
        if(conferences?.size() > position) {
            holder.bindConference(conferences[position])
        }
    }

    @Override
    int getItemCount() {
        conferences?.size() ?: 0
    }

    class ConferenceViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {

        TextView nameTextView
        Conference conference

        ConferenceViewHolder(View itemView) {
            super(itemView)
            nameTextView = (TextView) itemView.findViewById(R.id.nameTextView)

            itemView.setOnClickListener(this)
        }

        void bindConference(Conference conference) {
            this.conference = conference
            nameTextView?.text = "${conference.name} ( ${conference.start} - ${conference.end} )"
            nameTextView?.setTypeface(null, isSelectedConference(conference) ? Typeface.BOLD : Typeface.NORMAL)
        }

        boolean isSelectedConference(Conference conference) {
            new UserDefaults(context).conferenceId == conference.id
        }

        @Override
        void onClick(View v) {
            notifyDataSetChanged()
            adapterDelegate?.conferenceTapped(conference)
        }
    }

}