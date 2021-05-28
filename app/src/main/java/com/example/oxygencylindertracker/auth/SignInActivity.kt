package com.example.oxygencylindertracker.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
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

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks () {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            Log.i("AUTH_MESSAGE", "Verification Completed")
            signInWithPhoneAuthCredentials(p0)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("AUTH_MESSAGE", "onVerificationFailed", e)

            when (e) {
                is FirebaseAuthInvalidCredentialsException -> { }
                is FirebaseTooManyRequestsException -> { }
                else -> { }
            }
            Snackbar.make(findViewById(android.R.id.content), "Login Failed. Please Try Again", Snackbar.LENGTH_SHORT)
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
        val phoneNumberEditText = findViewById<TextInputLayout>(R.id.authPhoneNumberText)
        val getOTPButton = findViewById<Button>(R.id.authGetOTPButton)

        getOTPButton.setOnClickListener {
            authenticateUser(phoneNumberEditText.editText?.text.toString())
        }
    }

    private fun authenticateUser (phoneNumber : String) {
        Log.e("PHONE NUMBER", phoneNumber)
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun showMessage(message : String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun navigateToHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    fun signInWithPhoneAuthCredentials(phoneAuthCredentials : PhoneAuthCredential) {
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