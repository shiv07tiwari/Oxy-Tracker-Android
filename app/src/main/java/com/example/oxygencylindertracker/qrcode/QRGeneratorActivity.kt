package com.example.oxygencylindertracker.qrcode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View.VISIBLE
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.oxygencylindertracker.R

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class QRGeneratorActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrgenerator)

        val qrCodeImageView = findViewById<ImageView>(R.id.qr_code)
        val qrIdTextView = findViewById<TextView>(R.id.qr_id)
        val qrIdLayout = findViewById<LinearLayout>(R.id.qr_id_layout)
        val saveQRLayout = findViewById<LinearLayout>(R.id.save_qr_layout)
        val cylTypeEditText = findViewById<EditText>(R.id.cyl_type)
        val generateQrButton = findViewById<Button>(R.id.generate_qr_button)
        val copyCylIdButton = findViewById<Button>(R.id.copy_cyl_id)
        val downloadQRButton = findViewById<Button>(R.id.save_qr_button)

        var bitmap: Bitmap? = null
        var qrId = ""

        generateQrButton.setOnClickListener{

            val sdf = SimpleDateFormat("ddMMyyyy-hhmmss")
            val currentDate = sdf.format(Date()).toString()
            qrId = cylTypeEditText.text.toString() + "-" + currentDate
            if (TextUtils.isEmpty(qrId)) {
                Toast.makeText(applicationContext,
                    "Enter Cylinder Id to generate QR Code",
                    Toast.LENGTH_SHORT).show();
            }else {
                val multiFormatWriter = MultiFormatWriter()
                try {
                    val bitMatrix = multiFormatWriter.encode(qrId, BarcodeFormat.QR_CODE, 300, 300)
                    val barcodeEncoder = BarcodeEncoder()
                    bitmap = barcodeEncoder.createBitmap(bitMatrix)
                    qrCodeImageView.setImageBitmap(bitmap)
                    qrIdTextView.text = qrId
                    qrIdLayout.visibility = VISIBLE
                    copyCylIdButton.visibility = VISIBLE
                    saveQRLayout.visibility = VISIBLE
                } catch (e: WriterException) {
                    e.printStackTrace()
                }
            }
        }

        copyCylIdButton.setOnClickListener {
            val textToCopy = qrIdTextView.text
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", textToCopy)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Cylinder ID copied to clipboard", Toast.LENGTH_LONG).show()
        }

        downloadQRButton.setOnClickListener{
            bitmap?.let { saveImageToInternalStorage(it, qrId) }
        }

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
}
