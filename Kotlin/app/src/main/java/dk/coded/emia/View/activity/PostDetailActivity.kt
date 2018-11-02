package dk.coded.emia.View.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.database.DatabaseError

import dk.coded.emia.View.GlideApp
import dk.coded.emia.model.Data.User
import dk.coded.emia.notifications.RemoteNotifications
import dk.coded.emia.utils.Constants
import dk.coded.emia.utils.PositionedCropTransformation
import dk.coded.emia.model.observer.CommentObserver
import dk.coded.emia.model.observer.CommentObserverCallback
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.utils.BasicCallBack
import dk.coded.emia.model.Data.Comment
import dk.coded.emia.model.Data.Post
import dk.coded.emia.R

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import dk.coded.emia.utils.Utils

import com.bumptech.glide.request.RequestOptions.bitmapTransform

import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.post_detail_nav_header.*
import kotlinx.android.synthetic.main.notification.*
import kotlinx.android.synthetic.main.include_post_author.*
import kotlinx.android.synthetic.main.include_post_text.*

class PostDetailActivity : BaseActivity() {
    private var mAdapter: CommentAdapter? = null
    internal var activity: Activity = this@PostDetailActivity

    private val mAuthorView: TextView
        get() = post_author
    private val mBodyView: TextView
        get() = post_body
    private val mPhotoImageView: PhotoView
        get() = photoImageView
    private val mCommentField: EditText
        get() = field_comment_text
    private val mCommentButton: Button
        get() = post_comment_button
    private val mCommentsRecycler: RecyclerView
        get() = recycler_comments
    private val mBackButton: ImageButton
        get() = back_button
    private val mStarButton: ImageButton
        get() = star_button
    private val mPhotoAvatarImageView: ImageView
        get() = post_author_photo
    private val mSendEmailButton: Button
        get() = sendEmailButton
    private val mCreatedTextView: TextView
        get() = post_date_tv
    private val mTitleTextView: TextView
        get() = nav_bar_title_tv
    private val rlNotification: RelativeLayout
        get() = layout_Notification

    private var mPost: Post? = null
    private var mContext: Context? = null

    private var databaseInteractor: DatabaseInteractor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        val intent = intent
        mPost = intent.getSerializableExtra("post") as Post

        databaseInteractor = DatabaseFactory.databaseInteractor
        mCommentsRecycler.layoutManager = LinearLayoutManager(this)

        setUpButtonListeners()

        //prepareNotificationsListener(activity);
        rlNotification.visibility = View.GONE
    }

    private fun setUpButtonListeners() {
        mCommentButton.setOnClickListener { view -> sendComment() }
        mBackButton.setOnClickListener { view -> finish() }
        mStarButton.setOnClickListener { view -> onStarButtonPressed() }
        mPhotoImageView.setOnClickListener { view -> photoPreview() }
        mSendEmailButton.setOnClickListener { view -> sendEmail() }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    public override fun onStart() {
        super.onStart()

        if (mPost == null) {
            return
        }

        showData(mPost!!)
    }

    override fun onStop() {
        super.onStop()

        // Remove post value event listener
        databaseInteractor!!.removePostObserver()

        // Clean up comments listener
        mAdapter!!.cleanupListener()
    }

    private fun showData(post: Post) {
        mTitleTextView.text = post.title
        mBodyView.text = post.body

        setUpStar()

        val created = post.created * 1000
        val relativeTime = DateUtils.getRelativeTimeSpanString(created).toString()
        mCreatedTextView.setText(String.format("Published %s", relativeTime))

        databaseInteractor!!.getUser(post.uid!!, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val user = data as User
                mAuthorView.text = user.username
            }
        })

        mContext = this

        databaseInteractor!!.downloadPhoto(this, post.id!!, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val uri = data as Uri
                GlideApp.with(mContext!!.applicationContext)
                        .load(uri.toString())
                        .apply(bitmapTransform(PositionedCropTransformation(1f, 0f)))
                        .into(mPhotoImageView)
            }
        })

        databaseInteractor!!.downloadPhoto(this, post.uid!!, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val uri = data as Uri
                GlideApp.with(mContext!!.applicationContext)
                        .load(uri.toString())
                        .apply(bitmapTransform(PositionedCropTransformation(1f, 0f)))
                        .into(mPhotoAvatarImageView)
            }
        })

        // Listen for comments
        mAdapter = CommentAdapter(this, post.id!!, { status: Int, data: Any? ->
            // Auto Scroll to a new comment
            mCommentsRecycler.smoothScrollToPosition(0)

        })
        mCommentsRecycler.adapter = mAdapter
    }

    private fun setUpStar() {
        isItMyFavoritePost(mPost, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val isFavorite = data as Boolean
                var resId = R.drawable.ic_star_border_black_24dp
                if (isFavorite) {
                    resId = R.drawable.ic_star_black_24dp
                }
                mStarButton.setImageResource(resId)
                mStarButton.visibility = View.VISIBLE
            } else if (status == Constants.FAIL) {
                mStarButton.visibility = View.GONE
            }
        })
    }

    // Press Like button handler
    private fun onStarButtonPressed() {
        setUpPostAsMyFavorite({ status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                if (data as Boolean) {
                    sendLikeTypePushNotification()
                }
                setUpStar()
            } else if (status == Constants.FAIL) {

            }
        })
    }

    private fun setUpPostAsMyFavorite(callback: BasicCallBack) {
        isItMyFavoritePost(mPost, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val isFavorite = data as Boolean
                databaseInteractor!!.setUpMyFavoritePost(mPost!!, !isFavorite)
                callback(Constants.SUCCESS, !isFavorite)
            } else if (status == Constants.FAIL) {
                callback(Constants.FAIL, null!!)
            }
        })
    }

    private fun isItMyFavoritePost(post: Post?, callback: BasicCallBack) {
        databaseInteractor!!.isItMyFavoritePost(post!!, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val isFavorite = data as Boolean
                callback(Constants.SUCCESS, isFavorite)
            } else if (status == Constants.FAIL) {
                callback(Constants.FAIL, null!!)
            }
        })
    }

    private fun sendLikeTypePushNotification() {
        databaseInteractor!!.getUser(mPost!!.uid!!, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val recipientUser = data as User
                databaseInteractor!!.currentUser({ status2: Int, data2: Any? ->
                    if (status2 == Constants.SUCCESS) {
                        val senderUser = data2 as User
                        RemoteNotifications.sendNotification(recipientUser, senderUser, Constants.NOTIFICATION_TYPE_LIKE, mPost!!.title)
                    }
                })
            }
        })
    }

    // Show the post photo
    private fun photoPreview() {
        val intent = Intent(this@PostDetailActivity, PhotoBrowserActivity::class.java)
        intent.putExtra("post", mPost)
        this.startActivity(intent)
    }

    private fun sendComment() {
        if (mPost == null) {
            return
        }

        val commentText = mCommentField.text.toString()

        if (commentText.isEmpty()) {
            return
        }

        databaseInteractor!!.addCommentToPost(mPost!!, commentText, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                // Clear the field
                mCommentField.setText(null)
                Utils.hideSoftKeyboard(this)
            } else if (status == Constants.FAIL) {
                Toast.makeText(this, "Send message is failed", Toast.LENGTH_SHORT).show()
            } else if (status == Constants.CANCEL) {
                Toast.makeText(this, "Send message is cancelled", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var authorPhoto: ImageView
        var authorView: TextView
        var bodyView: TextView

        init {

            authorPhoto = itemView.findViewById(R.id.comment_photo)
            authorView = itemView.findViewById(R.id.comment_author)
            bodyView = itemView.findViewById(R.id.comment_body)
        }
    }

    private class CommentAdapter(private val mContext: Context, postId: String, collback: BasicCallBack) : RecyclerView.Adapter<CommentViewHolder>() {
        private val mObserver: CommentObserver

        private val mCommentIds = ArrayList<String>()
        private val mComments = ArrayList<Comment>()

        init {
            mObserver = CommentObserver()
            mObserver.addObserver(postId, object : CommentObserverCallback {
                override fun addComment(comment: Comment, id: String) {
                    Log.d(TAG, "onChildAdded:$id")

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mCommentIds.add(id)
                    mComments.add(comment)

                    Collections.sort(mComments) { c1, c2 -> if (c1.created < c2.created) 1 else -1 }

                    collback(0, null!!)

                    notifyItemInserted(0)
                    // [END_EXCLUDE]
                }

                override fun updateComment(comment: Comment, id: String) {
                    Log.d(TAG, "onChildChanged:$id")

                    // [START_EXCLUDE]
                    val commentIndex = mCommentIds.indexOf(id)
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments[commentIndex] = comment

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex)
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:$id")
                    }
                    // [END_EXCLUDE]
                }

                override fun deleteComment(id: String) {
                    Log.d(TAG, "onChildRemoved:$id")

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.

                    // [START_EXCLUDE]
                    val commentIndex = mCommentIds.indexOf(id)
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.removeAt(commentIndex)
                        mComments.removeAt(commentIndex)

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex)
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:$id")
                    }
                    // [END_EXCLUDE]
                }

                override fun moveComment(comment: Comment, id: String) {
                    Log.d(TAG, "onChildMoved:$id")

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    val movedComment = comment
                    val commentKey = id

                    // ...

                }

                override fun cancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException())
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show()
                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            val inflater = LayoutInflater.from(mContext)
            val view = inflater.inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(view)
        }

        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            val comment = mComments[position]
            holder.authorView.text = comment.author
            holder.bodyView.text = comment.text

            val databaseInteractor = DatabaseFactory.databaseInteractor
            databaseInteractor.downloadPhoto(null!!, comment.uid!!, { status: Int, data: Any? ->
                if (status == Constants.SUCCESS) {
                    val uri = data as Uri
                    GlideApp.with(mContext.applicationContext)
                            .load(uri.toString())
                            .apply(bitmapTransform(PositionedCropTransformation(1f, 0f)))
                            .into(holder.authorPhoto)
                }
            })

        }

        override fun getItemCount(): Int {
            return mComments.size
        }

        fun cleanupListener() {
            mObserver.unregisterServerObserver()
        }
    }

    // http://wisdomitsol.com/blog/android/sending-email-in-android-without-using-the-default-built-in-application

    private fun sendEmail() {

        databaseInteractor!!.getUser(mPost!!.uid!!, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val user = data as User

                val TO = user.email
                val SUBJECT = mPost!!.title
                val MESSAGE = "Message"

                val intent = Intent(Intent.ACTION_SEND)

                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(TO))
                intent.putExtra(Intent.EXTRA_SUBJECT, SUBJECT)
                intent.putExtra(Intent.EXTRA_TEXT, MESSAGE)

                intent.type = "message/rfc822"

                startActivity(Intent.createChooser(intent, "Select Email Sending App :"))
            }
        })
    }

    companion object {

        private val TAG = "PostDetailActivity"
    }

}
