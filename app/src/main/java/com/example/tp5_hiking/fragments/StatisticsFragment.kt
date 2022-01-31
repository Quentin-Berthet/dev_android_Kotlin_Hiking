package com.example.tp5_hiking.fragments

import android.app.AlertDialog
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tp5_hiking.Auth
import com.example.tp5_hiking.R
import com.example.tp5_hiking.Utils
import com.example.tp5_hiking.models.*
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

class StatisticsFragment : Fragment() {
    private lateinit var pathHikes: List<Hike>
    private lateinit var numberOfHike: TextView
    private lateinit var timeOfHike: TextView
    private lateinit var distOfHike: TextView
    private lateinit var resume: Button
    private lateinit var hikesPos: MutableList<LatLng>
    private lateinit var timeHike: MutableList<String>
    private lateinit var distHike: MutableList<Int>
    private lateinit var nameHike: MutableList<String>
    private lateinit var creatorHike: MutableList<String>
    private lateinit var user: User

    private lateinit var lstPosByHikes: MutableList<List<LatLng>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = Auth.getCurrentUser(view.context)!!
        val performedHikes = user.getPerformedHikes()
        numberOfHike = view.findViewById(R.id.Number_of_hike)
        timeOfHike = view.findViewById(R.id.Total_time)
        distOfHike = view.findViewById(R.id.Total_Km)
        resume = view.findViewById(R.id.resume)
        hikesPos = mutableListOf()
        lstPosByHikes = mutableListOf()
        timeHike = mutableListOf()
        distHike = mutableListOf()
        nameHike = mutableListOf()
        creatorHike = mutableListOf()
        pathHikes = HikingDatabase.hikes.asKotlinSequence().toList()

        numberOfHike.text = "Number of hike: " + pathHikes.size.toString()
        pathHikes.forEach { it ->
            nameHike.add(it.name)
            creatorHike.add(it.createdBy.pseudo)
            val path = it.getPath()
            var lstPosByHike: MutableList<LatLng> = mutableListOf()
            path.forEach {
                lstPosByHike.add(LatLng(it.longitude, it.latitude))
                hikesPos.add(LatLng(it.longitude, it.latitude))
            }
            lstPosByHikes.add(lstPosByHike)
        }
        lstPosByHikes.forEach {
            val (d, t) = getGlobalInfos(it as MutableList<LatLng>)
            distHike.add(d)
            timeHike.add(t)
        }

        var index = 0
        val (dist, temps) = computeAllDatas(distHike, timeHike)
        timeOfHike.text = "Total time of hiking: " + temps
        distOfHike.text = "Distance total of hiking: " + dist + "km"
        var fullNames = ""
        var fullDists = ""
        var fullTimes = ""
        var tmpResume = mutableListOf<String>()

        nameHike.forEach {
            fullNames += ("N°$index - ")
            fullNames += ("$it\n")
            tmpResume.add(("Name: $it"))
            index += 1
        }
        index = 0
        timeHike.forEach {
            fullTimes += ("${nameHike[index]} - $it\n")
            tmpResume[index] += ("\n     Time: $it")
            index += 1
        }
        index = 0
        distHike.forEach {
            fullDists += ("${nameHike[index]} - $it km\n")
            tmpResume[index] += ("\n     Distance: $it km ")
            index += 1
        }
        index = 0
        creatorHike.forEach {
            tmpResume[index] += ("\n     Creator: $it")
            index += 1
        }
        index = 0
        var fullResume = ""
        pathHikes.forEach {
            tmpResume[index] += "\n     Performed: ${performedHikes.count { ph -> ph.hikeId.id == it.id }} times"
            index += 1
        }
        tmpResume.forEach {
            fullResume += ("$it \n")
            index += 1
        }
        resume.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle("All stats resume")
            builder.setMessage(fullResume)

            builder.setPositiveButton("OK", null)
            builder.show()
        }
        numberOfHike.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle("All hike's name")
            builder.setMessage(fullNames)

            builder.setPositiveButton("OK", null)
            builder.show()
        }
        distOfHike.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle("All hike's distance")
            builder.setMessage(fullDists)

            builder.setPositiveButton("OK", null)
            builder.show()
        }
        timeOfHike.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle("All hike's duration")
            builder.setMessage(fullTimes)

            builder.setPositiveButton("OK", null)
            builder.show()
        }
    }

    private fun computeAllDatas(
        dists: MutableList<Int>,
        times: MutableList<String>
    ): Pair<Int, String> {
        var distG = 0
        var hour = 0
        var min = 0
        dists.forEach {
            distG += it
        }
        times.forEach {
            var l = it.split(":")
            hour += l[0].toInt()
            min += l[1].toInt()
        }
        var newMin = min / 60
        min = min - newMin * 60
        hour += newMin
        return Pair(distG, "$hour:$min")
    }

    private fun getGlobalInfos(points: MutableList<LatLng>): Pair<Int, String> {
        val dist = computeGlobalDist(points).roundToInt()
        val time = Utils.computeHikeTimeInSeconds(points)
        val formatted = Utils.formatTime(time)
        return Pair(dist, formatted)
    }

    private fun computeGlobalDist(points: List<LatLng>): Double {
        var distTot = 0.0
        var index = List<Int>(points.size - 1) { it }
        for (i in index) {
            val d = computeDistanceBetweenTwoPoints(points[i], points[i + 1])
            distTot += d

        }
        distTot /= 1000 // m->km
        return distTot
    }


    private fun computeTime(points: List<LatLng>): String {
        val walkingSpeed = 5 //vitesse moyenne ado [km/h] cf wiki
        val distance = computeGlobalDist(points)
        val ratio = distance / walkingSpeed
        var rest = 0.0
        var hour = 0
        if (ratio >= 1) {
            hour = ratio.toInt()
            rest = (ratio - hour).toDouble()
        }
        var decimal = (rest * 60).toInt()

        return "$hour:$decimal"
    }

    private fun computeDistanceBetweenTwoPoints(from: LatLng, to: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            from.latitude,
            from.longitude,
            to.latitude,
            to.longitude,
            results
        )
        return results[0].toDouble()
    }

    companion object {
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(): StatisticsFragment {
            return StatisticsFragment()
        }
    }
}


