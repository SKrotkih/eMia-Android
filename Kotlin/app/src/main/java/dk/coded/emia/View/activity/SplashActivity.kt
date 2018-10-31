package dk.coded.emia.View.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import dk.coded.emia.View.activity.MainActivity
import dk.coded.emia.model.Data.User
import dk.coded.emia.model.interactor.DatabaseFactory
import dk.coded.emia.model.interactor.DatabaseInteractor
import dk.coded.emia.utils.Constants
import dk.coded.emia.utils.Utils

import dk.coded.emia.utils.Constants.Companion.CANCEL
import dk.coded.emia.utils.Constants.Companion.FAIL
import dk.coded.emia.utils.Constants.Companion.SUCCESS

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val databaseInteractor = DatabaseFactory.databaseInteractor
        val intent: Intent
        if (databaseInteractor.isUserSignedIn!!) {
            intent = Intent(this, MainActivity::class.java)
        } else {
            intent = Intent(this, SignInActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
