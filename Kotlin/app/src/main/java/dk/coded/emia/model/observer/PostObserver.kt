package dk.coded.emia.model.observer

import android.content.Context

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference

import java.util.ArrayList

import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.utils.ProgressBarHandler

/**
 * Created by oldman on 11/30/17.
 */

class PostObserver private constructor() {

    private var mChildEventListener: ChildEventListener? = null
    private var mDatabaseReference: DatabaseReference? = null

    private var mIsFetchingData: Boolean? = false
    private val mInteractor: DatabaseInteractor
    private val mPostItems: MutableList<Post>
    private val mObservers: MutableList<PostObserverProtocol>

    private val MUTEX = Any()

    init {
        mPostItems = ArrayList()
        mObservers = ArrayList()
        mInteractor = DatabaseFactory.databaseInteractor
    }

    fun register(observer: PostObserverProtocol?, context: Context) {
        if (observer == null) {
            throw NullPointerException("Null Observer")
        }

        synchronized(MUTEX) {
            if (!mObservers.contains(observer)) {
                mObservers.add(observer)
            }
            startListening(observer, context)
        }
    }

    fun unregister(observer: PostObserverProtocol) {
        synchronized(MUTEX) {
            mObservers.remove(observer)
        }
    }

    private fun startListening(observer: PostObserverProtocol, context: Context) {
        if (mPostItems.size == 0) {
            if (mIsFetchingData!!) {
                return
            }
            mIsFetchingData = true
            val progress = ProgressBarHandler(context)
            progress.show()
            val r = {
                mInteractor.getAllPosts({ status: Int, data: Any? ->
                    progress.hide()
                    val posts = data as ArrayList<Post>
                    for (item in posts) {
                        addToPostItems(item)
                    }
                    startListening()
                    for (observerItem in mObservers) {
                        observerItem.update(mPostItems)
                    }
                    mIsFetchingData = false
                })
            }
            Thread(r).start()
        } else {
            observer.update(mPostItems)
        }
    }

    private fun notifyObservers() {
        var observersLocal: List<PostObserverProtocol>? = null
        synchronized(MUTEX) {
            observersLocal = ArrayList(mObservers)
        }
        for (observer in observersLocal!!) {
            observer.update(mPostItems)
        }
    }

    private fun addToPostItems(post: Post): Post? {
        val id = post.id
        var isPresented: Boolean? = false
        for (item in mPostItems) {
            if (item.id == id) {
                isPresented = true
                break
            }
        }
        if (isPresented == false) {
            mPostItems.add(post)
            return post
        } else {
            return null
        }
    }

    private fun startListening() {
        registerServerObserver(object : PostObserverCallback {

            override fun addPost(post: Post) {
                val item = addToPostItems(post) ?: return
                var observersLocal: List<PostObserverProtocol>? = null
                synchronized(MUTEX) {
                    observersLocal = ArrayList(mObservers)
                }
                for (observer in observersLocal!!) {
                    observer.newPost(item)
                }
            }

            override fun updatePost(post: Post) {

            }

            override fun deletePost(id: String) {

            }

            override fun movePost(post: Post) {

            }

            override fun cancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun registerServerObserver(callback: PostObserverCallback) {
        mDatabaseReference = mInteractor.dataBaseRef().child("posts")

        // Create child event listener
        // [START child_event_listener_recycler]
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // A new post has been added
                val post = dataSnapshot.getValue<Post>(Post::class.java)
                callback.addPost(post!!)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // A post has changed
                val newPost = dataSnapshot.getValue<Post>(Post::class.java)
                callback.updatePost(newPost!!)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // A post has changed
                val postKey = dataSnapshot.key
                callback.deletePost(postKey!!)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // A post has changed position
                val movedPost = dataSnapshot.getValue<Post>(Post::class.java)
                callback.movePost(movedPost!!)
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

    companion object {

        @Volatile
        private var sDefaltInstance: PostObserver? = null

        // One Syngleton instance
        //if there is no instance available... create new one
        val instance: PostObserver?
            get() {
                if (sDefaltInstance == null) {
                    synchronized(PostObserver::class.java) {
                        if (sDefaltInstance == null) {
                            sDefaltInstance = PostObserver()
                        }
                    }
                }

                return sDefaltInstance
            }
    }
}
