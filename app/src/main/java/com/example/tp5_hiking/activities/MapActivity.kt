package com.example.tp5_hiking.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tp5_hiking.R
import com.example.tp5_hiking.Utils
import com.example.tp5_hiking.adapters.PositionMarkerRecyclerViewAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


/**
 * This activity is used to create a path for the hike and to add marker
 */

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var addTypes: Int = ADD_POSITIONS

    private lateinit var mMap: GoogleMap

    private lateinit var polyLineOptions: PolylineOptions
    private lateinit var polyline: Polyline
    private val markersOptions = ArrayList<MarkerOptions>()

    private val markers = ArrayList<Marker>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PositionMarkerRecyclerViewAdapter

    private lateinit var btnDeletePositionMarker: Button

    private var selectedElement: Any? = null
    private var selectedView: View? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        addTypes = intent.getIntExtra("addTypes", ADD_POSITIONS)

        recyclerView = findViewById(R.id.rcvPositionsMarkers)
        btnDeletePositionMarker = findViewById(R.id.btnDeletePositionMarker)
        btnDeletePositionMarker.setOnClickListener(this::btnDeletePositionMarkerOnclick)

        initPolyline()
        initRecyclerView()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initPolyline() {
        polyLineOptions = Utils.defaultPolylineOptions()
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        if (addTypes == ADD_POSITIONS) {
            adapter =
                PositionMarkerRecyclerViewAdapter(
                    this,
                    this.polyLineOptions.points,
                    this::onItemClick
                )
        } else if (addTypes == ADD_MARKERS) {
            adapter = PositionMarkerRecyclerViewAdapter(this, this.markers, this::onItemClick)
        }
        recyclerView.adapter = adapter
    }

    private fun onItemClick(position: Int, item: Any, view: View) {
        selectedView?.setBackgroundColor(Color.TRANSPARENT)
        view.setBackgroundColor(Color.GRAY)
        this.selectedElement = item
        this.selectedView = view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener(this::onMapClick)
        val points: ArrayList<LatLng>? = intent.getParcelableArrayListExtra("points")
        if (points != null) {
            this.polyLineOptions.addAll(points)
            this.recyclerView.adapter?.notifyItemRangeInserted(0, points.size)
        }
        polyline = this.mMap.addPolyline(polyLineOptions)

        intent.getParcelableArrayListExtra<MarkerOptions?>("markers")?.forEach { addMarker(it) }
    }

    private fun onMapClick(p0: LatLng?) {
        if (p0 != null) {
            if (addTypes == ADD_POSITIONS) {
                polyLineOptions.add(p0)
                this.polyline.points = polyLineOptions.points
                recyclerView.adapter?.notifyItemInserted(polyLineOptions.points.size - 1)
            } else if (addTypes == ADD_MARKERS) {
                val markerOptions = MarkerOptions().position(p0)
                addMarker(markerOptions)
            }
        }
    }

    private fun addMarker(mo: MarkerOptions) {
        val marker = mMap.addMarker(mo)
        this.markersOptions.add(mo)
        this.markers.add(marker)
        recyclerView.adapter?.notifyItemInserted(markers.size - 1)
    }

    private fun btnDeletePositionMarkerOnclick(view: View) {
        if (selectedElement == null) {
            return
        }
        selectedView?.setBackgroundColor(Color.TRANSPARENT)
        if (addTypes == ADD_POSITIONS) {
            val idx = this.polyline.points.indexOf(selectedElement)
            if (idx >= 0) {
                this.polyLineOptions.points.removeAt(idx)
                this.polyline.points = polyLineOptions.points
                this.recyclerView.adapter?.notifyItemRemoved(idx)
            }
        } else if (addTypes == ADD_MARKERS) {
            val idx = this.markers.indexOf(selectedElement)
            if (idx >= 0) {
                this.markers[idx].remove()
                this.markers.removeAt(idx)
                this.markersOptions.removeAt(idx)
                this.recyclerView.adapter?.notifyItemRemoved(idx)
            }
        }
        this.selectedElement = null
        this.selectedView = null
    }

    override fun onBackPressed() {
        val intent = Intent()
        if (addTypes == ADD_POSITIONS) {
            intent.putParcelableArrayListExtra(
                "points",
                polyLineOptions.points as ArrayList<out Parcelable>
            )
        } else if (addTypes == ADD_MARKERS) {
            intent.putParcelableArrayListExtra(
                "markers",
                markersOptions as ArrayList<out Parcelable>
            )
        }

        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }

    companion object {
        const val ADD_POSITIONS = 1 // Activity is launched to create path
        const val ADD_MARKERS = 2 // Activity is launched to add markers
    }
}
