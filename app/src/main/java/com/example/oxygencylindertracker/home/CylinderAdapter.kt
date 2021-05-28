package com.example.oxygencylindertracker.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.oxygencylindertracker.R
import com.example.oxygencylindertracker.utils.Cylinder


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
        holder.cylinderIdText.text = cylinders[position].id
        holder.cylinderDateText.text = cylinders[position].timestamp
    }

    fun filterList (cylinders: List<Cylinder>) {
        this.cylinders = cylinders
        notifyDataSetChanged()
    }

}