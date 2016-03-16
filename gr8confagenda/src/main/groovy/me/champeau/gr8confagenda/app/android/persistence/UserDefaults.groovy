package me.champeau.gr8confagenda.app.android.persistence

import android.content.Context
import android.content.SharedPreferences
import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.Application
import me.champeau.gr8confagenda.app.R;

@CompileStatic
class UserDefaults {
    private static final String PREFS_NAME = 'GR8ConfPrefs'
    private static final String USER_DEFAULTS_KEY_FAVORITES = 'favorites'
    private static final String USER_DEFAULTS_KEY_CONFERENCE_ID = 'conferenceId'

    Context context

    UserDefaults(Context context) {
        this.context = context
    }

    private void edit(@DelegatesTo(SharedPreferences.Editor) Closure cl) {
        def edit = prefs().edit()
        cl.delegate = edit
        cl()
        edit.commit()
    }

    private SharedPreferences prefs() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    void setFavourites(Set<Long> favorites) {
        edit {
            putStringSet(USER_DEFAULTS_KEY_FAVORITES, favorites.collect { it.toString() } as Set)
        }
    }

    Set<Long> getFavourites() {
        prefs().getStringSet (USER_DEFAULTS_KEY_FAVORITES, new LinkedHashSet<String> ( ) ).collect {
            it.toLong()
        } as Set
    }

    void setConferenceId(int conferenceId) {
        edit {
            putInt(USER_DEFAULTS_KEY_CONFERENCE_ID, conferenceId)
        }
    }

    int getConferenceId() {
        int defaultConferenceId = !Application.instance.conferences?.isEmpty() ? Application.instance.conferences[0].id : 0
        prefs().getInt(USER_DEFAULTS_KEY_CONFERENCE_ID,defaultConferenceId)
    }
}