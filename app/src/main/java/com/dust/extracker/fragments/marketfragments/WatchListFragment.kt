package com.dust.extracker.fragments.marketfragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
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
        realmDB.getDollarPrice()?.price?.toDouble()?.let {
            dollarPrice = it
        }
    }

    private fun setUpApiCenter() {
        apiCenter = ApiCenter(requireActivity() , object :OnGetAllCryptoList{
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
            requireActivity().getSharedPreferences("FAV", Context.MODE_PRIVATE).getString("fav", "")
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
            WatchListRecyclerViewAdapter(list, requireActivity() , alphaAnimation , dollarPrice)
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
                Toast.makeText(requireActivity(), requireActivity().resources.getString(R.string.connectionFailure), Toast.LENGTH_SHORT).show()
            }
            swiprefreshLayout.isRefreshing = false
        }
    }

    private fun setUpViews(view: View) {
        market_watchlist_recyclerView = view.findViewById(R.id.market_watchlist_recyclerView)
        swiprefreshLayout = view.findViewById(R.id.swiprefreshLayout)

        setUpSwipeRefreshLayOut()

        market_watchlist_recyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
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
        notifyDataChanged = NotifyDataChanged()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requireActivity().registerReceiver(
                notifyDataChanged,
                IntentFilter("com.dust.extracker.notifyDataSetChanged"),
                Context.RECEIVER_EXPORTED
            )
        }else{
            requireActivity().registerReceiver(
                notifyDataChanged,
                IntentFilter("com.dust.extracker.notifyDataSetChanged")
            )
        }

    }

    override fun onStop() {
        super.onStop()
        stopTimer()
        requireActivity().unregisterReceiver(notifyDataChanged)
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
        for (element in datalist)
            coinList.add(element.Symbol!!)
        apiCenter.getMainPrices(coinList.joinToString(","), this)
        apiCenter.getDailyChanges(coinList.joinToString(","), this)
    }


    inner class MyTimerTask : TimerTask() {
        override fun run() {
            requireActivity().runOnUiThread {
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
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return info != null && info.isConnectedOrConnecting
    }

    override fun onGetPrices(priceList: List<PriceDataClass>) {
        val datalist = realmDB.getCryptoDataByIds(favlist)
        realmDB.updatePrices(datalist, priceList)
        val idlist = arrayListOf<String>()
        for (element in datalist)
            idlist.add(element.ID!!)
        updatePriceList(idlist)
        swiprefreshLayout.isRefreshing = false
    }

    private fun updatePriceList(list: List<String>) {
        realmDB.getDollarPrice()?.price?.let {
            val prices = realmDB.getPricesByIds(list)
            val intent = Intent("com.dust.extracker.UPDATE_ITEMS_WatchList")
            intent.putExtra("PRICE" , it)
            for (i in prices.indices) {
                intent.putExtra(prices[i].name, prices[i].price)
            }
            try {
                requireActivity().sendBroadcast(intent)
            }catch (e:Exception){}
        }

    }

    private fun updateDailyChangesList(list: List<String>) {
        val changes = realmDB.getLastDailyChangesByIds(list)
        val intent = Intent("com.dust.extracker.UPDATE_ITEMS_WatchList")
        intent.putExtra("IS_DAILY_CHANGE", true)
        for (i in changes.indices) {
            intent.putExtra(changes[i].CoinName, changes[i].ChangePercentage)
        }
        try {
            requireContext().sendBroadcast(intent)
        }catch (e:Exception){}
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