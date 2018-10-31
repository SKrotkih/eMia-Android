package dk.coded.emia.View.pages

import android.app.Activity

import dk.coded.emia.View.fragment.PostListFragment
import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.Data.User
import dk.coded.emia.model.adapter.PostListValidator
import dk.coded.emia.utils.BasicCallBack
import dk.coded.emia.utils.Constants.Companion.SUCCESS

class RecentPostsFragment : PostListFragment() {

    override fun needShow(post: Post, user: User, callback: BasicCallBack) {
        val isThisPostOk = mValidator.isValid(post, user)
        callback.callBack(PostListFragment.Companion.getSUCCESS(), isThisPostOk!!)
    }
}
