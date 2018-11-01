package dk.coded.emia.model.Data

import android.content.Context

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

import java.util.HashMap

import dk.coded.emia.utils.Constants
import dk.coded.emia.utils.Utils

// [START blog_user_class]
@IgnoreExtraProperties
class User {

    var id: String = ""
    var username: String = ""
    var email: String = ""
    var address: String? = null
    var gender: Int? = null
    var yearbirth: Int? = null
    var tokenAndroid: String? = null
    var tokenIOS: String? = null
    // [END post_to_map]

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    constructor(id: String, name: String, email: String) {
        this.id = id
        this.username = name
        this.email = email
        this.address = null
        this.yearbirth = null
        this.gender = null
        this.tokenAndroid = null
        this.tokenIOS = null
    }

    val iosTokens: Array<String>?
        @Exclude
        get() {
            var tokens: Array<String>? = null

            if (tokenIOS != null) {
                tokens = tokenIOS!!.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            }
            return tokens
        }

    val androidTokens: Array<String>?
        @Exclude
        get() {
            var tokens: Array<String>? = null

            if (tokenAndroid != null && !tokenAndroid!!.isEmpty()) {
                tokens = tokenAndroid!!.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            }
            return tokens
        }

    val avatarFullUrl: String
        @Exclude
        get() = Utils.getPhotoUrlFromStorage(id)

    // [START post_to_map]
    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result[Constants.Fields.User.id] = id
        result[Constants.Fields.User.username] = username
        result[Constants.Fields.User.email] = email
        result[Constants.Fields.User.yearbirth] = if (yearbirth == null) 0 else yearbirth!!
        result[Constants.Fields.User.gender] = if (gender == null) -1 else gender!!
        result[Constants.Fields.User.address] = if (address == null) "" else address!!
        result[Constants.Fields.User.tokenAndroid] = if (tokenAndroid == null) "" else tokenAndroid!!
        result[Constants.Fields.User.tokenIOS] = if (tokenIOS == null) "" else tokenIOS!!

        return result
    }
}
// [END blog_user_class]
