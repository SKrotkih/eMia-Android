package dk.coded.emia.View.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListAdapter
import android.widget.ListView

import java.util.ArrayList

import dk.coded.library.AsymmetricGridViewAdapter
import dk.coded.library.AsymmetricGridView
import dk.coded.library.Utils

import dk.coded.emia.R
import dk.coded.emia.View.activity.MainActivity
import dk.coded.emia.View.activity.PostDetailActivity
import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.Data.User
import dk.coded.emia.model.adapter.FilterStorage
import dk.coded.emia.model.adapter.PostListValidator
import dk.coded.emia.model.adapter.PostsCollectionViewItem
import dk.coded.emia.model.adapter.PostsListAdapter
import dk.coded.emia.utils.BasicCallBack

abstract class PostListFragment : Fragment(), AdapterView.OnItemClickListener {

    private var mLayoutManager: LinearLayoutManager? = null
    private var mPostListsAdapter: PostsListAdapter? = null
    private var mRouter: MainScreenRouter? = null

    private var mActivity: Activity? = null
    protected var mValidator: PostListValidator? = null

    abstract fun needShow(post: Post, user: User, callback: BasicCallBack)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                          savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_all_posts, container, false)

        mActivity = activity
        configure(activity = mActivity!!, view = rootView)

        return rootView
    }

    private fun configure(activity: Activity, view: View) {
        mValidator = PostListValidator(activity)
        mLayoutManager = LinearLayoutManager(activity)
        mRouter = MainScreenRouter(activity, activity, activity)
        mPostListsAdapter = configureGridView(view, activity)
    }

    // https://github.com/felipecsl/AsymmetricGridView
    private fun configureGridView(rootView: View, context: Context): PostsListAdapter {
        val postsListAdapter = PostsListAdapter(context)
        postsListAdapter.showPostsStrategy = this

        val gridView = rootView.findViewById<View>(R.id.messages_list) as AsymmetricGridView
        // Wrapper for the postsListAdapter
        val gridAdapter = AsymmetricGridViewAdapter(context, gridView, postsListAdapter)
        gridView.setAdapter(gridAdapter)

        gridView.setRequestedColumnCount(2)
        gridView.setBackgroundColor(Color.parseColor("#FFFFFF"))
        gridView.requestedHorizontalSpacing = Utils.dpToPx(context, 3f)
        gridView.isDebugging = true
        gridView.onItemClickListener = this

        return postsListAdapter
    }

    override fun onItemClick(parent: AdapterView<*>, view: View,
                             position: Int, id: Long) {
        val post = mPostListsAdapter!!.getItem(position)!!.post
        mRouter!!.browsePost(post)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Set up Layout Manager, reverse layout
        mLayoutManager!!.reverseLayout = true
        mLayoutManager!!.stackFromEnd = true
    }

    override fun onStart() {
        super.onStart()

        startSearchListening()
        startDataListening()
    }

    override fun onStop() {
        super.onStop()

        stopSearchListening()
        stopDataListening()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("itemCount", mPostListsAdapter!!.count)
        for (i in 0 until mPostListsAdapter!!.count) {
            outState.putParcelable("item_$i", mPostListsAdapter!!.getItem(i) as Parcelable)
        }
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val count = savedInstanceState.getInt("itemCount")
        val items = ArrayList<PostsCollectionViewItem>(count)
        for (i in 0 until count) {
            items.add(savedInstanceState.getParcelable<Parcelable>("item_$i") as PostsCollectionViewItem)
        }
        mPostListsAdapter!!.setItems(items)
    }

    private fun startSearchListening() {
        FilterStorage.instance!!.setListener(object : FilterStorage.ValueFilterListener {
            override fun onSearchTextChange(newSearchTemplate: String) {
                Log.d(TAG, "Search data with template $newSearchTemplate")
                startDataListening()
            }
        })
    }

    private fun stopSearchListening() {
        FilterStorage.instance!!.setListener(null!!)
    }

    private fun startDataListening() {
        if (mPostListsAdapter != null) {
            mPostListsAdapter!!.startListening()
        }
    }

    private fun stopDataListening() {
        if (mPostListsAdapter != null) {
            mPostListsAdapter!!.stopListening()
        }
    }

    /**
     * Created by oldman on 11/26/17.
     */

    class MainScreenRouter internal constructor(private val _activity: Activity, private val _context: Context, private val _packageContext: Context) {

        fun startMainScreen() {
            _activity.startActivity(Intent(_packageContext, MainActivity::class.java))
            _activity.finish()
        }

        fun browsePost(post: Post?) {
            val intent = Intent(_packageContext, PostDetailActivity::class.java)
            intent.putExtra("post", post)
            //        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, post.id);
            _activity.startActivity(intent)
        }

    }

    companion object {

        private val TAG = "PostListFragment"
    }
}
