package com.example.tp5_hiking.adapters

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tp5_hiking.R
import com.google.android.gms.maps.model.LatLng

class PositionMarkerRecyclerViewAdapter internal constructor(
    private val context: Context,
    data: List<Any>,
    private val listener: (Int, Any, View) -> Unit
) :
    RecyclerView.Adapter<PositionMarkerRecyclerViewAdapter.ViewHolder>() {
    private val mData: List<Any> = data
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.recycler_position_marker, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val obj = mData[position]
        holder.txvPositionMarker.text = when (obj) {
            is com.google.android.gms.maps.model.Marker -> {
                "Marker(${obj.position.latitude}, ${obj.position.longitude}): ${
                    getAddress(
                        obj.position.latitude,
                        obj.position.longitude
                    )
                }"
            }
            is LatLng -> {
                "Position(${obj.latitude}, ${obj.longitude}): ${
                    getAddress(
                        obj.latitude,
                        obj.longitude
                    )
                }"
            }
            else -> {
                ""
            }
        }
        holder.itemView.setOnClickListener { listener(position, obj, holder.txvPositionMarker) }
    }

    private fun getAddress(lat: Double, long: Double): String {
        val geocoder = Geocoder(this.context)
        val list = geocoder.getFromLocation(lat, long, 1)
        if (list.size > 0) {
            return list[0].getAddressLine(0)
        }
        return ""
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var txvPositionMarker: TextView = itemView.findViewById(R.id.txvPositionMarker)
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): Any {
        return mData[id]
    }
}
