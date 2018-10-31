package dk.coded.emia.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import dk.coded.emia.R
import dk.coded.emia.View.activity.MainActivity
import dk.coded.emia.utils.Constants
import dk.coded.emia.utils.Utils

class NotificationFirebaseMessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(Constants.TAG, "onCreate: ")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        if (remoteMessage == null) {
            return
        }
        if (remoteMessage.data != null) {
            val ctx = this
            val h = Handler(Looper.getMainLooper())
            h.post {
                if (!Utils.isAppIsInBackground(ctx)) {
                    val intent = Intent(Constants.NOTIFICATION_LOCAL_BROADCAST)
                    intent.putExtra(Constants.EXTRA_NOTIFICATION_TITLE, remoteMessage.data["title"])
                    intent.putExtra(Constants.EXTRA_NOTIFICATION_BODY, remoteMessage.data["body"])
                    intent.putExtra(Constants.EXTRA_NOTIFICATION_SENDER_ID, remoteMessage.data["uid"])
                    intent.putExtra(Constants.EXTRA_NOTIFICATION_MSG_TYPE, remoteMessage.data["messageType"])
                    intent.putExtra(Constants.EXTRA_NOTIFICATION_URL, remoteMessage.data["attachment-url"])
                    intent.putExtra(Constants.EXTRA_NOTIFICATION_LIKE, remoteMessage.data["like"])
                    intent.putExtra(Constants.EXTRA_NOTIFICATION_USERINFO, remoteMessage.data["userinfo"])
                    LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent)
                } else {
                    createNotification(remoteMessage, true)
                }
            }
        }
    }

    private fun createNotification(remoteMessage: RemoteMessage, isSound: Boolean) {

        val recipientID = remoteMessage.data["uid"].toString()
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val type = remoteMessage.data["messageType"]

        val intent: Intent

        when (type) {
            Constants.NOTIFICATION_TYPE_LIKE -> intent = Intent(this, MainActivity::class.java)
            else -> intent = Intent(this, MainActivity::class.java)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(Constants.PEOPLE_ID, recipientID)

        val resultIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_SOUND)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(resultIntent)

        if (isSound) {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)
            val notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notificationBuilder.setSound(notificationSoundURI)
        } else {
            notificationBuilder.setDefaults(0)
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color = resources.getColor(R.color.colorAccent)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(Constants.TAG, "onDestroy: ")
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        Log.d(Constants.TAG, "onTaskRemoved: ")
    }
}
