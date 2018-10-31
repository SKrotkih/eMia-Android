package dk.coded.emia.notifications

import android.app.Activity
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

import dk.coded.emia.R
import dk.coded.emia.View.GlideApp
import dk.coded.emia.View.activity.PostDetailActivity
import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.Data.User
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.utils.Constants
import dk.coded.emia.utils.PositionedCropTransformation
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

import com.bumptech.glide.request.RequestOptions.bitmapTransform
import dk.coded.emia.utils.Constants.SUCCESS

object RemoteNotifications {

    fun sendNotification(recipient: User, sender: User, type: String, body: Any) {
        val runnable = Runnable {
            val titleNotification = sender.username
            var bodyNotification = ""
            val attachmentUrl = sender.avatarFullUrl

            Log.d(Constants.TAG, "MESSAGE FROM $sender TO $recipient TYPE $type")

            when (type) {
                Constants.NOTIFICATION_TYPE_LIKE -> bodyNotification = Constants.NOTIFICATION_LIKE_BODY + " '" + body as String + "'"
            }

            val tokensIOS = recipient.getIOSTokens()
            val tokensAndroid = recipient.androidTokens

            //
            // SEND to all iOS devices
            //

            if (tokensIOS != null) {
                for (i in tokensIOS!!.indices) {
                    val client = OkHttpClient()
                    val json = JSONObject()
                    val notificationJson = JSONObject()
                    val dataJson = JSONObject()
                    try {
                        notificationJson.put("title", titleNotification)
                        notificationJson.put("body", bodyNotification)
                        notificationJson.put("sound", "default")

                        dataJson.put("uid", sender.id)
                        dataJson.put("title", titleNotification)
                        dataJson.put("body", bodyNotification)
                        dataJson.put("messageType", type)
                        dataJson.put("attachment-url", attachmentUrl)
                        dataJson.put("userdata", "")  // TODO: Send Post id

                        json.put("notification", notificationJson)
                        json.put("data", dataJson)

                        json.put("to", tokensIOS!![i])
                        json.put("content_available", false)

                        val JSON = MediaType.parse("application/json; charset=utf-8")
                        val body = RequestBody.create(JSON, json.toString())
                        val request = Request.Builder()
                                .header("Authorization", "key=" + Constants.FIREBASE_NOTIFICATION_SERVER_KEY)
                                .url(Constants.FIREBASE_NOTIFICATION_SERVER)
                                .post(body)
                                .build()
                        val response = client.newCall(request).execute()
                        //String finalResponse = response.body().string();
                        //Log.d(Constants.TAG, "sendNotification: " + finalResponse);
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }

            // Prevent send message it to itself
            if (sender.tokenAndroid == recipient.tokenAndroid) {
                return@Runnable
            }

            //
            // SEND to all Android devices
            //


            if (tokensAndroid != null) {
                for (i in tokensAndroid.indices) {
                    val client = OkHttpClient()
                    val json = JSONObject()
                    val dataJson = JSONObject()
                    try {
                        dataJson.put("uid", sender.id)
                        dataJson.put("title", titleNotification)
                        dataJson.put("body", bodyNotification)
                        dataJson.put("messageType", type)
                        dataJson.put("attachment-url", attachmentUrl)
                        dataJson.put("userdata", "")  // TODO: Send Post id

                        json.put("data", dataJson)

                        json.put("to", tokensAndroid[i])
                        json.put("content_available", false)

                        Log.d(Constants.TAG, "sendNotification JSON: $json")

                        val JSON = MediaType.parse("application/json; charset=utf-8")
                        val body = RequestBody.create(JSON, json.toString())
                        val request = Request.Builder()
                                .header("Authorization", "key=" + Constants.FIREBASE_NOTIFICATION_SERVER_KEY)
                                .url(Constants.FIREBASE_NOTIFICATION_SERVER)
                                .post(body)
                                .build()
                        val response = client.newCall(request).execute()
                        val finalResponse = response.body()!!.string()
                        Log.d(Constants.TAG, "sendNotification RES: $finalResponse")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }

        Thread(runnable).start()
    }

    fun showNotification(intent: Intent, activity: Activity, withClick: Boolean?) {
        val tvNotificationTitle = activity.findViewById<View>(R.id.tvNotificationTitle) as TextView
        val tvNotificationBody = activity.findViewById<View>(R.id.tvNotificationBody) as TextView
        val ivNotificationImage = activity.findViewById<View>(R.id.ivNotificationImage) as ImageView
        val rlNotification = activity.findViewById<View>(R.id.rlNotification) as RelativeLayout
        rlNotification.visibility = View.VISIBLE
        rlNotification.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_down))

        val title = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_TITLE)
        val body = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_BODY)
        val url = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_URL)
        val type = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_MSG_TYPE)
        val senderID = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_SENDER_ID)
        val postId = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_USERINFO)

        tvNotificationTitle.text = title
        tvNotificationBody.text = body

        if (URLUtil.isValidUrl(url)) {
            Glide.with(activity).load(url).into(ivNotificationImage)
        } else {
            DatabaseFactory.databaseInteractor.downloadPhoto(activity, senderID, { status: Int, data: Any ->
                if (status == SUCCESS) {
                    val uri = data as Uri
                    GlideApp.with(activity.applicationContext)
                            .load(uri.toString())
                            .apply(bitmapTransform(PositionedCropTransformation(1f, 0f)))
                            .into(ivNotificationImage)
                }
            })
        }

        if (withClick!!) {
            rlNotification.setOnClickListener {
                when (type) {
                    Constants.NOTIFICATION_TYPE_LIKE -> browsePost(activity, postId)
                    else -> {
                    }
                }
            }
        }

        playSound(activity)

        Handler().postDelayed({
            rlNotification.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_up))
            rlNotification.animation
                    .setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(arg0: Animation) {
                            rlNotification.visibility = View.GONE
                        }

                        override fun onAnimationRepeat(arg0: Animation) {}
                        override fun onAnimationStart(arg0: Animation) {}
                    })
        }, Constants.NOTIFICATION_HIDE_TIME.toLong())
    }

    private fun playSound(activity: Activity) {
        val defaultRingtone = RingtoneManager.getRingtone(activity, Settings.System.DEFAULT_NOTIFICATION_URI)
        val currentRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(activity.applicationContext, RingtoneManager.TYPE_NOTIFICATION)
        val currentRingtone = RingtoneManager.getRingtone(activity, currentRintoneUri)
        currentRingtone.play()
    }

    private fun browsePost(activity: Activity, postId: String) {
        DatabaseFactory.databaseInteractor.getPost(postId, { status: Int, data: Any ->
            if (status == SUCCESS) {
                val intent = Intent(activity, PostDetailActivity::class.java)
                val post = data as Post
                intent.putExtra("post", post)
                activity.startActivity(intent)
            }
        })
    }
}
