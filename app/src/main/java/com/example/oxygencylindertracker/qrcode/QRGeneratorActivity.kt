package com.example.oxygencylindertracker.qrcode

import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View.VISIBLE
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class QRGeneratorActivity : AppCompatActivity() {

    lateinit var firebaseDBHelper: FirebaseDBHelper
    lateinit var saveQRLayout: LinearLayout
    lateinit var qrIdLayout: LinearLayout
    lateinit var qrIdTextView: TextView
    lateinit var qrCodeImageView: ImageView
    lateinit var cylTypeEditText: EditText
    var bitmap: Bitmap? = null
    var qrId: String = ""

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrgenerator)
        firebaseDBHelper = FirebaseDBHelper()
        qrCodeImageView = findViewById(R.id.qr_code)
        qrIdTextView = findViewById(R.id.qr_id)
        qrIdLayout = findViewById(R.id.qr_id_layout)
        saveQRLayout = findViewById(R.id.save_qr_layout)
        cylTypeEditText = findViewById(R.id.cyl_type)
        val generateQrButton = findViewById<Button>(R.id.generate_qr_button)
        val copyCylIdButton = findViewById<Button>(R.id.copy_cyl_id)
        val downloadQRButton = findViewById<Button>(R.id.save_qr_button)
        val shareQRButton = findViewById<Button>(R.id.share_qr_btn)

        generateQrButton.setOnClickListener{
            generateQRCode()
        }

        copyCylIdButton.setOnClickListener {
            copyCylinderId()
        }

        downloadQRButton.setOnClickListener{
            if (qrId != "") {
                bitmap?.let { saveImageToInternalStorage(it, qrId) }
            }
        }

        shareQRButton.setOnClickListener {
            bitmap?.let { shareQRCode(it)}
        }
    }

    private fun generateQRCode(){
        val sdf = SimpleDateFormat("ddMMyyyy-hhmmss")
        val currDate = Date()
        val currentDate = sdf.format(currDate).toString()
        val cylType = cylTypeEditText.text.toString()
        qrId = cylType + "-" + currentDate
        if (TextUtils.isEmpty(cylType)) {
            showMessage("Enter Cylinder Id to generate QR Code")
        }else {
            try {
                bitmap = getQRBitmap(qrId)
                firebaseDBHelper.addCylinderToDatabase(this, currDate, qrId)
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }
    }

    private fun getQRBitmap(qrId: String): Bitmap?{
        val multiFormatWriter = MultiFormatWriter()
        val bitMatrix = multiFormatWriter.encode(qrId, BarcodeFormat.QR_CODE, 300, 300)
        val barcodeEncoder = BarcodeEncoder()
        bitmap = barcodeEncoder.createBitmap(bitMatrix)
        qrCodeImageView.setImageBitmap(bitmap)
        return bitmap
    }

    fun onQRGenerationSuccess(qrId: String){
        qrIdTextView.text = qrId
        qrIdLayout.visibility = VISIBLE
        saveQRLayout.visibility = VISIBLE
    }

    fun showMessage(message : String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap, cylinderId: String) {
        // TODO: Save it in gallery instead of root level dir
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
        file = File(file, "${cylinderId}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
            Toast.makeText(this, "QR download successful. Path: ${Uri.parse(file.absolutePath)}", Toast.LENGTH_LONG).show()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }
    }

    private fun shareQRCode(bitmap: Bitmap){
        val icon: Bitmap = bitmap
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/jpeg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "title")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        val outstream: OutputStream?
        try {
            outstream = contentResolver.openOutputStream(uri!!)
            icon.compress(Bitmap.CompressFormat.JPEG, 100, outstream)
            outstream!!.close()
        } catch (e: Exception) {
            Log.e("Error", e.toString())
        }

        share.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(share, "Share Image"))
    }

    private fun copyCylinderId(){
        val textToCopy = qrIdTextView.text
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        showMessage("Cylinder ID copied to clipboard")
    }
}
