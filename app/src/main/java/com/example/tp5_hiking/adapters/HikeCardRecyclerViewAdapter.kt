package com.example.tp5_hiking.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.tp5_hiking.R
import com.example.tp5_hiking.activities.PerformHikeActivity
import com.example.tp5_hiking.models.Hike
import com.example.tp5_hiking.models.UserHike


class HikeCardRecyclerViewAdapter internal constructor(
    private val context: Context,
    private val performedHikes: List<UserHike>,
    hikes: List<Hike>,
    private val listener: (Hike) -> Unit
) :
    RecyclerView.Adapter<HikeCardRecyclerViewAdapter.ViewHolder>() {
    private val mData: List<Hike> = hikes
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.recycler_card_hike, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hike = mData[position]
        holder.hikeId = hike.id
        holder.txvHikeName.text = hike.name
        holder.txvDifficulty.text = context.getString(Hike.DIFFICULTIES[hike.difficulty])
        holder.txvCreator.text = hike.createdBy.pseudo
        holder.txvPerformedTimes.text = context.getString(
            R.string.x_times,
            performedHikes.count { ph -> ph.hikeId.id == hike.id })
        holder.itemView.setOnClickListener { listener(hike) }
        holder.rtbScore.rating = hike.getAverageScore()
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var hikeId: Int? = null
        val txvHikeName: TextView = itemView.findViewById(R.id.txvHikeName)
        val txvDifficulty: TextView = itemView.findViewById(R.id.txvDifficulty)
        val txvCreator: TextView = itemView.findViewById(R.id.txvCreator)
        val txvPerformedTimes: TextView = itemView.findViewById(R.id.txvPerformedTimes)
        val btnPerform: Button = itemView.findViewById(R.id.btnPerform)
        val rtbScore: RatingBar = itemView.findViewById(R.id.rtbScore)

        init {
            btnPerform.setOnClickListener(this::btnPerformOnClick)
        }

        private fun btnPerformOnClick(view: View?) {
            if (view != null) {
                val intent = Intent(view.context, PerformHikeActivity::class.java)
                intent.putExtra("hikeId", hikeId!!)
                startActivity(view.context, intent, null)
            }

        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): Hike {
        return mData[id]
    }
}
