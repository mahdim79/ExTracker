package com.dust.extracker.adapters.viewpagersadapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.dust.extracker.R
import com.dust.extracker.fragments.mainviewpagerfragments.*
import com.dust.extracker.fragments.marketfragments.AccessibilityFragment
import com.dust.extracker.fragments.marketfragments.CryptoFragment
import com.dust.extracker.fragments.marketfragments.ExchangersFragment
import com.dust.extracker.fragments.marketfragments.WatchListFragment

class MarketViewPagerAdapter(fm:FragmentManager , var context: Context): FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when(position)
        {
            0 -> CryptoFragment().newInstance()
            1 -> WatchListFragment().newInstance()
            else -> ExchangersFragment().newInstance()
           // else -> AccessibilityFragment().newInstance()
        }
    }

    override fun getCount(): Int = 3

    override fun getPageTitle(position: Int): CharSequence {

        return when(position)
        {
            0 -> context.resources.getString(R.string.cryptocurrency)
            1 -> context.resources.getString(R.string.watchList)
            2 -> context.resources.getString(R.string.exchangers)
            else -> context.resources.getString(R.string.Accessibility)
        }

    }
}