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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    lateinit var firebaseDBHelper : FirebaseDBHelper
    lateinit var localStorageHelper: LocalStorageHelper
    lateinit var mProgressBar : ProgressBar
    lateinit var getOTPButton : Button
    lateinit var logInButton : Button
    lateinit var phoneNumberEditText : EditText
    lateinit var OTPEditText : EditText
    lateinit var titleText : TextView
    lateinit var subText : TextView
    private var isLoginInitiated = false

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks () {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            Log.i("AUTH_MESSAGE", "Verification Completed")
//            signInWithPhoneAuthCredentials(p0)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("AUTH_MESSAGE", "onVerificationFailed"+ e.message.toString())

            when (e) {
                is FirebaseAuthInvalidCredentialsException -> { }
                is FirebaseTooManyRequestsException -> { }
                else -> { }
            }
            showMessage("Login Failed. Please Try Again")
        }

        override fun onCodeAutoRetrievalTimeOut(p0: String) {
            super.onCodeAutoRetrievalTimeOut(p0)
            Log.e("Timeout", "Auto Retrieval Time Out")
        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)
            titleText.text = "Auto Retrieving OTP...."
            subText.text = "You can also enter the OTP manually below and login"
            mProgressBar.visibility = View.GONE
            logInButton.visibility = View.VISIBLE
            OTPEditText.visibility = View.VISIBLE
            phoneNumberEditText.visibility = View.GONE

            logInButton.setOnClickListener {
                val otp = OTPEditText.text.toString()
                if (otp.length != 6) {
                    showMessage("Invalid OTP")
                } else {
                    val credential = PhoneAuthProvider.getCredential(p0, otp)
                    signInWithPhoneAuthCredentials(credential)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("AUTH CHECK", auth.currentUser.toString())
        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        firebaseDBHelper = FirebaseDBHelper()
        localStorageHelper = LocalStorageHelper()

        phoneNumberEditText = findViewById(R.id.authPhoneNumberText)

        OTPEditText = findViewById(R.id.authOTPText)

        getOTPButton = findViewById(R.id.authGetOTPButton)
        logInButton = findViewById(R.id.authLoginButton)
        subText = findViewById(R.id.subtext)
        titleText = findViewById(R.id.headtext)

        mProgressBar = findViewById(R.id.signInProgressBar)
        mProgressBar.visibility = View.GONE
        logInButton.visibility = View.GONE
        OTPEditText.visibility = View.GONE

        getOTPButton.setOnClickListener {
            authenticateUser(phoneNumberEditText.text.toString())
        }
    }

    private fun authenticateUser (phoneNumber : String) {
        mProgressBar.visibility = View.VISIBLE
        getOTPButton.visibility = View.GONE

        Log.e("PHONE NUMBER", phoneNumber)
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(1L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun showMessage(message : String) {
        mProgressBar.visibility = View.GONE
        getOTPButton.visibility = View.VISIBLE
        phoneNumberEditText.visibility = View.VISIBLE
        logInButton.visibility = View.GONE
        OTPEditText.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun navigateToHomeScreen(phoneNumber: String, userName : String) {
        val intent = Intent(this, HomeActivity::class.java)
        localStorageHelper.savePhoneNumber(phoneNumber, this)
        localStorageHelper.saveUserName(userName, this)
        startActivity(intent)
        finish()
    }

    fun signInWithPhoneAuthCredentials(phoneAuthCredentials : PhoneAuthCredential) {
        if (isLoginInitiated)
            return
        isLoginInitiated = true
        mProgressBar.visibility = View.VISIBLE
        logInButton.visibility = View.GONE
        getOTPButton.visibility = View.GONE

        auth.signInWithCredential(phoneAuthCredentials)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Log.e("UPDATE UI", "Firebase Auth Successful $user")
                    firebaseDBHelper.validateUserLogin(this)
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Snackbar.make(findViewById(android.R.id.content), "Login Failed. Please try again", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
    }
}