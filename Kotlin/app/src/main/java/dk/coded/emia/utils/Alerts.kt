package dk.coded.emia.utils

import android.app.Activity
import android.content.DialogInterface
import android.support.v7.app.AlertDialog

import dk.coded.emia.utils.Constants

/**
 * Created by oldman on 1/28/18.
 */

object Alerts {

    fun alertOk(activity: Activity, title: String, message: String, callback: BasicCallBack) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
        builder.setMessage(message)
        val alert = builder.create()
        alert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, which -> callback(Constants.SUCCESS, null) }
        alert.show()
    }

}
