package com.dust.extracker.fragments.portfoliofragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.R

class EmptyPortfolioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_empty_portfolio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn: Button = view.findViewById(R.id.add_portfolio)
        btn.setOnClickListener {
            fragmentManager?.beginTransaction()!!
                .replace(R.id.frame_holder,
                    SelectCtyptoFragment()
                )
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("SelectCtyptoFragment")
                .commit()
        }
    }


    fun newInstance(): EmptyPortfolioFragment {
        val args = Bundle()
        val fragment =
            EmptyPortfolioFragment()
        fragment.arguments = args
        return fragment
    }

}