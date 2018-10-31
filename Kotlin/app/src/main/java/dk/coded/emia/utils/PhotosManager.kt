package dk.coded.emia.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.request.target.BaseTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.transition.Transition

import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

import dk.coded.emia.R
import dk.coded.emia.View.GlideApp
import dk.coded.emia.View.activity.NewPostActivity

import android.app.Activity.RESULT_OK

/**
 * Created by oldman on 12/9/17.
 */

class PhotosManager {

    private val permissions = ArrayList<String>()
    private var permissionsToRequest: ArrayList<String>? = null
    private val permissionsRejected = ArrayList<String>()

    private var file: File? = null
    private var uri: Uri? = null

    private var parentActivity: Activity? = null
    private var delegate: PhotosManagerDelegate? = null

    private var activityIndicator: ProgressBarHandler? = null

    fun showPhotoDialog(parentActivity: Activity, delegate: PhotosManagerDelegate) {

        this.parentActivity = parentActivity
        this.activityIndicator = ProgressBarHandler(parentActivity)
        this.delegate = delegate

        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionsToRequest = findUnAskedPermissions(permissions)

        val items = arrayOf("Capture Photo", "Choose from Gallery", "Cancel")

        val myMsg = TextView(this.parentActivity)
        myMsg.text = "Add Photo"
        myMsg.gravity = Gravity.CENTER_HORIZONTAL
        myMsg.setPadding(10, 10, 10, 10)
        myMsg.textSize = 20f
        myMsg.setTextColor(this.parentActivity!!.resources.getColor(R.color.colorPrimary))

        activityIndicator!!.show()

        val activity = this.parentActivity
        val progressIndicator = this.activityIndicator

        val builder = AlertDialog.Builder(activity!!)
        builder.setCustomTitle(myMsg)
        builder.setItems(items) { dialog, item ->
            if (items[item] == "Capture Photo") {

                //Log.d(Constants.TAG, "permissionsToRequest: " + permissionsToRequest.size());
                if (checkCameraExists()) {
                    if (permissionsToRequest!!.size > 0) {
                        activity.requestPermissions(permissionsToRequest!!.toTypedArray<String>(), ALL_PERMISSIONS_RESULT)
                    } else {
                        //Toast.makeText(activity, "Permissions already granted.", Toast.LENGTH_LONG).show();
                        launchCamera()
                    }
                } else {
                    Toast.makeText(activity, "Camera not available.", Toast.LENGTH_SHORT).show()
                    progressIndicator!!.hide()
                }

            } else if (items[item] == "Choose from Gallery") {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                activity.startActivityForResult(intent, PICK_IMAGE_REQUEST)
            } else if (items[item] == "Cancel") {
                dialog.dismiss()
                progressIndicator!!.hide()
            }
        }
        builder.show()
    }

    fun checkCameraExists(): Boolean {
        return this.parentActivity!!.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this.parentActivity!!)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }

    private fun launchCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        file = outputMediaFile
        uri = FileProvider.getUriForFile(this.parentActivity, this.parentActivity!!.applicationContext.packageName + ".dk.coded.emia.provider", file)
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri)
        this.parentActivity!!.startActivityForResult(intent, CAMERA_TAKE_REQUEST)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        val progressIndicator = this.activityIndicator
        val _delegate = this.delegate

        when (requestCode) {
            PICK_IMAGE_REQUEST -> if (resultCode == RESULT_OK) {
                val selectedImage = data.data
                val bs = object : BaseTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
                        _delegate!!.setPhoto(resource)
                        progressIndicator!!.hide()
                    }

                    override fun removeCallback(cb: SizeReadyCallback) {}

                    override fun getSize(cb: SizeReadyCallback) {}
                }

                GlideApp.with(this.parentActivity!!.applicationContext).asBitmap()
                        .load(ImageFilePath.getPath(this.parentActivity!!.applicationContext, selectedImage!!))
                        .override(PHOTO_MAX_WIDTH, PHOTO_MAX_HEIGHT)
                        .fitCenter().into<BaseTarget>(bs)
            }
            CAMERA_TAKE_REQUEST -> {
                val bs = object : BaseTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
                        _delegate!!.setPhoto(resource)
                        progressIndicator!!.hide()
                    }

                    override fun removeCallback(cb: SizeReadyCallback) {}

                    override fun getSize(cb: SizeReadyCallback) {}
                }

                GlideApp.with(this.parentActivity!!.applicationContext).asBitmap()
                        .load(file!!.absolutePath)
                        .override(PHOTO_MAX_WIDTH, PHOTO_MAX_HEIGHT)
                        .fitCenter().into<BaseTarget>(bs)
            }
        }
    }

    //
    // Permissions
    //

    private fun findUnAskedPermissions(wanted: ArrayList<String>): ArrayList<*> {
        val result = ArrayList()

        for (perm in wanted) {
            if (!hasPermission(perm)) {
                result.add(perm)
            }
        }

        return result
    }

    private fun hasPermission(permission: String): Boolean {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return this.parentActivity!!.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }
        }
        return true
    }

    private fun canAskPermission(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        val activity = this.parentActivity

        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {
                for (perms in permissionsToRequest!!) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms)
                    }
                }

                if (permissionsRejected.size > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (this.parentActivity!!.shouldShowRequestPermissionRationale(permissionsRejected[0])) {
                            val msg = "These permissions are mandatory for the application. Please allow access."
                            showMessageOKCancel(msg,
                                    DialogInterface.OnClickListener { dialog, which ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            activity!!.requestPermissions(permissionsRejected.toTypedArray<String>(), ALL_PERMISSIONS_RESULT)
                                        }
                                    })
                            return
                        }
                    }
                } else {
                    //Toast.makeText(activity, "Permissions garanted.", Toast.LENGTH_LONG).show();
                    launchCamera()
                }
            }
        }
    }

    companion object {

        private val PICK_IMAGE_REQUEST = 100
        private val CAMERA_TAKE_REQUEST = 200
        private val PHOTO_MAX_WIDTH = 1024
        private val PHOTO_MAX_HEIGHT = 768
        private val ALL_PERMISSIONS_RESULT = 101

        private val outputMediaFile: File?
            get() {
                val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "@string/appName")

                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("@string/appName", "failed to create directory")
                        return null
                    }
                }

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                return File(mediaStorageDir.path + File.separator +
                        "IMG_" + timeStamp + ".jpg")
            }
    }

}
