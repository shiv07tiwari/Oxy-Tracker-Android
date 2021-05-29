package com.example.oxygencylindertracker.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.example.oxygencylindertracker.qrcode.QRGeneratorActivity
import com.example.oxygencylindertracker.qrcode.QRScannerActivity
import com.example.oxygencylindertracker.utils.Cylinder
import com.example.oxygencylindertracker.dB.LocalStorageHelper


class HomeActivity : AppCompatActivity() {

    lateinit var mRecyclerView: RecyclerView
    private lateinit var totalCylindersText : TextView
    lateinit var userTextView : TextView
    lateinit var  mAdapter: CylinderAdapter
    lateinit var mLayoutManager: RecyclerView.LayoutManager
    lateinit var mProgressBar : ProgressBar
    lateinit var firebaseDBHelper : FirebaseDBHelper

    lateinit var searchEditText : EditText
    lateinit var filtersLL : LinearLayout
    lateinit var scanQRButton : Button
    var cylinders : List<Cylinder> = listOf()
    lateinit var localStorageHelper: LocalStorageHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        firebaseDBHelper = FirebaseDBHelper()
        localStorageHelper = LocalStorageHelper()

        mProgressBar = findViewById(R.id.homeProgressBar)
        totalCylindersText = findViewById(R.id.homeCylinderText)
        userTextView = findViewById(R.id.homeUserName)
        mRecyclerView = findViewById(R.id.homeCylinderRV)

        searchEditText = findViewById(R.id.editTextSearch)
        filtersLL = findViewById(R.id.filtersLL)
        scanQRButton = findViewById(R.id.homeScanQRBtn)

        userTextView.text = "Welcome ${localStorageHelper.getUserName(this)}"

        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager

        fetchCylindersData()

        scanQRButton = findViewById(R.id.homeScanQRBtn)
        scanQRButton.setOnClickListener {
            startActivity(Intent(this, QRScannerActivity::class.java))
        }
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
        totalCylindersText.text = "You have a custody of ${cylinders.size} Cylinder(s)"

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

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when (view.getId()) {
                R.id.radioId ->
                    if (checked) {
                        mAdapter.filterList(this.cylinders.sortedBy { it.id })
                    }
                R.id.radioDate ->
                    if (checked) {
                        mAdapter.filterList(this.cylinders.sortedBy { it.timestamp })
                    }
            }
        }
    }

    fun displayEmptyList () {}

    fun showMessage(message : String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.button_item -> {
                val intent = Intent(this, QRGeneratorActivity::class.java)
                startActivity(intent)
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}