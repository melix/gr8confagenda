package me.champeau.gr8confagenda.app.client

import android.content.Context
import android.util.Log
import android.widget.Toast
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

@CompileStatic
class AgendaClient {
    private final String AGENDA_FILE = "agenda.json"
    private final String STATUS_FILE = "status.bin"
    private final int CONFERENCE_ID = 2;

    public static final long GENERATED_ID_BASE = 1_000_000_000
    private long generatedId = GENERATED_ID_BASE

    private final String baseApiUrl

    AgendaClient(String baseApiUrl) {
        this.baseApiUrl = baseApiUrl
    }

    String fetchAgenda() {
        try {
            return new URL("$baseApiUrl/api/agenda/$CONFERENCE_ID").getText('utf-8')
        } catch (Exception e) {
            return null
        }
    }

    boolean shouldUpdate(Context context) {
        def statusFile = new File(context.getCacheDir(), STATUS_FILE)
        Status old = new Status()
        if (statusFile.exists()) {
            statusFile.withObjectInputStream { ObjectInputStream oin ->
                try {
                    old = (Status) oin.readObject()
                } catch (InvalidClassException e) {
                    old = null
                }
            }
        }
        try {
            def json = (Map) new JsonSlurper().parse([:], new URL("$baseApiUrl/api2/status/$CONFERENCE_ID"), 'utf-8')
            Status status = new Status(
                    talks: (String) json.talks,
                    speakers: (String) json.speakers,
                    agenda: (String) json.agenda,
                    favorites: (String) json.favorites
            )
            statusFile.withObjectOutputStream { it.writeObject(status) }

            status != old
        } catch (Exception e) {
            Log.e("AgendaClient", "Unable to fetch status. Network down?")
            false
        }
    }

    File fetchAndCacheAgenda(Context context) {
        def agendaFile = new File(context.getCacheDir(), AGENDA_FILE)
        if (agendaFile.exists() && !shouldUpdate(context)) {
            return agendaFile
        }
        String agenda = fetchAgenda()
        if (agenda) {
            agendaFile.write(agenda ?: "", 'UTF-8')
        }
        agendaFile
    }

    void fetchAgenda(Context ctx, Closure callback) {
        File agenda = fetchAndCacheAgenda(ctx)
        if (agenda.exists()) {
            def feed = (Map) new JsonSlurper().parse(agenda, 'utf-8')
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
                        if (speaker) {
                            speakers << speaker
                        }
                        def session = toSession(item)
                        session.speakerId = speaker?.id

                        session
                    }
                }
            }
            speakers.each { speaker ->
                speaker.talks = new ArrayList<Session>(sessions.findAll {
                    it.speakerId == speaker.id
                })
            }

            callback(speakers, sessions.sort { it.slot.startTime }.unique { it.id })
        } else {
            Toast.makeText(ctx, "Unable to fetch agenda. Please check connectivity.", Toast.LENGTH_SHORT).show()
        }
    }

    protected Speaker toSpeaker(Map source) {
        if (source==null) {
            return null
        }
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

    protected Session toSession(Map source) {
        def tags = (List) source.tags
        if (tags==null) { tags = [] }
        Long id = makeId((String)source.id)
        def session = new Session(
                id: id,
                title: (String) source.title,
                summary: (String) source.summary?:'No detail available for this session',
                tags: new ArrayList<String>(tags),
                slot: toSlot((Map) source.slot)
        )
        session.slot.trackName = source.trackName
        session.slot.trackColor = source.trackColor=="#000000"?"#FFFFFF":source.trackColor

        session
    }

    protected static Slot toSlot(Map source) {
        new Slot(
                date: (String) source.date,
                startTime: ((String) source.startTime).substring(0,5),
                endTime: ((String) source.endTime).substring(0,5)
        )
    }

    private Long makeId(String str) {
        if (str) {
            return (str as Long)
        }
        generatedId++
    }

}