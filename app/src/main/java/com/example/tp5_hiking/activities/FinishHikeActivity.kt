package com.example.tp5_hiking.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.*
import com.example.tp5_hiking.Auth
import com.example.tp5_hiking.R
import com.example.tp5_hiking.models.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.find
import java.lang.Exception

class FinishHikeActivity : AppCompatActivity() {
    private lateinit var txvHikeName: TextView
    private lateinit var rtbHike: RatingBar
    private lateinit var edtComment: EditText
    private lateinit var btnSend: Button

    private var hikeId: Int? = null
    private var loggedUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_hike)
        txvHikeName = findViewById(R.id.txvHikeName)
        rtbHike = findViewById(R.id.rtbHike)
        edtComment = findViewById(R.id.edtComment)
        btnSend = findViewById(R.id.btnSend)
        btnSend.setOnClickListener(this::btnSendOnClick)
        txvHikeName.text = intent.getStringExtra("hikeName")
        hikeId = intent.getIntExtra("hikeId", -1)
        loggedUser = Auth.getCurrentUser(this)
    }

    private fun btnSendOnClick(p0: View?) {
        GlobalScope.launch {
            try {
                HikingDatabase.database.useTransaction {
                    val hike = HikingDatabase.hikes.find { it.id eq hikeId!! }
                    if (hike != null) {
                        HikingDatabase.database.insertAndGenerateKey(UserHikes) {
                            set(it.hikeId, hike.id)
                            set(it.userId, loggedUser!!.id)
                            set(it.note, rtbHike.rating.toInt())
                            set(it.comment, edtComment.text.toString())
                        }
                    }
                    runOnUiThread {
                        val i = Intent(baseContext, MenuActivity::class.java)
                        startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@FinishHikeActivity,
                        getString(R.string.error_occurred_comment),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        btnSendOnClick(null)
    }
}
