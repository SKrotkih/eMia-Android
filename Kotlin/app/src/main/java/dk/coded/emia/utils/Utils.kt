package dk.coded.emia.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.inputmethod.InputMethodManager

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

import java.util.HashSet

import dk.coded.emia.R

import android.content.Context.INPUT_METHOD_SERVICE
import dk.coded.emia.utils.Constants.Companion.FIREBASE_STORAGE

/**
 * Created by oldman on 12/7/17.
 */

object Utils {

    val glideCacheOptions: RequestOptions
        get() {
            val glideOptions = RequestOptions()
            glideOptions.placeholder(R.drawable.logo)
            glideOptions.dontAnimate()
            glideOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
            return glideOptions
        }

    fun getColor(context: Context, id: Int): Int {
        val version = Build.VERSION.SDK_INT
        return if (version >= 23) {
            ContextCompat.getColor(context, id)
        } else {
            context.resources.getColor(id)
        }
    }

    fun hideSoftKeyboard(activity: Activity) {
        if (activity.currentFocus != null) {
            val inputMethodManager = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    }

    fun showSoftKeyboard(view: View, activity: Activity) {
        val inputMethodManager = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        inputMethodManager.showSoftInput(view, 0)
    }

    fun getPhotoUrlFromStorage(userID: String): String {
        val path = String.format("%s.jpg", userID)
        return String.format("%s%%2F%s?alt=media", Companion.getFIREBASE_STORAGE(), path)
    }

    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo.packageName == context.packageName) {
                isInBackground = false
            }
        }

        return isInBackground
    }

    // STORE

    fun getStringPreference(context: Context, key: String): String {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        return sp.getString(key, "")
    }

    fun setStringPreference(context: Context, key: String, value: String) {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun getIntPreference(context: Context, key: String): Int {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        return sp.getInt(key, 0)
    }

    fun getIntPreference(context: Context, key: String, value: Int): Int {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        return sp.getInt(key, value)
    }

    fun setIntPreference(context: Context, key: String, value: Int) {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putInt(key, value)
        editor.commit()
    }

    fun setLongPreference(context: Context, key: String, value: Long) {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putLong(key, value)
        editor.commit()
    }

    fun getLongPreference(context: Context, key: String): Long {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        return sp.getLong(key, 0)
    }

    fun getFloatPreference(context: Context, key: String): Float {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        return sp.getFloat(key, 0f)
    }

    fun setFloatPreference(context: Context, key: String, value: Float) {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putFloat(key, value)
        editor.commit()
    }

    fun getBooleanPreference(context: Context, key: String): Boolean {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        return sp.getBoolean(key, false)
    }

    fun getBooleanPreference(context: Context, key: String, defVal: Boolean): Boolean {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        return sp.getBoolean(key, defVal)
    }

    fun setBooleanPreference(context: Context, key: String, value: Boolean) {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun getStringSetPreference(context: Context, key: String): Set<String> {
        //HashSet<String> empty = new HashSet<String>();
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        return sp.getStringSet(key, HashSet())
    }

    fun setStringSetPreference(context: Context, key: String, value: Set<String>) {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putStringSet(key, value)
        editor.commit()
    }


    fun hasPreference(context: Context, key: String): Boolean {
        val sp = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        return sp.contains(key)
    }


}
