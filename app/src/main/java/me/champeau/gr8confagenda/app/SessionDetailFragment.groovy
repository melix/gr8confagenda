package me.champeau.gr8confagenda.app

import android.app.Fragment
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper
import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.client.Session

/**
 * A fragment representing a single Session detail screen.
 * This fragment is either contained in a {@link SessionListActivity}
 * in two-pane mode (on tablets) or a {@link SessionDetailActivity}
 * on handsets.
 */
@CompileStatic
class SessionDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Session mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SessionDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        hasOptionsMenu = true
        fetchItem(arguments)
    }

    private void fetchItem(Bundle bundle) {
        if (bundle?.containsKey(ARG_ITEM_ID)) {
            mItem = Application.instance.sessions.find { it.id == bundle.get(ARG_ITEM_ID) }
        }
    }

    @Override
    void onSaveInstanceState(Bundle outState) {
        outState.putLong(ARG_ITEM_ID, mItem.id)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fetchItem(savedInstanceState)
        ScrollView rootView = (ScrollView) inflater.inflate(R.layout.fragment_session_detail, container, false);

        if (mItem != null) {
            def view = rootView.findViewById(R.id.session_detail)
            def speaker = Application.instance.speakers.find { it.id == mItem.speakerId }
            ((TextView) view.findViewById(R.id.session_detail_speaker)).setText(speaker.name)
            UrlImageViewHelper.setUrlDrawable((ImageView) view.findViewById(R.id.session_detail_image), speaker.image)
            ((TextView) view.findViewById(R.id.session_detail_title)).setText(mItem.title)
            def body = (TextView) view.findViewById(R.id.session_detail_body)
            body.setText(Html.fromHtml(mItem.summary))
            ((TextView) view.findViewById(R.id.session_detail_slot)).setText("${mItem.slot.startTime}-${mItem.slot.endTime}")
            def track = (TextView) view.findViewById(R.id.session_detail_track)
            track.setText(mItem.slot.trackName)
            track.setBackgroundColor(Color.parseColor(mItem.slot.trackColor))
            ((TextView) view.findViewById(R.id.session_detail_speaker_detail)).setText(Html.fromHtml(speaker.bio))
            def twitter = (TextView) view.findViewById(R.id.session_detail_twitter)
            twitter.setText("@${speaker.twitter}")
            twitter.clickable = true
            twitter.onClickListener= new View.OnClickListener() {
                @Override
                void onClick(View v) {
                    def intent = new Intent(Intent.ACTION_VIEW)
                    intent.setData(Uri.parse("https://twitter.com/${speaker.twitter}"))
                    activity.startActivity(intent)
                }
            }
        }
        rootView.post {
            rootView.fullScroll(ScrollView.FOCUS_UP)
        }

        rootView
    }

    @Override
    void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.session_detail_menu, menu)
    }
}
