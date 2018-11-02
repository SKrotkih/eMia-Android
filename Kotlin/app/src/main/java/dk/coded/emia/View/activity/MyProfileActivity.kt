package dk.coded.emia.View.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.text.TextUtils
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import android.widget.EditText

import java.util.ArrayList
import java.util.Arrays
import java.util.StringTokenizer

import butterknife.ButterKnife
import dk.coded.emia.R
import dk.coded.emia.View.GlideApp
import dk.coded.emia.model.Data.User
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.utils.PhotosManager
import dk.coded.emia.utils.PhotosManagerDelegate
import dk.coded.emia.utils.PositionedCropTransformation

import com.bumptech.glide.request.RequestOptions.bitmapTransform

import dk.coded.emia.utils.Constants

import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.notification.*
import kotlinx.android.synthetic.main.post_detail_nav_header.*

/**
 * Created by oldman on 12/8/17.
 */

class MyProfileActivity : BaseActivity(), View.OnClickListener, PhotosManagerDelegate {

    private val  mBackButton: ImageButton
        get() = back_button
    private val  userNameTitleTextView: TextView
        get() = user_name_label
    private val  userNameTextView: EditText
        get() = user_name_text
    private val  userEmailTitleTextView: TextView
        get() = user_email_label
    private val  userEmailTextView: TextView
        get() = user_email_text
    private val  userPhotoTitleTextView: TextView
        get() = user_photo_label
    private val  userPhotoImageView: ImageView
        get() = user_photo
    private val  userYearOfBirthSpinner: Spinner
        get() = yerarbirth_spinner
    private val  userGenderSpinner: Spinner
        get() = gender_spinner
    private val  userMunicipalitySpinner: Spinner
        get() = municipality_spinner
    private val  mSubmitButton: FloatingActionButton
        get() = fab_submit_post
    private val  titleEditText: TextView
        get() = nav_bar_title_tv
    private val  starButton: ImageButton
        get() = star_button
    private val  rlNotification: RelativeLayout
        get() = layout_Notification

    private var photosManager: PhotosManager? = null
    private var databaseInteractor: DatabaseInteractor? = null
    private var mUser: User? = null

    private val selectedMunicipality: String
        get() {
            val municips = Arrays.asList(*resources.getStringArray(R.array.municipalities_arrays))
            val municipalityIndex = userMunicipalitySpinner.selectedItemPosition
            if (municipalityIndex > 0) {
                val municipalityItem = municips[municipalityIndex].toString()
                val tokens = StringTokenizer(municipalityItem, "|")
                return tokens.nextToken()
            } else {
                return ""
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        titleEditText.text = resources.getString(R.string.my_profile_title)

        photosManager = PhotosManager()

        databaseInteractor = DatabaseFactory.databaseInteractor

        userPhotoImageView.setOnClickListener { view -> changeMyPhoto() }
        mBackButton.setOnClickListener { view -> finish() }
        mSubmitButton.setOnClickListener { view -> submitProfile() }

        addItemsOnSpinnerYears()
        addItemsOnSpinnerMunicipality()

        //prepareNotificationsListener(this);
        rlNotification.visibility = View.GONE
        starButton.visibility = View.GONE
    }

    override fun onClick(v: View) {}

    public override fun onStart() {
        super.onStart()

        val thisContext = this
        databaseInteractor!!.currentUser(){ status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val user = data as User
                mUser = user
                userNameTextView.setText(user.username)
                userEmailTextView.text = user.email

                if (user.yearbirth != null && user.yearbirth!! > 0) {
                    val index = 2006 - user.yearbirth!!
                    userYearOfBirthSpinner.setSelection(index)
                } else {
                    userYearOfBirthSpinner.setSelection(0)
                }
                if (user.gender != null) {
                    userGenderSpinner.setSelection(user.gender!!)
                } else {
                    userGenderSpinner.setSelection(0)
                }
                if (user.address != null && !user.address!!.isEmpty()) {
                    val municipalityId = user.address
                    val position = getMunicipalityIndexWith(municipalityId)
                    userMunicipalitySpinner.setSelection(position!!)
                } else {
                    userMunicipalitySpinner.setSelection(0)
                }
                databaseInteractor!!.downloadPhoto(this@MyProfileActivity, user.id, { status2: Int, data2: Any? ->
                    if (status2 == Constants.SUCCESS) {
                        val uri = data2 as Uri
                        GlideApp.with(thisContext.applicationContext)
                                .load(uri.toString())
                                .apply(bitmapTransform(PositionedCropTransformation(1f, 0f)))
                                .into(userPhotoImageView)
                    }
                })
            } else if (status == Constants.FAIL) {
                userNameTextView.setText("")
                userEmailTextView.text = ""
                userPhotoImageView.setImageBitmap(null)
                userYearOfBirthSpinner.setSelection(0)
                userGenderSpinner.setSelection(0)
                userMunicipalitySpinner.setSelection(0)
            }
        }
    }

    private fun changeMyPhoto() {
        photosManager!!.showPhotoDialog(this, this)
    }

    // PhotosManagerDelegate protocol:

    override fun setPhoto(bitmap: Bitmap) {
        val fileName = databaseInteractor!!.currentUserId
        databaseInteractor!!.uploadPhotoBitmap(bitmap, fileName, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                userPhotoImageView.setImageBitmap(bitmap)
            } else if (status == Constants.FAIL) {
            }
        })
    }

    private fun submitProfile() {
        if (mUser == null) {
            return
        }
        val name = userNameTextView.text.toString()
        // Name is required
        if (TextUtils.isEmpty(name)) {
            return
        }
        val yearbirth = userYearOfBirthSpinner.selectedItem.toString()
        val gender = userGenderSpinner.selectedItemPosition
        mUser!!.username = name
        mUser!!.yearbirth = Integer.parseInt(yearbirth)
        mUser!!.gender = gender
        mUser!!.address = selectedMunicipality
        databaseInteractor!!.updateUser(mUser!!, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                finish()
            } else if (status == Constants.FAIL) {
            } else if (status == Constants.CANCEL) {
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        photosManager!!.onActivityResult(requestCode, resultCode, data)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        photosManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Year of birth spinner data source

    private fun addItemsOnSpinnerYears() {
        val list = ArrayList<String>()
        for (year in 2006 downTo 1901) {
            list.add(year.toString())
        }
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, list)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userYearOfBirthSpinner.adapter = dataAdapter
    }

    private fun addItemsOnSpinnerMunicipality() {
        val municips = Arrays.asList(*resources.getStringArray(R.array.municipalities_arrays))
        val list = ArrayList<String>()
        for (item in municips) {
            if (item.length > 1) {
                val tokens = StringTokenizer(item, "|")
                val id = tokens.nextToken()
                val name = tokens.nextToken()
                list.add(name)
            } else {
                list.add("")
            }
        }
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, list)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userMunicipalitySpinner.adapter = dataAdapter
    }

    private fun getMunicipalityIndexWith(municipalityId: String?): Int? {
        val municips = Arrays.asList(*resources.getStringArray(R.array.municipalities_arrays))
        var index: Int? = -1
        for (i in 1 until municips.size) {
            val item = municips[i]
            val tokens = StringTokenizer(item, "|")
            val id = tokens.nextToken()
            if (id == municipalityId) {
                index = i
                break
            }
        }
        return if (index == -1) {
            0
        } else {
            index
        }
    }

}
