package com.example.tp5_hiking.models

import android.util.Log
import com.example.tp5_hiking.R
import com.example.tp5_hiking.models.HikingDatabase.getList
import org.ktorm.dsl.*
import org.ktorm.entity.Entity
import org.ktorm.entity.count
import org.ktorm.entity.filter

interface Hike : Entity<Hike> {
    companion object : Entity.Factory<Hike>() {
        const val DIFFICULTY_EASY = 0
        const val DIFFICULTY_AVERAGE = 1
        const val DIFFICULTY_HARD = 2

        val DIFFICULTIES = listOf(R.string.easy, R.string.average, R.string.hard)
    }

    var id: Int
    var name: String
    var difficulty: Int
    var distanceKm: Double
    var timeSec: Long
    var comment: String
    var createdBy: User

    fun getMarkers(): List<Marker> {
        val markers = MarkerHikes.getList { it.hikeId eq id }
        return markers.flatMap { mh ->
            HikingDatabase.database.from(Markers)
                .select(Markers.id, Markers.latitude, Markers.longitude)
                .where {
                    Markers.id eq mh.marker.id
                }.map { row ->
                    Marker {
                        id = row[Markers.id]!!
                        latitude = row[Markers.latitude]!!
                        longitude = row[Markers.longitude]!!
                    }
                }
        }
    }

    fun getPath(): List<Position> {
        val path = PathHikes.getList { it.hikeId eq id }
        return path.flatMap { ph ->
            HikingDatabase.database.from(Positions)
                .select(Positions.id, Positions.latitude, Positions.longitude)
                .where {
                    Positions.id eq ph.position.id
                }.map { row ->
                    Position {
                        id = row[Positions.id]!!
                        latitude = row[Positions.latitude]!!
                        longitude = row[Positions.longitude]!!
                    }
                }
        }
    }

    fun getAverageScore(): Float {
        return HikingDatabase.database.from(UserHikes)
            .select(avg(UserHikes.note))
            .where(UserHikes.hikeId eq id)
            .map { row -> row.getFloat(1) }[0]
    }
}
