package me.champeau.gr8confagenda.app.client

import groovy.transform.CompileStatic

@CompileStatic
class Conference {
    int id
    String name
    String location
    // yyyy-MM-dd
    String start
    // yyyy-MM-dd
    String end
    String timeZone
}