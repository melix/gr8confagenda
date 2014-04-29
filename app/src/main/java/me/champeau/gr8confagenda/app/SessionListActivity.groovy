package me.champeau.gr8confagenda.app

import android.app.ActionBar
import android.app.Activity
import android.app.FragmentTransaction
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        }

        populateActionBar()

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
}
