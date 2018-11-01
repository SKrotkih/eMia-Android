package dk.coded.emia.model.adapter

import android.app.Activity
import android.util.Log
import android.widget.Toast

import dk.coded.emia.R
import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.Data.User
import dk.coded.emia.utils.Constants

/**
 * Created by oldman on 1/2/18.
 */

class PostListValidator(private val mActivity: Activity) {

    fun isValid(post: Post, user: User?): Boolean? {
        if (user == null) {
            Toast.makeText(mActivity, "Author of the post id" + post.id + " is not presented!", Toast.LENGTH_SHORT).show()
            return false
        }
        val filter = FilterStorage.instance!!.getFilter(mActivity)

        val ageMin = filter.ageMin
        val ageMax = filter.ageMax
        val gender = filter.gender
        val favoriteStatus = filter.status
        val searchString = FilterStorage.instance!!.getSearchText(mActivity)


        var isValidGender: Boolean? = false
        if (gender == Constants.GENDER_BOTH) {
            isValidGender = true
        } else if (user.gender === gender) {

            Log.d("Validator:", "I'm " + user.gender.toString() + "; Looking for " + gender.toString())

            isValidGender = true
        }

        var isSearchOk: Boolean? = true
        if (searchString != null && !searchString.isEmpty()) {

            Log.d("Validator:", "Search $searchString")

            val title = post.title
            val body = post.body
            isSearchOk = title.toLowerCase().contains(searchString.toLowerCase()) || body.toLowerCase().contains(searchString.toLowerCase())
        }

        return isValidGender!! && isSearchOk!!
    }

}
