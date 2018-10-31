package dk.coded.emia.model.interactor

/**
 * Created by oldman on 11/30/17.
 */


object DatabaseFactory {

    // TODO: Select Rules need
    val databaseInteractor: DatabaseInteractor
        get() = FirebaseInteractor.instance

}
