package com.example.oxygencylindertracker.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.oxygencylindertracker.R
import com.google.android.material.textfield.TextInputLayout

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val phoneNumberEditTetxt = findViewById<TextInputLayout>(R.id.authPhoneNumberText)
        val getOTPButton = findViewById<Button>(R.id.authGetOTPButton)
    }
}