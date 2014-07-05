package me.champeau.gr8confagenda.app

import android.app.ActionBar
import android.app.Activity
import android.app.FragmentTransaction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
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
class SessionListActivity extends FragmentActivity
        implements SessionListFragment.Callbacks, ActionBar.TabListener {

    private final static String SELECTED_TAB = "selectedTab";

    private BroadcastReceiver broadcastReceiver

    private void updateFavoriteIcon() {
        SessionDetailFragment fragment = (SessionDetailFragment) supportFragmentManager.findFragmentById(R.id.session_detail_container)
        if (fragment) {
            fragment.updateFavoritesIcon()
        }

        sessionListAdapter.notifyDataSetChanged()
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * The view pager, for swiping between tabs
     */
    private ViewPager mPager

    SessionListAdapter sessionListAdapter

    public boolean isTwoPane() {
        mTwoPane
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list)
        sessionListAdapter = new SessionListAdapter(this)
        if (findViewById(R.id.session_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            if (Application.instance.sessions && savedInstanceState == null) {
                onItemSelected(Application.instance.sessions[0].id)
            }

            SessionListFragment sessionListFragment = (SessionListFragment) supportFragmentManager.findFragmentById(R.id.session_list)
            sessionListFragment.listAdapter = sessionListAdapter

        } else {
            mPager = (ViewPager) findViewById(R.id.pager)
            mPager.onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    // When swiping between pages, select the
                    // corresponding tab.
                    getActionBar().setSelectedNavigationItem(position);
                }
            }
            mPager.adapter = new SessionListFragmentAdapter()
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            void onReceive(Context context, Intent intent) {
                if (intent.action==AgendaService.UPDATE_FAVORITES_RESPONSE) {
                    updateFavoriteIcon()
                } else {
                    doFilter()
                }
            }
        }
        def intentFilter = new IntentFilter()
        intentFilter.addAction(AgendaService.UPDATE_FAVORITES_RESPONSE)
        intentFilter.addAction(AgendaService.SESSION_LIST_RESPONSE)
        intentFilter.addCategory(AgendaService.CATEGORY)
        registerReceiver(broadcastReceiver, intentFilter)

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

    @Override
    protected void onResume() {
        // done only to redraw stars, there must be a better way to do this
        sessionListAdapter.notifyDataSetChanged()
        super.onResume()
    }

    private void populateActionBar() {
        def bar = getActionBar()
        bar.navigationMode = ActionBar.NAVIGATION_MODE_TABS
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
            supportFragmentManager.beginTransaction()
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
        sessionListAdapter.filter.filter("2014-07-${28 + tab.position}")

    }

    @Override
    void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mPager?.currentItem = tab.position
        doFilter()
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
        [trackNames, speakerNames]*.add('')

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
                ((SessionListAdapter.SessionFilter) getSessionListAdapter().filter).trackName = trackAdapter.getItem(position)
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
                ((SessionListAdapter.SessionFilter) getSessionListAdapter().filter).speakerId =
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
                trackView.adapter = trackAdapter // clears selection (ugly)
                ((SessionListAdapter.SessionFilter) sessionListAdapter.filter).trackName=null
                doFilter()
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
                speakersView.adapter = speakerAdapter // clears selection (ugly)
                ((SessionListAdapter.SessionFilter) sessionListAdapter.filter).speakerId=null
                doFilter()
                true
            }
        })

        true
    }

    private static class SimpleArrayAdapter extends ArrayAdapter<String> {

        SimpleArrayAdapter(Context context, List<String> objects) {
            super(context, android.R.layout.simple_list_item_1, objects)
        }

    }

    private class SessionListFragmentAdapter extends FragmentPagerAdapter {
        final SessionListFragment[] items = new SessionListFragment[3]

        SessionListFragmentAdapter() {
            super(getSupportFragmentManager())
        }

        @Override
        Fragment getItem(int position) {
            if (items[position]==null) {
                def fragment = new SessionListFragment()
                fragment.listAdapter = sessionListAdapter
                fragment.onAttach(SessionListActivity.this)
                if (isTwoPane()) {
                    fragment.activateOnItemClick = true
                }
                items[position] = fragment
            }
            items[position]
        }

        @Override
        int getCount() {
            3
        }
    }


    public void switchFavorite(MenuItem item) {
        SessionDetailFragment fragment = (SessionDetailFragment) supportFragmentManager.findFragmentById(R.id.session_detail_container)
        Intent intent = new Intent(this, AgendaService)
        intent.action = AgendaService.ACTION_FAVORITE
        intent.putExtra(AgendaService.SESSION_ID, fragment.sessionItem.id)
        startService(intent)
    }

    public void refreshAgenda(MenuItem item) {
        Toast.makeText(this, "Refreshing agenda", Toast.LENGTH_SHORT).show()
        Intent intent = new Intent(this, AgendaService)
        startService(intent)
    }

    public void chooseTrack(MenuItem item) {

    }

    public void chooseSpeaker(MenuItem item) {

    }
}
