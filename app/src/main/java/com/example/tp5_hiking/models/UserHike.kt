package com.example.tp5_hiking.models

import org.ktorm.entity.Entity

interface UserHike : Entity<UserHike> {
    companion object : Entity.Factory<UserHike>()

    val id: Int
    var hikeId: Hike
    var userId: User
    var note: Int
    var comment: String
}
