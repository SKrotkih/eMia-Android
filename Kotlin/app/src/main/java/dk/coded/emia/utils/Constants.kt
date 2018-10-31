package dk.coded.emia.utils


/**
 * Created by manuja on 14/5/17.
 */

class Constants {

    // Table field names

    inner class Fields {
        object User {
            val id = "id"
            val username = "username"
            val email = "email"
            val yearbirth = "yearbirth"
            val gender = "gender"
            val address = "address"
            val tokenAndroid = "tokenAndroid"
            val tokenIOS = "tokenIOS"
        }

        object Post {
            val id = "id"
            val uid = "uid"
            val author = "author"
            val title = "title"
            val body = "body"
            val starCount = "starCount"
            val stars = "stars"
            val photosize = "photosize"
            val created = "created"
        }

        object Favorite {
            val level = "level"
        }

        object Comment {
            val id = "id"
            val uid = "uid"
            val postid = "postid"
            val author = "author"
            val text = "text"
            val created = "created"
        }

    }

    companion object {

        val TAG = "BOBLBERGDBG"

        val DATABASE_NAME = "main"

        var FIREBASE_NOTIFICATION_SERVER = "https://fcm.googleapis.com/fcm/send"
        var FIREBASE_NOTIFICATION_SERVER_KEY = "AAAAdWppnwU:APA91bGrOKOmQcWVrhqCVrRphwzoegc4Rk9qD3urkG4jYiFGazB18dzjbb01uARQPSHPGMXyALtf8pW2mPudQN9e_L4wY-bISpaiH2QaLfEdILcRJjMfvZHq5LDdDfESngBEShCHhp9Q"
        val FIREBASE_STORAGE = "gs://boblberg-b8a0f.appspot.com"

        val TRANSPARENT_COLOR = "#00000000"

        val PREFERENCES = "PREFERENCES"

        var EXTRA_FILTER_GENDER = "EXTRA_FILTER_GENDER"
        var EXTRA_FILTER_STATUS = "EXTRA_FILTER_STATUS"
        var EXTRA_FILTER_AGE_MIN = "EXTRA_FILTER_AGE_MIN"
        var EXTRA_FILTER_AGE_MAX = "EXTRA_FILTER_AGE_MAX"
        var EXTRA_SEARCH_TEXT = "EXTRA_SEARCH_TEXT"

        // Notifications

        var EXTRA_NOTIFICATION_TITLE = "EXTRA_NOTIFICATION_TITLE"
        var EXTRA_NOTIFICATION_BODY = "EXTRA_NOTIFICATION_BODY"
        var EXTRA_NOTIFICATION_URL = "EXTRA_NOTIFICATION_URL"
        var EXTRA_NOTIFICATION_MSG_TYPE = "EXTRA_NOTIFICATION_MSG_TYPE"
        var EXTRA_NOTIFICATION_LIKE = "EXTRA_NOTIFICATION_LIKE"
        var EXTRA_NOTIFICATION_SENDER_ID = "EXTRA_NOTIFICATION_SENDER_ID"
        var EXTRA_NOTIFICATION_USERINFO = "EXTRA_NOTIFICATION_USERINFO"

        val NOTIFICATION_TYPE_LIKE = "Emia.Post.like"

        var NOTIFICATION_LIKE_BODY = "Likes your post"
        var NOTIFICATION_LOCAL_BROADCAST = "NOTIFICATION_LOCAL_BROADCAST"
        var NOTIFICATION_HIDE_TIME = 6000  // 6 seconds

        var EXTRA_TOKEN = "EXTRA_TOKEN"
        var PEOPLE_ID = "PEOPLE_ID"

        // -

        var FILTER_MIN_AGE = 17
        var FILTER_MAX_AGE = 40
        var FILTER_MIN_AGE_LIMIT = 0
        var FILTER_MAX_AGE_LIMIT = 100

        // Gender (User property for filter)
        val GENDER_BOTH = 0
        val GENDER_GUY = 1
        val GENDER_GIRL = 2

        // State of my favorite
        val STATUS_ALL = 0
        val STATUS_MY_FAVORITE = 1

        var SUCCESS = 0
        var FAIL = 1
        var CANCEL = 2
    }

}
