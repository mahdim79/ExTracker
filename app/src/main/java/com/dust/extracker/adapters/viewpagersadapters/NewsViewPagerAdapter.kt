package com.dust.extracker.adapters.viewpagersadapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.dust.extracker.R
import com.dust.extracker.fragments.blogfragments.NewsFragment

class NewsViewPagerAdapter(fm:FragmentManager , var context: Context):FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return NewsFragment().newInstance(position)
    }

    override fun getCount(): Int = 5

    override fun getPageTitle(position: Int): CharSequence {
        return when(position)
        {
            0 -> context.resources.getString(R.string.all)
            1 -> context.resources.getString(R.string.bitcoin)
            2 -> context.resources.getString(R.string.etherum)
            3 -> context.resources.getString(R.string.tradingSignals)
            else -> context.resources.getString(R.string.altcoins)
        }
    }
}