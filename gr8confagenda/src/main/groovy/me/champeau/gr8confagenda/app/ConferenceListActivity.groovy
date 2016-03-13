package me.champeau.gr8confagenda.app
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.android.adapters.ConferencesAdapter
import me.champeau.gr8confagenda.app.android.adapters.IConferenceAdapterDelegate
import me.champeau.gr8confagenda.app.client.Conference

@CompileStatic
class ConferenceListActivity extends Activity implements IConferenceAdapterDelegate {

    private static final String TAG = ConferenceListActivity.class.simpleName

    private BroadcastReceiver broadcastReceiver

    RecyclerView recyclerView

    ConferencesAdapter adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conferences)

        recyclerView = (RecyclerView)findViewById(R.id.reyclerView)

        actionBar.displayHomeAsUpEnabled = true

        adapter = new ConferencesAdapter(this,this)
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this)
        recyclerView.setLayoutManager(layoutManager)
        recyclerView.setHasFixedSize(true)
        recyclerView.setAdapter(adapter)

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            void onReceive(Context context, Intent intent) {
                refreshUi();
            }
        }

        def intentFilter = new IntentFilter()
        intentFilter.addAction(ConferencesService.CONFERENCES_LIST_RESPONSE)
        intentFilter.addCategory(ConferencesService.CONFERENCES_CATEGORY)
        registerReceiver(broadcastReceiver, intentFilter)

        refreshConferences(null)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy()
        if (broadcastReceiver) {
            unregisterReceiver(broadcastReceiver)
        }
    }

    void refreshUi() {
        adapter.conferences = Application.instance.conferences
    }

    @Override
    boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater()
        inflater.inflate(R.menu.menu_conference_list, menu)
        true
    }

    void refreshConferences(MenuItem menuItem) {
        Toast.makeText(this, "Refreshing Conferences", Toast.LENGTH_SHORT).show()
        Intent intent = new Intent(this, ConferencesService)
        startService(intent)
    }

    void conferenceTapped(Conference conference) {
    }
}