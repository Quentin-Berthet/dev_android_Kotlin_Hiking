package com.example.tp5_hiking.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.tp5_hiking.R
import com.example.tp5_hiking.adapters.MainSectionsPagerAdapter
import com.example.tp5_hiking.models.HikingDatabase
import com.google.android.material.tabs.TabLayout
import org.ktorm.database.use


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = MainSectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val sqlScript = application.assets.open("tp5_hiking.sql").bufferedReader().use {
            it.readText()
        }
        HikingDatabase.create(sqlScript)
    }
}
