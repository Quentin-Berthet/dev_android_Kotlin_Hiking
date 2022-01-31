package com.example.tp5_hiking.models

import org.ktorm.schema.Table
import org.ktorm.schema.int

object PathHikes : Table<PathHike>("path_hike") {
    var hikeId = int("hike_id").references(Hikes) { it.hike }
    var positionId = int("position_id").references(Positions) { it.position }
}
