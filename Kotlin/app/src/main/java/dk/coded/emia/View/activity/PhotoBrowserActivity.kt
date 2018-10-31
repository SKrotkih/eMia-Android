package dk.coded.emia.View.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView

import com.github.chrisbanes.photoview.PhotoView

import butterknife.BindView
import butterknife.ButterKnife
import dk.coded.emia.R
import dk.coded.emia.View.GlideApp
import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.utils.PositionedCropTransformation

import android.widget.ImageView.ScaleType.MATRIX
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import dk.coded.emia.utils.Constants.Companion.TRANSPARENT_COLOR

/**
 * Created by oldman on 12/8/17.
 */

class PhotoBrowserActivity : BaseActivity() {

    @BindView(R.id.back_button)
    internal var mBackButton: ImageButton? = null
    @BindView(R.id.photoImageView)
    internal var mPhotoImageView: PhotoView? = null
    @BindView(R.id.star_button)
    internal var starButton: ImageButton? = null
    @BindView(R.id.nav_header)
    internal var navBar: RelativeLayout? = null
    @BindView(R.id.nav_bar_title_tv)
    internal var mTitleTextView: TextView? = null
    @BindView(R.id.rlNotification)
    internal var rlNotification: RelativeLayout? = null

    private var mPost: Post? = null
    private var mContext: Context? = null
    private var databaseInteractor: DatabaseInteractor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_browser)

        ButterKnife.bind(this)

        databaseInteractor = DatabaseFactory.databaseInteractor

        mBackButton!!.setOnClickListener { view -> this.finish() }

        navBar!!.setBackgroundColor(Color.parseColor(Companion.getTRANSPARENT_COLOR()))
        val intent = intent
        mPost = intent.getSerializableExtra("post") as Post

        //prepareNotificationsListener(this);
        rlNotification!!.visibility = View.GONE
        starButton!!.visibility = View.GONE
    }

    public override fun onStart() {
        super.onStart()

        mContext = this

        mTitleTextView!!.text = mPost!!.title
        //mPhotoImageView.setScaleType(MATRIX);

        databaseInteractor!!.downloadPhoto(this, mPost!!.id!!, { status: Int, data: Any ->
            val uri = data as Uri
            GlideApp.with(mContext!!.applicationContext)
                    .load(uri.toString())
                    .apply(bitmapTransform(PositionedCropTransformation(1f, 0f)))
                    .into(mPhotoImageView!!)
        })
    }
}
