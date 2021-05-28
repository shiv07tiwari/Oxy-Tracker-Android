package com.example.oxygencylindertracker.qrcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.oxygencylindertracker.transactions.EntryTransactionActivity
import me.dm7.barcodescanner.zxing.ZXingScannerView
import com.google.zxing.Result

class QRScannerActivity: Activity(), ZXingScannerView.ResultHandler {
    lateinit var mScannerView: ZXingScannerView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
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
        val intent = Intent(this, EntryTransactionActivity::class.java)
        intent.putExtra("CylinderId", rawResult.text)
        startActivity(intent)
        finish()
    }
}
