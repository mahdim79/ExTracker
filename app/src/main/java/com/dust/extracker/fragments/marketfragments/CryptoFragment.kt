package com.dust.extracker.fragments.marketfragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.toolbox.Volley
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.MarketRecyclerViewAdapter
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.LastChangeDataClass
import com.dust.extracker.dataclasses.PriceDataClass
import com.dust.extracker.interfaces.*
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import java.util.*

class CryptoFragment : Fragment(), OnGetAllCryptoList, OnRealmDataChanged, OnGetMainPrices,
    OnGetDailyChanges {

    private lateinit var market_recyclerView: RecyclerView
    private lateinit var market_progressBar: ProgressBar
    private lateinit var Crypto_main_nested: NestedScrollView
    private lateinit var crypto_swipe: SwipeRefreshLayout
    private lateinit var sort_refresh: TextView
    private lateinit var sort_by_name: TextView
    private lateinit var sort_by_price: TextView
    private lateinit var sort_by_daily_change: TextView
    private lateinit var sort_by_comments: TextView
    lateinit var realmDB: RealmDataBaseCenter
    lateinit var apiCenter: ApiCenter
    private var datalist = arrayListOf<MainRealmObject>()
    private lateinit var ondataRecieve: onDataRecieve

    private lateinit var connectionReceiver: ConnectionReceiver
    private lateinit var alphaAnimation: AlphaAnimation
    private lateinit var searchNotifier: SearchNotifier
    private var timer: Timer? = null
    private var INDEX: Int? = 0
    private var SEARCH_MODE = false

    private val SORT_BY_REFRESH = 0
    private val SORT_BY_NAME = 1
    private val SORT_BY_PRICE = 2
    private val SORT_BY_DAILY_CHANGE = 3
    private val SORT_BY_COMMENTS = 4
    private val SORT_ASCENDING = 10
    private val SORT_DESCENDING = 11

    private var SORT = SORT_BY_REFRESH
    private var SORT_TYPE = SORT_ASCENDING

    var dollarPrice = 0.0

    val priceTotalList = arrayListOf<PriceDataClass>()
    val PCTTotalList = arrayListOf<LastChangeDataClass>()

    private var RequestNum = 0
    private var RequestNumPCT = 0
    private var RequestTotal = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crypto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAlphaAnimation()
        setUpViews(view)
        setUpSwipeRefreshLayOut()
        setUpApiCenter()
        setUpDataBase()
        setDollarPrice()
        setDataRequest()
    }


    private fun setDollarPrice() {
        dollarPrice = realmDB.getDollarPrice().price.toDouble()
    }

    fun startTimer() {
        if (timer == null) {
            if (checkConnection()) {
                Log.i("Update", "xsta")
                timer = Timer()
                timer!!.schedule(MyTimerTask(), 10000, 15000)
            }
        }
    }

    private fun stopTimer() {
        if (timer != null) {
            Log.i("Update", "xsto")
            timer!!.purge()
            timer!!.cancel()
            timer = null
        }
    }

    private fun setUpAlphaAnimation() {
        alphaAnimation = AlphaAnimation(0.3f, 1.0f)
        alphaAnimation.duration = 1000
    }

    private fun setUpSwipeRefreshLayOut() {
        crypto_swipe.setColorSchemeColors(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.CYAN
        )
        crypto_swipe.setOnRefreshListener {
            if (checkConnection()) {
                updateOnline()
            } else {
                Toast.makeText(requireActivity(), "No Connection!", Toast.LENGTH_SHORT).show()
            }
            crypto_swipe.isRefreshing = false
        }
    }

    private fun setUpApiCenter() {
        apiCenter = ApiCenter(requireActivity(), this)
    }

    inner class onDataRecieve : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            loadData()
        }
    }

    inner class ConnectionReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (checkConnection())
                if (INDEX != 0) {
                    startTimer()
                }
        }
    }

    inner class MyTimerTask : TimerTask() {
        override fun run() {
            requireActivity().runOnUiThread {
                if (checkConnection()) {
                    if (INDEX != 0) {
                        Log.i("Update", "xtask")
                        updateOnline()
                    }
                } else {
                    stopTimer()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        searchNotifier = SearchNotifier()
        ondataRecieve = onDataRecieve()
        connectionReceiver = ConnectionReceiver()
        requireActivity().registerReceiver(ondataRecieve, IntentFilter("com.dust.extracker.onGetMainData"))
        requireActivity().registerReceiver(searchNotifier, IntentFilter("com.dust.extracker.OnSearchData"))
        requireActivity().registerReceiver(
            connectionReceiver,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
        if (INDEX != 0)
            startTimer()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(ondataRecieve)
        requireActivity().unregisterReceiver(connectionReceiver)
        requireActivity().unregisterReceiver(searchNotifier)
        stopTimer()
    }

    private fun setUpDataBase() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setDataRequest() {
        if (realmDB.getCryptoDataCount() != 0) {
            loadData()
            return
        }
        val api = ApiCenter(requireActivity(), this)
        api.getAllCryptoList()
    }

    private fun checkConnection(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return info != null && info.isConnectedOrConnecting
    }

    private fun loadData() {
        datalist.clear()

        datalist.addAll(realmDB.getPopularCoins())

        updateOffline()
        if (checkConnection()) {
            apiCenter.getMarketCapSortOrder(object : OnUpdateSortOrder {
                override fun onUpdateSortOrder(list: List<Pair<String, Int>>) {
                    realmDB.updateSortOrders(list, object : OnRealmDataSorted {
                        override fun onSortFinished() {
                            datalist.clear()
                            datalist.addAll(realmDB.getPopularCoins())
                            updateOnline()
                        }
                    })
                }
            })
        }
    }


    private fun setUpViews(view: View) {

        market_recyclerView = view.findViewById(R.id.market_recyclerView)
        market_progressBar = view.findViewById(R.id.market_progressBar)
        Crypto_main_nested = view.findViewById(R.id.Crypto_main_nested)
        crypto_swipe = view.findViewById(R.id.crypto_swipe)
        sort_refresh = view.findViewById(R.id.sort_refresh)
        sort_by_name = view.findViewById(R.id.sort_by_name)
        sort_by_price = view.findViewById(R.id.sort_by_price)
        sort_by_daily_change = view.findViewById(R.id.sort_by_daily_change)
        sort_by_comments = view.findViewById(R.id.sort_by_comments)

        market_recyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

    }

    fun newInstance(): CryptoFragment {
        val args = Bundle()
        val fragment = CryptoFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {
        updateSortOrders(cryptoList)
    }

    override fun onGetByName(price: Double, dataNum: Int) {

    }

    override fun onAddComplete(list: List<MainRealmObject>) {
        requireActivity().sendBroadcast(Intent("com.dust.extracker.onGetMainData"))
        loadData()
    }

    override fun onGetPrices(priceList: List<PriceDataClass>) {
        realmDB.updatePrices(datalist, priceList)
        val idlist = arrayListOf<String>()
        for (i in 0 until datalist.size)
            idlist.add(datalist[i].ID!!)
        try {
            updatePriceList(idlist)
        }catch (e:Exception){}
    }

    fun updatePriceList(list: List<String>) {
        val prices = realmDB.getPricesByIds(list)
        val intent = Intent("com.dust.extracker.UPDATE_ITEMS")
        intent.putExtra("PRICE", realmDB.getDollarPrice().price)
        for (i in 0 until prices.size) {
            intent.putExtra(prices[i].name, prices[i].price)
        }

        requireActivity().sendBroadcast(intent)

    }

    private fun updateDailyChangesList(list: List<String>) {
        val changes = realmDB.getLastDailyChangesByIds(list)
        val intent = Intent("com.dust.extracker.UPDATE_ITEMS")
        intent.putExtra("IS_DAILY_CHANGE", true)
        for (i in 0 until changes.size) {
            intent.putExtra(changes[i].CoinName, changes[i].ChangePercentage)
        }

        requireActivity().sendBroadcast(intent)

    }

    override fun onGetDailyChanges(list: List<LastChangeDataClass>) {
        realmDB.updateDailyChanges(datalist, list)
        val idlist = arrayListOf<String>()
        for (i in 0 until datalist.size)
            idlist.add(datalist[i].ID!!)
        try {
            updateDailyChangesList(idlist)
        }catch (e:Exception){}
    }

    private fun updateOnline() {
        val coinList = arrayListOf<String>()
        for (i in 0 until datalist.size)
            coinList.add(datalist[i].Name!!)
        apiCenter.getMainPrices(coinList.joinToString(","), this)
        apiCenter.getDailyChanges(coinList.joinToString(","), this)
    }

    private fun updateOffline() {
        market_recyclerView.adapter =
            MarketRecyclerViewAdapter(
                datalist,
                requireActivity(),
                alphaAnimation,
                dollarPrice
            )
        INDEX = 1
        if (timer == null)
            startTimer()
        market_progressBar.visibility = View.GONE

        val price = realmDB.getDollarPrice()
        val intent = Intent("com.dust.extracker.onDollarPriceRecieve")
        intent.putExtra("PRICE", price.price)
        requireActivity().sendBroadcast(intent)
    }

    private fun updateSortOrders(list1: List<CryptoMainData>) {
        apiCenter.getMarketCapSortOrder(object : OnUpdateSortOrder {
            override fun onUpdateSortOrder(list: List<Pair<String, Int>>) {
                list.forEach { pair ->
                    list1.forEach {
                        if (pair.first == it.CoinName)
                            it.SortOrder = pair.second.toString()
                    }
                }
                realmDB.insertAllCryptoData(list1, this@CryptoFragment, requireActivity())
            }
        })
    }

    inner class SearchNotifier : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1!!.extras != null && p1.extras!!.containsKey("EXTRA_DATA")) {
                val data = p1.extras!!.getString("EXTRA_DATA")
                if (data == "com.dust.extracker.kdsfjgksdjflasdk.START") {
                    SEARCH_MODE = true
                    stopTimer()
                    market_recyclerView.adapter =
                        MarketRecyclerViewAdapter(
                            arrayListOf(),
                            requireActivity(),
                            alphaAnimation,
                            dollarPrice
                        )
                } else if (data == "com.dust.extracker.kdsfjgksdjflasdk.STOP") {
                    SEARCH_MODE = false
                    startTimer()
                    market_recyclerView.adapter =
                        MarketRecyclerViewAdapter(
                            datalist,
                            requireActivity(),
                            alphaAnimation,
                            dollarPrice
                        )
                } else {
                    if (data == "") {
                        market_recyclerView.adapter =
                            MarketRecyclerViewAdapter(
                                arrayListOf(),
                                requireActivity(),
                                alphaAnimation,
                                dollarPrice
                            )
                        return
                    }
                    val results = realmDB.getCryptoDataByIds(data!!.split(","))
                    if (results.size > 25) {
                        val pList = arrayListOf<MainRealmObject>()
                        for (i in 0 until results.size) {
                            if (i < 24)
                                pList.add(results[i])
                            else
                                break
                        }
                        market_recyclerView.adapter =
                            MarketRecyclerViewAdapter(
                                pList,
                                requireActivity(),
                                alphaAnimation,
                                dollarPrice
                            )

                    } else {
                        market_recyclerView.adapter =
                            MarketRecyclerViewAdapter(
                                results,
                                requireActivity(),
                                alphaAnimation,
                                dollarPrice
                            )
                    }
                }
            }
        }
    }
}