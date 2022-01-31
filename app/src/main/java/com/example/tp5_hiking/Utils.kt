package com.example.tp5_hiking

import android.graphics.Color
import android.location.Location
import android.text.format.DateUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap

object Utils {
    const val DEFAULT_WALKING_SPEED = 5.5 // Vitesse moyenne d'un [Km/h] cf wiki
    const val PSEUDO_MAX_LENGTH = 20
    const val BCRYPT_COST = 12

    fun defaultPolylineOptions(): PolylineOptions {
        return PolylineOptions()
            .clickable(false)
            .startCap(RoundCap())
            .endCap(RoundCap())
            .width(2.5f)
            .color(Color.RED)
    }

    fun computeDistanceBetweenTwoPoints(from: LatLng, to: LatLng): Double {
        val results = FloatArray(3)
        Location.distanceBetween(
            from.latitude,
            from.longitude,
            to.latitude,
            to.longitude,
            results
        )
        return results[0].toDouble()
    }

    fun computeHikeDistanceInKilometers(points: List<LatLng>): Double {
        return (0..points.size - 2).map {
            computeDistanceBetweenTwoPoints(
                points[it],
                points[it + 1]
            )
        }.sum() / 1000
    }

    fun computeHikeTimeInSeconds(points: List<LatLng>): Long {
        val walkingSpeed = DEFAULT_WALKING_SPEED
        val distance = computeHikeDistanceInKilometers(points)
        val hours = distance / walkingSpeed
        return (hours * 3600).toLong()
    }

    fun formatTime(secs: Long): String {
        return DateUtils.formatElapsedTime(secs)
    }
}
