package com.example.tp5_hiking.models

import org.ktorm.schema.Table
import org.ktorm.schema.int

object MarkerHikes: Table<MarkerHike>("marker_hike") {
    var hikeId = int("hike_id").references(Hikes) { it.hike }
    var markerId = int("marker_id").references(Markers) { it.marker }
}
