package com.dust.extracker.fragments.mainviewpagerfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.R
import com.dust.extracker.fragments.othersfragment.OthersFragment

class OthersFragmentHolder:Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_others_holder , container , false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMainOthersFragment()

    }

    private fun setUpMainOthersFragment() {

        fragmentManager!!.beginTransaction()
            .replace(R.id.others_frame_holder , OthersFragment())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("OthersFragment")
            .commit()

    }

    fun newInstance(): OthersFragmentHolder {
        val args = Bundle()
        val fragment =
            OthersFragmentHolder()
        fragment.arguments = args
        return fragment
    }
}