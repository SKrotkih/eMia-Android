package dk.coded.emia.model.observer

import android.content.Context

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference

import java.util.ArrayList

import dk.coded.emia.model.Data.User
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.utils.ProgressBarHandler

/**
 * Created by oldman on 11/30/17.
 */

class UserObserver private constructor() {

    private var mChildEventListener: ChildEventListener? = null
    private var mDatabaseReference: DatabaseReference? = null

    private var mIsFetchingData: Boolean? = false
    private val mInteractor: DatabaseInteractor
    private val mUserItems: MutableList<User>
    private val mObservers: MutableList<UserObserverProtocol>

    private val MUTEX = Any()

    init {
        mUserItems = ArrayList()
        mObservers = ArrayList()
        mInteractor = DatabaseFactory.databaseInteractor
    }

    fun register(observer: UserObserverProtocol?, context: Context) {
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

    fun unregister(observer: UserObserverProtocol) {
        synchronized(MUTEX) {
            mObservers.remove(observer)
        }
    }

    private fun startListening(observer: UserObserverProtocol, context: Context) {
        if (mUserItems.size == 0) {
            if (mIsFetchingData!!) {
                return
            }
            mIsFetchingData = true
            val progress = ProgressBarHandler(context)
            progress.show()
            val r = {
                mInteractor.getAllUsers({ status: Int, data: Any? ->
                    progress.hide()
                    val users = data as ArrayList<User>
                    for (item in users) {
                        addToUserItems(item)
                    }
                    startListening()
                    for (observerItem in mObservers) {
                        observerItem.updateUsers(mUserItems)
                    }
                    mIsFetchingData = false
                })
            }
            Thread(r).start()
        } else {
            observer.updateUsers(mUserItems)
        }
    }

    private fun notifyObservers() {
        var observersLocal: List<UserObserverProtocol>? = null
        synchronized(MUTEX) {
            observersLocal = ArrayList(mObservers)
        }
        for (observer in observersLocal!!) {
            observer.updateUsers(mUserItems)
        }
    }

    private fun addToUserItems(user: User): User? {
        val id = user.id
        var isPresented: Boolean? = false
        for (item in mUserItems) {
            if (item.id == id) {
                isPresented = true
                break
            }
        }
        if (isPresented == false) {
            mUserItems.add(user)
            return user
        } else {
            return null
        }
    }

    private fun startListening() {
        registerServerObserver(object : UserObserverCallback {

            override fun addUser(post: User) {
                val item = addToUserItems(post) ?: return
                var observersLocal: List<UserObserverProtocol>? = null
                synchronized(MUTEX) {
                    observersLocal = ArrayList(mObservers)
                }
                for (observer in observersLocal!!) {
                    observer.newUser(item)
                }
            }

            override fun updateUser(user: User) {

            }

            override fun deleteUser(id: String) {

            }

            override fun moveUser(user: User) {

            }

            override fun cancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun registerServerObserver(callback: UserObserverCallback) {
        mDatabaseReference = mInteractor.dataBaseRef().child("users")

        // Create child event listener
        // [START child_event_listener_recycler]
        val childEventListener = object : ChildEventListener {

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // A new user has been added
                val user = dataSnapshot.getValue<User>(User::class.java)
                callback.addUser(user!!)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // A comment has changed position
                val movedUser = dataSnapshot.getValue<User>(User::class.java)
                callback.moveUser(movedUser!!)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // A user has changed
                val newUser = dataSnapshot.getValue<User>(User::class.java)
                callback.updateUser(newUser!!)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // A comment has changed
                val userKey = dataSnapshot.key
                callback.deleteUser(userKey!!)
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
        private var sDefaltInstance: UserObserver? = null

        // One Syngleton instance
        //if there is no instance available... create new one
        val instance: UserObserver?
            get() {
                if (sDefaltInstance == null) {
                    synchronized(UserObserver::class.java) {
                        if (sDefaltInstance == null) {
                            sDefaltInstance = UserObserver()
                        }
                    }
                }

                return sDefaltInstance
            }
    }
}
