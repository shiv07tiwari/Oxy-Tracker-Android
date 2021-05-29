package com.example.oxygencylindertracker.qrcode

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.example.oxygencylindertracker.transactions.EntryTransactionActivity
import com.example.oxygencylindertracker.transactions.FormActivity
import com.example.oxygencylindertracker.utils.Cylinder
import me.dm7.barcodescanner.zxing.ZXingScannerView
import com.google.zxing.Result

class QRScannerActivity: Activity(), ZXingScannerView.ResultHandler {
    lateinit var mScannerView: ZXingScannerView
    lateinit var firebaseDBHelper: FirebaseDBHelper
    private val MY_CAMERA_PERMISSION_CODE = 101
    private val CAMERA_REQUEST = 102

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        firebaseDBHelper = FirebaseDBHelper()
        mScannerView = ZXingScannerView(this) // Programmatically initialize the scanner view
        mScannerView.setAutoFocus(true)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                MY_CAMERA_PERMISSION_CODE
            )

        } else {
            setContentView(mScannerView) // Set the scanner view as the content view
        }
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView.startCamera() // Start camera on resume
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
                setContentView(mScannerView)
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera() // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        Toast.makeText(this, "Please Wait..", Toast.LENGTH_SHORT).show()
        firebaseDBHelper.checkIfExitTransaction(this, rawResult.text)
    }

    fun showMessage(message : String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun openExitTransactionScreen(cylinderId: String){
        val intent = Intent(this, FormActivity::class.java)
        intent.putExtra("cylinderId", cylinderId)
        startActivity(intent)
    }

    fun openEntryTransactionScreen(cylinderId: String){
        val intent = Intent(this, EntryTransactionActivity::class.java)
        intent.putExtra("cylinderId", cylinderId)
        startActivity(intent)
    }

    fun resumeScanner(){
        mScannerView.resumeCameraPreview(this)
    }
}
