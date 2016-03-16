package me.champeau.gr8confagenda.app

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import groovy.transform.CompileStatic

@CompileStatic
class SplashScreenActivity extends Activity {

    private BroadcastReceiver broadcastReceiver

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            void onReceive(Context context, Intent intent) {

                if(intent.action == ConferencesService.CONFERENCES_LIST_RESPONSE) {
                    Intent i = new Intent(context, AgendaService)
                    context.startService(i)


                } else if(intent.action == AgendaService.SESSION_LIST_RESPONSE) {
                    def next = new Intent(context, SessionListActivity)
                    context.startActivity(next)
                    finish()
                }
            }
        }

        def intentFilter = new IntentFilter()
        intentFilter.addAction(ConferencesService.CONFERENCES_LIST_RESPONSE)
        intentFilter.addCategory(ConferencesService.CONFERENCES_CATEGORY)
        intentFilter.addAction(AgendaService.SESSION_LIST_RESPONSE)
        intentFilter.addCategory(AgendaService.CATEGORY)
        registerReceiver(broadcastReceiver, intentFilter)

        Intent intent = new Intent(this, ConferencesService)
        startService(intent)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy()
        if(broadcastReceiver) {
            unregisterReceiver(broadcastReceiver);
        }
    }
}