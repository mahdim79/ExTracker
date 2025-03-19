package com.dust.extracker.fragments.mainviewpagerfragments

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.inputmethodservice.InputMethodService
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.dust.extracker.R
import com.dust.extracker.adapters.viewpagersadapters.MarketViewPagerAdapter
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.application.MyApplication
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.DollarInfoDataClass
import com.dust.extracker.fragments.marketfragments.CryptoDetailsFragment
import com.dust.extracker.fragments.marketfragments.TradingViewChartFragment
import com.dust.extracker.fragments.othersfragment.NotificationChooseCryptoFragment
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnGetDollarPrice
import com.dust.extracker.interfaces.OnGetTotalMarketCap
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.dust.extracker.utils.Utils
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MarketFragment : Fragment(), OnGetDollarPrice, OnGetAllCryptoList {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager
    private lateinit var header_rel: RelativeLayout
    private lateinit var search_linear: LinearLayout
    private lateinit var access_txt: TextView
    private lateinit var dollar_price: CTextView
    private lateinit var totalMarketCap: CTextView
    private lateinit var img_search: ImageView
    private lateinit var back_btn: ImageView
    private lateinit var notificationIcon: ImageView
    private lateinit var edt_search: EditText
    private lateinit var onNetworkConnected: OnNetworkConnected
    lateinit var realmDB: RealmDataBaseCenter
    lateinit var apiCenter: ApiCenter
    lateinit var marketCapTimer: Timer
    lateinit var shared: SharedPreferencesCenter
    var SEARCHMODE = false

    private var searchJob: Job? = null

    private lateinit var onClickMainData: OnClickMainData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_market, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpViewPager()
        setUpApiCenter()
        setUpDataBase()
        setUpSharedPreferencesCenter()
        try {
            totalMarketCap.text =
                "${Utils.formatPriceNumber((shared.getMarketCap().toDouble() / 1000000000),2)} B$"
        } catch (e: Exception) {
        }
        loadDollarPrice()
        setUpSearchTool()
    }

    private fun hideKeyBoard() {
        val softInputManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        softInputManager.hideSoftInputFromWindow(
            edt_search.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    private fun runNotificationFragment() {
        requireFragmentManager().beginTransaction()
            .replace(
                R.id.main_frame,
                NotificationChooseCryptoFragment().newInstance()
            )
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("NotificationChooseCryptoFragment")
            .commit()
    }

    private fun setUpSearchTool() {

        img_search.setOnClickListener {
            header_rel.visibility = View.GONE
            search_linear.visibility = View.VISIBLE
            sendSearchBroadcast("", "com.dust.extracker.kdsfjgksdjflasdk.START")
            SEARCHMODE = true
        }
        back_btn.setOnClickListener {
            search_linear.visibility = View.GONE
            header_rel.visibility = View.VISIBLE
            hideKeyBoard()
            sendSearchBroadcast("", "com.dust.extracker.kdsfjgksdjflasdk.STOP")
            SEARCHMODE = false
            edt_search.setText("")
        }

        val allData = realmDB.getAllCryptoData()
        val allExchangers = realmDB.getAllExchangerData()
        edt_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                if (!SEARCHMODE)
                    return

                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    var exchanger = ""
                    var mainData = ""
                    if (edt_search.text.toString() == "") {
                        sendSearchBroadcast(exchanger, mainData)
                    }else{
                        //set Main Data
                        val listTemp1 = arrayListOf<String>()
                        for (i in 0 until allData.size) {
                            if (allData[i]!!.FullName!!.indexOf(
                                    edt_search.text.toString(),
                                    ignoreCase = true
                                ) != -1
                            ) {
                                try {
                                    listTemp1.add(allData[i]!!.ID!!)
                                } catch (e: Exception) {
                                }
                            }
                        }
                        mainData = listTemp1.joinToString(",")

                        // set Exchanger Data
                        val listTemp = arrayListOf<Int>()
                        for (i in 0 until allExchangers.size) {
                            if (allExchangers[i]!!.name.indexOf(
                                    edt_search.text.toString(),
                                    ignoreCase = true
                                ) != -1
                            ) {
                                try {
                                    listTemp.add(allExchangers[i]!!.id!!)
                                } catch (e: Exception) {
                                }
                            }
                        }
                        exchanger = listTemp.joinToString(",")
                        sendSearchBroadcast(exchanger, mainData)
                    }
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })


    }

    private fun sendSearchBroadcast(exchanger: String, data: String) {
        val intent = Intent("com.dust.extracker.OnSearchData")
        intent.putExtra("EXTRA_DATA", data)
        intent.putExtra("EXTRA_DATA_EXCHANGER", exchanger)
        requireActivity().sendBroadcast(intent)
    }

    private fun setUpSharedPreferencesCenter() {
        shared = SharedPreferencesCenter(requireActivity())
    }

    private fun setTotalMarketCap() {
        apiCenter.getTotalMarketCap(object : OnGetTotalMarketCap {
            override fun onGetMarketCap(marketCap: Double) {
                shared.setMarketCap(marketCap.toString())
                totalMarketCap.text = "${Utils.formatPriceNumber((marketCap / 1000000000),0)} B$"
            }
        })
    }

    private fun setUpApiCenter() {
        apiCenter = ApiCenter(requireActivity(), this)
    }

    private fun setUpViewPager() {
        viewPager.adapter =
            MarketViewPagerAdapter(
                childFragmentManager,
                requireActivity()
            )
        viewPager.offscreenPageLimit = 6
        tabLayout.setupWithViewPager(viewPager)
        for (i in 0 until tabLayout.childCount) {
            val viewGroup: ViewGroup = tabLayout.getChildAt(i) as ViewGroup
            for (j in 0 until viewGroup.childCount) {
                val viewGroup2: ViewGroup = viewGroup.getChildAt(j) as ViewGroup
                for (k in 0 until viewGroup2.childCount) {
                    if (viewGroup2.getChildAt(k) is TextView) {
                        (viewGroup2.getChildAt(k) as TextView).textSize = 8.5f
                        (viewGroup2.getChildAt(k) as TextView).typeface =
                            (requireActivity().applicationContext as MyApplication).initializeTypeFace()
                    }
                }
            }
        }

        viewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {

                if (!SEARCHMODE)
                    if (position == 3) {
                        header_rel.visibility = View.GONE
                        access_txt.visibility = View.VISIBLE
                    } else {
                        if (!header_rel.isVisible) {
                            header_rel.visibility = View.VISIBLE
                            access_txt.visibility = View.GONE
                        }
                    }
                else
                    if (position == 3) {
                        search_linear.visibility = View.GONE
                        access_txt.visibility = View.VISIBLE
                    } else {
                        if (!search_linear.isVisible) {
                            search_linear.visibility = View.VISIBLE
                            access_txt.visibility = View.GONE
                        }
                    }
            }
        })
    }

    private fun setUpViews(view: View) {
        tabLayout = view.findViewById(R.id.marketTabLayout)
        notificationIcon = view.findViewById(R.id.notificationIcon)
        viewPager = view.findViewById(R.id.marketViewPager)
        header_rel = view.findViewById(R.id.header_rel)
        access_txt = view.findViewById(R.id.access_txt)
        dollar_price = view.findViewById(R.id.dollar_price)
        totalMarketCap = view.findViewById(R.id.totalMarketCap)
        search_linear = view.findViewById(R.id.search_linear)
        img_search = view.findViewById(R.id.img_search)
        back_btn = view.findViewById(R.id.back_btn)
        edt_search = view.findViewById(R.id.edt_search)
        notificationIcon.setOnClickListener {
            if (shared.getNotificationEnabled())
                runNotificationFragment()
            else
                Toast.makeText(requireActivity(), requireActivity().resources.getString(R.string.notificationDisabled), Toast.LENGTH_LONG).show()
        }
    }

    private fun setUpDataBase() {
        realmDB = RealmDataBaseCenter()
    }

    fun newInstance(): MarketFragment {
        val args = Bundle()
        val fragment =
            MarketFragment()
        fragment.arguments = args
        return fragment
    }

    private fun loadDollarPrice() {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val res = networkInfo != null && networkInfo.isConnectedOrConnecting
        if (res) {
            apiCenter.getDollarPrice(this)
        } else {
            setDollarPrice()
        }
    }

    private fun setDollarPrice() {
        if (realmDB.checkDollarPriceAvailability()) {
            realmDB.getDollarPrice()?.price?.toDouble()?.let { dollarPrice ->
                try {
                    dollar_price.text = "${Utils.formatPriceNumber(dollarPrice,0)} T"
                }catch (e:Exception){}
            }
        }
    }

    override fun onGet(price: DollarInfoDataClass) {
        realmDB.insertDollarPrice(price)
        val intent = Intent("com.dust.extracker.onDollarPriceRecieve")
        intent.putExtra("PRICE", price.price)
        requireActivity().sendBroadcast(intent)
        setDollarPrice()
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {}

    override fun onGetByName(price: Double, dataNum: Int) {}

    override fun onStart() {
        super.onStart()
        startMarketCapTimer()
        onNetworkConnected = OnNetworkConnected()
        requireActivity().registerReceiver(
            onNetworkConnected,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
        onClickMainData = OnClickMainData()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requireActivity().registerReceiver(
                onClickMainData,
                IntentFilter("com.dust.extracker.OnClickMainData"),
                Context.RECEIVER_EXPORTED
            )
        }else{
            requireActivity().registerReceiver(
                onClickMainData,
                IntentFilter("com.dust.extracker.OnClickMainData")
            )        }

    }

    private fun startMarketCapTimer() {
        marketCapTimer = Timer()
        marketCapTimer.schedule(object : TimerTask() {
            override fun run() {
                Log.i("errorLog1", "timer")
                if (checkNetWorkConnectivity()) {
                    setTotalMarketCap()
                } else {
                    marketCapTimer.purge()
                    marketCapTimer.cancel()
                }
            }
        }, 1000, 60000)
    }

    override fun onStop() {
        stopMarketCapTimer()
        requireActivity().unregisterReceiver(onClickMainData)
        requireActivity().unregisterReceiver(onNetworkConnected)
        super.onStop()
    }

    override fun onDestroy() {
        try {
            marketCapTimer.purge()
            marketCapTimer.cancel()
        } catch (e: Exception) {
        }
        super.onDestroy()
    }

    private fun stopMarketCapTimer() {
        try {
            marketCapTimer.purge()
            marketCapTimer.cancel()
        } catch (e: Exception) {
        }
    }

    private fun checkNetWorkConnectivity(): Boolean {
        var netInfo: NetworkInfo? = null
        try {
            val connectivityManager =
                requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            netInfo = connectivityManager.activeNetworkInfo
        } catch (e: Exception) {
            return false
        }
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    inner class OnNetworkConnected : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.i("errorLog1", "receiver")

            if (checkNetWorkConnectivity())
                startMarketCapTimer()
            else
                stopMarketCapTimer()
        }
    }

    inner class OnClickMainData : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1!!.extras != null && p1.extras!!.containsKey("COIN_NAME")) {

                fragmentManager!!.beginTransaction()
                    .replace(
                        R.id.main_frame,
                        CryptoDetailsFragment().newInstance(
                            p1.extras!!.getString(
                                "COIN_NAME",
                                "BTC"
                            ), "CryptoDetailsFragment_MAIN"
                        )
                    )
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("CryptoDetailsFragment_MAIN")
                    .commit()

            }

            if (p1.extras != null && p1.extras!!.containsKey("COIN")) {

                fragmentManager!!.beginTransaction()
                    .replace(
                        R.id.main_frame,
                        TradingViewChartFragment().newInstance(p1.extras!!.getString("COIN", "BTC"))
                    )
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("TradingViewChartFragment")
                    .commit()

            }
        }
    }
}