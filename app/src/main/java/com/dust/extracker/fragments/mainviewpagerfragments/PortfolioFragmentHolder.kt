package com.dust.extracker.fragments.mainviewpagerfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.R
import com.dust.extracker.fragments.portfoliofragments.EmptyPortfolioFragment
import com.dust.extracker.fragments.portfoliofragments.PortfolioFragment
import com.dust.extracker.realmdb.RealmDataBaseCenter

class PortfolioFragmentHolder : Fragment() {
    lateinit var fragment: Fragment
    lateinit var realmDb:RealmDataBaseCenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_portfolio_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRealmDb()
        var name = ""
        if (realmDb.getAllHistoryData().isEmpty()){
            fragment =
                EmptyPortfolioFragment()
            name = "EmptyPortfolioFragment"
        } else{
            fragment =
                PortfolioFragment()
            name = "PortfolioFragment"
        }

        fragmentManager?.beginTransaction()!!
            .replace(R.id.frame_holder,fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack(name)
            .commit()
    }

    private fun setUpRealmDb() {
        realmDb = RealmDataBaseCenter()
    }

    fun newInstance(): PortfolioFragmentHolder {
        val args = Bundle()
        val fragment =
            PortfolioFragmentHolder()
        fragment.arguments = args
        return fragment
    }

}