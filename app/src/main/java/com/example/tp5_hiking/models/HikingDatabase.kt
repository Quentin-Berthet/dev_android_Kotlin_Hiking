package com.example.tp5_hiking.models

import org.ktorm.database.Database
import org.ktorm.database.use
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.schema.BaseTable
import org.ktorm.schema.ColumnDeclaring

object HikingDatabase {
    private val Database.users get() = this.sequenceOf(Users)
    private val Database.hikes get() = this.sequenceOf(Hikes)
    private val Database.positions get() = this.sequenceOf(Positions)
    private val Database.markers get() = this.sequenceOf(Markers)
    private val Database.pathHikes get() = this.sequenceOf(PathHikes)
    private val Database.markerHikes get() = this.sequenceOf(MarkerHikes)
    private val Database.performedHikes get() = this.sequenceOf(UserHikes)

    val database get() = db

    val users get() = db.users
    val hikes get() = db.hikes
    val positions get() = db.positions
    val markers get() = db.markers
    val pathHikes get() = db.pathHikes
    val markerHikes get() = db.markerHikes
    val performedHikes get() = db.performedHikes



    private val db: Database by lazy {
        connect()
    }

    private fun connect(): Database {
        val url = "jdbc:sqldroid:/data/data/com.example.tp5_hiking/tp5_hiking.db"
        return Database.connect(
            url = url,
            driver = "org.sqldroid.SQLDroidDriver"
        )
    }

    fun create(sqlScript: String) {
        db.useConnection { connection ->
            connection.createStatement().use { statement ->
                sqlScript
                    .split(";")
                    .filter { sql -> sql.any { it.isLetterOrDigit() } }
                    .forEach { sql -> statement.executeUpdate(sql) }
            }
        }
    }

    // https://github.com/kotlin-orm/ktorm/issues/73#issuecomment-570912767
    inline fun <E : Any, T : BaseTable<E>> T.getList(predicate: (T) -> ColumnDeclaring<Boolean>): List<E> {
        return database.sequenceOf(this).filter(predicate).toList()
    }
}
