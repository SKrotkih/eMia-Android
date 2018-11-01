package dk.coded.emia.model.Data

import android.graphics.Bitmap

import java.io.Serializable

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

import java.util.Calendar
import java.util.Date
import java.util.HashMap

import dk.coded.emia.utils.Constants

// [START post_class]
@IgnoreExtraProperties
class Post : Serializable {

    var id: String? = null
    var uid: String? = null
    var author: String? = null
    var title: String = ""
    var body: String = ""
    var photoBitmap: Bitmap? = null
    var created: Long = 0
    var photosize: String = ""
    var starCount = 0
    var stars: Map<String, Boolean> = HashMap()

    val date: Calendar
        get() {
            val date = Calendar.getInstance()
            date.timeInMillis = created * 1000
            return date
        }

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    constructor(title: String, body: String, bitmap: Bitmap) {
        this.title = title
        this.body = body
        this.photoBitmap = bitmap
        this.photosize = String.format("%d;%d", bitmap.width, bitmap.height)
        this.created = System.currentTimeMillis() / 1000
    }

    // [START post_to_map]
    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result[Constants.Fields.Post.id] = id!!
        result[Constants.Fields.Post.uid] = uid!!
        result[Constants.Fields.Post.author] = author!!
        result[Constants.Fields.Post.title] = title
        result[Constants.Fields.Post.body] = body
        result[Constants.Fields.Post.starCount] = starCount
        result[Constants.Fields.Post.stars] = stars
        result[Constants.Fields.Post.photosize] = photosize
        result[Constants.Fields.Post.created] = created
        return result
    }
    // [END post_to_map]

    fun photoWidth(): Double? {
        val separated = photosize.split(";".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        return if (separated.size == 2) {
            java.lang.Double.parseDouble(separated[0])
        } else {
            0.0
        }
    }

    fun photoHeight(): Double? {
        val separated = photosize.split(";".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        return if (separated.size == 2) {
            java.lang.Double.parseDouble(separated[1])
        } else {
            0.0
        }
    }

}
// [END post_class]
