package com.example.tp5_hiking.models

import org.ktorm.entity.Entity

interface Position : Entity<Position> {
    companion object : Entity.Factory<Position>()

    var id: Int
    var latitude: Double
    var longitude: Double
}
