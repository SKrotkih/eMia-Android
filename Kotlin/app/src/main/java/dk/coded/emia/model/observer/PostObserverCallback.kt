package dk.coded.emia.model.observer

import com.google.firebase.database.DatabaseError

import dk.coded.emia.model.Data.Post

/**
 * Created by oldman on 11/30/17.
 */

interface PostObserverCallback {

    fun addPost(post: Post)
    fun updatePost(post: Post)
    fun deletePost(id: String)
    fun movePost(post: Post)
    fun cancelled(databaseError: DatabaseError)

}
