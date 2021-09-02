package com.dust.extracker.adapters.viewpagersadapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.dust.extracker.dataclasses.HistoryDataClass
import com.dust.extracker.fragments.portfoliofragments.innerfragments.PortfolioAddMoreFragment
import com.dust.extracker.fragments.portfoliofragments.innerfragments.PortfolioBaseFragment
import com.dust.extracker.interfaces.OnHistoryFragmentUpdate

class PortfolioBaseViewPagerAdapter(
    var list: List<HistoryDataClass>,
    fragmentManager: FragmentManager,
    var onHistoryFragmentUpdate: OnHistoryFragmentUpdate
) : FragmentStatePagerAdapter(fragmentManager) {
    var baseId = 0L
    override fun getItem(position: Int): Fragment {
        if (position != list.size){
            return PortfolioBaseFragment(list[position] , onHistoryFragmentUpdate)
        }
        return PortfolioAddMoreFragment()
    }

    override fun getCount(): Int = list.size + 1
}