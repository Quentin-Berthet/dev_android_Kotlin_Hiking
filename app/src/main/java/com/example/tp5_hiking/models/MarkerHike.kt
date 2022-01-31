package com.example.tp5_hiking.models

import org.ktorm.entity.Entity

interface MarkerHike : Entity<MarkerHike> {
    companion object : Entity.Factory<MarkerHike>()

    var hike: Hike
    var marker: Marker
}
