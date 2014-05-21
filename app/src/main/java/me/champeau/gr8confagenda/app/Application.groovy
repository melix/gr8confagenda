package me.champeau.gr8confagenda.app

import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.client.Session
import me.champeau.gr8confagenda.app.client.Speaker;

@CompileStatic
@Singleton
// todo: replace with proper data content provider
class Application {
    List<Session> sessions = []
    List<Speaker> speakers = []
    Set<Long> favorites = []
}