package me.champeau.gr8confagenda.app.client

import android.content.Context
import android.util.Log
import android.widget.Toast
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

@CompileStatic
class ConferencesClient {
    private static final String TAG = ConferencesClient.class.simpleName
    private static String ENDPOINT_CONFERECES = 'conferences'
    private final String CONFERENCES_FILE = "conferences.json"

    private final String baseApiUrl
    private final String apiVersion

    ConferencesClient(String baseApiUrl, String apiVersion) {
        this.baseApiUrl = baseApiUrl
        this.apiVersion = apiVersion
    }

    String fetchConferences() {
        try {
            return new URL("${baseApiUrl}/${apiVersion}/${ENDPOINT_CONFERECES}").getText('utf-8')
        } catch (Exception e) {
            Log.e(TAG, 'Exception caught while calling the conferences endpoint')
            return null
        }
    }

    File fetchAndCacheConferences(Context context) {
        def conferencesFile = new File(context.getCacheDir(), CONFERENCES_FILE)
        String conferences = fetchConferences()
        if (conferences) {
            conferencesFile.write(conferences ?: "", 'UTF-8')
        }
        conferencesFile
    }

    void fetchConferences(Context ctx, Closure callback) {
        File conferencesFile = fetchAndCacheConferences(ctx)
        if (conferencesFile.exists()) {
            def result = new JsonSlurper().parse(conferencesFile, 'utf-8')
            List<Conference> conferences = []
            if(result in List) {
                for(def obj : (List)result) {
                    if(obj in Map) {
                        Map m = (Map)obj
                        Conference conference = toConference(m)
                        if(conference) {
                            conferences << conference
                        }
                    }
                }
            }
            conferences.sort { a, b ->
                b.start <=> a.start
            }

            callback(conferences)
        } else {
            Toast.makeText(ctx, "Unable to fetch conferences. Please check connectivity.", Toast.LENGTH_SHORT).show()
        }
    }

    protected Conference toConference(Map source) {
        if (source==null) {
            return null
        }
        new Conference(
                id: (int) source.id,
                name: (String) source.name,
                location: (String) source.location,
                start: (String) source.start,
                end: (String) source.end,
                timeZone: (String) source.timeZone)
    }
}