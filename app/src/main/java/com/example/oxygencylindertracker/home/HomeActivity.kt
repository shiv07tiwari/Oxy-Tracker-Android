package com.example.oxygencylindertracker.home

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.dB.FirebaseDBHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {

    private val mRecyclerView: RecyclerView? = null
    private val mAdapter: RecyclerView.Adapter<*>? = null
    private val mLayoutManager: RecyclerView.LayoutManager? = null
    var auth = Firebase.auth
    lateinit var mProgressBar : ProgressBar
    lateinit var firebaseDBHelper : FirebaseDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        firebaseDBHelper = FirebaseDBHelper()
        mProgressBar = findViewById(R.id.homeProgressBar)
        mProgressBar.visibility = View.GONE
        fetchCylindersData()
    }

    private fun fetchCylindersData() {
        firebaseDBHelper.getCylindersDataForUser(auth.currentUser?.phoneNumber, this)
    }
}