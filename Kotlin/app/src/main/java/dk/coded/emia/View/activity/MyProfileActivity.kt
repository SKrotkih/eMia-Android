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
import dk.coded.emia.utils.Constants.Companion.CANCEL
import dk.coded.emia.utils.Constants.Companion.FAIL
import dk.coded.emia.utils.Constants.Companion.SUCCESS

import butterknife.BindView

/**
 * Created by oldman on 12/8/17.
 */

class MyProfileActivity : BaseActivity(), View.OnClickListener, PhotosManagerDelegate {

    @BindView(R.id.back_button)
    internal var mBackButton: ImageButton? = null
    @BindView(R.id.user_name_label)
    internal var userNameTitleTextView: TextView? = null
    @BindView(R.id.user_name_text)
    internal var userNameTextView: EditText? = null
    @BindView(R.id.user_email_label)
    internal var userEmailTitleTextView: TextView? = null
    @BindView(R.id.user_email_text)
    internal var userEmailTextView: TextView? = null
    @BindView(R.id.user_photo_label)
    internal var userPhotoTitleTextView: TextView? = null
    @BindView(R.id.user_photo)
    internal var userPhotoImageView: ImageView? = null
    @BindView(R.id.yerarbirth_spinner)
    internal var userYearOfBirthSpinner: Spinner? = null
    @BindView(R.id.gender_spinner)
    internal var userGenderSpinner: Spinner? = null
    @BindView(R.id.municipality_spinner)
    internal var userMunicipalitySpinner: Spinner? = null
    @BindView(R.id.fab_submit_post)
    internal var mSubmitButton: FloatingActionButton? = null
    @BindView(R.id.nav_bar_title_tv)
    internal var titleEditText: TextView? = null
    @BindView(R.id.star_button)
    internal var starButton: ImageButton? = null
    @BindView(R.id.rlNotification)
    internal var rlNotification: RelativeLayout? = null

    private var photosManager: PhotosManager? = null
    private var databaseInteractor: DatabaseInteractor? = null
    private var mUser: User? = null

    private val selectedMunicipality: String
        get() {
            val municips = Arrays.asList(*resources.getStringArray(R.array.municipalities_arrays))
            val municipalityIndex = userMunicipalitySpinner!!.selectedItemPosition
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
        ButterKnife.bind(this)

        titleEditText!!.text = resources.getString(R.string.my_profile_title)

        photosManager = PhotosManager()

        databaseInteractor = DatabaseFactory.databaseInteractor

        userPhotoImageView!!.setOnClickListener { view -> changeMyPhoto() }
        mBackButton!!.setOnClickListener { view -> finish() }
        mSubmitButton!!.setOnClickListener { view -> submitProfile() }

        addItemsOnSpinnerYears()
        addItemsOnSpinnerMunicipality()

        //prepareNotificationsListener(this);
        rlNotification!!.visibility = View.GONE
        starButton!!.visibility = View.GONE
    }

    override fun onClick(v: View) {}

    public override fun onStart() {
        super.onStart()

        val thisContext = this
        databaseInteractor!!.currentUser({ status: Int, data: Any ->
            if (status == Companion.getSUCCESS()) {
                val user = data as User
                mUser = user
                userNameTextView!!.setText(user.username)
                userEmailTextView!!.text = user.email

                if (user.yearbirth != null && user.yearbirth > 0) {
                    val index = 2006 - user.yearbirth!!
                    userYearOfBirthSpinner!!.setSelection(index)
                } else {
                    userYearOfBirthSpinner!!.setSelection(0)
                }
                if (user.gender != null) {
                    userGenderSpinner!!.setSelection(user.gender!!)
                } else {
                    userGenderSpinner!!.setSelection(0)
                }
                if (user.address != null && !user.address!!.isEmpty()) {
                    val municipalityId = user.address
                    val position = getMunicipalityIndexWith(municipalityId)
                    userMunicipalitySpinner!!.setSelection(position!!)
                } else {
                    userMunicipalitySpinner!!.setSelection(0)
                }
                databaseInteractor!!.downloadPhoto(this@MyProfileActivity, user.id, { status2: Int, data2: Any ->
                    if (status2 == Companion.getSUCCESS()) {
                        val uri = data2 as Uri
                        GlideApp.with(thisContext.applicationContext)
                                .load(uri.toString())
                                .apply(bitmapTransform(PositionedCropTransformation(1f, 0f)))
                                .into(userPhotoImageView!!)
                    }
                })
            } else if (status == Companion.getFAIL()) {
                userNameTextView!!.setText("")
                userEmailTextView!!.text = ""
                userPhotoImageView!!.setImageBitmap(null)
                userYearOfBirthSpinner!!.setSelection(0)
                userGenderSpinner!!.setSelection(0)
                userMunicipalitySpinner!!.setSelection(0)
            }
        })
    }

    private fun changeMyPhoto() {
        photosManager!!.showPhotoDialog(this, this)
    }

    // PhotosManagerDelegate protocol:

    override fun setPhoto(bitmap: Bitmap) {
        val fileName = databaseInteractor!!.currentUserId
        databaseInteractor!!.uploadPhotoBitmap(bitmap, fileName, { status: Int, data: Any ->
            if (status == Companion.getSUCCESS()) {
                userPhotoImageView!!.setImageBitmap(bitmap)
            } else if (status == Companion.getFAIL()) {
            }
        })
    }

    private fun submitProfile() {
        if (mUser == null) {
            return
        }
        val name = userNameTextView!!.text.toString()
        // Name is required
        if (TextUtils.isEmpty(name)) {
            return
        }
        val yearbirth = userYearOfBirthSpinner!!.selectedItem.toString()
        val gender = userGenderSpinner!!.selectedItemPosition
        mUser!!.username = name
        mUser!!.yearbirth = Integer.parseInt(yearbirth)
        mUser!!.gender = gender
        mUser!!.address = selectedMunicipality
        databaseInteractor!!.updateUser(mUser!!, { status: Int, data: Any ->
            if (status == Companion.getSUCCESS()) {
                finish()
            } else if (status == Companion.getFAIL()) {
            } else if (status == Companion.getCANCEL()) {
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
        userYearOfBirthSpinner!!.adapter = dataAdapter
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
        userMunicipalitySpinner!!.adapter = dataAdapter
    }

    private fun getMunicipalityIndexWith(municipalityId: String?): Int? {
        val municips = Arrays.asList(*resources.getStringArray(R.array.municipalities_arrays))
        var index: Int? = -1
        for (i in 1 until municips.size) {
            val item = municips[i!!]
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
