package dk.coded.emia.model.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import dk.coded.emia.R
import dk.coded.emia.View.GlideApp
import dk.coded.emia.View.fragment.PostListFragment
import dk.coded.emia.model.Data.User
import dk.coded.emia.model.comparator.PostComparator
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.observer.PostObserver
import dk.coded.emia.model.observer.PostObserverProtocol
import dk.coded.emia.model.observer.UserObserver
import dk.coded.emia.model.observer.UserObserverProtocol
import dk.coded.emia.utils.PositionedCropTransformation

import java.util.ArrayList
import java.util.Collections
import java.util.stream.Collectors

import com.bumptech.glide.request.RequestOptions.bitmapTransform
import dk.coded.emia.utils.Constants.FAIL
import dk.coded.emia.utils.Constants.SUCCESS

class PostsListAdapter(private val mContext: Context) : ArrayAdapter<PostsCollectionViewItem>(mContext, 0), PostsListAdapterProtocol, PostObserverProtocol, UserObserverProtocol {
    private val mInteractor: DatabaseInteractor
    private var mDecCounter: Int = 0
    var showPostsStrategy: PostListFragment? = null

    private val mUsers: MutableList<User>

    init {
        mUsers = ArrayList()
        mInteractor = DatabaseFactory.databaseInteractor
    }

    override fun startListening() {
        UserObserver.instance.register(this, mContext)
    }

    // UserObserverProtocol
    override fun updateUsers(users: List<User>) {
        mUsers.clear()
        mUsers.addAll(users)
        PostObserver.instance.register(this, mContext)
    }

    override fun newUser(user: User) {
        mUsers.add(user)
    }

    // PostObserverProtocol
    override fun update(posts: List<Post>) {
        presentFilteredData(posts)
    }

    override fun newPost(post: Post) {
        val user = getUser(post)
        showPostsStrategy!!.needShow(post, user) { status: Int, data: Any ->
            if (status == SUCCESS) {
                val success = data as Boolean
                if (success) {
                    val colSpan = 1
                    // TODO: something like this. But it does not work!
                    // int rowSpan = post.photoHeight() > post.photoWidth() ? 2 : 1;
                    val rowSpan = if (post.photoHeight() > post.photoWidth()) 1 else 1
                    val coolectionItem = PostsCollectionViewItem(colSpan, rowSpan, 0)
                    coolectionItem.post = post
                    val items = ArrayList<PostsCollectionViewItem>()
                    items.add(coolectionItem)
                    insert(coolectionItem, 0)
                }
            }
        }
    }

    private fun presentFilteredData(posts: List<Post>) {
        if (posts.size == 0) {
            return
        }
        val filteredItems = ArrayList<PostsCollectionViewItem>()
        val decCounter = posts.size
        mDecCounter = posts.size
        for (item in posts) {
            val user = getUser(item)
            showPostsStrategy!!.needShow(item, user) { status: Int, data: Any ->
                val success = data as Boolean
                if (status == SUCCESS) {
                    mDecCounter--
                    if (success) {
                        val colSpan = 1
                        // TODO: something like this. But it does not work!
                        // int rowSpan = item.photoHeight() > item.photoWidth() ? 2 : 1;
                        val rowSpan = if (item.photoHeight() > item.photoWidth()) 1 else 1
                        val coolectionItem = PostsCollectionViewItem(colSpan, rowSpan, 0)
                        coolectionItem.post = item
                        filteredItems.add(coolectionItem)
                    }
                    if (mDecCounter == 0) {
                        Collections.sort(filteredItems, PostComparator())
                        setItems(filteredItems)
                    }
                }
            }
        }
    }

    private fun getUser(post: Post): User? {
        var author: User? = null
        for (user in mUsers) {
            if (user.id == post.uid) {
                author = user
                break
            }
        }
        return author
    }

    private fun showData(view: View, post: Post) {
        val titleTextView = view.findViewById<View>(R.id.titleTextView) as TextView
        val descriptionTextView = view.findViewById<View>(R.id.descriptionTextView) as TextView
        val photoImageView = view.findViewById<View>(R.id.photoImageView) as ImageView
        val starButton = view.findViewById<View>(R.id.star_button) as ImageButton

        mInteractor.downloadPhoto(mContext, post.id) { status: Int, data: Any ->
            if (status == SUCCESS) {
                val uri = data as Uri
                GlideApp.with(mContext.applicationContext)
                        .load(uri.toString())
                        .apply(bitmapTransform(PositionedCropTransformation(1f, 0f)))
                        .into(photoImageView)
            }
        }

        titleTextView.text = post.title
        descriptionTextView.text = post.body
        starButton.visibility = View.GONE

        mInteractor.isItMyFavoritePost(post) { status: Int, data: Any ->
            if (status == SUCCESS) {
                val isFavorite = data as Boolean
                if (isFavorite) {
                    starButton.visibility = View.VISIBLE
                }
            } else if (status == FAIL) {

            }
        }
    }

    override fun stopListening() {
        PostObserver.instance.unregister(this)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        return bindData(item!!.post, convertView, parent, item.rowSpan)
    }

    private fun bindData(post: Post?, convertView: View?, parent: ViewGroup, rowSpan: Int): View {
        val view: View
        val layoutInflater = LayoutInflater.from(mContext)
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.horizontal_message_cell, parent, false)
        } else {
            view = convertView
        }
        showData(view, post!!)
        return view
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun appendItems(newItems: List<PostsCollectionViewItem>) {
        addAll(newItems)
        notifyDataSetChanged()
    }

    override fun setItems(moreItems: List<PostsCollectionViewItem>) {
        clear()
        appendItems(moreItems)
    }
}
