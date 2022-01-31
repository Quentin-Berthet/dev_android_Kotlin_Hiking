package com.example.tp5_hiking.models

import org.ktorm.schema.Table
import org.ktorm.schema.double
import org.ktorm.schema.int

object Positions : Table<Position>("position") {
    var id = int("id").primaryKey().bindTo { it.id }
    var latitude = double("latitude")
    var longitude = double("longitude")
}
