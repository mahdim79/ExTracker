package com.dust.extracker.fragments.mainviewpagerfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.R
import com.dust.extracker.fragments.exchangerfragments.ExchangerFragment

class FragmentExchangerHolder:Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exchanger_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMainExchangerFragment()
    }

    private fun setUpMainExchangerFragment() {
        fragmentManager!!.beginTransaction().replace(R.id.exchanger_holder ,
            ExchangerFragment()
        )
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("ExchangerFragment")
            .commit()
    }

    fun newInstance(): FragmentExchangerHolder {
        val args = Bundle()
        val fragment = FragmentExchangerHolder()
        fragment.arguments = args
        return fragment
    }

}