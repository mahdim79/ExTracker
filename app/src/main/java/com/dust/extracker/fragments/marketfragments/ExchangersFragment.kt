package com.dust.extracker.fragments.marketfragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.MarketExchangerRecyclerViewAdapter
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.ExchangerDataClass
import com.dust.extracker.interfaces.OnExchangerRealmDataAdded
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnGetExchangersData
import com.dust.extracker.realmdb.ExchangerObject
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.RealmDataBaseCenter

class ExchangersFragment : Fragment(), OnGetExchangersData, OnExchangerRealmDataAdded {

    private lateinit var exchanger_recyclerView: RecyclerView
    private lateinit var exchanger_progressBar: ProgressBar
    private lateinit var exchanger_main_nested: NestedScrollView
    lateinit var realmDB: RealmDataBaseCenter
    lateinit var apiService: ApiCenter
    var list = arrayListOf<ExchangerObject>()

    var PaginationCount: Int = 1


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exchangers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpApiService()
        setUpRealmDB()
        setUpViews(view)
        setPrimaryData()
        setUpPagination()
    }

    private fun setUpPagination() {
        exchanger_main_nested.setOnScrollChangeListener { view: NestedScrollView?, _: Int, i2: Int, _: Int, _: Int ->
            if (i2 == view!!.getChildAt(0).measuredHeight - view.measuredHeight){
                if (PaginationCount != 3){
                    PaginationCount++
                    list.addAll(realmDB.getExchangersData(PaginationCount))
                    setUpRecyclerViewAdapter(list)
                }
            }
        }
    }

    private fun setPrimaryData() {
        if (realmDB.getExchangersDataCount() != 0){
            list.addAll(realmDB.getExchangersData(PaginationCount))
            setUpRecyclerViewAdapter(list)
        }

        if (checkNetWorkConnectivity()) {
            apiService.getExchangersData(this)
        }
    }

    private fun setUpRealmDB() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setUpApiService() {
        apiService = ApiCenter(requireContext(), object : OnGetAllCryptoList {
            override fun onGet(cryptoList: List<CryptoMainData>) {}

            override fun onGetByName(price: Double, dataNum: Int) {}
        })
    }

    private fun setUpRecyclerViewAdapter(data: List<ExchangerObject>) {
        exchanger_recyclerView.adapter = MarketExchangerRecyclerViewAdapter(requireContext(), data)
        exchanger_progressBar.visibility = View.GONE
    }

    private fun setUpViews(view: View) {

        exchanger_recyclerView = view.findViewById(R.id.exchanger_recyclerView)
        exchanger_progressBar = view.findViewById(R.id.exchanger_progressBar)
        exchanger_main_nested = view.findViewById(R.id.exchanger_main_nested)

        exchanger_recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

    }

    fun newInstance(): ExchangersFragment {
        val args = Bundle()
        val fragment = ExchangersFragment()
        fragment.arguments = args
        return fragment
    }

    private fun checkNetWorkConnectivity(): Boolean {
        val conn = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = conn.activeNetworkInfo
        return info != null && info.isConnectedOrConnecting
    }

    override fun onGetExchangerData(list: List<ExchangerDataClass>) {
        if (realmDB.getExchangersDataCount() != 0) {
            realmDB.updateExchangersData(list)
            setUpRecyclerViewAdapter(realmDB.getExchangersData(PaginationCount))
        } else {
            realmDB.insertExchangerData(list, this)
        }
    }

    override fun onExchangerDataAdded() {
        list.clear()
        list.addAll(realmDB.getExchangersData(PaginationCount))
        setUpRecyclerViewAdapter(list)
    }

}