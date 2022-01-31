package com.example.tp5_hiking.models

import org.ktorm.schema.*

object Hikes : Table<Hike>("hike") {
    var id = int("id").primaryKey().bindTo { it.id }
    var name = varchar("name").bindTo { it.name }
    var difficulty = int("difficulty").bindTo { it.difficulty }
    var distanceKm = double("distance_km").bindTo { it.distanceKm }
    var timeSec = long("time_sec").bindTo { it.timeSec }
    var comment = varchar("comment").bindTo { it.comment }
    var createdBy = int("created_by").references(Users) { it.createdBy }
}
