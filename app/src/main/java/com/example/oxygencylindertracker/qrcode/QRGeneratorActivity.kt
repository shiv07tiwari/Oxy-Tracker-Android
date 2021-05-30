package com.example.oxygencylindertracker.qrcode

import android.content.*
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.activity_qrgenerator.*


class QRGeneratorActivity : AppCompatActivity() {

    lateinit var firebaseDBHelper: FirebaseDBHelper
    lateinit var saveQRLayout: LinearLayout
    lateinit var qrIdLayout: LinearLayout
    lateinit var typeLayout: LinearLayout
    lateinit var qrIdTextView: TextView
    lateinit var qrCodeImageView: ImageView
    lateinit var cylTypeEditText: EditText
    lateinit var qrGeneratorProgressBar: ProgressBar
    lateinit var generateQrButton: Button
    lateinit var generateNewQrButton: Button
    lateinit var qrCodeLayout: ConstraintLayout

    var bitmap: Bitmap? = null
    var qrId: String = ""
    private var allOptionsList = mutableListOf<String>("B", "D")

    interface OnUploadResult{
        fun onSuccess(path: String)
        fun onFaliure()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrgenerator)
        firebaseDBHelper = FirebaseDBHelper()
        qrCodeImageView = findViewById(R.id.qr_code)
        qrIdTextView = findViewById(R.id.qr_id)
        qrIdLayout = findViewById(R.id.qr_id_layout)
        saveQRLayout = findViewById(R.id.save_qr_layout)
//        cylTypeEditText = findViewById(R.id.cyl_type)
        generateQrButton = findViewById(R.id.generate_qr_button)
        generateNewQrButton = findViewById(R.id.new_qr_btn)
        qrGeneratorProgressBar = findViewById(R.id.qrGeneratorProgressBar)
        typeLayout = findViewById(R.id.type_layout)
        qrCodeLayout = findViewById(R.id.qr_code_layout)
        val copyCylIdButton = findViewById<Button>(R.id.copy_cyl_id)
        val downloadQRButton = findViewById<Button>(R.id.save_qr_button)
        val shareQRButton = findViewById<Button>(R.id.share_qr_btn)

        checkIfCanGenerateQR()

        generateQrButton.setOnClickListener{
            generateQRCode()
        }

        copyCylIdButton.setOnClickListener {
            copyCylinderId()
        }

        downloadQRButton.setOnClickListener{
            if (qrId != "") {
                bitmap?.let { saveImageToExternal(qrId, it) }
            }
        }

        shareQRButton.setOnClickListener {
            bitmap?.let { shareQRCode(it)}
        }

        generateNewQrButton.setOnClickListener {
            recreate()
        }

        optionEditText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                optionSpinnerView.performClick()
            }
        }

        optionEditText.showSoftInputOnFocus = false

        optionSpinnerView.onAnyItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                optionEditText.clearFocus()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                optionEditText.setText(allOptionsList?.get(position))
                optionEditText.clearFocus()
            }
        }
        optionSpinnerView.adapter = OptionsSpinnerAdapter(this, allOptionsList)


    }

    fun checkIfCanGenerateQR(){
        qrGeneratorProgressBar.visibility = VISIBLE
        firebaseDBHelper.checkIfCanGenerateQR(this)
    }

    fun canGenerateQR(){
        qrGeneratorProgressBar.visibility = View.GONE
        qrCodeLayout.visibility = VISIBLE
    }

    fun cannotGenerateQR(){
        qrGeneratorProgressBar.visibility = View.GONE
        showMessage("Unauthorized to Generate QR. Please contact Admins")
        finish()
    }

    private fun generateQRCode(){
        val sdf = SimpleDateFormat("ddMMyyyy-hhmmss")
        val currDate = Date()
        val currentDate = sdf.format(currDate).toString()
        val cylType = optionEditText.text.toString()
        if (cylType != "B" && cylType != "D") {
            showMessage("Invalid Cylinder Type. Please Add B / D")
            return
        }
        qrId = cylType + "-" + currentDate
        if (TextUtils.isEmpty(cylType)) {
            showMessage("Enter Cylinder Id to generate QR Code")
        }else {
            try {
                bitmap = getQRBitmap(qrId)
                beforeQRScan()
                uploadQRCodeImage(bitmap, currDate)
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
        return bitmap
    }

    fun onQRGenerationSuccess(qrId: String){
        qrGeneratorProgressBar.visibility = View.GONE
        qrIdTextView.text = qrId
        qrIdLayout.visibility = VISIBLE
        saveQRLayout.visibility = VISIBLE
        generateNewQrButton.visibility = VISIBLE
        optionEditText.text  = null
        typeLayout.visibility = View.GONE
        qrCodeImageView.setImageBitmap(bitmap)
    }

    fun showMessage(message : String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun saveImageToExternal(imgName: String, bm: Bitmap) {
        val savedImageURL = MediaStore.Images.Media.insertImage(
            contentResolver,
            bm,
            imgName,
            "Image of $title"
        )
        Log.e("IMAGE", savedImageURL)
        showMessage("QR Downloaded and saved to gallery!")
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

    private fun uploadQRCodeImage(bitmap: Bitmap?, currDate: Date){
        val context = this
        firebaseDBHelper.pushGeneratedQRCodeImage(qrId,
            bitmap, object: OnUploadResult {
                override fun onSuccess(path : String) {
                    firebaseDBHelper.addCylinderToDatabase(context, currDate, qrId, path)
                }
                override fun onFaliure() {
                    showMessage("Some error occurred. Please try again")
                }

            })
    }

    fun beforeQRScan(){
        qrGeneratorProgressBar.visibility = VISIBLE
        generateQrButton.visibility = View.GONE
        qrIdLayout.visibility = View.GONE
        saveQRLayout.visibility = View.GONE
        generateNewQrButton.visibility = View.GONE
    }

    inner class OptionsSpinnerAdapter(private var mContext: Context, private val list : List<String>) : ArrayAdapter<String>(mContext, R.layout.list_item_spinner_drop_down, list) {
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(mContext).inflate(R.layout.list_item_spinner_drop_down,parent, false)
            if(view is TextView) {
                view.text = list[position]
            }
            return view
        }
    }
}
