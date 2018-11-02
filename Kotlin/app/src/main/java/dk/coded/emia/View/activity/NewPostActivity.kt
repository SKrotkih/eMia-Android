package dk.coded.emia.View.activity

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.github.chrisbanes.photoview.PhotoView

import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.R
import dk.coded.emia.utils.Alerts
import dk.coded.emia.utils.Constants
import dk.coded.emia.utils.PhotosManager
import dk.coded.emia.utils.PhotosManagerDelegate

import kotlinx.android.synthetic.main.activity_new_post.*
import kotlinx.android.synthetic.main.notification.*
import kotlinx.android.synthetic.main.post_detail_nav_header.*

class NewPostActivity : BaseActivity(), View.OnClickListener, PhotosManagerDelegate {
    internal var activity: Activity = this@NewPostActivity

    private val mTitleField: EditText
        get() = field_title
    private val mBodyField: EditText
        get() = field_body
    private val mSubmitButton: FloatingActionButton
        get() = fab_submit_post
    private val ivPhoto: PhotoView
        get() = photo_post
    private val addPhotoButton: Button
        get() = add_to_post_photo_button
    private val mBackButton: ImageButton
        get() = back_button
    private val mStarButton: ImageButton
        get() = star_button
    private val mTitleTextView: TextView
        get() = nav_bar_title_tv
    private val rlNotification: RelativeLayout
        get() = layout_Notification

    private var photoBitmap: Bitmap? = null
    private var databaseInteractor: DatabaseInteractor? = null
    private var photosManager: PhotosManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        databaseInteractor = DatabaseFactory.databaseInteractor
        photosManager = PhotosManager()

        addPhotoButton.setOnClickListener(this)
        mBackButton.setOnClickListener(this)
        mSubmitButton.setOnClickListener(this)

        mStarButton.visibility = View.GONE

        showKeyboard(mTitleField)

        mTitleTextView.text = resources.getString(R.string.new_post_title)

        //prepareNotificationsListener(activity);
        rlNotification.visibility = View.GONE
    }

    override fun onClick(button: View) {
        hideKeyboard()
        val buttonId = button.id
        if (buttonId == R.id.add_to_post_photo_button) {
            photosManager!!.showPhotoDialog(this, this)
        } else if (buttonId == R.id.back_button) {
            finish()
        } else if (buttonId == R.id.fab_submit_post) {
            submitPost()
        }
    }

    private fun submitPost() {
        val title = mTitleField.text.toString()
        val body = mBodyField.text.toString()

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.error = resources.getString(R.string.field_required)
            return
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            mBodyField.error = resources.getString(R.string.field_required)
            return
        }

        if (photoBitmap == null) {
            Alerts.alertOk(this, "", resources.getString(R.string.add_photo), { state, data ->
                if (state == Constants.SUCCESS) {
                    showPhotoDialog()
                }
            })
            return
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false)
        Toast.makeText(this, resources.getString(R.string.waiting_post), Toast.LENGTH_SHORT).show()

        val post = Post(title, body, photoBitmap!!)

        showProgressDialog()
        databaseInteractor!!.addPost(post, { status: Int, data: Any? ->
            hideProgressDialog()
            if (status == Constants.SUCCESS) {
                // Finish this Activity, back to the stream
                setEditingEnabled(true)
                finish()
                // [END_EXCLUDE]
            } else if (status == Constants.FAIL) {
                val errorDescription = data as String
                // User is null, error out
                Log.e(TAG, errorDescription)
                Toast.makeText(this@NewPostActivity,
                        "Error: could not fetch user.",
                        Toast.LENGTH_SHORT).show()
            } else if (status == Constants.CANCEL) {
                // [START_EXCLUDE]
                setEditingEnabled(true)
                // [END_EXCLUDE]
            }
        })
    }

    private fun setEditingEnabled(enabled: Boolean) {
        mTitleField.isEnabled = enabled
        mBodyField.isEnabled = enabled
        if (enabled) {
            mSubmitButton.visibility = View.VISIBLE
        } else {
            mSubmitButton.visibility = View.GONE
        }
    }

    // PhotosManagerDelegate protocol:

    private fun showPhotoDialog() {
        photosManager!!.showPhotoDialog(this, this)
    }

    override fun setPhoto(bitmap: Bitmap) {
        ivPhoto.setImageBitmap(bitmap)
        photoBitmap = bitmap
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        photosManager!!.onActivityResult(requestCode, resultCode, data)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        photosManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {

        private val TAG = "NewPostActivity"
    }
}
