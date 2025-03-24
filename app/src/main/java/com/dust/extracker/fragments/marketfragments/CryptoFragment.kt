package com.dust.extracker.fragments.marketfragments

import android.Manifest
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.toolbox.Volley
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.MarketRecyclerViewAdapter
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.customviews.CButton
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.LastChangeDataClass
import com.dust.extracker.dataclasses.PriceDataClass
import com.dust.extracker.interfaces.*
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import androidx.core.graphics.drawable.toDrawable

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
    private var timer: Timer? = null
    private var INDEX: Int? = 0

    private lateinit var recyclerAdapter:MarketRecyclerViewAdapter

    var dollarPrice = 0.0

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it)
            requestPermissions()
    }

    private val batteryIgnoreLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}


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
        realmDB.getDollarPrice()?.price?.toDouble()?.let {
            dollarPrice = it
        }
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
            if (checkConnection()) {
                updateOnline()
            }
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
        ondataRecieve = onDataRecieve()
        connectionReceiver = ConnectionReceiver()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requireActivity().registerReceiver(ondataRecieve, IntentFilter("com.dust.extracker.onGetMainData"),Context.RECEIVER_EXPORTED)
            requireActivity().registerReceiver(
                connectionReceiver,
                IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"),
                Context.RECEIVER_EXPORTED
            )
        }else{
            requireActivity().registerReceiver(ondataRecieve, IntentFilter("com.dust.extracker.onGetMainData"))
            requireActivity().registerReceiver(
                connectionReceiver,
                IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
            )
        }
        if (INDEX != 0)
            startTimer()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(ondataRecieve)
        requireActivity().unregisterReceiver(connectionReceiver)
        stopTimer()
    }

    private fun setUpDataBase() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setDataRequest() {
        loadData()
        if (checkConnection()){
            val api = ApiCenter(requireActivity(), this)
            api.getAllCryptoList()
        }
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

        recyclerAdapter = MarketRecyclerViewAdapter(
            requireActivity(),
            alphaAnimation,
            dollarPrice
        )
        market_recyclerView.adapter = recyclerAdapter


    }

    fun newInstance(): CryptoFragment {
        val args = Bundle()
        val fragment = CryptoFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {
        realmDB.insertAllCryptoData(cryptoList, this@CryptoFragment, requireActivity())
    }

    override fun onGetByName(price: Double, dataNum: Int) {

    }

    override fun onAddComplete() {
        requireActivity().sendBroadcast(Intent("com.dust.extracker.onGetMainData"))
        runPermissionCheckProcess()
    }

    private fun runPermissionCheckProcess() {
        if (checkRunTimePermissionNeed()){
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_grant_permission)
        dialog.findViewById<CButton>(R.id.dialog_grantPermission_continue).setOnClickListener {
            dialog.dismiss()
            requestPermissions()
        }
        dialog.apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            window?.setDimAmount(0.4f)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            show()
        }
    }

    private fun checkRunTimePermissionNeed():Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                return true
            }
        }
        if (!isIgnoringBatteryOptimization())
            return true
        return false
    }

    private fun requestPermissions(){
        // notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // battery optimization
        if (!isIgnoringBatteryOptimization()){
            val intent = Intent()
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:${requireContext().packageName}")
            batteryIgnoreLauncher.launch(intent)
        }
    }

    private fun isIgnoringBatteryOptimization(): Boolean {
        val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(requireContext().packageName)
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

    private fun updatePriceList(list: List<String>) {
        Log.i("updatePriceList","call")
        realmDB.getDollarPrice()?.price?.let {
            val prices = realmDB.getPricesByIds(list)
            val intent = Intent("com.dust.extracker.UPDATE_ITEMS")
            intent.putExtra("PRICE", it)
            for (i in prices.indices) {
                intent.putExtra(prices[i].name, prices[i].price)
            }

            requireActivity().sendBroadcast(intent)
        }
    }

    private fun updateDailyChangesList(list: List<String>) {
        val changes = realmDB.getLastDailyChangesByIds(list)
        val intent = Intent("com.dust.extracker.UPDATE_ITEMS")
        intent.putExtra("IS_DAILY_CHANGE", true)
        for (i in changes.indices) {
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
            coinList.add(datalist[i].Symbol!!)
        apiCenter.getMainPrices(coinList.joinToString(","), this)
        apiCenter.getDailyChanges(coinList.joinToString(","), this)
    }

    private fun updateOffline() {
        if (datalist.isEmpty())
            return
        recyclerAdapter.submitList(datalist)
        INDEX = 1
        if (timer == null)
            startTimer()
        market_progressBar.visibility = View.GONE

        realmDB.getDollarPrice()?.let { price ->
            val intent = Intent("com.dust.extracker.onDollarPriceRecieve")
            intent.putExtra("PRICE", price.price)
            requireActivity().sendBroadcast(intent)
        }
    }

}