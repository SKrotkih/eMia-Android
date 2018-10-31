package dk.coded.emia.notifications

import android.content.Intent

interface LocalNotificationListener {
    fun onLocalNotificationUpdate(intent: Intent)
}
