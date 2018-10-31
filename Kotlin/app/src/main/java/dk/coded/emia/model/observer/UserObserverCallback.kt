package dk.coded.emia.model.observer

import com.google.firebase.database.DatabaseError

import dk.coded.emia.model.Data.User

/**
 * Created by oldman on 11/30/17.
 */

interface UserObserverCallback {

    fun addUser(user: User)
    fun updateUser(user: User)
    fun deleteUser(id: String)
    fun moveUser(user: User)
    fun cancelled(databaseError: DatabaseError)

}
