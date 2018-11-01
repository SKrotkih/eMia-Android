package dk.coded.emia.model.interactor

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.Serializable
import java.util.ArrayList
import java.util.HashMap
import java.util.UUID

import dk.coded.emia.View.activity.SignInActivity
import dk.coded.emia.model.Data.Comment
import dk.coded.emia.model.Data.Favorite
import dk.coded.emia.model.Data.Post
import dk.coded.emia.model.Data.User
import dk.coded.emia.utils.BasicCallBack

import android.content.ContentValues.TAG
import dk.coded.emia.utils.Constants

/**
 * Created by oldman on 11/30/17.
 */

class FirebaseInteractor//private constructor.
private constructor() : Serializable, DatabaseInteractor {

    // [START declare_database_ref]
    private var mDatabase: DatabaseReference? = null
    // [END declare_database_ref]

    private var mPostReference: DatabaseReference? = null
    private var mPostListener: ValueEventListener? = null

    override val currentUserId: String
        get() = currentUser()!!.uid

    override val currentUserEmail: String
        get() = currentUser()?.email!!

    // Sign Up, Sign In

    override val isUserSignedIn: Boolean?
        get() = currentUser() != null

    init {

        //Prevent form the reflection api.
        if (sDefaltInstance != null) {
            throw RuntimeException("Use getInstance() method to get the single instance of this class.")
        }
    }

    //Make singleton from serialize and deserialize operation.
    protected fun readResolve(): FirebaseInteractor? {
        return instance
    }

    override fun dataBaseRef(): DatabaseReference {
        if (mDatabase == null) {
            // [START initialize_database_ref]
            mDatabase = FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_NAME)
            // [END initialize_database_ref]
        }
        return mDatabase!!
    }

    private fun currentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    override fun signIn(email: String, password: String, acrtivity: Activity, collback: BasicCallBack) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(acrtivity) { task ->
                    Log.d(TAG, "signIn:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
                        saveToken()
                        collback(Constants.SUCCESS, task.result.user)
                    } else {
                        collback(Constants.FAIL, null)
                    }
                }
    }

    override fun signUp(email: String, password: String, acrtivity: Activity, collback: BasicCallBack) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(acrtivity) { task ->
                    Log.d(TAG, "createUser:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
                        saveToken()
                        collback(Constants.SUCCESS, task.result.user)
                    } else {
                        collback(Constants.FAIL, null)
                    }
                }
    }

    override fun logOut() {
        FirebaseAuth.getInstance().signOut()
    }

    private fun saveToken() {
        currentUser({ status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val user = data as User
                val currentToken = FirebaseInstanceId.getInstance().token
                if (user.tokenAndroid == null) {
                    user.tokenAndroid = currentToken
                    updateUser(user, { updatestatus, updateddata -> })
                } else if (user.tokenAndroid != currentToken || user.tokenAndroid!!.isEmpty()) {
                    user.tokenAndroid = currentToken
                    updateUser(user, { updatestatus, updateddata -> })
                }
            } else if (status == Constants.FAIL) {
            }
        })
    }

    // Public methods

    override fun getAllUsers(collback: BasicCallBack) {
        val usersRef = dataBaseRef().child("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = ArrayList<User>()
                if (dataSnapshot.hasChildren()) {
                    for (ds in dataSnapshot.children) {
                        users.add(ds.getValue<User>(User::class.java)!!)
                    }
                }
                collback(Constants.SUCCESS, users)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                collback(Constants.SUCCESS, ArrayList<Any>())
            }
        })
    }

    override fun addUser(user: User) {
        val dbRef = dataBaseRef().child("users").child(user.id)
        dbRef.setValue(user)
    }

    override fun updateUser(user: User, callback: BasicCallBack) {
        val userValues = user.toMap()
        val childUpdates = HashMap<String, Any>()
        childUpdates["/users/" + user.id] = userValues
        dataBaseRef().updateChildren(childUpdates)
        callback(Constants.SUCCESS, 0)
    }

    override fun currentUser(callback: BasicCallBack) {
        val userId = currentUserId
        getUser(userId, callback)
    }

    override fun getUser(userId: String, callback: BasicCallBack) {
        val usersRef = dataBaseRef().child("users")
        usersRef.child(userId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Get user value
                        val user = dataSnapshot.getValue<User>(User::class.java)

                        if (user == null) {
                            // User is null, error out
                            callback(Constants.FAIL, "User $userId is unexpectedly null")
                        } else {
                            callback(Constants.SUCCESS, user)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException())
                    }
                })
    }

    override fun addPost(post: Post, callback: BasicCallBack) {
        currentUser({ status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val user = data as User
                post.uid = user.id
                post.author = user.username
                writeNewPost(post)
                callback(Constants.SUCCESS, null)
            } else if (status == Constants.FAIL) {
                val errorDescription = data as String
                callback(Constants.FAIL, errorDescription)
            }
        })
    }

    private fun writeNewPost(post: Post) {
        val key = dataBaseRef().child("posts").push().key
        post.id = key
        uploadPhotoBitmap(post.photoBitmap!!, key, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val fileName = data as String
                // Create new post at /user-posts/$userid/$postid and at
                // /posts/$postid simultaneously

                val postValues = post.toMap()

                val childUpdates = HashMap<String, Any>()
                childUpdates["/posts/$key"] = postValues
                childUpdates["/user-posts/" + post.uid + "/" + post.id] = postValues

                dataBaseRef().updateChildren(childUpdates)
            } else if (status == Constants.FAIL) {
            }
        })
    }

    override fun uploadPhotoBitmap(bitmap: Bitmap, fileName: String, callbak: BasicCallBack) {

        // bitmap to stream
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos)
        val bitmapdata = bos.toByteArray()
        val bs = ByteArrayInputStream(bitmapdata)

        uploadPhotoFromStream(bs, fileName, { status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val uploadedFileName = data as String
                callbak(Constants.SUCCESS, uploadedFileName)
            } else if (status == Constants.FAIL) {
                val errorDescription = data as String
                callbak(Constants.FAIL, errorDescription)
            }
        })
    }

    fun uploadPhotoFromStream(stream: InputStream, id: String, callback: BasicCallBack) {
        val uuid = UUID.randomUUID().toString()
        val path = String.format("%s.jpg", id)

        val storage = FirebaseStorage.getInstance()
        val ref = storage.getReferenceFromUrl(Constants.FIREBASE_STORAGE).child(path)

        ref.putStream(stream)
                .addOnSuccessListener { callback(Constants.SUCCESS, path) }
                .addOnFailureListener { exception ->
                    val errorDescription = "setPersonPhotoFromStream upload onFailure: " + exception.toString()
                    callback(Constants.FAIL, errorDescription)
                }
    }

    override fun downloadPhoto(ctx: Context, id: String, collback: BasicCallBack) {
        val path = String.format("%s.jpg", id)
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl(Constants.FIREBASE_STORAGE).child(path)
        storageRef.downloadUrl.addOnSuccessListener { uri -> collback(Constants.SUCCESS, uri) }
    }

    override fun addCommentToPost(post: Post, commentText: String, callback: BasicCallBack) {

        currentUser({ status: Int, data: Any? ->
            if (status == Constants.SUCCESS) {
                val user = data as User
                val key = dataBaseRef().child("comments").child(post.id!!).push().key
                val comment = Comment(commentText)
                comment.id = key
                comment.uid = user.id
                comment.postid = post.id
                comment.author = user.username
                val commentValues = comment.toMap()
                val childUpdates = HashMap<String, Any>()
                childUpdates["/comments/" + post.id + "/" + key] = commentValues
                dataBaseRef().updateChildren(childUpdates)
                callback(Constants.SUCCESS, 0)
            } else if (status == Constants.FAIL) {
                val errorDescription = data as String
                callback(Constants.FAIL, errorDescription)
            }
        })
    }

    override fun getPost(mPostKey: String, callback: BasicCallBack) {

        // Initialize Database
        mPostReference = dataBaseRef().child("posts").child(mPostKey)

        // Add value event listener to the post
        // [START post_value_event_listener]
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val post = dataSnapshot.getValue<Post>(Post::class.java)
                callback(Constants.SUCCESS, post)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(Constants.FAIL, databaseError)
            }
        }
        mPostReference!!.addValueEventListener(postListener)
        // [END post_value_event_listener]

        mPostListener = postListener
    }

    override fun getAllPosts(collback: BasicCallBack) {
        val postsRef = dataBaseRef().child("posts")
        postsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val posts = ArrayList<Post>()
                if (dataSnapshot.hasChildren()) {
                    for (ds in dataSnapshot.children) {
                        posts.add(ds.getValue<Post>(Post::class.java)!!)
                    }
                }
                collback(Constants.SUCCESS, posts)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                collback(Constants.SUCCESS, ArrayList<Any>())
            }
        })
    }

    // Set Up My Favorite Post

    override fun isItMyFavoritePost(post: Post, callback: BasicCallBack) {
        val userId = currentUserId

        // Check on it is my post
        if (post.uid == userId) {
            callback(Constants.FAIL, null)
        } else {
            val usersRef = dataBaseRef().child("favorites").child(post.id!!).child(userId)
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val favorite = dataSnapshot.getValue<Favorite>(Favorite::class.java)
                    if (favorite == null) {
                        callback(Constants.SUCCESS, false)
                    } else {
                        // It does not matter the favorite level now
                        callback(Constants.SUCCESS, true)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "getUser:onCancelled", databaseError.toException())
                    callback(Constants.FAIL, null)
                }
            })
        }
    }


    override fun setUpMyFavoritePost(post: Post, isFavorite: Boolean?) {
        val userId = currentUserId
        val favorite = Favorite()
        if (isFavorite!!) {
            val favoriteValues = favorite.toMap()
            val childUpdates = HashMap<String, Any>()
            val favoritePath = "/favorites/" + post.id + "/" + userId
            childUpdates[favoritePath] = favoriteValues
            dataBaseRef().updateChildren(childUpdates)
        } else {
            val favoriteRef = dataBaseRef().child("favorites").child(post.id!!).child(userId)
            favoriteRef.removeValue()
        }
    }

    // End Set Up My Favorite Post

    override fun removePostObserver() {
        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference!!.removeEventListener(mPostListener!!)
        }
    }

    companion object {

        @Volatile
        private var sDefaltInstance: FirebaseInteractor? = null

        // One Syngleton instance
        //if there is no instance available... create new one
        val instance: FirebaseInteractor?
            get() {
                if (sDefaltInstance == null) {
                    synchronized(FirebaseInteractor::class.java) {
                        if (sDefaltInstance == null) sDefaltInstance = FirebaseInteractor()
                    }
                }

                return sDefaltInstance
            }
    }


}
