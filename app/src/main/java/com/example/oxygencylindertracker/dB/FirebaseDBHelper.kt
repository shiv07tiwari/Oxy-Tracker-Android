package com.example.oxygencylindertracker.dB

import android.util.Log
import com.example.oxygencylindertracker.auth.SignInActivity
import com.example.oxygencylindertracker.home.HomeActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirebaseDBHelper  {

    companion object {
        private val db = Firebase.firestore
    }

    fun validateUserLogin(phoneNumber : String?, activity: SignInActivity) {
        Log.e("Validating Phone Number", " :  $phoneNumber")
        db.collection("users").whereEqualTo("phone_number", phoneNumber).get()
            .addOnSuccessListener { documents ->
                when (documents.isEmpty) {
                    true -> {
                        activity.showSnackBar("You are not authorized. Please contact the Admin")
                    }
                    false -> {

                        Log.e("Auth Success", "User Validated on Firebase")
                        activity.navigateToHomeScreen()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error getting documents: ", exception)
                activity.showSnackBar("Login Failed. Please try again")
            }
    }

    fun getCylindersDataForUser(phoneNumber: String?, activity: HomeActivity) {
        db.collection("users").whereEqualTo("phone_number", phoneNumber).get()
            .addOnSuccessListener { documents ->
                Log.e("gg", documents.toString())
                if (documents.isEmpty) {

                } else {
                    Log.e("gg", documents.documents[0].data.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error getting documents: ", exception)
            }
    }
}