package com.example.oxygencylindertracker.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.example.oxygencylindertracker.dB.LocalStorageHelper
import com.example.oxygencylindertracker.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    lateinit var firebaseDBHelper : FirebaseDBHelper
    lateinit var localStorageHelper: LocalStorageHelper
    lateinit var mProgressBar : ProgressBar
    lateinit var logInButton : Button
    lateinit var titleText : TextView
    private val RC_SIGN_IN = 420

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("AUTH CHECK", auth.currentUser.toString())
        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        firebaseDBHelper = FirebaseDBHelper()
        localStorageHelper = LocalStorageHelper()


        logInButton = findViewById(R.id.signInButton)
        titleText = findViewById(R.id.headtext)

        mProgressBar = findViewById(R.id.signInProgressBar)
        mProgressBar.visibility = View.GONE


        logInButton.setOnClickListener {
            logInButton.visibility = View.GONE
            mProgressBar.visibility = View.VISIBLE
            val activity = this
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(activity, gso)
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("TAG", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "signInWithCredential:success")
                    val user = auth.currentUser
                    navigateToHomeScreen(user?.email ?: "", user?.displayName ?: "")
                } else {
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    showMessage("Login Error. Please try again")
                }
            }
    }

    fun showMessage(message : String) {
        // Used to reflect any login error. Need to reset the screen system
        mProgressBar.visibility = View.GONE
        logInButton.visibility = View.VISIBLE
        titleText.text = "Please use your authorized Email to Login"

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun navigateToHomeScreen(email: String, userName : String) {
        val intent = Intent(this, HomeActivity::class.java)
        localStorageHelper.savePhoneNumber(email, this)
        localStorageHelper.saveUserName(userName, this)
        startActivity(intent)
        finish()
    }
}