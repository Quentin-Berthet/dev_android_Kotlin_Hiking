package com.example.tp5_hiking.models

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object Users : Table<User>("user") {
    var id = int("id").primaryKey().bindTo { it.id }
    var pseudo = varchar("pseudo").bindTo { it.pseudo }
    var password = varchar("password").bindTo { it.password }
}
