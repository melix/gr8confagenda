package me.champeau.gr8confagenda.app;

import android.app.IntentService;
import android.content.Intent
import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.client.AgendaClient
import me.champeau.gr8confagenda.app.client.Session
import me.champeau.gr8confagenda.app.client.Speaker

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread. This service contacts the remote REST
 * API in order to download the agenda.
 * <p>
 */
@CompileStatic
class AgendaService extends IntentService {
    public static final String CATEGORY = "${AgendaService}Category"
    public static final String SESSION_LIST_RESPONSE = "${AgendaService}.SessionListResponse"

    public AgendaService() {
        super("AgendaService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (Application.instance.sessions.empty) {
                def client = new AgendaClient('http://cfp.gr8conf.org')
                client.fetchAgenda(applicationContext) { speakers, sessions ->
                    Application.instance.sessions = (List<Session>) sessions
                    Application.instance.speakers = (List<Speaker>) speakers
                }
            }
            def response = new Intent()
            response.setAction(SESSION_LIST_RESPONSE)
            response.addCategory(CATEGORY)
            sendBroadcast(response)
        }
    }
}
