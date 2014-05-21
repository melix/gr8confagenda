package me.champeau.gr8confagenda.app

import android.app.ActionBar
import android.app.Activity
import android.app.FragmentTransaction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import groovy.transform.CompileStatic

/**
 * An activity representing a list of Sessions. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SessionDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link SessionListFragment} and the item details
 * (if present) is a {@link SessionDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link SessionListFragment.Callbacks} interface
 * to listen for item selections.
 */
@CompileStatic
class SessionListActivity extends Activity
        implements SessionListFragment.Callbacks, ActionBar.TabListener {

    private final static String SELECTED_TAB = "selectedTab";

    private BroadcastReceiver broadcastReceiver

    private void updateFavoriteIcon() {
        SessionDetailFragment fragment = (SessionDetailFragment) fragmentManager.findFragmentById(R.id.session_detail_container)
        if (fragment) {
            fragment.updateFavoritesIcon()
        }
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    protected SessionListAdapter sessionListAdapter() {
        def sessionListFragment = (SessionListFragment) fragmentManager
                .findFragmentById(R.id.session_list)
        (SessionListAdapter) sessionListFragment.listAdapter
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list)


        def sessionListFragment = (SessionListFragment) fragmentManager
                .findFragmentById(R.id.session_list)
        if (findViewById(R.id.session_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            sessionListFragment
                    .activateOnItemClick = true

            broadcastReceiver= new BroadcastReceiver() {
                @Override
                void onReceive(Context context, Intent intent) {
                    updateFavoriteIcon()
                }
            }
            def intentFilter = new IntentFilter(AgendaService.UPDATE_FAVORITES_RESPONSE)
            intentFilter.addCategory(AgendaService.CATEGORY)
            registerReceiver(broadcastReceiver, intentFilter)

            if (Application.instance.sessions && savedInstanceState==null) {
                onItemSelected(Application.instance.sessions[0].id)
            }
        }

        populateActionBar()
    }

    @Override
    protected void onDestroy() {
        super.onDestroy()
        if (broadcastReceiver) {
            unregisterReceiver(broadcastReceiver)
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_TAB, actionBar.selectedTab.position)
        super.onSaveInstanceState(outState)
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState?.containsKey(SELECTED_TAB)) {
            def tabIndex = savedInstanceState.getInt(SELECTED_TAB)
            actionBar.selectTab(actionBar.getTabAt(tabIndex))
        }
    }

    private void populateActionBar() {
        def bar = getActionBar()
        bar.navigationMode = ActionBar.NAVIGATION_MODE_TABS
        bar.addTab bar.newTab().setText('University day').setTabListener(this)
        bar.addTab bar.newTab().setText('Conference day 1').setTabListener(this)
        bar.addTab bar.newTab().setText('Conference day 2').setTabListener(this)
    }

    /**
     * Callback method from {@link SessionListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Long id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(SessionDetailFragment.ARG_ITEM_ID, id);
            SessionDetailFragment fragment = new SessionDetailFragment();
            fragment.arguments = arguments
            fragmentManager.beginTransaction()
                    .replace(R.id.session_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, SessionDetailActivity);
            detailIntent.putExtra(SessionDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    protected void doFilter() {
        def tab = actionBar.selectedTab
        sessionListAdapter().filter.filter("2014-06-0${2 + tab.position}")

    }

    @Override
    void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        doFilter()
        def sessionListFragment = (SessionListFragment) fragmentManager
                .findFragmentById(R.id.session_list)
        sessionListFragment.listView.clearChoices()
    }

    @Override
    void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    boolean onCreateOptionsMenu(Menu menu) {
        menuInflater.inflate(R.menu.session_list_menu, menu)
        def tracks = menu.findItem(R.id.action_select_track)
        def speakers = menu.findItem(R.id.action_select_speaker)

        Set<String> trackNames = new TreeSet<String>()
        Set<String> speakerNames = new TreeSet<String>()
        [trackNames,speakerNames]*.add('')
        def sessionListAdapter = sessionListAdapter()

        Application.instance.sessions.collect(trackNames) { item ->
            item.slot.trackName
        }
        Application.instance.speakers.collect(speakerNames) { item ->
            item.name
        }

        Spinner trackView = (Spinner) tracks.actionView.findViewById(R.id.menu_session_list)
        Spinner speakersView = (Spinner) speakers.actionView.findViewById(R.id.menu_speaker_list)
        def trackAdapter = new SimpleArrayAdapter(this, trackNames.toList())
        def speakerAdapter = new SimpleArrayAdapter(this, speakerNames.toList())
        trackView.adapter = trackAdapter
        speakersView.adapter = speakerAdapter
        trackView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((SessionListAdapter.SessionFilter)sessionListAdapter.filter).trackName = trackAdapter.getItem(position)
                doFilter()
            }

            @Override
            void onNothingSelected(AdapterView<?> parent) {

            }
        })
        speakersView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                def item = speakerAdapter.getItem(position)
                ((SessionListAdapter.SessionFilter)sessionListAdapter.filter).speakerId =
                        Application.instance.speakers.find { it.name == item }?.id
                doFilter()
            }

            @Override
            void onNothingSelected(AdapterView<?> parent) {

            }
        })

        // the following code is a workaround for action views overlapping...
        // there must be a better way to handle this!
        MenuItemCompat.setOnActionExpandListener(tracks, new MenuItemCompat.OnActionExpandListener() {
            @Override
            boolean onMenuItemActionExpand(MenuItem item) {
                speakers.collapseActionView()
                true
            }

            @Override
            boolean onMenuItemActionCollapse(MenuItem item) {
                true
            }
        })
        MenuItemCompat.setOnActionExpandListener(speakers, new MenuItemCompat.OnActionExpandListener() {
            @Override
            boolean onMenuItemActionExpand(MenuItem item) {
                tracks.collapseActionView()
                true
            }

            @Override
            boolean onMenuItemActionCollapse(MenuItem item) {
                true
            }
        })

        true
    }

    private static class SimpleArrayAdapter extends ArrayAdapter<String> {

        SimpleArrayAdapter(Context context, List<String> objects) {
            super(context,android.R.layout.simple_list_item_1,objects)
        }

    }


    public void switchFavorite ( MenuItem item ) {
        SessionDetailFragment fragment = (SessionDetailFragment) fragmentManager.findFragmentById(R.id.session_detail_container)
        Intent intent = new Intent(this, AgendaService)
        intent.action = AgendaService.ACTION_FAVORITE
        intent.putExtra(AgendaService.SESSION_ID, fragment.sessionItem.id)
        startService(intent)
    }

    public void chooseTrack(MenuItem item) {

    }

    public void chooseSpeaker(MenuItem item) {

    }
}
