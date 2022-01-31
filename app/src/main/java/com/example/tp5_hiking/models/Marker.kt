package com.example.tp5_hiking.models

import org.ktorm.entity.Entity

interface Marker : Entity<Marker> {
    companion object : Entity.Factory<Marker>()

    var id: Int
    var latitude: Double
    var longitude: Double
}
