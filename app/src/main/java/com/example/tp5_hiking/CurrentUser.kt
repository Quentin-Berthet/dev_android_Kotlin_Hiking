package com.example.tp5_hiking

import android.content.Context
import com.example.tp5_hiking.models.HikingDatabase
import com.example.tp5_hiking.models.User
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import java.io.File

object Auth {
    private const val FILE_NAME = "islogged"

    private var user: User? = null

    fun getCurrentUser(context: Context): User? {
        if (this.user != null) {
            return user
        }
        val file = File(context.filesDir.path, FILE_NAME)
        if (!file.exists()) {
            return null
        }
        val lines = file.readLines()
        if (lines.size < 2) {
            return null
        }
        user = HikingDatabase.users.firstOrNull {
            it.pseudo eq lines[0].trim() and (it.password eq lines[1])
        }
        return user
    }

    fun setCurrentUser(context: Context, user: User) {
        File(context.filesDir.path, FILE_NAME)
            .writeText("${user.pseudo}\n${user.password}")
    }
}
