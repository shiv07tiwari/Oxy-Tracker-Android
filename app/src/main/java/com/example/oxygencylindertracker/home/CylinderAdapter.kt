package com.example.oxygencylindertracker.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.utils.Cylinder
import java.text.SimpleDateFormat
import java.util.*


class CylinderAdapter (cylinders : List<Cylinder>) : RecyclerView.Adapter<CylinderAdapter.CylinderItemViewHolder>() {
    
    var cylinders : List<Cylinder> = cylinders

    class CylinderItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var cylinderIdText : TextView = v.findViewById(R.id.cylinderItemId)
        var cylinderDateText : TextView = v.findViewById(R.id.cylinderItemDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CylinderItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cylinder_item, parent, false)
        return CylinderItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cylinders.size
    }

    override fun onBindViewHolder(holder: CylinderItemViewHolder, position: Int) {
        val month_date = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val sdf = SimpleDateFormat("MM/dd/yyyyy")

        val actualDate = cylinders[position].timestamp

        val date: Date = sdf.parse(actualDate)

        val month_name: String = month_date.format(date)
        holder.cylinderIdText.text = cylinders[position].id
        holder.cylinderDateText.text = month_name
    }

    fun filterList (cylinders: List<Cylinder>) {
        this.cylinders = cylinders
        notifyDataSetChanged()
    }

}