package com.example.oxygencylindertracker.home

import android.Manifest
import android.app.Activity
import android.bluetooth.le.AdvertiseData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.oxygencylindertracker.R
import com.google.android.material.textfield.TextInputLayout
import java.net.Inet4Address


class FormActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var submitBtn: Button
    private lateinit var customerName: TextInputLayout
    private lateinit var contactNumber: TextInputLayout
    private lateinit var address: TextInputLayout
    private lateinit var picText: TextView
    private  var imageSet: Boolean = false
    private val MY_CAMERA_PERMISSION_CODE = 101
    private val CAMERA_REQUEST = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_form)
        imageView = findViewById(R.id.reciptImage)
        submitBtn = findViewById(R.id.submitBtn);
        customerName = findViewById(R.id.customerName)
        contactNumber= findViewById(R.id.contactNumber)
        address = findViewById(R.id.address)
        picText = findViewById(R.id.textView5)

        imageView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    MY_CAMERA_PERMISSION_CODE
                )

            } else {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }
        }

        submitBtn.setOnClickListener {
            checkInputs()
        }
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
        }else if(!android.util.Patterns.PHONE.matcher(contactNumber.editText?.text).matches()){
            contactNumber.requestFocus()
            Toast.makeText(this, "Invalid contact number", Toast.LENGTH_LONG).show()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === CAMERA_REQUEST && resultCode === Activity.RESULT_OK) {
            imageView.setImageBitmap(data!!.extras!!.get("data") as Bitmap?)
            imageSet = true
            picText.visibility = View.GONE
        }
    }
}