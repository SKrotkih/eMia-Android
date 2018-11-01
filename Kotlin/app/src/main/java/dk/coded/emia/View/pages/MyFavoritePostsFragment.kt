package dk.coded.emia.View.pages

import android.app.Activity
import android.util.Log
import dk.coded.emia.View.fragment.PostListFragment
import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.Data.User
import dk.coded.emia.model.adapter.PostListValidator
import dk.coded.emia.utils.BasicCallBack
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.utils.Constants

class MyFavoritePostsFragment : PostListFragment() {

    private val mInteractor: DatabaseInteractor

    init {
        mInteractor = DatabaseFactory.databaseInteractor
    }

    override fun needShow(post: Post, user: User, callback: BasicCallBack) {

        Log.d(javaClass.name, "handle: " + post.id!!)

        if ((mValidator!!.isValid(post, user))!! == false) {
            callback(Constants.SUCCESS, false)
        } else {
            mInteractor.isItMyFavoritePost(post) { status: Int, data: Any? ->
                if (status == Constants.SUCCESS) {
                    val isFavorite = data as Boolean
                    Log.d(javaClass.name, "Post " + post.id + " is my favorite - " + isFavorite.toString())
                    callback(Constants.SUCCESS, isFavorite)
                } else if (status == Constants.FAIL) {
                    callback(Constants.SUCCESS, false)
                }
            }
        }
    }
}
