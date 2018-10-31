package dk.coded.emia.model.Data

import java.io.Serializable
import java.util.HashMap

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

import dk.coded.emia.utils.Constants

// [START comment_class]
@IgnoreExtraProperties
class Favorite : Serializable {

    var level: Int = 0

    init {
        this.level = 0
    }

    // [START post_to_map]
    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result[Constants.Fields.Favorite.level] = level
        return result
    }
    // [END post_to_map]
}
// [END comment_class]
