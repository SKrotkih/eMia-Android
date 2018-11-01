package dk.coded.emia.notifications

import android.content.Context
import android.util.Log

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

import dk.coded.emia.model.Data.User
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.utils.Constants
import dk.coded.emia.utils.Utils

import android.content.ContentValues.TAG

class NotificationFirebaseInstanceIdService : FirebaseInstanceIdService() {
    private var databaseInteractor: DatabaseInteractor? = null

    internal var context: Context? = null

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        context = this
        databaseInteractor = DatabaseFactory.databaseInteractor
        databaseInteractor!!.currentUser({ status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val user = data as User
                user.tokenAndroid = refreshedToken
                databaseInteractor!!.updateUser(user, { result: Int, `object`: Any? ->
                    if (result == Constants.SUCCESS) {
                        Utils.setStringPreference(context!!, Constants.EXTRA_TOKEN, refreshedToken!!)
                    } else if (result == Constants.FAIL) {
                    } else if (result == Constants.CANCEL) {
                    }
                })
            }
        })

        Log.d(Constants.TAG, "Refreshed token: " + refreshedToken!!)

        sendRegistrationToServer(refreshedToken)
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
    }
}
