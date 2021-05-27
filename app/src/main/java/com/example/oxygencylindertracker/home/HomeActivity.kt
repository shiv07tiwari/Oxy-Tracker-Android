package com.example.oxygencylindertracker.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.example.oxygencylindertracker.qrcode.QRGeneratorActivity
import com.example.oxygencylindertracker.utils.Cylinder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class HomeActivity : AppCompatActivity() {

    lateinit var mRecyclerView: RecyclerView
    private lateinit var totalCylindersText : TextView
    lateinit var userTextView : TextView
    lateinit var  mAdapter: Adapter<CylinderAdapter.CylinderItemViewHolder>
    lateinit var mLayoutManager: RecyclerView.LayoutManager
    var auth = Firebase.auth
    lateinit var mProgressBar : ProgressBar
    lateinit var firebaseDBHelper : FirebaseDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        firebaseDBHelper = FirebaseDBHelper()
        mProgressBar = findViewById(R.id.homeProgressBar)
        totalCylindersText = findViewById(R.id.homeCylinderText)
        userTextView = findViewById(R.id.homeUserName)
        mRecyclerView = findViewById(R.id.homeCylinderRV)
        userTextView.text = "Hello Shivansh"
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager
        fetchCylindersData()
    }

    private fun fetchCylindersData() {
        mRecyclerView.visibility = View.GONE
        mProgressBar.visibility = View.VISIBLE
        totalCylindersText.visibility = View.GONE
        firebaseDBHelper.getCylindersDataForUser(this)
    }

    fun displayCylinderList (cylinders: List<Cylinder>) {
        totalCylindersText.text = "You have a custody of ${cylinders.size} Cylinder(s)"
        mAdapter = CylinderAdapter(cylinders)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.visibility = View.VISIBLE
        mProgressBar.visibility = View.GONE
        totalCylindersText.visibility = View.VISIBLE
    }

    fun displayEmptyList () {

    }

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