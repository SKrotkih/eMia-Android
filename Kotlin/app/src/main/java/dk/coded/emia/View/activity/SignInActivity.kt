package dk.coded.emia.View.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.model.Data.User
import dk.coded.emia.R

import butterknife.BindView
import dk.coded.emia.utils.Constants

import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.main_nav_header.*

class SignInActivity : BaseActivity(), View.OnClickListener {

    private val interactor: DatabaseInteractor
        get() = DatabaseFactory.databaseInteractor

    private val mPasswordField: EditText
        get() = field_password
    private val mEmailField: EditText
        get() = field_email
    private val mSignInButton: Button
        get() = button_sign_in
    private val mSignUpButton: Button
        get() = button_sign_up
    private val titleTextView: TextView
        get() = nav_title
    private val starButton: ImageButton
        get() = star_button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        starButton.visibility = View.GONE

        titleTextView.text = resources.getString(R.string.sign_in_title)

        // Click listeners
        mSignInButton.setOnClickListener { view -> signIn() }
        mSignUpButton.setOnClickListener { view -> signUp() }

        showKeyboard(mEmailField)
    }

    override fun onClick(v: View) {}

    public override fun onStart() {
        super.onStart()

        // Check auth on Activity start
        if (interactor.isUserSignedIn!!) {
            onAuthSuccess()
        }
    }

    private fun signIn() {
        hideKeyboard()
        Log.d(TAG, "signIn")
        if (!validateForm()) {
            return
        }
        val email = mEmailField.text.toString()
        val password = mPasswordField.text.toString()
        showProgressDialog()
        interactor.signIn(email, password, this, { status, user ->
            hideProgressDialog()
            if (status == Constants.SUCCESS) {
                onAuthSuccess()
            } else {
                Toast.makeText(this@SignInActivity, resources.getString(R.string.signin_failed),
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun signUp() {
        hideKeyboard()

        Log.d(TAG, "signUp")
        if (!validateForm()) {
            return
        }
        val email = mEmailField.text.toString()
        val password = mPasswordField.text.toString()
        showProgressDialog()
        interactor.signUp(email, password, this, { status, user ->
            hideProgressDialog()
            if (status == Constants.SUCCESS) {
                onAuthSuccess()
            } else {
                Toast.makeText(this@SignInActivity, resources.getString(R.string.signup_failed),
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onAuthSuccess() {
        val userId = interactor.currentUserId
        val userEmail = interactor.currentUserEmail

        val username = usernameFromEmail(userEmail)

        // Write new user
        writeNewUser(userId, username, userEmail)

        // Go to MainActivity
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    private fun usernameFromEmail(email: String): String {
        return if (email.contains("@")) {
            email.split("@".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]
        } else {
            email
        }
    }

    private fun validateForm(): Boolean {
        var result = true
        if (TextUtils.isEmpty(mEmailField.text.toString())) {
            mEmailField.error = resources.getString(R.string.field_required)
            result = false
        } else {
            mEmailField.error = null
        }
        val password = mPasswordField.text.toString()
        if (TextUtils.isEmpty(password)) {
            mPasswordField.error = resources.getString(R.string.field_required)
            result = false
        } else if (password.length < 7) {
            mPasswordField.error = resources.getString(R.string.password_too_short)
            result = false
        } else {
            mPasswordField.error = null
        }

        return result
    }

    // [START basic_write]
    private fun writeNewUser(userId: String, name: String, email: String) {
        val user = User(userId, name, email)
        interactor.addUser(user)
    }

    companion object {

        private val TAG = "SignInActivity"
    }
    // [END basic_write]
}
