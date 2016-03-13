package me.champeau.gr8confagenda.app
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import groovy.transform.CompileStatic

@CompileStatic
class ConferenceListActivity extends Activity {
    private static final String TAG = ConferenceListActivity.class.simpleName

    private BroadcastReceiver broadcastReceiver

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conferences)

        actionBar.displayHomeAsUpEnabled = true

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
}