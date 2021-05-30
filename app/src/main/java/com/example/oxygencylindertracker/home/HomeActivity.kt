package com.example.oxygencylindertracker.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.auth.SignInActivity
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.example.oxygencylindertracker.dB.LocalStorageHelper
import com.example.oxygencylindertracker.qrcode.QRGeneratorActivity
import com.example.oxygencylindertracker.qrcode.QRScannerActivity
import com.example.oxygencylindertracker.utils.Cylinder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity() {

    lateinit var mRecyclerView: RecyclerView
    private lateinit var totalCylindersText : TextView
    lateinit var  mAdapter: CylinderAdapter
    lateinit var mLayoutManager: RecyclerView.LayoutManager
    lateinit var mProgressBar : ProgressBar
    lateinit var firebaseDBHelper : FirebaseDBHelper
    lateinit var searchEditText : EditText
    lateinit var filtersLL : LinearLayout
    lateinit var scanQRButton : Button
    var cylinders : List<Cylinder> = listOf()
    lateinit var localStorageHelper: LocalStorageHelper
    lateinit var sortBy: AppCompatAutoCompleteTextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        firebaseDBHelper = FirebaseDBHelper()
        localStorageHelper = LocalStorageHelper()

        mProgressBar = findViewById(R.id.homeProgressBar)
        totalCylindersText = findViewById(R.id.homeCylinderText)

        mRecyclerView = findViewById(R.id.homeCylinderRV)

        searchEditText = findViewById(R.id.editTextSearch)
        filtersLL = findViewById(R.id.filtersLL)
        scanQRButton = findViewById(R.id.homeScanQRBtn)
//        sortBy = findViewById(R.id.sortby)


        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager

        fetchCylindersData()

        scanQRButton = findViewById(R.id.homeScanQRBtn)
        scanQRButton.setOnClickListener {
            startActivity(Intent(this, QRScannerActivity::class.java))
        }

        val customerList= mutableListOf<String>("ID","Date")

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            customerList)

    }

    fun showPopupSort(v: View) {
        val popup = PopupMenu(this, v)
        val inflater: MenuInflater = popup.menuInflater
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.date -> {
                    mAdapter.filterList(cylinders.sortedBy { it.timestamp })
                }
                R.id.id -> {
                    mAdapter.filterList(cylinders.sortedBy { it.id })
                }
                else -> super.onOptionsItemSelected(it)
            }
            true
        }

        inflater.inflate(R.menu.sort_menu, popup.menu)
        popup.show()
    }

    fun showPopup(v: View) {
        val popup = PopupMenu(this, v)
        val inflater: MenuInflater = popup.menuInflater
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.generate_qr -> {
                    val intent = Intent(this, QRGeneratorActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.log_out -> {
                    Firebase.auth.signOut()
                    val intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> super.onOptionsItemSelected(it)
            }
            true
        }

        inflater.inflate(R.menu.home_menu, popup.menu)
        popup.show()
    }



    private fun fetchCylindersData() {
        mRecyclerView.visibility = View.GONE
        mProgressBar.visibility = View.VISIBLE
        totalCylindersText.visibility = View.GONE
        filtersLL.visibility = View.GONE
        searchEditText.visibility = View.GONE
        firebaseDBHelper.getCylindersDataForUser(this)
    }

    fun displayCylinderList (cylinders: List<Cylinder>) {

        this.cylinders = cylinders
        totalCylindersText.text = "Welcome ${localStorageHelper.getUserName(this)}! You own ${cylinders.size} Cylinder(s)"

        mAdapter = CylinderAdapter(cylinders)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.visibility = View.VISIBLE
        mProgressBar.visibility = View.GONE
        totalCylindersText.visibility = View.VISIBLE
        filtersLL.visibility = View.VISIBLE
        searchEditText.visibility = View.VISIBLE

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(
                charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                filter(editable.toString())
            }
        })

        // Testing code
        //firebaseDBHelper.checkIfExitTransaction("A-27052021-102659")
        //firebaseDBHelper.performEntryTransaction("B-28052021-175552")
    }

    private fun filter(text: String) {
        if (text.isEmpty())
            mAdapter.filterList(this.cylinders)
        mAdapter.filterList(this.cylinders.filter { it.id.contains(text, true) })
    }

//    fun onRadioButtonClicked(view: View) {
//        if (view is RadioButton) {
//            val checked = view.isChecked
//
//            when (view.getId()) {
//                R.id.radioId ->
//                    if (checked) {
//                        mAdapter.filterList(this.cylinders.sortedBy { it.id })
//                    }
//                R.id.radioDate ->
//                    if (checked) {
//                        mAdapter.filterList(this.cylinders.sortedBy { it.timestamp })
//                    }
//            }
//        }
//    }

    fun displayEmptyList () {}

    fun showMessage(message : String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.generate_qr -> {
//                val intent = Intent(this, QRGeneratorActivity::class.java)
//                startActivity(intent)
//            }
//            R.id.log_out -> {
//                Firebase.auth.signOut()
//                val intent = Intent(this, SignInActivity::class.java)
//                startActivity(intent)
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//        return true
//    }
}