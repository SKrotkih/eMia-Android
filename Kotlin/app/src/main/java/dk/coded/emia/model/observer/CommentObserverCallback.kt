package dk.coded.emia.model.observer

import com.google.firebase.database.DatabaseError

import dk.coded.emia.model.Data.Comment

/**
 * Created by oldman on 11/30/17.
 */

interface CommentObserverCallback {

    fun addComment(comment: Comment, id: String)
    fun updateComment(comment: Comment, id: String)
    fun deleteComment(id: String)
    fun moveComment(comment: Comment, id: String)
    fun cancelled(databaseError: DatabaseError)

}
