package me.champeau.gr8confagenda.app
import android.app.IntentService
import android.content.Intent
import android.os.Handler
import groovy.transform.CompileStatic
import me.champeau.gr8confagenda.app.client.Conference
import me.champeau.gr8confagenda.app.client.ConferencesClient
import me.champeau.gr8confagenda.app.gr8confapi.GR8ConfAPI
/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread. This service contacts the remote REST
 * API in order to download the conferences
 * <p>
 */
@CompileStatic
class ConferencesService extends IntentService {
    private static final String TAG = ConferencesService.class.simpleName
    public static final String CONFERENCES_CATEGORY = "${ConferencesService}Category"
    public static final String CONFERENCES_LIST_RESPONSE = "${ConferencesService}.SessionListResponse"

    Handler mainThreadHandler = null;

    ConferencesService() {
        super("ConferencesService")
        mainThreadHandler = new Handler()
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            doFetchConferences()
        }
    }

    private void doFetchConferences() {
        def client = new ConferencesClient(GR8ConfAPI.ROOT_API_URL, GR8ConfAPI.API_VERSION_2)
        client.fetchConferences(applicationContext) { conferences ->
            Application.instance.conferences = (List<Conference>) conferences
        }
        def response = new Intent()
        response.setAction(CONFERENCES_LIST_RESPONSE)
        response.addCategory(CONFERENCES_CATEGORY)
        sendBroadcast(response)
    }
}
