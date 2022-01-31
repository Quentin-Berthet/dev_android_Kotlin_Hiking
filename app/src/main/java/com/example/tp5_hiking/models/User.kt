package com.example.tp5_hiking.models

import com.example.tp5_hiking.models.HikingDatabase.getList
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    var id: Int
    var pseudo: String
    var password: String

    fun getPerformedHikes(): List<UserHike> = UserHikes.getList { it.userId eq id }
}
