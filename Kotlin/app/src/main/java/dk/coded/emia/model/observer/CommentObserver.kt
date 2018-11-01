package dk.coded.emia.model.observer

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference

import dk.coded.emia.model.Data.Comment
import dk.coded.emia.model.interactor.DatabaseFactory

/**
 * Created by oldman on 11/30/17.
 */

class CommentObserver {

    private var mChildEventListener: ChildEventListener? = null
    private var mDatabaseReference: DatabaseReference? = null

    fun addObserver(mPostKey: String, callback: CommentObserverCallback) {

        val dataBaseRef = DatabaseFactory.databaseInteractor.dataBaseRef()
        mDatabaseReference = dataBaseRef.child("comments").child(mPostKey)

        // Create child event listener
        // [START child_event_listener_recycler]
        val childEventListener = object : ChildEventListener {

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // A new comment has been added
                val comment = dataSnapshot.getValue<Comment>(Comment::class.java)
                callback.addComment(comment!!, dataSnapshot.key!!)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // A comment has changed
                val newComment = dataSnapshot.getValue<Comment>(Comment::class.java)
                val commentKey = dataSnapshot.key
                callback.updateComment(newComment!!, commentKey!!)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // A comment has changed
                val commentKey = dataSnapshot.key
                callback.deleteComment(commentKey!!)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // A comment has changed position
                val movedComment = dataSnapshot.getValue<Comment>(Comment::class.java)
                val commentKey = dataSnapshot.key
                callback.moveComment(movedComment!!, commentKey!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.cancelled(databaseError)
            }
        }
        mDatabaseReference!!.addChildEventListener(childEventListener)
        // [END child_event_listener_recycler]

        // Store reference to listener so it can be removed on app stop
        mChildEventListener = childEventListener
    }

    fun unregisterServerObserver() {
        if (mChildEventListener != null) {
            mDatabaseReference!!.removeEventListener(mChildEventListener!!)
        }
    }
}
