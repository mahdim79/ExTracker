package com.dust.extracker.adapters.viewpagersadapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.dust.extracker.dataclasses.SliderDataClass
import com.dust.extracker.fragments.blogfragments.SliderFragment

class SliderViewPagerAdapter(fm: FragmentManager, var list: List<SliderDataClass>) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return SliderFragment(list , position)
    }

    override fun getCount(): Int = list.size
}