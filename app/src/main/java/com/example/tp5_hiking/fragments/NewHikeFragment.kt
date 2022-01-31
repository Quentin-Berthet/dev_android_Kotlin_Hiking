package com.example.tp5_hiking.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tp5_hiking.Auth
import com.example.tp5_hiking.R
import com.example.tp5_hiking.Utils
import com.example.tp5_hiking.activities.MapActivity
import com.example.tp5_hiking.models.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.add
import java.lang.Exception

class NewHikeFragment : Fragment() {
    private lateinit var user: User

    private lateinit var edtHikeName: EditText
    private lateinit var btnCreatePath: Button
    private lateinit var btnAddMarkers: Button
    private lateinit var rdbEasy: RadioButton
    private lateinit var rdbAverage: RadioButton
    private lateinit var rdbHard: RadioButton
    private lateinit var edtComment: EditText
    private lateinit var btnAdd: Button

    private var points: ArrayList<LatLng> = ArrayList()
    private var markersOptions: ArrayList<MarkerOptions> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edtHikeName = view.findViewById(R.id.edtHikeName)
        btnCreatePath = view.findViewById(R.id.btnCreatePath)
        btnCreatePath.setOnClickListener(this::showCreatePathActivity)
        btnAddMarkers = view.findViewById(R.id.btnAddMarkers)
        btnAddMarkers.setOnClickListener(this::showAddMarkersActivity)
        rdbEasy = view.findViewById(R.id.rdbEasy)
        rdbAverage = view.findViewById(R.id.rdbAverage)
        rdbHard = view.findViewById(R.id.rdbHard)
        edtComment = view.findViewById(R.id.edtComment)
        btnAdd = view.findViewById(R.id.btnAdd)
        btnAdd.setOnClickListener(this::addHike)

        user = Auth.getCurrentUser(view.context)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_new_hike, container, false)
    }

    private fun addHike(view: View?) {
        if (edtHikeName.text.isNotEmpty() && edtComment.text.isNotEmpty() && points.size > 0) {
            GlobalScope.launch {
                try {
                    HikingDatabase.database.useTransaction {
                        /* Create new hike */
                        val newHike = Hike {
                            name = edtHikeName.text.trim().toString()
                            difficulty = when {
                                rdbEasy.isChecked -> {
                                    Hike.DIFFICULTY_EASY
                                }
                                rdbAverage.isChecked -> {
                                    Hike.DIFFICULTY_AVERAGE
                                }
                                else -> {
                                    Hike.DIFFICULTY_HARD
                                }
                            }
                            distanceKm = Utils.computeHikeDistanceInKilometers(points)
                            timeSec = Utils.computeHikeTimeInSeconds(points)
                            comment = edtComment.text.trim().toString()
                            createdBy = user
                        }

                        /* Insert it into the DB */
                        newHike.id = HikingDatabase.hikes.database.insertAndGenerateKey(Hikes) {
                            set(it.name, newHike.name)
                            set(it.difficulty, newHike.difficulty)
                            set(it.distanceKm, newHike.distanceKm)
                            set(it.timeSec, newHike.timeSec)
                            set(it.comment, newHike.comment)
                            set(it.createdBy, newHike.createdBy.id)
                        } as Int

                        /* Creates all points and insert it into the DB */
                        points.forEach { point ->
                            val newPosition = Position {
                                latitude = point.latitude
                                longitude = point.longitude
                            }
                            newPosition.id =
                                HikingDatabase.positions.database.insertAndGenerateKey(Positions) {
                                    set(it.latitude, newPosition.latitude)
                                    set(it.longitude, newPosition.longitude)
                                } as Int

                            /* Assign position to the hike */
                            HikingDatabase.pathHikes.add(PathHike {
                                hike = newHike
                                position = newPosition
                            })
                        }

                        /* Create markers and insert it into the DB */
                        markersOptions.forEach { mo ->
                            val newMarker = Marker {
                                latitude = mo.position.latitude
                                longitude = mo.position.longitude
                            }
                            newMarker.id =
                                HikingDatabase.markers.database.insertAndGenerateKey(Markers) {
                                    set(it.latitude, newMarker.latitude)
                                    set(it.longitude, newMarker.longitude)
                                } as Int

                            /* Assign marker to the hike */
                            HikingDatabase.markerHikes.add(MarkerHike {
                                hike = newHike
                                marker = newMarker
                            })
                        }
                        GlobalScope.launch(Dispatchers.Main) {
                            Toast.makeText(requireView().context, getString(R.string.hike_created), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } catch (e: Exception) {
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            requireView().context,
                            getString(R.string.error_occurred),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun showCreatePathActivity(view: View?) {
        val intent = Intent(requireView().context, MapActivity::class.java)
        intent.putExtra("addTypes", MapActivity.ADD_POSITIONS)
        intent.putExtra("points", points)
        intent.putExtra("markers", markersOptions)
        startActivityForResult(intent, CREATE_PATH_ACTIVITY_RESULT)
    }

    private fun showAddMarkersActivity(view: View?) {
        val intent = Intent(requireView().context, MapActivity::class.java)
        intent.putExtra("addTypes", MapActivity.ADD_MARKERS)
        intent.putExtra("points", points)
        intent.putExtra("markers", markersOptions)
        startActivityForResult(intent, ADD_MARKERS_ACTIVITY_RESULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == CREATE_PATH_ACTIVITY_RESULT) {
                points = data.getParcelableArrayListExtra<LatLng>("points") as ArrayList<LatLng>
            } else if (requestCode == ADD_MARKERS_ACTIVITY_RESULT) {
                markersOptions =
                    data.getParcelableArrayListExtra<MarkerOptions>("markers") as ArrayList<MarkerOptions>
            }
        }
    }

    companion object {
        private const val CREATE_PATH_ACTIVITY_RESULT = 1
        private const val ADD_MARKERS_ACTIVITY_RESULT = 2

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(): NewHikeFragment {
            return NewHikeFragment()
        }
    }
}
