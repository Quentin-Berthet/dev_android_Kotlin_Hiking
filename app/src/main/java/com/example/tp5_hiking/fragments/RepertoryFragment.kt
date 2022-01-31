package com.example.tp5_hiking.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tp5_hiking.Auth
import com.example.tp5_hiking.R
import com.example.tp5_hiking.adapters.HikeCardRecyclerViewAdapter
import com.example.tp5_hiking.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RepertoryFragment : Fragment() {
    private lateinit var adapter: HikeCardRecyclerViewAdapter
    private lateinit var hikes: MutableList<Hike>
    private lateinit var user: User


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_repertory, container, false)
        val recyclerView = root.findViewById<RecyclerView>(R.id.rcvHikesCard)
        recyclerView.layoutManager = LinearLayoutManager(context)

        GlobalScope.launch {
            user = Auth.getCurrentUser(root.context)!!
            hikes = HikingDatabase.hikes.asKotlinSequence().toMutableList()
            val performedHikes = user.getPerformedHikes()

            adapter = HikeCardRecyclerViewAdapter(requireContext(), performedHikes, hikes) { hike ->
                val builder = AlertDialog.Builder(root.context)
                builder.setTitle(getString(R.string.comment))
                builder.setMessage(hike.comment)

                builder.setPositiveButton(getString(R.string.ok), null)
                builder.show()
            }

            GlobalScope.launch(Dispatchers.Main) {
                recyclerView.adapter = adapter
            }
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            GlobalScope.launch {
                hikes.clear()
                hikes.addAll(HikingDatabase.hikes.asKotlinSequence())
                GlobalScope.launch(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    companion object {
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(): RepertoryFragment {
            return RepertoryFragment()
        }
    }
}
