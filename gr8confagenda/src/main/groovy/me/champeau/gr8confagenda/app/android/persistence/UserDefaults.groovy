package me.champeau.gr8confagenda.app.android.persistence

import android.content.Context
import android.content.SharedPreferences
import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.R;

@CompileStatic
class UserDefaults {

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
            putStringSet("favorites", favorites.collect { it.toString() } as Set)
        }
    }

    Set<Long> getFavourites() {
        SharedPreferences sharedPref = prefs()
        sharedPref.getStringSet ( "favorites", new LinkedHashSet<String> ( ) ).collect {
            it.toLong()
        } as Set
    }
}