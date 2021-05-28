package com.example.oxygencylindertracker.dB

import android.content.Context

class LocalStorageHelper {
    private val userNameKey = "userName"
    private val phoneNumberKey = "phoneNumber"

    fun saveUserName (userName : String, context: Context) {
        val sharedPref = context.getSharedPreferences("sf", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(userNameKey,  userName)
            apply()
        }
    }

    fun getUserName (context: Context) : String {
        val sharedPref = context.getSharedPreferences("sf", Context.MODE_PRIVATE) ?: return ""
        return sharedPref.getString(userNameKey, "User") ?: "User"
    }

    fun savePhoneNumber (phoneNumber : String, context: Context) {
        val sharedPref = context.getSharedPreferences("sf", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(phoneNumberKey,  phoneNumber)
            apply()
        }
    }

    fun getPhoneNumber (context: Context) : String {
        val sharedPref = context.getSharedPreferences("sf", Context.MODE_PRIVATE) ?: return ""
        return sharedPref.getString(phoneNumberKey, "") ?: ""
    }
}