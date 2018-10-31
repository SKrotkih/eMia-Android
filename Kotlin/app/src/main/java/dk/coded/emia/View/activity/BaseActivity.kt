package dk.coded.emia.View.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager

import dk.coded.emia.notifications.LocalNotificationListener
import dk.coded.emia.notifications.LocalNotificationReceiver
import dk.coded.emia.notifications.RemoteNotifications
import dk.coded.emia.utils.Constants

open class BaseActivity : AppCompatActivity() {

    private var mActivity: Activity? = null
    private var mNotificationListening: Boolean? = false
    private var mProgressDialog: ProgressDialog? = null
    private var mFocusView: View? = null
    private var notificationReceiver: LocalNotificationReceiver? = null

    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.setCancelable(false)
            mProgressDialog!!.setMessage("Loading...")
        }
        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    // Push Notifications
    internal fun prepareNotificationsListener(activity: Activity) {
        mNotificationListening = true
        mActivity = activity
        notificationReceiver = LocalNotificationReceiver()
        notificationReceiver!!.addUpdateListener((mActivity as LocalNotificationListener?)!!)
    }

    public override fun onResume() {
        super.onResume()

        if (mNotificationListening!!) {
            val filter = IntentFilter(Constants.NOTIFICATION_LOCAL_BROADCAST)
            LocalBroadcastManager.getInstance(mActivity).registerReceiver(notificationReceiver, filter)
        }
    }

    public override fun onPause() {
        if (mNotificationListening!!) {
            LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(notificationReceiver)
        }
        super.onPause()
    }

    public override fun onStop() {
        hideKeyboard()
        super.onStop()
    }

    // LocalNotificationListener Interface implementation
    fun onLocalNotificationUpdate(intent: Intent) {
        RemoteNotifications.showNotification(intent, mActivity!!, true)
    }

    // Keyboard

    fun showKeyboard(focusView: View) {
        mFocusView = focusView
        mFocusView!!.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun hideKeyboard() {
        if (mFocusView == null) {
            return
        }
        val imm = getSystemService(this.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mFocusView!!.windowToken, 0)
        mFocusView = null
    }

}
