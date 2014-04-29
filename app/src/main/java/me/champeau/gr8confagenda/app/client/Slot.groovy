package me.champeau.gr8confagenda.app.client

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@CompileStatic
@ToString(includeNames = true)
@EqualsAndHashCode
class Slot implements Serializable{
    String date
    String startTime
    String endTime
    String trackName
    String trackColor
}