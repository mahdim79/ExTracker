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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.MarketRecyclerViewAdapter
import com.dust.extracker.adapters.recyclerviewadapters.WatchListRecyclerViewAdapter
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.LastChangeDataClass
import com.dust.extracker.dataclasses.PriceDataClass
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnGetDailyChanges
import com.dust.extracker.interfaces.OnGetMainPrices
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import java.util.*

class WatchListFragment() : Fragment() , OnGetMainPrices,
    OnGetDailyChanges {

    private lateinit var market_watchlist_recyclerView: RecyclerView
    lateinit var realmDB: RealmDataBaseCenter
    lateinit var apiCenter: ApiCenter
    private lateinit var favlist: List<String>
    private lateinit var alphaAnimation: AlphaAnimation
    private lateinit var searchNotifier: SearchNotifier
    private var timer: Timer? = null
    private lateinit var swiprefreshLayout: SwipeRefreshLayout
    var dollarPrice = 0.0



    private lateinit var notifyDataChanged: NotifyDataChanged
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_watchlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpAlphaAnimation()
        setUpDataBase()
        setUpApiCenter()
        getSharedPreferencesData()
        setDollarPrice()
        if (favlist.isNotEmpty())
            setPrimaryDataInRecyclerView()
    }

    private fun setDollarPrice() {
        dollarPrice = realmDB.getDollarPrice().price.toDouble()
    }

    private fun setUpApiCenter() {
        apiCenter = ApiCenter(activity!! , object :OnGetAllCryptoList{
            override fun onGet(cryptoList: List<CryptoMainData>) {

            }

            override fun onGetByName(price: Double, dataNum: Int) {
            }

        })
    }

    private fun setUpDataBase() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setPrimaryDataInRecyclerView() {
        setUpRecyclerView(favlist)
    }

    private fun getSharedPreferencesData() {
        val rawData =
            activity!!.getSharedPreferences("FAV", Context.MODE_PRIVATE).getString("fav", "")
        if (rawData != "")
            favlist = rawData!!.split(',')
        else
            favlist = arrayListOf()
    }


    private fun setUpRecyclerView(favList: List<String>) {
        val list = realmDB.getCryptoDataByIds(favList)
        setRecyclerAdapter(list)
    }

    private fun setRecyclerAdapter(list:List<MainRealmObject>){
        market_watchlist_recyclerView.adapter =
            WatchListRecyclerViewAdapter(list, activity!! , alphaAnimation , dollarPrice)
    }

    private fun setUpAlphaAnimation() {
        alphaAnimation = AlphaAnimation(0.3f, 1.0f)
        alphaAnimation.duration = 1000
    }

    private fun setUpSwipeRefreshLayOut() {
        swiprefreshLayout.setColorSchemeColors(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.CYAN
        )
        swiprefreshLayout.setOnRefreshListener {
            if (checkConnection()) {
                updateOnline()
            } else {
                Toast.makeText(activity!!, activity!!.resources.getString(R.string.connectionFailure), Toast.LENGTH_SHORT).show()
            }
            swiprefreshLayout.isRefreshing = false
        }
    }

    private fun setUpViews(view: View) {
        market_watchlist_recyclerView = view.findViewById(R.id.market_watchlist_recyclerView)
        swiprefreshLayout = view.findViewById(R.id.swiprefreshLayout)

        setUpSwipeRefreshLayOut()

        market_watchlist_recyclerView.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
    }

    inner class NotifyDataChanged : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            refreshData()
        }
    }

    fun refreshData(){
        getSharedPreferencesData()
        setPrimaryDataInRecyclerView()
    }

    fun newInstance(): WatchListFragment {
        val args = Bundle()
        val fragment = WatchListFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onStart() {
        super.onStart()
        startTimer()
        searchNotifier = SearchNotifier()
        activity!!.registerReceiver(searchNotifier, IntentFilter("com.dust.extracker.OnSearchData"))

        notifyDataChanged = NotifyDataChanged()
        activity!!.registerReceiver(
            notifyDataChanged,
            IntentFilter("com.dust.extracker.notifyDataSetChanged")
        )
    }

    override fun onStop() {
        super.onStop()
        stopTimer()
        activity!!.unregisterReceiver(searchNotifier)
        activity!!.unregisterReceiver(notifyDataChanged)
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

    private fun updateOnline() {
        val coinList = arrayListOf<String>()
        val datalist = realmDB.getCryptoDataByIds(favlist)
        for (i in 0 until datalist.size)
            coinList.add(datalist[i].Name!!)
        apiCenter.getMainPrices(coinList.joinToString(","), this)
        apiCenter.getDailyChanges(coinList.joinToString(","), this)
    }


    inner class MyTimerTask : TimerTask() {
        override fun run() {
            activity!!.runOnUiThread {
                if (checkConnection()) {
                        updateOnline()
                } else {
                    stopTimer()
                }
            }
        }

    }


    private fun checkConnection(): Boolean {
        val connectivityManager =
            activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return info != null && info.isConnectedOrConnecting
    }

    inner class SearchNotifier: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1!!.extras != null && p1.extras!!.containsKey("EXTRA_DATA")){
                val data = p1.extras!!.getString("EXTRA_DATA")
                if (data == "com.dust.extracker.kdsfjgksdjflasdk.START"){
                    stopTimer()
                    setRecyclerAdapter(arrayListOf())
                }else if (data == "com.dust.extracker.kdsfjgksdjflasdk.STOP"){
                    startTimer()
                    setPrimaryDataInRecyclerView()
                }else{
                    if (data == ""){
                        setRecyclerAdapter(arrayListOf())
                        return
                    }
                    val results = realmDB.getCryptoDataByIds(data!!.split(","))
                    val finalData = arrayListOf<MainRealmObject>()
                    val favData = realmDB.getCryptoDataByIds(favlist)
                    results.forEach {res ->
                        favData.forEach {
                            if (res.CoinName == it.CoinName)
                                finalData.add(it)
                        }
                    }
                    if (finalData.size > 25){
                        val pList = arrayListOf<MainRealmObject>()
                        for (i in 0 until finalData.size){
                            if (i < 24)
                                pList.add(finalData[i])
                            else
                                break
                        }
                        setRecyclerAdapter(pList)
                    }else{
                        setRecyclerAdapter(finalData)
                    }
                }
            }
        }
    }

    override fun onGetPrices(priceList: List<PriceDataClass>) {
        val datalist = realmDB.getCryptoDataByIds(favlist)
        realmDB.updatePrices(datalist, priceList)
        val idlist = arrayListOf<String>()
        for (i in 0 until datalist.size)
            idlist.add(datalist[i].ID!!)
        updatePriceList(idlist)
        swiprefreshLayout.isRefreshing = false
    }

    fun updatePriceList(list: List<String>) {
        val prices = realmDB.getPricesByIds(list)
        val intent = Intent("com.dust.extracker.UPDATE_ITEMS_WatchList")
        intent.putExtra("PRICE" , realmDB.getDollarPrice().price)
        for (i in 0 until prices.size) {
            intent.putExtra(prices[i].name, prices[i].price)
        }
        try {
            activity!!.sendBroadcast(intent)
        }catch (e:Exception){}
    }

    private fun updateDailyChangesList(list: List<String>) {
        val changes = realmDB.getLastDailyChangesByIds(list)
        val intent = Intent("com.dust.extracker.UPDATE_ITEMS_WatchList")
        intent.putExtra("IS_DAILY_CHANGE", true)
        for (i in 0 until changes.size) {
            intent.putExtra(changes[i].CoinName, changes[i].ChangePercentage)
        }
        activity!!.sendBroadcast(intent)
    }

    override fun onGetDailyChanges(list: List<LastChangeDataClass>) {
        val datalist = realmDB.getCryptoDataByIds(favlist)
        realmDB.updateDailyChanges(datalist, list)
        val idlist = arrayListOf<String>()
        for (i in 0 until datalist.size)
            idlist.add(datalist[i].ID!!)
        updateDailyChangesList(idlist)
    }

}