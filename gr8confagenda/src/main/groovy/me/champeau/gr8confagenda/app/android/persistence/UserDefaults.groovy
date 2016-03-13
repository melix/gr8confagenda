package me.champeau.gr8confagenda.app.android.persistence

import android.content.Context
import android.content.SharedPreferences
import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.R;

@CompileStatic
class UserDefaults {
    private static final String USER_DEFAULTS_KEY_FAVORITES = 'favorites'

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
        context.getSharedPreferences(context.getString(R.string.favorites_list), Context.MODE_PRIVATE)
    }

    void setFavourites(List<Long> favorites) {
        edit {
            putStringSet(USER_DEFAULTS_KEY_FAVORITES, favorites.collect { it.toString() } as Set)
        }
    }

    Set<Long> getFavourites() {
        SharedPreferences sharedPref = prefs()
        sharedPref.getStringSet (USER_DEFAULTS_KEY_FAVORITES, new LinkedHashSet<String> ( ) ).collect {
            it.toLong()
        } as Set
    }
}