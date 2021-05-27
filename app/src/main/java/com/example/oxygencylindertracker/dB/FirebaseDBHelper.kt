package com.example.oxygencylindertracker.dB

import android.util.Log
import com.example.oxygencylindertracker.auth.SignInActivity
import com.example.oxygencylindertracker.home.HomeActivity
import com.example.oxygencylindertracker.utils.Cylinder
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class FirebaseDBHelper  {

    companion object {
        private val db = Firebase.firestore
    }

    private val isCitizenKey = "isCitizen"
    private val currentOwnerKey = "current_owner"
    private val timestampKey = "timestamp"
    private val usersDB = "users"
    private val cylindersDB = "cylinders"

    fun validateUserLogin(phoneNumber : String?, activity: SignInActivity) {
        Log.e("Validating Phone Number", " :  $phoneNumber")
        db.collection(usersDB).document(phoneNumber?: "").get()
            .addOnSuccessListener { snapshot ->
                Log.e("User Validation DOCS", snapshot.exists().toString())
                when (snapshot.exists()) {
                    false -> {
                        Firebase.auth.signOut()
                        activity.showMessage("You are not authorized. Please contact the Admin")
                    }
                    true -> {
                        Log.e("Auth Success", "User Validated on Firebase")
                        activity.navigateToHomeScreen()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error getting documents: ", exception)
                Firebase.auth.signOut()
                activity.showMessage("Login Failed. Please try again")
            }
    }

    fun getCylindersDataForUser(phoneNumber: String?, activity: HomeActivity) {
        db.collection(cylindersDB).whereEqualTo(currentOwnerKey, phoneNumber?: "").get()
            .addOnSuccessListener { documents ->
                val cylinders = mutableListOf<Cylinder>()
                documents.documents.map { snapshot ->
                    val data = snapshot.data
                    if (data != null) {
                        val timeStamp = data[timestampKey] as Timestamp
                        val isCitizen = data[isCitizenKey] as Boolean
                        cylinders.add(
                            Cylinder(
                                snapshot.id,
                                data["current_owner"].toString(),
                                timeStamp.getDateTime(),
                                snapshot.id[0].toString(),
                                isCitizen
                            ))
                    }
                }
                Log.e("Cylinders", cylinders.toString())
                activity.displayCylinderList(cylinders)
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error getting documents: ", exception)
                activity.showMessage("Error fetching Cylinders. Please try again later")
                activity.displayEmptyList()
            }
    }

    private fun Timestamp.convertTimeStampToString() : String {
        return this.toString()
    }


    private fun Timestamp.getDateTime(): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val netDate = Date(this.seconds * 1000)
        return sdf.format(netDate)
    }
}