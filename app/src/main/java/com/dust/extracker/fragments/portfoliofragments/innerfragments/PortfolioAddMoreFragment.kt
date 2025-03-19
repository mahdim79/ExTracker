package com.dust.extracker.fragments.portfoliofragments.innerfragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.R
import com.dust.extracker.fragments.portfoliofragments.SelectCtyptoFragment

class PortfolioAddMoreFragment:Fragment() {
    private lateinit var add_portfolio:Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_portfolio_addmore , container , false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
    }

    private fun setUpViews(view: View) {
        add_portfolio = view.findViewById(R.id.add_portfolio)
        add_portfolio.setOnClickListener {
            requireActivity().sendBroadcast(Intent("com.dust.extracker.addMoreHistory"))
        }
    }
}