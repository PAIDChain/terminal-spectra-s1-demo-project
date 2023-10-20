package my.paidchain.spectraterminaldemo.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log

class StartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            log(Level.INFO, javaClass.simpleName) { "StartReceiver onReceive" }
            Intent(context, IntentService::class.java).also {
                it.action = ServiceState.START.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    log(Level.INFO, javaClass.simpleName) { "Starting the service in >=26 Mode from a BroadcastReceiver" }
                    context.startForegroundService(it)
                    return
                }
                log(Level.INFO, javaClass.simpleName) { "Starting the service in < 26 Mode from a BroadcastReceiver" }
                context.startService(it)
            }
        }
    }
}