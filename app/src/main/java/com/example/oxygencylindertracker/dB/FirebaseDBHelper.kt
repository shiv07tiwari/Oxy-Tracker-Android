package com.example.oxygencylindertracker.dB

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.oxygencylindertracker.auth.SignInActivity
import com.example.oxygencylindertracker.home.HomeActivity
import com.example.oxygencylindertracker.qrcode.QRGeneratorActivity
import com.example.oxygencylindertracker.qrcode.QRScannerActivity
import com.example.oxygencylindertracker.transactions.EntryTransactionActivity
import com.example.oxygencylindertracker.transactions.FormActivity
import com.example.oxygencylindertracker.utils.Citizen
import com.example.oxygencylindertracker.utils.Cylinder
import com.google.android.gms.tasks.Task
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
        private val storageRef = storage.reference
    }

    private val isCitizenKey = "isCitizen"
    private val currentOwnerKey = "current_owner"
    private val timestampKey = "timestamp"
    private val usersDB = "users"
    private val cylindersDB = "cylinders"
    private val cylindersKey = "cylinders"
    private val nameKey = "name"
    private val citizensDB = "citizens"
    private val historyDB = "history"
    private val ownersKey = "owners"
    private val canExitKey = "canExit"

    fun validateUserLogin (activity: SignInActivity) {
        val userPhoneNumber = Firebase.auth.currentUser?.phoneNumber?.removePrefix("+91") ?: ""

        Log.e("Validating Phone Number", " :  $userPhoneNumber")
        db.collection(usersDB).document(userPhoneNumber).get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.data
                Log.e("User Validation DOCS", snapshot.exists().toString())
                when (snapshot.exists()) {
                    false -> {
                        Firebase.auth.signOut()
                        activity.showMessage("You are not authorized. Please contact the Admin")
                    }
                    true -> {
                        val userName = data?.get("name") as String
                        Log.e("Auth Success", "User Validated on Firebase")
                        activity.navigateToHomeScreen(snapshot.id, userName)
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
                                timeStamp.seconds,
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

    fun checkIfExitTransaction (activity: QRScannerActivity, cylinderId : String) {
        val userPhoneNumber = Firebase.auth.currentUser?.phoneNumber?.removePrefix("+91") ?: ""

        db.runTransaction { transaction ->
            val userDocument = db.collection(usersDB).document(userPhoneNumber)
            val userSnapshot = transaction.get(userDocument)
            val canExit = userSnapshot.getBoolean(canExitKey) ?: false

            val cylinderDocument = db.collection(cylindersDB).document(cylinderId)
            val cylinderSnapshot = transaction.get(cylinderDocument)

            if (cylinderSnapshot.exists()) {
                val ownerPhoneNumber = cylinderSnapshot.getString(currentOwnerKey)
                val isExitCase = (ownerPhoneNumber == userPhoneNumber)
                if (isExitCase) {
                    if (!canExit) {
                        throw Exception("Unauthorized to perform Exit Transaction. Please contact Admins")
                    }
                }
                isExitCase
            } else {
                throw Exception("Invalid Cylinder. Please Try Again")
            }
        }.addOnSuccessListener {
            if (it) {
                activity.openExitTransactionScreen(cylinderId)
            } else {
                activity.openEntryTransactionScreen(cylinderId)
            }
        }.addOnFailureListener {
            activity.showMessage(it.message ?: "Unexpected Error. Please try again")
            activity.resumeScanner()
        }
    }

    fun performEntryTransaction (cylinderId: String, activity: EntryTransactionActivity) {
        val userPhoneNumber = Firebase.auth.currentUser?.phoneNumber?.removePrefix("+91") ?: ""
        db.runTransaction {transaction ->
            var isCitizen = true
            val cylinderDocument = db.collection(cylindersDB).document(cylinderId)
            val cylinderSnapshot = transaction.get(cylinderDocument)
            if (!cylinderSnapshot.exists()) {
                throw Exception("Invalid Cylinder ID")
            }
            val currentOwnerId = cylinderSnapshot.getString(currentOwnerKey)
                ?: throw Exception("Current Owner is Null inside Cylinder")

            Log.e("OWNER", currentOwnerId)

            val currentOwnerDocument = db.collection(usersDB).document(currentOwnerId)
            val currentOwnerSnapshot = transaction.get(currentOwnerDocument)
            val newOwnerDocument = db.collection(usersDB).document(userPhoneNumber)
            val historyDocument = db.collection(historyDB).document(cylinderId)
            val historySnapshot = transaction.get(historyDocument)

            if (currentOwnerSnapshot.exists()) {
                // Handling this in case of taking the cylinder from the user and not citizen
                isCitizen = false
                val currentOwnerCylinders = currentOwnerSnapshot.get(cylindersKey) as List<String>
                transaction.update(currentOwnerDocument, cylindersKey, currentOwnerCylinders.filter { it != cylinderId })
            }

            val cylinderStatePast = hashMapOf(
                currentOwnerKey to cylinderSnapshot.getString(currentOwnerKey),
                isCitizenKey to cylinderSnapshot.getBoolean(isCitizenKey),
                timestampKey to cylinderSnapshot.get(timestampKey)
            )

            if (historySnapshot.exists()) {
                transaction.update(historyDocument, ownersKey, FieldValue.arrayUnion(cylinderStatePast))
            } else {
                val newHistoryData = hashMapOf(
                    ownersKey to listOf(cylinderStatePast)
                )
                transaction.set(historyDocument, newHistoryData)
            }

            transaction.update(newOwnerDocument, cylindersKey, FieldValue.arrayUnion(cylinderId))
            transaction.update(cylinderDocument, currentOwnerKey, userPhoneNumber)
            transaction.update(cylinderDocument, isCitizenKey, isCitizen)
            transaction.update(cylinderDocument, timestampKey, getCurrentTimeStamp())

        }.addOnSuccessListener {
            Log.e("performEntryTransaction", "SUCCESS")
            activity.onTransactionSuccess()
        }.addOnFailureListener {
            Log.e("performEntryTransaction", "FAILURE $it")
            activity.onTransactionFailure()
        }
    }

    fun performExitTransaction (cylinderId: String, citizen: Citizen, callback: FormActivity.OnExitTransaction) {

        db.runTransaction { transaction ->
            val cylinderDocument = db.collection(cylindersDB).document(cylinderId)
            val cylinderSnapshot = transaction.get(cylinderDocument)

            if (!cylinderSnapshot.exists()) {
                throw Exception("Invalid Cylinder ID")
            }
            val currentOwnerId = cylinderSnapshot.getString(currentOwnerKey)
                ?: throw Exception("Current Owner is Null inside Cylinder")

            val currentOwnerSnapshot = db.collection(usersDB).document(currentOwnerId)
            val currentOwnerCylinders = transaction.get(currentOwnerSnapshot).get(cylindersKey) as List<String>

            val citizenNewDoc = db.collection(citizensDB).document()

            val citizenData = hashMapOf(
                "address" to citizen.address,
                "imageLink" to citizen.imageLink,
                "name" to citizen.name,
                "phone" to citizen.phone,
                "timestamp" to getCurrentTimeStamp()
            )

            val cylinderStatePast = hashMapOf(
                currentOwnerKey to cylinderSnapshot.getString(currentOwnerKey),
                isCitizenKey to cylinderSnapshot.getBoolean(isCitizenKey),
                timestampKey to cylinderSnapshot.get(timestampKey)
            )

            val historyDocument = db.collection(historyDB).document(cylinderId)
            val historySnapshot = transaction.get(historyDocument)
            if (historySnapshot.exists()) {
                transaction.update(historyDocument, ownersKey, FieldValue.arrayUnion(cylinderStatePast))
            } else {
                val newHistoryData = hashMapOf(
                    ownersKey to listOf(cylinderStatePast)
                )
                transaction.set(historyDocument, newHistoryData)
            }

            transaction.set(citizenNewDoc, citizenData)
            transaction.update(cylinderDocument, currentOwnerKey, citizenNewDoc.id)
            transaction.update(cylinderDocument, timestampKey, getCurrentTimeStamp())
            transaction.update(cylinderDocument, isCitizenKey, true)
            transaction.update(currentOwnerSnapshot, cylindersKey, currentOwnerCylinders.filter { it != cylinderId })

        }.addOnSuccessListener {
            callback.onSuccess()
        }.addOnFailureListener {
            callback.onFailure()
        }
    }

    fun getCurrentHolderName(cylinderId: String, activity: EntryTransactionActivity) {

        db.runTransaction { transaction ->
            val cylinderDocument = db.collection(cylindersDB).document(cylinderId)
            val cylinderSnapshot = transaction.get(cylinderDocument)
            if (!cylinderSnapshot.exists()) {
                throw Exception("Invalid Cylinder ID")
            }

            val isCitizen = cylinderSnapshot.getBoolean(isCitizenKey)
            val currentOwnerId = cylinderSnapshot.getString(currentOwnerKey)
                ?: throw Exception("Current Owner is Null inside Cylinder")

            when (isCitizen) {
                false -> {
                    val currentOwnerSnapshot = db.collection(usersDB).document(currentOwnerId)
                    transaction.get(currentOwnerSnapshot).get(nameKey) as String
                }
                true -> {
                    val currentOwnerSnapshot = db.collection(citizensDB).document(currentOwnerId)
                    val name = transaction.get(currentOwnerSnapshot).get(nameKey) as String
                    "Citizen : $name"
                }
                else -> {
                    throw Exception("UnExpected Error. Please try again")
                }
            }
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
        val imageref = storageRef.child(cylinderId+getCurrentTimeStamp().seconds+".jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = imageref.putBytes(data)
        uploadTask.addOnFailureListener {
            Log.e("IMAGE UPLOAD", it.message.toString())
            callback.onFaliure()

        }.addOnSuccessListener {
            imageref.downloadUrl.addOnSuccessListener {
                Log.e("URL", it.encodedPath.toString())
                callback.onSuccess(imageref.downloadUrl)
            }.addOnFailureListener {
                Log.e("IMAGE URL", it.message.toString())
                callback.onFaliure()
            }

        }
    }

    private fun Timestamp.getDateTime(): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val netDate = Date(this.seconds * 1000)
        return sdf.format(netDate)
    }

    fun addCylinderToDatabase(qrGeneratorActivity: QRGeneratorActivity, timestamp: Date, cylId: String, uri: Task<Uri>) {
        val cylinder = HashMap<String, Any>()
        cylinder["timestamp"] = timestamp
        val currOwner = Firebase.auth.currentUser?.phoneNumber?.removePrefix("+91") ?: return
        cylinder["current_owner"] = currOwner
        cylinder["createdBy"] = currOwner
        cylinder["isCitizen"] = false
        cylinder["imageUrl"] = uri.toString()
        val cylCollection = db.collection("cylinders")
        cylCollection.document(cylId)
            .set(cylinder)
            .addOnSuccessListener {
                qrGeneratorActivity.onQRGenerationSuccess(cylId)
                Log.d("Tag", "Cylinder added successfully!")
            }
            .addOnFailureListener {
                qrGeneratorActivity.showMessage("Error Generating QR. Try again!")
            }

        val userCollection = db.collection("users")
        userCollection.document(currOwner).update("cylinders", FieldValue.arrayUnion(cylId))
    }

    private fun getCurrentTimeStamp(): Timestamp {
        return Timestamp.now()
    }

    fun pushGeneratedQRCodeImage(cylinderId: String, bitmap: Bitmap?, callback: QRGeneratorActivity.OnUploadResult){
        val imageref = storageRef.child("$cylinderId.jpg")
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageref.putBytes(data)
        uploadTask.addOnFailureListener {
            callback.onFaliure()
        }.addOnSuccessListener { taskSnapshot ->
            callback.onSuccess(imageref.downloadUrl)
            taskSnapshot
        }
    }
}