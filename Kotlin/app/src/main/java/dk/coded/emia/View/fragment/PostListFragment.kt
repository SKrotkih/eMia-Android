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
import android.widget.ListView

import java.util.ArrayList

import dk.coded.emia.AsymmetricGridViewAdapter
import dk.coded.emia.AsymmetricGridView
import dk.coded.emia.Utils

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
    private var mListView: ListView? = null
    private var mAdapter: PostsListAdapter? = null
    private var mRouter: MainScreenRouter? = null

    private var mActivity: Activity? = null
    protected var mValidator: PostListValidator

    abstract fun needShow(post: Post, user: User, callback: BasicCallBack)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        mActivity = activity

        mValidator = PostListValidator(mActivity!!)

        val rootView = inflater!!.inflate(R.layout.fragment_all_posts, container, false)

        mLayoutManager = LinearLayoutManager(mActivity)

        mRouter = MainScreenRouter(mActivity, mActivity, mActivity)

        mListView = createListView(rootView)

        mAdapter = configureListViewAdapter(mListView!!)

        return rootView
    }

    // https://github.com/felipecsl/AsymmetricGridView
    private fun createListView(rootView: View): ListView {
        val gridView = rootView.findViewById<View>(R.id.messages_list) as AsymmetricGridView
        val activity = activity
        gridView.setRequestedColumnCount(2)
        gridView.setBackgroundColor(Color.parseColor("#FFFFFF"))
        gridView.requestedHorizontalSpacing = Utils.dpToPx(activity, 3f)
        gridView.isDebugging = true
        gridView.onItemClickListener = this
        return gridView
    }

    private fun configureListViewAdapter(listView: ListView): PostsListAdapter {
        val activity = activity
        val asymmetricGridView = listView as AsymmetricGridView
        val adapter = PostsListAdapter(activity)
        // We use just needShow method. TODO: make interface with needShow method
        adapter.showPostsStrategy = this
        val listViewAdapter = AsymmetricGridViewAdapter(activity, asymmetricGridView, adapter)
        listView.setAdapter(listViewAdapter)
        return adapter
    }

    override fun onItemClick(parent: AdapterView<*>, view: View,
                             position: Int, id: Long) {
        val post = mAdapter!!.getItem(position)!!.post
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

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putInt("itemCount", mAdapter!!.count)
        for (i in 0 until mAdapter!!.count) {
            outState.putParcelable("item_$i", mAdapter!!.getItem(i) as Parcelable)
        }
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val count = savedInstanceState.getInt("itemCount")
        val items = ArrayList<PostsCollectionViewItem>(count)
        for (i in 0 until count) {
            items.add(savedInstanceState.getParcelable<Parcelable>("item_$i") as PostsCollectionViewItem)
        }
        mAdapter!!.setItems(items)
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
        if (mAdapter != null) {
            mAdapter!!.startListening()
        }
    }

    private fun stopDataListening() {
        if (mAdapter != null) {
            mAdapter!!.stopListening()
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
