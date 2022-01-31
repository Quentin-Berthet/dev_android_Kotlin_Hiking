package com.example.tp5_hiking.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.tp5_hiking.R
import com.example.tp5_hiking.fragments.StatisticsFragment
import com.example.tp5_hiking.fragments.NewHikeFragment
import com.example.tp5_hiking.fragments.RepertoryFragment

private val TAB_TITLES = arrayOf(
    R.string.menu_repertory_tab,
    R.string.menu_new_hike_tab,
    R.string.menu_stats_tab
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class MenuSectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return if (position == 0) {
            RepertoryFragment.newInstance()
        } else if (position == 1) {
            NewHikeFragment.newInstance()
        } else {
            StatisticsFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}
