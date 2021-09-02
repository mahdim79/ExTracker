package com.dust.extracker.adapters.viewpagersadapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.dust.extracker.fragments.mainviewpagerfragments.*
import com.dust.extracker.fragments.mainviewpagerfragments.MarketFragment

class MainViewPagerAdapter(fm:FragmentManager): FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when(position)
        {
            2 -> MarketFragment()
                .newInstance()
            1 -> PortfolioFragmentHolder().newInstance()
            0 -> FragmentExchangerHolder().newInstance()
            3 -> BlogFragmentHolder().newInstance()
            else -> OthersFragmentHolder().newInstance()
        }
    }

    override fun getCount(): Int = 5

}