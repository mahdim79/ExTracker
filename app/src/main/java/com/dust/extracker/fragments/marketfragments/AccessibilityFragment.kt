package com.dust.extracker.fragments.marketfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.MarketAccessibilityRecyclerViewAdapter
import com.dust.extracker.dataclasses.AccessibilityDataClass

class AccessibilityFragment : Fragment() {

    private lateinit var accessibility_recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_accessibility, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpDataAndRecyclerViewAdapter()

    }

    private fun setUpDataAndRecyclerViewAdapter() {

        accessibility_recyclerView.adapter =
            MarketAccessibilityRecyclerViewAdapter(
                createDataClass(),
                activity!!
            )
    }

    private fun setUpViews(view: View) {
        accessibility_recyclerView = view.findViewById(R.id.accessibility_recyclerView)

        accessibility_recyclerView.layoutManager = GridLayoutManager(activity!!, 3)
    }

    private fun createDataClass(): List<AccessibilityDataClass> {
        val list = arrayListOf<AccessibilityDataClass>()
        list.add(
            AccessibilityDataClass(
                R.drawable.one,
                activity!!.resources.getString(R.string.one),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.two,
                activity!!.resources.getString(R.string.two),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.three,
                activity!!.resources.getString(R.string.three),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.four,
                activity!!.resources.getString(R.string.four),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.five,
                activity!!.resources.getString(R.string.five),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.six,
                activity!!.resources.getString(R.string.six),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.seven,
                activity!!.resources.getString(R.string.seven),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.eight,
                activity!!.resources.getString(R.string.eight),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.nine,
                activity!!.resources.getString(R.string.nine),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.ten,
                activity!!.resources.getString(R.string.ten),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.elev,
                activity!!.resources.getString(R.string.eleven),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.twelve,
                activity!!.resources.getString(R.string.twelve),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.threeteen,
                activity!!.resources.getString(R.string.threeteen),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.fourteen,
                activity!!.resources.getString(R.string.fourteen),
                "https://www.google.com"
            )
        )
        list.add(
            AccessibilityDataClass(
                R.drawable.fifteen,
                activity!!.resources.getString(R.string.fifteen),
                "https://www.google.com"
            )
        )

        return list
    }

    fun newInstance(): AccessibilityFragment {
        val args = Bundle()
        val fragment = AccessibilityFragment()
        fragment.arguments = args
        return fragment
    }
}