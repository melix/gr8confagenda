package me.champeau.gr8confagenda.app

import android.app.Activity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import groovy.transform.CompileStatic

@CompileStatic
class ConferenceListActivity extends Activity {
    @Override
    boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater()
        inflater.inflate(R.menu.menu_conference_list, menu)
        true
    }

    void refreshConferences(MenuItem menuItem) {

    }
}