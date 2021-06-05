package com.example.oxygencylindertracker.transactions

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.example.oxygencylindertracker.home.HomeActivity
import com.example.oxygencylindertracker.utils.Citizen
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_form.*


class FormActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var submitBtn: Button
    private lateinit var customerName: TextInputLayout
    private lateinit var contactNumber: TextInputLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var address: TextInputLayout
    private lateinit var cylinderIdTextView: TextView
    private  var imageSet: Boolean = false
    private val MY_CAMERA_PERMISSION_CODE = 101
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 102
    private val CAMERA_REQUEST = 102
    private lateinit var firebaseDBHelper: FirebaseDBHelper
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri
    private val context = this
    lateinit var cylinderId: String

    interface OnUploadResult{
        fun onSuccess(path : String)
        fun onFaliure()
    }

    interface OnExitTransaction {
        fun onSuccess()
        fun onFailure()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cylinderId = intent.getStringExtra("cylinderId").toString()
        firebaseDBHelper = FirebaseDBHelper()
        setContentView(R.layout.activity_form)
        imageView = findViewById(R.id.reciptImage)
        submitBtn = findViewById(R.id.submitBtn);
        customerName = findViewById(R.id.customerName)
        contactNumber= findViewById(R.id.contactNumber)
        progressBar = findViewById(R.id.exitProgressBar)
        progressBar.visibility = View.GONE
        address = findViewById(R.id.address)
        cylinderIdTextView = findViewById(R.id.cylinder_id)
        cylinderIdTextView.text = "Cylinder ID: $cylinderId"


        imageConstraint.setOnClickListener {
            if(checkAndRequestPermissions()){
                startCameraIntent()
            }
        }


        submitBtn.setOnClickListener {
            checkInputs()
        }
    }

    private fun startCameraIntent(){
        var values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        //todo handle return later
        imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        ) ?: return
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    private fun checkInputs(){

        if(customerName.editText?.text !=null && customerName.editText?.text!!.isEmpty()!!){
            customerName.requestFocus()
            Toast.makeText(this, "This field cannot remain empty", Toast.LENGTH_LONG).show()
        }else if(contactNumber.editText?.text!=null && contactNumber.editText?.text!!.isEmpty()){
            contactNumber.requestFocus()
            Toast.makeText(this, "This field cannot remain empty", Toast.LENGTH_LONG).show()
        }else if(address.editText?.text!=null && address.editText?.text!!.isEmpty()){
            address.requestFocus()
            Toast.makeText(this, "This field cannot remain empty", Toast.LENGTH_LONG).show()
        }else if(!imageSet){
            Toast.makeText(this, "Receipt image not taken", Toast.LENGTH_LONG).show()
        }else if(contactNumber.editText?.text?.length !=10 || !android.util.Patterns.PHONE.matcher(contactNumber.editText?.text).matches()){
            contactNumber.requestFocus()
            if(!android.util.Patterns.PHONE.matcher(contactNumber.editText?.text).matches()){
                Toast.makeText(this, "Invalid contact number", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "Number should be of 10 digits", Toast.LENGTH_LONG).show()
            }
        } else {
            progressBar.visibility = View.VISIBLE
            submitBtn.visibility = View.GONE
            uploadReceiptImage()
        }
    }

    fun uploadReceiptImage(){
        firebaseDBHelper.pushReceiptImage(cylinderIdTextView.text.toString(),
            imageBitmap, object: OnUploadResult{
                override fun onSuccess(path: String) {
                    Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show()

                    val citizen = Citizen(
                        address.editText?.text.toString(),
                        customerName.editText?.text.toString(),
                        imageLink = path,
                        phone = contactNumber.editText?.text.toString()
                    )

                    firebaseDBHelper.performExitTransaction(cylinderId, citizen, object : OnExitTransaction {
                        override fun onSuccess() {
                            Toast.makeText(context, "Success! Cylinder handed over to Citizen", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(context, HomeActivity::class.java))
                            finish()
                        }

                        override fun onFailure() {
                            progressBar.visibility = View.GONE
                            submitBtn.visibility = View.VISIBLE
                            Toast.makeText(context, "Unexpected Error. Please Try Again.", Toast.LENGTH_SHORT).show()
                        }

                    })

                }
                override fun onFaliure() {
                    progressBar.visibility = View.GONE
                    submitBtn.visibility = View.VISIBLE
                    Toast.makeText(context, "Upload failed. Please try again", Toast.LENGTH_SHORT).show()
                }

            })
    }


//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
//                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                startActivityForResult(cameraIntent, CAMERA_REQUEST)
//            } else {
//                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
//            }
//        }
//    }


    private fun checkAndRequestPermissions(): Boolean {
        val camPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        val storagePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (camPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {
                val perms: MutableMap<String, Int> = HashMap()
                perms[Manifest.permission.SEND_SMS] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.ACCESS_FINE_LOCATION] = PackageManager.PERMISSION_GRANTED

                if (grantResults.isNotEmpty()) {
                    var i = 0
                    while (i < permissions.size) {
                        perms[permissions[i]] = grantResults[i]
                        i++
                    }

                    if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(this, "permissions granted", Toast.LENGTH_LONG).show()
                        startCameraIntent()
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.CAMERA
                            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        ) {
                            Toast.makeText(
                                this,
                                "Permissions not allowed",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === CAMERA_REQUEST && resultCode === Activity.RESULT_OK) {

            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(
                    this.contentResolver, imageUri
                )
                imageView2.visibility = View.GONE
                textView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                imageView.requestLayout()
                imageView.layoutParams.height = convertdpToPx(240)
                imageView.setImageBitmap(imageBitmap)
                imageSet = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun convertdpToPx(dpVal: Int): Int{
        val scale = resources.displayMetrics.density
        val dpHeightInPx = (dpVal * scale).toInt()
        return dpHeightInPx
    }
}