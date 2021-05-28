package com.example.oxygencylindertracker.qrcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.example.oxygencylindertracker.transactions.EntryTransactionActivity
import com.example.oxygencylindertracker.transactions.FormActivity
import com.example.oxygencylindertracker.utils.Cylinder
import me.dm7.barcodescanner.zxing.ZXingScannerView
import com.google.zxing.Result

class QRScannerActivity: Activity(), ZXingScannerView.ResultHandler {
    lateinit var mScannerView: ZXingScannerView
    lateinit var firebaseDBHelper: FirebaseDBHelper

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        firebaseDBHelper = FirebaseDBHelper()
        mScannerView = ZXingScannerView(this) // Programmatically initialize the scanner view
        mScannerView.setAutoFocus(true)
        setContentView(mScannerView) // Set the scanner view as the content view
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView.startCamera() // Start camera on resume
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera() // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        Toast.makeText(this, rawResult.text, Toast.LENGTH_SHORT).show()
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
