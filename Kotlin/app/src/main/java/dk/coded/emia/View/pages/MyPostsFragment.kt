package dk.coded.emia.View.pages

import android.app.Activity

import dk.coded.emia.View.fragment.PostListFragment
import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.Data.User
import dk.coded.emia.model.adapter.PostListValidator
import dk.coded.emia.utils.BasicCallBack
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.utils.Constants

class MyPostsFragment : PostListFragment() {

    private val mInteractor: DatabaseInteractor

    init {
        mInteractor = DatabaseFactory.databaseInteractor
    }

    override fun needShow(post: Post, user: User, callback: BasicCallBack) {
        if ((mValidator!!.isValid(post, user))!! == false) {
            callback(Constants.SUCCESS, false)
        } else {
            val currentUserId = mInteractor.currentUserId
            val isItMyPost = post.uid == currentUserId
            callback(Constants.SUCCESS, isItMyPost)
        }
    }
}
