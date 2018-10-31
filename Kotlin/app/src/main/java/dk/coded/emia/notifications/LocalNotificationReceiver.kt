package dk.coded.emia.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LocalNotificationReceiver : BroadcastReceiver() {
    internal var listener: LocalNotificationListener

    fun addUpdateListener(listener: LocalNotificationListener) {
        this.listener = listener
    }

    override fun onReceive(context: Context, intent: Intent) {
        listener.onLocalNotificationUpdate(intent)
    }
}
