package dk.coded.emia.model.interactor

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference

import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.Data.User
import dk.coded.emia.utils.BasicCallBack

/**
 * Created by oldman on 11/30/17.
 */

interface DatabaseInteractor {

    val isUserSignedIn: Boolean?
    val currentUserEmail: String
    val currentUserId: String

    fun dataBaseRef(): DatabaseReference
    fun signIn(email: String, password: String, acrtivity: Activity, collback: BasicCallBack)
    fun signUp(email: String, password: String, acrtivity: Activity, collback: BasicCallBack)
    fun logOut()
    fun addUser(user: User)
    fun updateUser(user: User, callback: BasicCallBack)
    fun addPost(post: Post, callback: BasicCallBack)
    fun addCommentToPost(post: Post, commentText: String, callback: BasicCallBack)
    fun getPost(mPostKey: String, callback: BasicCallBack)
    fun downloadPhoto(ctx: Context, id: String, collback: BasicCallBack)
    fun removePostObserver()
    fun currentUser(callback: BasicCallBack)
    fun isItMyFavoritePost(post: Post, callback: BasicCallBack)
    fun setUpMyFavoritePost(post: Post, isFavorite: Boolean?)
    fun getAllPosts(collback: BasicCallBack)
    fun getAllUsers(collback: BasicCallBack)
    fun getUser(userId: String, callback: BasicCallBack)
    fun uploadPhotoBitmap(bitmap: Bitmap, fileName: String, callbak: BasicCallBack)
}
