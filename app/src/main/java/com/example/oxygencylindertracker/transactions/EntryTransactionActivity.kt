package com.example.oxygencylindertracker.transactions

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.example.oxygencylindertracker.home.HomeActivity

class EntryTransactionActivity : AppCompatActivity() {

    var firebaseDBHelper = FirebaseDBHelper()
    lateinit var cylinderIdTxtView : TextView
    lateinit var currentOwnerNameTxtView : TextView
    lateinit var progressBar : ProgressBar
    lateinit var confirmBtn : Button
    lateinit var entryDetailsLL : LinearLayout
    lateinit var cylinderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_transaction)
        cylinderId = intent.getStringExtra("cylinderId").toString()
        cylinderIdTxtView = findViewById(R.id.cylinderIDTxt)
        currentOwnerNameTxtView = findViewById(R.id.currentHolderNameTxt)
        progressBar = findViewById(R.id.entryProgressBar)
        confirmBtn = findViewById(R.id.confirmBtn)
        entryDetailsLL = findViewById(R.id.entryDetailsLL)

        entryDetailsLL.visibility = View.GONE
        confirmBtn.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        firebaseDBHelper.getCurrentHolderName(cylinderId, this)

        confirmBtn.setOnClickListener {
            confirmBtn.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            firebaseDBHelper.performEntryTransaction(cylinderId, this)
        }

    }

    fun displayData(currentHolderName : String) {
        cylinderIdTxtView.text = cylinderId
        currentOwnerNameTxtView.text = currentHolderName
        entryDetailsLL.visibility = View.VISIBLE
        confirmBtn.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    fun onTransactionSuccess () {
        Toast.makeText(applicationContext, "Success. You have the custody of the cylinder", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun onTransactionFailure() {
        Toast.makeText(this, "Failure. Please try again", Toast.LENGTH_SHORT).show()
        confirmBtn.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    fun showUserErrorMessage(message : String) {
        progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}