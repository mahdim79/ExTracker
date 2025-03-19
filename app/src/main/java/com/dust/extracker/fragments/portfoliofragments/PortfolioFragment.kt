package com.dust.extracker.fragments.portfoliofragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.dust.extracker.R
import com.dust.extracker.adapters.viewpagersadapters.PortfolioBaseViewPagerAdapter
import com.dust.extracker.fragments.marketfragments.CryptoDetailsFragment
import com.dust.extracker.fragments.portfoliofragments.innerfragments.EditHistoryNameFragment
import com.dust.extracker.interfaces.OnHistoryFragmentUpdate
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class PortfolioFragment:Fragment() {

    private lateinit var portfolio_main_viewpager:ViewPager
    private lateinit var totalFund:TextView
    private lateinit var dots_indicator:DotsIndicator
    private lateinit var realmDB:RealmDataBaseCenter
    private lateinit var addmoreFragments:AddMoreFragments
    private lateinit var deleteFragment:DeleteFragment
    private lateinit var adapter: PortfolioBaseViewPagerAdapter
    private lateinit var onClickTransactionData:OnClickTransactionData
    private lateinit var onUpdateTotalFund:OnUpdateTotalFund

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_portfolio , container , false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRealmDB()
        setUpViews(view)
        setUpViewPagerAdapter()
    }

    private fun setUpRealmDB() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setUpViewPagerAdapter() {
        val data = realmDB.getAllHistoryData()
        if (data.isEmpty()){
         //   fragmentManager?.popBackStack("PortfolioFragment" , FragmentManager.POP_BACK_STACK_INCLUSIVE)
            fragmentManager?.beginTransaction()!!
                .replace(R.id.frame_holder,EmptyPortfolioFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("EmptyPortfolioFragment")
                .commit()
            return
        }
        adapter =
            PortfolioBaseViewPagerAdapter(
                data,
                childFragmentManager,
                object : OnHistoryFragmentUpdate {
                    override fun onHistoryFragmentUpdate() {
                        /*fragmentManager?.popBackStack(
                            "PortfolioFragment",
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )*/
                        fragmentManager?.beginTransaction()!!
                            .replace(R.id.frame_holder, PortfolioFragment())
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack("PortfolioFragment")
                            .commit()
                    }
                })
        portfolio_main_viewpager.adapter =adapter

        dots_indicator.setViewPager(portfolio_main_viewpager)
        portfolio_main_viewpager.offscreenPageLimit = realmDB.getHistoryDataCount() + 2

    }

    private fun setUpViews(view: View) {
        portfolio_main_viewpager = view.findViewById(R.id.portfolio_main_viewpager)
        dots_indicator = view.findViewById(R.id.dots_indicator)
        totalFund = view.findViewById(R.id.totalFund)
        setUpTotalFund()
    }

    private fun setUpTotalFund(){
        var money = 0.toDouble()
        realmDB.getAllHistoryData().forEach {
            money += it.totalCapital
        }
        totalFund.text = "${requireActivity().resources.getString(R.string.total_fund)} ${String.format("%.2f" , money)} $"
    }

    fun newInstance(): PortfolioFragment {
        val args = Bundle()
        val fragment =
            PortfolioFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onStart() {
        super.onStart()
        deleteFragment = DeleteFragment()
        onClickTransactionData = OnClickTransactionData()
        addmoreFragments = AddMoreFragments()
        onUpdateTotalFund = OnUpdateTotalFund()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requireActivity().registerReceiver(addmoreFragments , IntentFilter("com.dust.extracker.addMoreHistory"),Context.RECEIVER_EXPORTED)
            requireActivity().registerReceiver(deleteFragment , IntentFilter("com.dust.extracker.DeleteFragment"),Context.RECEIVER_EXPORTED)
            requireActivity().registerReceiver(onClickTransactionData , IntentFilter("com.dust.extracker.OnClickTransactionData"),Context.RECEIVER_EXPORTED)
            requireActivity().registerReceiver(onUpdateTotalFund , IntentFilter("com.dust.extracker.OnUpdateTotalFund"),Context.RECEIVER_EXPORTED)
        }else{
            requireActivity().registerReceiver(addmoreFragments , IntentFilter("com.dust.extracker.addMoreHistory"))
            requireActivity().registerReceiver(deleteFragment , IntentFilter("com.dust.extracker.DeleteFragment"))
            requireActivity().registerReceiver(onClickTransactionData , IntentFilter("com.dust.extracker.OnClickTransactionData"))
            requireActivity().registerReceiver(onUpdateTotalFund , IntentFilter("com.dust.extracker.OnUpdateTotalFund"))
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(addmoreFragments)
        requireActivity().unregisterReceiver(deleteFragment)
        requireActivity().unregisterReceiver(onClickTransactionData)
        requireActivity().unregisterReceiver(onUpdateTotalFund)
    }

    inner class AddMoreFragments:BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            var bool = false
            var PortfolioName = "SecKey=sdffgvbnmsdfghjkrtyuio"
            if (p1!!.extras != null && p1.extras!!.containsKey("IS_TRANSACTION")){
                PortfolioName = p1.extras!!.getString("PortfolioName")!!
                bool = p1.extras!!.getBoolean("IS_TRANSACTION" , false)
            }

            fragmentManager?.beginTransaction()!!
                .replace(R.id.frame_holder,
                    SelectCtyptoFragment().newInstance(bool ,PortfolioName)
                )
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("SelectCtyptoFragment")
                .commit()
        }
    }

    inner class DeleteFragment:BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            fragmentManager?.popBackStack("PortfolioFragment" , FragmentManager.POP_BACK_STACK_INCLUSIVE)
            fragmentManager?.beginTransaction()!!
                .replace(R.id.frame_holder, EmptyPortfolioFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("EmptyPortfolioFragment")
                .commit()
        }
    }

    inner class OnClickTransactionData:BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {

            if (p1!!.extras != null && p1.extras!!.containsKey("COIN_NAME")){

                fragmentManager!!.beginTransaction()
                    .replace(R.id.frame_holder , CryptoDetailsFragment().newInstance(p1.extras!!.getString("COIN_NAME" , "BTC")))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("CryptoDetailsFragment")
                    .commit()
            }else if (p1.extras != null && p1.extras!!.containsKey("PORTFOLIO_NAME_EDIT")){
                fragmentManager!!.beginTransaction()
                    .replace(R.id.frame_holder , EditHistoryNameFragment().newInstance(p1.extras!!.getString("PORTFOLIO_NAME_EDIT" , "")))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("EditHistoryNameFragment")
                    .commit()
            }
        }
    }

    inner class OnUpdateTotalFund:BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            setUpTotalFund()
        }
    }

}