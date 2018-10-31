package dk.coded.emia.model.Data

import java.io.Serializable
import java.util.HashMap

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

import dk.coded.emia.utils.Constants

// [START comment_class]
@IgnoreExtraProperties
class Comment : Serializable {

    var id: String? = null
    var uid: String? = null
    var postid: String? = null
    var author: String? = null
    var text: String
    var created: Long = 0

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    constructor(text: String) {
        this.text = text
        this.created = System.currentTimeMillis() / 1000
    }

    // [START post_to_map]
    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result[Constants.Fields.Comment.id] = id
        result[Constants.Fields.Comment.uid] = uid
        result[Constants.Fields.Comment.postid] = postid
        result[Constants.Fields.Comment.author] = author
        result[Constants.Fields.Comment.text] = text
        result[Constants.Fields.Comment.created] = created
        return result
    }
    // [END post_to_map]


}
// [END comment_class]
