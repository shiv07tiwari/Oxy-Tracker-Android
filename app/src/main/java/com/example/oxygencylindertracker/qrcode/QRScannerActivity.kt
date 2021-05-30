package com.example.oxygencylindertracker.qrcode

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.example.oxygencylindertracker.transactions.EntryTransactionActivity
import com.example.oxygencylindertracker.transactions.FormActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class QRScannerActivity: AppCompatActivity(), ZXingScannerView.ResultHandler {
    lateinit var mScannerView: ZXingScannerView
    lateinit var firebaseDBHelper: FirebaseDBHelper
    private val MY_CAMERA_PERMISSION_CODE = 101
    private val CAMERA_REQUEST = 102
    lateinit var contentFrame: ViewGroup
    lateinit var scanQRButton: MaterialButton
    lateinit var openManualQRIdViewButton: Button
    lateinit var mManualQRIdView: View
    lateinit var manualQRIdSubmitBtn: MaterialButton
    lateinit var manualQRIdEditText: TextInputLayout

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_qrscanner)
        firebaseDBHelper = FirebaseDBHelper()
        scanQRButton = findViewById(R.id.scan_qr_button)
        openManualQRIdViewButton = findViewById(R.id.enter_id_btn)
        mManualQRIdView = getManualQRIdView()
        manualQRIdEditText = mManualQRIdView.findViewById(R.id.enter_qr_id_text)
        manualQRIdSubmitBtn = mManualQRIdView.findViewById(R.id.enter_qr_id_button)
        mScannerView = ZXingScannerView(this) // Programmatically initialize the scanner view
        mScannerView.setAutoFocus(true)

        contentFrame = findViewById<View>(R.id.content_frame) as ViewGroup
        setScannerInFrame()

        scanQRButton.setOnClickListener {
            setScannerInFrame()
        }

        openManualQRIdViewButton.setOnClickListener {
            setManualEditIdScreenInFrame()
        }

        manualQRIdSubmitBtn.setOnClickListener {
            val cylinderId = manualQRIdEditText.editText?.text.toString()
            if (cylinderId == ""){
                showMessage("Please enter Cylinder Id to proceed!")
            }else {
                processCylinderTransaction(cylinderId)
            }
        }
    }

    fun getManualQRIdView(): View{
        val mainLayout = findViewById<ViewGroup?>(android.R.id.content)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.activity_qrmanual, mainLayout, false)
        return view
    }

    fun setManualEditIdScreenInFrame(){
        contentFrame.removeView(mScannerView)
        contentFrame.addView(mManualQRIdView)
        openManualQRIdViewButton.visibility = View.GONE
        scanQRButton.visibility = View.VISIBLE
    }

    fun setScannerInFrame(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                MY_CAMERA_PERMISSION_CODE
            )
        } else {
            addScannerViewInFrame()
        }
    }

    fun addScannerViewInFrame(){
        mScannerView.startCamera()
        contentFrame.removeView(mManualQRIdView)
        contentFrame.addView(mScannerView) // Set the scanner view as the content view
        scanQRButton.visibility = View.GONE
        openManualQRIdViewButton.visibility = View.VISIBLE
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
                addScannerViewInFrame()
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
        showMessage("Please Wait..")
        processCylinderTransaction(rawResult.text)
    }

    fun showMessage(message : String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun openExitTransactionScreen(cylinderId: String){
        val intent = Intent(this, FormActivity::class.java)
        intent.putExtra("cylinderId", cylinderId)
        startActivity(intent)
        finish()
    }

    fun openEntryTransactionScreen(cylinderId: String){
        val intent = Intent(this, EntryTransactionActivity::class.java)
        intent.putExtra("cylinderId", cylinderId)
        startActivity(intent)
        finish()
    }

    fun resumeScanner(){
        mScannerView.resumeCameraPreview(this)
    }

    fun processCylinderTransaction(cylinderId: String){
        firebaseDBHelper.checkIfExitTransaction(this, cylinderId)
    }
}
