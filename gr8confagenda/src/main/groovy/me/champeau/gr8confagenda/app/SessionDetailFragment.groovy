package me.champeau.gr8confagenda.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.*
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper
import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.client.AgendaClient
import me.champeau.gr8confagenda.app.client.Session
import me.champeau.gr8confagenda.app.client.Speaker

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
    public static final Speaker DEFAULT_SPEAKER = new Speaker(
            name: 'GR8Conf',
            twitter: 'gr8conf',
            bio: 'GR8Conf is an independent, affordable series of conferences in Denmark and the US. It\'s\n' +
                    'dedicated to the technologies in the Groovy ecosystem.',
            employer: 'GR8Conf',
            image: 'https://lh6.googleusercontent.com/-CV91c1R_zCw/AAAAAAAAAAI/AAAAAAAAAI8/CGoEd0oB8Pc/photo.jpg'
    )

    Session sessionItem;

    private MenuItem favoritesMenu

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SessionDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        fetchItem(arguments)
    }

    private void fetchItem(Bundle bundle) {
        if (bundle?.containsKey(ARG_ITEM_ID)) {
            sessionItem = Application.instance.sessions.find { it.id == bundle.get(ARG_ITEM_ID) }
        }
    }

    @Override
    void onSaveInstanceState(Bundle outState) {
        outState.putLong(ARG_ITEM_ID, sessionItem.id)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fetchItem(savedInstanceState)
        ScrollView rootView = (ScrollView) inflater.inflate(R.layout.fragment_session_detail, container, false);

        if (sessionItem != null) {
            def view = rootView.findViewById(R.id.session_detail)
            def speaker = Application.instance.speakers.find { it.id == sessionItem.speakerId }
            if (!speaker) {
                speaker = DEFAULT_SPEAKER
            }
            ((TextView) view.findViewById(R.id.session_detail_speaker)).setText(speaker.name)
            UrlImageViewHelper.setUrlDrawable((ImageView) view.findViewById(R.id.session_detail_image), speaker.image)
            ((TextView) view.findViewById(R.id.session_detail_title)).setText(sessionItem.title)
            def body = (TextView) view.findViewById(R.id.session_detail_body)
            body.setText(Html.fromHtml(sessionItem.summary ?: ''))
            ((TextView) view.findViewById(R.id.session_detail_slot)).setText("${sessionItem.slot.startTime}-${sessionItem.slot.endTime}")
            def track = (TextView) view.findViewById(R.id.session_detail_track)
            track.setText(sessionItem.slot.trackName)
            track.setBackgroundColor(Color.parseColor(sessionItem.slot.trackColor))
            ((TextView) view.findViewById(R.id.session_detail_speaker_detail)).setText(Html.fromHtml(speaker.bio))
            def twitter = (TextView) view.findViewById(R.id.session_detail_twitter)
            twitter.setText("@${speaker.twitter}")
            twitter.clickable = true
            twitter.onClickListener = new View.OnClickListener() {
                @Override
                void onClick(View v) {
                    def intent = new Intent(Intent.ACTION_VIEW)
                    intent.setData(Uri.parse("https://twitter.com/${speaker.twitter}"))
                    getActivity().startActivity(intent)
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
        favoritesMenu = menu.findItem(R.id.action_add_to_favorites)
        updateFavoritesIcon()
    }

    public void updateFavoritesIcon() {
        if (sessionItem) {
            boolean favorite = Application.instance.favorites.contains(sessionItem.id)
            favoritesMenu.setIcon(favorite ? R.drawable.ic_action_important : R.drawable.ic_action_not_important)
            favoritesMenu.setVisible(sessionItem.id<AgendaClient.GENERATED_ID_BASE)
        }
    }

}
