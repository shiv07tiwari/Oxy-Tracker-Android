package com.example.oxygencylindertracker.dB

import android.graphics.Bitmap
import android.util.Log
import com.example.oxygencylindertracker.auth.SignInActivity
import com.example.oxygencylindertracker.home.HomeActivity
import com.example.oxygencylindertracker.transactions.EntryTransactionActivity
import com.example.oxygencylindertracker.transactions.FormActivity
import com.example.oxygencylindertracker.utils.Cylinder
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class FirebaseDBHelper  {

    companion object {
        private val db = Firebase.firestore
        private val storage = Firebase.storage("gs://o2-tracker.appspot.com")
    }

    private val isCitizenKey = "isCitizen"
    private val currentOwnerKey = "current_owner"
    private val timestampKey = "timestamp"
    private val usersDB = "users"
    private val cylindersDB = "cylinders"
    private val cylindersKey = "cylinders"
    private val nameKey = "name"

    fun validateUserLogin (activity: SignInActivity) {
        val userPhoneNumber = Firebase.auth.currentUser?.phoneNumber?.removePrefix("+91") ?: ""

        Log.e("Validating Phone Number", " :  $userPhoneNumber")
        db.collection(usersDB).document(userPhoneNumber).get()
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

    fun getCylindersDataForUser (activity: HomeActivity) {
        val userPhoneNumber = Firebase.auth.currentUser?.phoneNumber?.removePrefix("+91") ?: ""
        db.collection(cylindersDB).whereEqualTo(currentOwnerKey, userPhoneNumber).get()
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

    fun checkIfExitTransaction (cylinderId : String) {
        val userPhoneNumber = Firebase.auth.currentUser?.phoneNumber?.removePrefix("+91") ?: ""
        db.collection(cylindersDB).document(cylinderId).get()
            .addOnSuccessListener { snapshot ->
                Log.e("User Validation DOCS", snapshot.exists().toString())
                when (snapshot.exists()) {
                    false -> {
                        Log.e("Cylinder Status", "Invalid Cylinder. Please Try Again")
                    }
                    true -> {
                        val ownerPhoneNumber = snapshot.data?.get("current_owner") ?: ""
                        if (ownerPhoneNumber == userPhoneNumber) {
                            Log.e("Cylinder Status", "Exit Transaction")
                        } else {
                            Log.e("Cylinder Status", "Entry Transaction")
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error getting documents: ", exception)
                Firebase.auth.signOut()
            }
    }

    fun performEntryTransaction (cylinderId: String, activity: EntryTransactionActivity) {
        val userPhoneNumber = Firebase.auth.currentUser?.phoneNumber?.removePrefix("+91") ?: ""
        db.runTransaction {transaction ->

            val cylinderDocument = db.collection(cylindersDB).document(cylinderId)
            val cylinderSnapshot = transaction.get(cylinderDocument)
            if (!cylinderSnapshot.exists()) {
                throw Exception("Invalid Cylinder ID")
            }
            val currentOwnerId = cylinderSnapshot.getString(currentOwnerKey)
                ?: throw Exception("Current Owner is Null inside Cylinder")

            Log.e("OWNER", currentOwnerId)

            val currentOwnerSnapshot = db.collection(usersDB).document(currentOwnerId)
            val currentOwnerCylinders = transaction.get(currentOwnerSnapshot).get(cylindersKey) as List<String>
            val newOwnerSnapshot = db.collection(usersDB).document(userPhoneNumber)

            transaction.update(currentOwnerSnapshot, cylindersKey, currentOwnerCylinders.filter { it != cylinderId })
            transaction.update(newOwnerSnapshot, cylindersKey, FieldValue.arrayUnion(cylinderId))
            transaction.update(cylinderDocument, currentOwnerKey, userPhoneNumber)
            transaction.update(cylinderDocument, timestampKey, getCurrentTimeStamp())

        }.addOnSuccessListener {
            Log.e("performEntryTransaction", "SUCCESS")
            activity.onTransactionSuccess()
        }.addOnFailureListener {
            Log.e("performEntryTransaction", "FAILURE $it")
            activity.onTransactionFailure()
        }
    }

    fun getCurrentHolderName(cylinderId: String, activity: EntryTransactionActivity) {

        db.runTransaction { transaction ->
            val cylinderDocument = db.collection(cylindersDB).document(cylinderId)
            val cylinderSnapshot = transaction.get(cylinderDocument)
            if (!cylinderSnapshot.exists()) {
                throw Exception("Invalid Cylinder ID")
            }
            val currentOwnerId = cylinderSnapshot.getString(currentOwnerKey)
                ?: throw Exception("Current Owner is Null inside Cylinder")
            val currentOwnerSnapshot = db.collection(usersDB).document(currentOwnerId)
            transaction.get(currentOwnerSnapshot).get(nameKey) as String

        }.addOnSuccessListener {
            if (it.isNullOrEmpty()) {
                activity.showUserErrorMessage("Unexpected Error. Please try again")
            } else {
                activity.displayData(it)
            }
        }.addOnFailureListener {
            activity.showUserErrorMessage("Unexpected Error. Please try again")
        }

    }

     fun pushReciptImage(cylinderId: String, bitmap: Bitmap, callback: FormActivity.OnUploadResult){
        val storageRef = storage.reference
         //todo check later for better file name
        val imageref = storageRef.child(cylinderId+getCurrentTimeStamp()+".jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = imageref.putBytes(data)
        uploadTask.addOnFailureListener {
            callback.onFaliure()
        }.addOnSuccessListener { taskSnapshot ->
            callback.onSuccess(imageref.downloadUrl)
            taskSnapshot
        }


    }
    private fun Timestamp.getDateTime(): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val netDate = Date(this.seconds * 1000)
        return sdf.format(netDate)
    }

    private fun getCurrentTimeStamp(): Timestamp {
        return Timestamp.now()
    }
}