package me.champeau.gr8confagenda.app.client

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

@CompileStatic
class AgendaClient {
    private final String baseApiUrl

    AgendaClient(String baseApiUrl) {
        this.baseApiUrl = baseApiUrl
    }

    void fetchAgenda(Closure callback) {
        def feed = (Map) new JsonSlurper().parse([:], new URL("$baseApiUrl/agenda/1"), "utf-8")
        List<Session> sessions = []
        List<Speaker> speakers = []
        feed.agendaDays.each { Map day ->
            day.tracks.each { Map track ->
                String color = track.color
                String name = track.name
                track.agendaItems.collect(sessions) { Map item ->
                    item.trackColor = color
                    item.trackName = name
                    def speaker = toSpeaker((Map) item.speaker)
                    speakers << speaker
                    def session = toSession(item)
                    session.speakerId = speaker.id

                    session
                }
            }
        }
        speakers.each { speaker ->
            speaker.talks = new ArrayList<Session>(sessions.findAll { it.speakerId == speaker.id })
        }

        callback(speakers, sessions.sort { it.slot.startTime} )
    }

    protected static Speaker toSpeaker(Map source) {
        def speaker = new Speaker(
                id: source.id as Long,
                name: (String) source.name,
                employer: (String) source.employer,
                image: (String) source.image,
                twitter: (String) source.twitter,
                bio: (String) source.bio)
        speaker.talks = source.talks.collect { toSession((Map) it) }
        speaker.talks*.speakerId = speaker.id
        speaker
    }

    protected static Session toSession(Map source) {
        def session = new Session(
                id: source.id as Long,
                title: (String) source.title,
                summary: (String) source.summary,
                tags: new ArrayList<String>((List) source.tags),
                slot: toSlot((Map) source.slot)
        )
        session.slot.trackName = source.trackName
        session.slot.trackColor = source.trackColor

        session
    }

    protected static Slot toSlot(Map source) {
        new Slot(
                date: (String) source.date,
                startTime: ((String) source.startTime).substring(0,5),
                endTime: ((String) source.endTime).substring(0,5)
        )
    }

}