package dk.coded.emia.model.adapter

import android.app.Activity

import java.io.Serializable

import dk.coded.emia.utils.Constants
import dk.coded.emia.utils.Utils

/**
 * Created by oldman on 1/1/18.
 */

class FilterStorage//private constructor.
private constructor() : Serializable {

    private var mListener: ValueFilterListener? = null

    init {

        //Prevent form the reflection api.
        if (sDefaltInstance != null) {
            throw RuntimeException("Use getInstance() method to get the single instance of this class.")
        }
    }

    //Make singleton from serialize and deserialize operation.
    protected fun readResolve(): FilterStorage? {
        return instance
    }

    interface ValueFilterListener {
        fun onSearchTextChange(newSearchTemplate: String)
    }

    inner class Filter {
        var ageMin: Int? = null
        var ageMax: Int? = null
        var gender: Int? = null
        var status: Int? = null
    }

    fun getFilter(activity: Activity): Filter {
        val filter = Filter()
        filter.ageMin = getAgeMin(activity)
        filter.ageMax = getAgeMax(activity)
        filter.gender = getGender(activity)
        filter.status = getStatus(activity)
        return filter
    }

    fun setFilter(filter: Filter, activity: Activity) {
        setAgeMin(filter.ageMin, activity)
        setAgeMax(filter.ageMax, activity)
        setGender(filter.gender, activity)
        setStatus(filter.status, activity)
    }

    fun setListener(listener: ValueFilterListener) {
        mListener = listener
    }

    private fun getAgeMin(activity: Activity): Int? {
        return Utils.getIntPreference(activity, Constants.EXTRA_FILTER_AGE_MIN, Constants.FILTER_MIN_AGE)
    }

    private fun setAgeMin(ageMin: Int?, activity: Activity) {
        Utils.setIntPreference(activity, Constants.EXTRA_FILTER_AGE_MIN, ageMin!!)
    }

    private fun getAgeMax(activity: Activity): Int? {
        return Utils.getIntPreference(activity, Constants.EXTRA_FILTER_AGE_MAX, Constants.FILTER_MAX_AGE)
    }

    private fun setAgeMax(ageMax: Int?, activity: Activity) {
        Utils.setIntPreference(activity, Constants.EXTRA_FILTER_AGE_MAX, ageMax!!)
    }

    private fun getGender(activity: Activity): Int? {
        return Utils.getIntPreference(activity, Constants.EXTRA_FILTER_GENDER, Constants.GENDER_BOTH)
    }

    private fun setGender(gender: Int?, activity: Activity) {
        Utils.setIntPreference(activity, Constants.EXTRA_FILTER_GENDER, gender!!)
    }

    private fun getStatus(activity: Activity): Int? {
        return Utils.getIntPreference(activity, Constants.EXTRA_FILTER_STATUS, Constants.STATUS_MY_FAVORITE)
    }

    private fun setStatus(status: Int?, activity: Activity) {
        Utils.setIntPreference(activity, Constants.EXTRA_FILTER_STATUS, status!!)
    }

    fun getSearchText(activity: Activity): String? {
        var text: String? = Utils.getStringPreference(activity, Constants.EXTRA_SEARCH_TEXT)
        if (text != null && !text.isEmpty()) {
            text = text.trim { it <= ' ' }
        }
        return text
    }

    fun setSearchText(text: String, activity: Activity) {
        Utils.setStringPreference(activity, Constants.EXTRA_SEARCH_TEXT, text)
        if (mListener != null) {
            mListener!!.onSearchTextChange(text)
        }
    }

    companion object {

        @Volatile
        private var sDefaltInstance: FilterStorage? = null

        //if there is no instance available... create new one
        val instance: FilterStorage?
            get() {
                if (sDefaltInstance == null) {
                    synchronized(FilterStorage::class.java) {
                        if (sDefaltInstance == null) {
                            sDefaltInstance = FilterStorage()
                        }
                    }
                }

                return sDefaltInstance
            }
    }

}
