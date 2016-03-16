package me.champeau.gr8confagenda.app.android.adapters

import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.client.Conference;

@CompileStatic
interface IConferenceAdapterDelegate {
    void conferenceTapped(Conference conference)
}