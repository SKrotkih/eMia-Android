package dk.coded.emia.model.observer

import dk.coded.emia.model.Data.User

/**
 * Created by oldman on 12/10/17.
 */

interface UserObserverProtocol {

    fun updateUsers(users: List<User>)
    fun newUser(user: User)

}
