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
import dk.coded.emia.utils.Constants.Companion.FAIL
import dk.coded.emia.utils.Constants.Companion.SUCCESS

class MyFavoritePostsFragment : PostListFragment() {

    private val mInteractor: DatabaseInteractor

    init {
        mInteractor = DatabaseFactory.databaseInteractor
    }

    override fun needShow(post: Post, user: User, callback: BasicCallBack) {

        Log.d(javaClass.getName(), "handle: " + post.id!!)

        if ((!mValidator.isValid(post, user))!!) {
            callback.callBack(PostListFragment.Companion.getSUCCESS(), false)
        } else {
            mInteractor.isItMyFavoritePost(post, { status: Int, data: Any ->
                if (status == PostListFragment.Companion.getSUCCESS()) {
                    val isFavorite = data as Boolean
                    Log.d(javaClass.getName(), "Post " + post.id + " is my favorite - " + isFavorite.toString())
                    callback.callBack(PostListFragment.Companion.getSUCCESS(), isFavorite)
                } else if (status == PostListFragment.Companion.getFAIL()) {
                    callback.callBack(PostListFragment.Companion.getSUCCESS(), false)
                }
            })
        }
    }
}
