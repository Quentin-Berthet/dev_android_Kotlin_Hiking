package com.example.tp5_hiking.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tp5_hiking.PermissionUtils.isPermissionGranted
import com.example.tp5_hiking.PermissionUtils.requestPermission
import com.example.tp5_hiking.R
import com.example.tp5_hiking.Utils
import com.example.tp5_hiking.models.Hike
import com.example.tp5_hiking.models.HikingDatabase
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ktorm.dsl.*
import org.ktorm.entity.find


class  PerformHikeActivity : AppCompatActivity(),
    OnMapReadyCallback, Chronometer.OnChronometerTickListener,
    ActivityCompat.OnRequestPermissionsResultCallback {
    private var started = false
    private lateinit var btnStartPause: Button
    private lateinit var btnFinish: Button
    private lateinit var txvChronometer: Chronometer
    private lateinit var txvKilometers: TextView
    private lateinit var txvCurrentSpeed: TextView

    private lateinit var mMap: GoogleMap
    private lateinit var polyline: Polyline
    private var markers = ArrayList<Marker>()

    private lateinit var hike: Hike

    private var permissionDenied = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest = LocationRequest.create().setInterval(LOCATION_REQUEST_INTERVAL)

    private var lastLocation: Location? = null
    private var meters = 0.0
    var timeWhenStopped: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perform_hike)
        btnStartPause = findViewById(R.id.btnStartPause)
        btnStartPause.setOnClickListener(this::btnStartPauseOnClick)
        btnFinish = findViewById(R.id.btnFinish)
        btnFinish.setOnClickListener(this::btnFinishOnClick)
        txvChronometer = findViewById(R.id.txvChronometer)
        txvKilometers = findViewById(R.id.txvKilometers)
        txvKilometers.text = getString(R.string.kilometers, 0.00, 0.00)
        txvCurrentSpeed = findViewById(R.id.txvCurrentSpeed)
        txvCurrentSpeed.text = getString(
            R.string.current_speed_value,
            0.00
        )

        txvChronometer.onChronometerTickListener = this

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (lastLocation == null) {
                        lastLocation = location
                    } else {
                        /* Update traveled distance, current speed */
                        meters += lastLocation!!.distanceTo(location)
                        lastLocation = location
                        txvKilometers.text =
                            getString(R.string.kilometers, meters / 1000, hike.distanceKm)
                    }
                    txvCurrentSpeed.text = getString(
                        R.string.current_speed_value,
                        lastLocation!!.speed
                    )
                    hasFinishedTheHike(
                        location,
                        { stopLocationUpdates() },
                        { showFinishedDialog() })
                }
            }
        }
    }

    private fun hasFinishedTheHike(location: Location, vararg thens: () -> Unit) {
        val lasPoint = polyline.points[polyline.points.size - 1]
        val results = FloatArray(3)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            lasPoint.latitude,
            lasPoint.longitude,
            results
        )
        if (results[0] < (FINISH_ACCURACY_THRESHOLD + location.accuracy)) {
            thens.forEach { it() }
        }
    }

    private fun showFinishedDialog() {
        val builder = AlertDialog.Builder(this@PerformHikeActivity)
        builder.setTitle(getString(R.string.finish))
        builder.setMessage(getString(R.string.hike_finished))
        builder.setNeutralButton(getString(R.string.ok)) { _, _ ->
            btnFinish.performClick()
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        GlobalScope.launch {
            /**
             * Get current Hike
             * Get hike's path and display it on the map
             * Get hike's markers and display it on the map
             */
            val hikeId = intent.getIntExtra("hikeId", 0)
            hike = HikingDatabase.hikes.find { h -> h.id eq hikeId }!!

            val polylineOptions = Utils.defaultPolylineOptions()

            hike.getPath().forEach {
                polylineOptions.add(LatLng(it.latitude, it.longitude))
            }

            runOnUiThread {
                polyline = mMap.addPolyline(polylineOptions)
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        polylineOptions.points[0],
                        15.0f
                    )
                )
            }

            hike.getMarkers()
                .forEach {
                    val mo = MarkerOptions().position(LatLng(it.latitude, it.longitude))
                    runOnUiThread {
                        markers.add(mMap.addMarker(mo))
                    }
                }
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            startLocationUpdates()
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun btnStartPauseOnClick(view: View?) {
        if (!started) {
            this.btnStartPause.text = getString(R.string.pause)
            txvChronometer.base = SystemClock.elapsedRealtime() + timeWhenStopped
            txvChronometer.start()
        } else {
            this.btnStartPause.text = getString(R.string.start)
            timeWhenStopped = txvChronometer.base - SystemClock.elapsedRealtime()
            this.txvChronometer.stop()
        }
        this.started = !started
    }

    private fun btnFinishOnClick(view: View?) {
        val i = Intent(this.baseContext, FinishHikeActivity::class.java).also {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            it.putExtra("hikeId", hike.id)
            it.putExtra("hikeName", hike.name)
        }
        startActivity(i)
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(getString(R.string.finish_the_hike))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            super.onBackPressed()
        }
        builder.setNegativeButton(getString(R.string.no), null)
        builder.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
        }
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val FINISH_ACCURACY_THRESHOLD = 100
        const val LOCATION_REQUEST_INTERVAL = 1000L // ms
    }

    override fun onChronometerTick(p0: Chronometer) {
        val time = (SystemClock.elapsedRealtime() - p0.base) / 1000
        val totalTime = hike.timeSec
        txvChronometer.text =
            getString(R.string.chronometer_value, Utils.formatTime(time), Utils.formatTime(totalTime))
    }
}
