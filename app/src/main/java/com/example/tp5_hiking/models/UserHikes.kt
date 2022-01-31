package com.example.tp5_hiking.models

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object UserHikes : Table<UserHike>("user_hike") {
    var id = int("id").primaryKey()
    var hikeId = int("hike_id").references(Hikes) { it.hikeId }
    var userId = int("user_id").references(Users) { it.userId }
    var note = int("note")
    var comment = varchar("comment")
}
