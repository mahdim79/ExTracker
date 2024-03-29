package com.dust.extracker.fragments.marketfragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dust.extracker.R
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.customviews.CButton
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.*
import com.dust.extracker.fragments.portfoliofragments.InputDataFragment
import com.dust.extracker.interfaces.*
import com.dust.extracker.realmdb.DollarObject
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import org.w3c.dom.Text
import java.util.*

class CryptoDetailsFragment : Fragment(), OnGetChartData, View.OnClickListener,
    OnDetailsDataReceive {

    lateinit var image_back: ImageView
    lateinit var coinImg: CircleImageView
    lateinit var details_title: CTextView
    lateinit var details_fullName: TextView
    lateinit var details_text: TextView
    lateinit var tomanPrice: TextView
    lateinit var dollarPrice: TextView
    lateinit var date: TextView
    lateinit var coinPrice: TextView
    lateinit var coin_btcBase_Price: TextView
    lateinit var foreign_date: TextView
    lateinit var lineChart: LineChartView
    lateinit var chartProgressBar: ProgressBar
    lateinit var swiprefreshLayout: SwipeRefreshLayout
    private lateinit var twentyFourtime: CTextView
    private lateinit var oneWeek: CTextView
    private lateinit var oneMonth: CTextView
    private lateinit var sixMonth: CTextView
    private lateinit var threeMonth: CTextView
    private lateinit var oneYear: CTextView
    private lateinit var all_Time: CTextView
    private lateinit var TotalCoinDailyChange: TextView
    private lateinit var total_frame: FrameLayout
    private lateinit var shareImg: ImageView
    private lateinit var addToeWatchListbtn: CButton
    private lateinit var addTransactionbtn: CButton
    private lateinit var fullChart: CButton

    lateinit var rank: TextView
    lateinit var price: TextView
    lateinit var marketCap: TextView
    lateinit var dailyVolume: TextView
    lateinit var dailyChange: TextView
    lateinit var weeklyChange: TextView
    lateinit var monthlyChange: TextView
    lateinit var sixmonthChange: TextView
    lateinit var yearlyChange: TextView
    lateinit var totalSupply: TextView
    lateinit var marketCoins: TextView
    lateinit var dominance: TextView

    private var TIME_PERIOD = "24H"

    lateinit var realmDB: RealmDataBaseCenter

    private lateinit var apiService: ApiCenter

    private lateinit var freshDollarPrice: DollarObject

    private lateinit var mainObject: MainRealmObject

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crypto_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRealmDB()
        setUpApiService()
        setDollarPrice()
        setUpCoinList()
        setUpViews(view)
        hideKeyBoard()
        setData()
        requestCoinsData()
        requestChartData()

    }

    private fun hideKeyBoard() {
        val softInputManager =
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        softInputManager.hideSoftInputFromWindow(
            total_frame.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }


    private fun setUpApiService() {
        apiService = ApiCenter(activity!!, object : OnGetAllCryptoList {
            override fun onGet(cryptoList: List<CryptoMainData>) {}

            override fun onGetByName(price: Double, dataNum: Int) {}
        })
    }

    private fun checkNetworkConnectivity(): Boolean {
        val connectivityManager =
            activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    private fun requestChartData() {
        if (checkNetworkConnectivity()) {
            apiService.getChartData(mainObject.Name!!, TIME_PERIOD, this)
            chartProgressBar.visibility = View.VISIBLE
        }
    }

    private fun setDollarPrice() {
        freshDollarPrice = realmDB.getDollarPrice()
    }

    private fun setUpCoinList() {
        mainObject = realmDB.getCryptoDataByName(arguments!!.getString("COIN_NAME")!!)
    }

    private fun setUpRealmDB() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setUpViews(view: View) {

        total_frame = view.findViewById(R.id.total_frame)
        fullChart = view.findViewById(R.id.fullChart)
        addToeWatchListbtn = view.findViewById(R.id.addToeWatchListbtn)
        addTransactionbtn = view.findViewById(R.id.addTransactionbtn)
        shareImg = view.findViewById(R.id.shareImg)
        TotalCoinDailyChange = view.findViewById(R.id.TotalCoinDailyChange)
        TotalCoinDailyChange.setTextColor(Color.WHITE)
        weeklyChange = view.findViewById(R.id.weeklyChange)
        monthlyChange = view.findViewById(R.id.monthlyChange)
        sixmonthChange = view.findViewById(R.id.sixmonthChange)
        yearlyChange = view.findViewById(R.id.yearlyChange)
        marketCap = view.findViewById(R.id.marketCap)
        dailyVolume = view.findViewById(R.id.dailyVolume)
        foreign_date = view.findViewById(R.id.foreign_date)
        marketCoins = view.findViewById(R.id.marketCoins)
        dominance = view.findViewById(R.id.dominance)
        lineChart = view.findViewById(R.id.lineChart)
        chartProgressBar = view.findViewById(R.id.chartProgressBar)
        swiprefreshLayout = view.findViewById(R.id.swiprefreshLayout)
        coin_btcBase_Price = view.findViewById(R.id.coin_btcBase_Price)
        date = view.findViewById(R.id.date)
        tomanPrice = view.findViewById(R.id.tomanPrice)
        dollarPrice = view.findViewById(R.id.dollarPrice)
        rank = view.findViewById(R.id.rank)
        price = view.findViewById(R.id.price)
        dailyChange = view.findViewById(R.id.dailyChange)
        totalSupply = view.findViewById(R.id.totalSupply)
        details_text = view.findViewById(R.id.details_text)
        details_fullName = view.findViewById(R.id.details_fullName)
        coinImg = view.findViewById(R.id.coinImg)
        image_back = view.findViewById(R.id.image_back)
        details_title = view.findViewById(R.id.details_title)
        coinPrice = view.findViewById(R.id.coinPrice)
        twentyFourtime = view.findViewById(R.id.twentyFourtime)
        oneWeek = view.findViewById(R.id.oneWeek)
        oneMonth = view.findViewById(R.id.oneMonth)
        sixMonth = view.findViewById(R.id.sixMonth)
        oneYear = view.findViewById(R.id.oneYear)
        threeMonth = view.findViewById(R.id.threeMonth)
        all_Time = view.findViewById(R.id.all_Time)
        twentyFourtime.setTextColor(ContextCompat.getColor(activity!!, R.color.light_orange))

        twentyFourtime.setOnClickListener(this)
        oneWeek.setOnClickListener(this)
        oneMonth.setOnClickListener(this)
        threeMonth.setOnClickListener(this)
        sixMonth.setOnClickListener(this)
        oneYear.setOnClickListener(this)
        all_Time.setOnClickListener(this)

        swiprefreshLayout.setColorSchemeColors(
            Color.BLACK,
            Color.RED,
            Color.GREEN,
            Color.YELLOW
        )
        swiprefreshLayout.setOnRefreshListener {
            chartProgressBar.visibility = View.VISIBLE
            if (checkNetworkConnectivity()) {
                requestCoinsData()
                requestChartData()
            } else {
                chartProgressBar.visibility = View.GONE
                showErrorSnack(activity!!.resources.getString(R.string.connectionFailure))
                swiprefreshLayout.isRefreshing = false
            }
        }

        shareImg.setOnClickListener {
            val txtBody =
                "ارز دیجیتال${mainObject.Name}در بیست و چهار ساعت گذشته %${mainObject.DailyChangePCT!!} بازدهی داشته! میتونی از طریق این لینک جزییاتش رو ببینی.  www.ExSite.ir/coins/${mainObject.Name}"
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, txtBody)
            activity!!.startActivity(Intent.createChooser(intent, "ارسال با ..."))
        }

        addToeWatchListbtn.setTextColor(Color.WHITE)
        addTransactionbtn.setTextColor(Color.WHITE)
        fullChart.setTextColor(Color.WHITE)

        val sharedPreferences1 = activity!!.getSharedPreferences("FAV", Context.MODE_PRIVATE)
        if (sharedPreferences1.getString("fav", "")!!.indexOf(mainObject.ID!!) != -1) {
            addToeWatchListbtn.text = activity!!.resources.getString(R.string.deleteFromWatchlist)
            addToeWatchListbtn.background = ResourcesCompat.getDrawable(
                activity!!.resources,
                R.drawable.details_addtowatch_added,
                null
            )
        } else {
            addToeWatchListbtn.text = activity!!.resources.getString(R.string.addToWatchlist)
            addToeWatchListbtn.background = ResourcesCompat.getDrawable(
                activity!!.resources,
                R.drawable.details_addtowatch_not_added,
                null
            )
        }

        addToeWatchListbtn.setOnClickListener {
            val sharedPreferences = activity!!.getSharedPreferences("FAV", Context.MODE_PRIVATE)
            val rawData = sharedPreferences.getString("fav", "")
            val resultList = arrayListOf<String>()
            var listRawData = listOf<String>()
            if (rawData != "") {
                listRawData = rawData!!.split(',')
                resultList.addAll(listRawData)
            }
            if (rawData.indexOf("${mainObject.ID!!}") != -1) {
                resultList.remove(mainObject.ID!!)
                addToeWatchListbtn.text = activity!!.resources.getString(R.string.addToWatchlist)
                addToeWatchListbtn.background = ResourcesCompat.getDrawable(
                    activity!!.resources,
                    R.drawable.details_addtowatch_not_added,
                    null
                )

            } else {
                resultList.add(mainObject.ID!!)
                addToeWatchListbtn.text = activity!!.resources.getString(R.string.deleteFromWatchlist)
                addToeWatchListbtn.background = ResourcesCompat.getDrawable(
                    activity!!.resources,
                    R.drawable.details_addtowatch_added,
                    null
                )

            }
            sharedPreferences.edit().putString("fav", resultList.joinToString(",")).apply()
            activity!!.sendBroadcast(Intent("com.dust.extracker.notifyDataSetChanged"))
        }

        addTransactionbtn.setOnClickListener {
            var intent = Intent("com.dust.extracker.OnPageChange")
            intent.putExtra("PAGE", 1)
            activity!!.sendBroadcast(intent)
            val coinName = mainObject.Name!!
            var IS_TRANSACTION = false
            var portfolioName = ""
            if (realmDB.getAllHistoryData().isEmpty()) {
                IS_TRANSACTION = false
                portfolioName = "SecKey=sdffgvbnmsdfghjkrtyuio"
            } else {
                IS_TRANSACTION = true
                portfolioName = realmDB.getAllHistoryData()[0].portfolioName
            }
            fragmentManager?.beginTransaction()!!
                .replace(
                    R.id.frame_holder,
                    InputDataFragment().newInstance(
                        coinName,
                        IS_TRANSACTION,
                        portfolioName
                    )
                )
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("InputDataFragment")
                .commit()
        }

        fullChart.setOnClickListener {
            val intent1 = Intent("com.dust.extracker.OnPageChange")
            intent1.putExtra("PAGE", 2)
            activity!!.sendBroadcast(intent1)

            val intent = Intent("com.dust.extracker.OnClickMainData")
            intent.putExtra("COIN", mainObject.Name)
            activity!!.sendBroadcast(intent)

        }
    }

    private fun showErrorSnack(txt: String) {
        val snackBar = Snackbar.make(
            swiprefreshLayout,
            txt,
            Snackbar.LENGTH_LONG
        ).setAction(
            activity!!.resources.getString(R.string.connect)
        ) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting")
            activity!!.startActivity(intent)

        }
        snackBar.setTextColor(Color.BLACK)
        snackBar.setActionTextColor(Color.BLACK)
        snackBar.view.setBackgroundColor(Color.RED)
        snackBar.show()
    }

    private fun setUpTotalChange(list: List<PointValue> = arrayListOf()) {
        var dailychange: Double? = null
        if (TIME_PERIOD == "24H") {
            dailychange = mainObject.DailyChangePCT!!
        } else {
            if (list.isNotEmpty()) {

                val lastPrice = list.first().y.toDouble()
                val currentPrice = list.last().y.toDouble()

                if (currentPrice > lastPrice) {
                    dailychange = ((currentPrice - lastPrice) / lastPrice) * 100
                } else {
                    dailychange = ((lastPrice - currentPrice) / lastPrice) * 100
                }

            } else {
                return
            }
        }
        var changeStr = ""
        var color = Color.GREEN
        if (dailychange > 0) {
            changeStr = "+${String.format("%.2f", dailychange)}"
        } else {
            changeStr = "${String.format("%.2f", dailychange)}"
            color = Color.RED
        }
        TotalCoinDailyChange.text = changeStr
        if (color == Color.RED)
            total_frame.background = ResourcesCompat.getDrawable(
                activity!!.resources,
                R.drawable.cryptodetails_frame_shape_red,
                null
            )
        else
            total_frame.background = ResourcesCompat.getDrawable(
                activity!!.resources,
                R.drawable.cryptodetails_frame_shape_green,
                null
            )
    }

    private fun setUpLiveChart(list: List<PointValue>) {
        setUpTotalChange(list)
        lineChart.isInteractive = true
        lineChart.isZoomEnabled = true
        lineChart.setOnClickListener {
            lineChart.setZoomLevelWithAnimation(1f, 1f, 1f)
        }
        lineChart.isValueTouchEnabled = true
        lineChart.isValueSelectionEnabled = true
        val line = Line(list)
        line.color = Color.DKGRAY
        line.isCubic = true
        line.strokeWidth = 2
        line.areaTransparency = 80
        line.pointRadius = 2
        line.setHasLabels(false)
        line.setHasPoints(true)
        line.color = Color.LTGRAY

        line.pointColor = ContextCompat.getColor(activity!!, R.color.light_orange)
        val lineList = arrayListOf<Line>(line)
        val cList = arrayListOf<Double>()
        list.forEach {
            cList.add(it.y.toDouble())
        }
        cList.sort()
        val axListY = arrayListOf<AxisValue>()
        val min = cList[0]
        val max = cList[cList.size - 1]
        for (i in 0..10)
            axListY.add(AxisValue((min + (max - min) * (0.1 * i)).toFloat()))
        val data = LineChartData(lineList)

        val axListX = arrayListOf<AxisValue>()

        list.forEach {
            axListX.add(AxisValue(it.x))
        }

        data.axisXBottom =
            Axis(axListX).setLineColor(Color.GRAY).setTextColor(Color.GRAY).setTextSize(0)
        val formatter = SimpleAxisValueFormatter()
        formatter.decimalSeparator = '.'
        formatter.decimalDigitsNumber = 2
        formatter.prependedText = CharArray(1) { '$' }
        data.axisYRight =
            Axis(
                axListY
            ).setLineColor(Color.GRAY).setTextColor(Color.GRAY).setTextSize(8)
                .setFormatter(formatter)
        lineChart.lineChartData = data
        swiprefreshLayout.isRefreshing = false
        chartProgressBar.visibility = View.GONE
    }

    private fun setCoinBTCBase() {
        val newValue =
            mainObject.LastPrice!!.toDouble() / realmDB.getCryptoDataByName("BTC").LastPrice!!
        val oldValue = calculateOldValue()
        var changeStr = ""
        var color = Color.GREEN
        var dailychange = 0.toDouble()
        if (newValue > oldValue) {
            dailychange = ((newValue - oldValue) / oldValue) * 100
            changeStr = "+${String.format("%.2f", dailychange)}"
        } else {
            dailychange = ((oldValue - newValue) / oldValue) * 100
            changeStr = "-${String.format("%.2f", dailychange)}"
            color = Color.RED
        }

        val str = "${String.format(
            "%.6f",
            newValue
        )} BTC | $changeStr"
        val spannableString = SpannableString(str)
        spannableString.setSpan(
            ForegroundColorSpan(color),
            (str.indexOf("|") + 1),
            (str.lastIndex + 1),
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        coin_btcBase_Price.text = spannableString
    }

    private fun calculateOldValue(): Double {
        val btc = realmDB.getCryptoDataByName("BTC")
        var resultBtc = 0.toDouble()
        if (btc.DailyChangePCT!! > 0) {
            resultBtc = btc.LastPrice!! / (1 + (btc.DailyChangePCT!! / 100))
        } else {
            resultBtc = btc.LastPrice!! / (1 - (btc.DailyChangePCT!! / 100))
        }

        var resultCoin = 0.toDouble()
        if (mainObject.DailyChangePCT!! > 0) {
            resultCoin = mainObject.LastPrice!! / (1 + (mainObject.DailyChangePCT!! / 100))
        } else {
            resultCoin = mainObject.LastPrice!! / (1 - (mainObject.DailyChangePCT!! / 100))
        }

        return (resultCoin / resultBtc)
    }

    private fun setData() {

        setCoinBTCBase()

        setUpTotalChange()

        coinPrice.text = "$${String.format("%.6f", mainObject.LastPrice!!)}"

        date.text = "${activity!!.resources.getString(R.string.time)} ${freshDollarPrice.date}"

        dollarPrice.text = "${activity!!.resources.getString(R.string.dollarPrice)} ${freshDollarPrice.price}"

        tomanPrice.text = "${String.format(
            "%.2f",
            (mainObject.LastPrice!!.toDouble() * freshDollarPrice.price.toDouble())
        )} ${activity!!.resources.getString(R.string.toman)}"

        rank.text = "#${mainObject.SortOrder}"

        price.text = "$${mainObject.LastPrice}"

        dailyChange.text = "${mainObject.DailyChangePCT}"

        totalSupply.text = "${String.format("%.2f", mainObject.MaxSupply)} ${mainObject.Name}"

        details_text.text = activity!!.getString(R.string.details, mainObject.Name)

        details_fullName.text = mainObject.FullName

        Picasso.get().load("${mainObject.BaseImageUrl}${mainObject.ImageUrl}").into(coinImg)

        apiService.getDetails(mainObject.Name!!, this)

        calculateChangesPCT("1W")
        calculateChangesPCT("1M")
        calculateChangesPCT("6M")
        calculateChangesPCT("1Y")

        image_back.setOnClickListener {

            var str = "CryptoDetailsFragment"
            if (arguments!!.getString(
                    "Type",
                    "CryptoDetailsFragment"
                ) == "CryptoDetailsFragment_MAIN"
            ) {
                str = "CryptoDetailsFragment_MAIN"
            }
            fragmentManager?.popBackStack(
                str,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        details_title.text = activity!!.resources.getString(
            R.string.priceAndChart,
            mainObject.Name
        )

    }

    fun newInstance(
        COIN_NAME: String,
        Type: String = "CryptoDetailsFragment"
    ): CryptoDetailsFragment {
        val args = Bundle()
        args.putString("COIN_NAME", COIN_NAME)
        args.putString("Type", Type)
        val fragment = CryptoDetailsFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onGetChartData(data: List<ChartDataClass>) {
        val pointList = arrayListOf<PointValue>()
        data.forEach {
            pointList.add(PointValue(it.time.toFloat(), it.closePrice.toFloat()))
        }
        try {
            setUpLiveChart(pointList)
        }catch (e:Exception){}
    }

    override fun onFailureChartData() {
        try {
            showErrorSnack(activity!!.resources.getString(R.string.errorLog))
            chartProgressBar.visibility = View.GONE
        }catch (e:Exception){}
    }

    override fun onClick(p0: View?) {
        var views =
            arrayListOf<View>(
                twentyFourtime,
                oneWeek,
                oneMonth,
                sixMonth,
                oneYear,
                all_Time,
                threeMonth
            )
        when (p0!!.id) {
            R.id.twentyFourtime -> {
                setCurrentItemTextColor(views, twentyFourtime)
                TIME_PERIOD = "24H"
                requestChartData()
            }

            R.id.oneWeek -> {
                setCurrentItemTextColor(views, oneWeek)
                TIME_PERIOD = "1W"
                requestChartData()
            }

            R.id.oneMonth -> {
                setCurrentItemTextColor(views, oneMonth)
                TIME_PERIOD = "1M"
                requestChartData()
            }

            R.id.threeMonth -> {
                setCurrentItemTextColor(views, threeMonth)
                TIME_PERIOD = "3M"
                requestChartData()
            }

            R.id.sixMonth -> {
                setCurrentItemTextColor(views, sixMonth)
                TIME_PERIOD = "6M"
                requestChartData()
            }

            R.id.oneYear -> {
                setCurrentItemTextColor(views, oneYear)
                TIME_PERIOD = "1Y"
                requestChartData()
            }

            R.id.all_Time -> {
                setCurrentItemTextColor(views, all_Time)
                TIME_PERIOD = "ALL"
                requestChartData()
            }

        }
    }

    fun setCurrentItemTextColor(views: List<View>, view: View) {
        (view as CTextView).setTextColor(ContextCompat.getColor(activity!!, R.color.light_orange))
        views.forEach {
            if (it.id != view.id) {
                if (SharedPreferencesCenter(activity!!).getNightMode())
                    (it as CTextView).setTextColor(
                        ContextCompat.getColor(
                            activity!!,
                            R.color.white
                        )
                    )
                else
                    (it as CTextView).setTextColor(
                        ContextCompat.getColor(
                            activity!!,
                            R.color.black
                        )
                    )
            }
        }
    }

    private fun requestCoinsData() {
        if (checkNetworkConnectivity()) {
            apiService.getMainPrices("BTC,${mainObject.Name}", object : OnGetMainPrices {
                override fun onGetPrices(priceList: List<PriceDataClass>) {
                    priceList.forEach {
                        realmDB.updatePrice(it)
                    }
                    apiService.getDailyChanges(
                        "BTC,${mainObject.Name}",
                        object : OnGetDailyChanges {
                            override fun onGetDailyChanges(list: List<LastChangeDataClass>) {
                                list.forEach {
                                    realmDB.updateDailyChange(it)
                                }
                                setUpCoinList()
                                try {
                                    setData()
                                }catch (e:Exception){}
                                val time = java.util.Calendar.getInstance().time.toString()
                                foreign_date.text = "${time.substring(0, 10)}-${time.substring(
                                    (time.length - 5),
                                    time.length
                                )}"
                            }
                        })
                }
            })
        }
    }

    override fun onReceive(data: DetailsDataClass) {
        marketCap.text = "$ ${String.format("%.2f", data.marketCap.toDouble())}"
        dailyVolume.text =
            "${String.format("%.2f", data.dailyVolume.toDouble())} ${mainObject.Name}"
        marketCoins.text = "${String.format("%.2f", data.supply.toDouble())} ${mainObject.Name}"
    }

    private fun calculateChangesPCT(P: String) {
        apiService.getChartData(mainObject.Name!!, P, object : OnGetChartData {
            override fun onGetChartData(data: List<ChartDataClass>) {
                val list = arrayListOf<PointValue>()
                list.add(PointValue(data.first().time.toFloat(), data.first().closePrice.toFloat()))
                list.add(PointValue(data.last().time.toFloat(), data.last().closePrice.toFloat()))

                var dailychange: Double? = null
                if (list.isNotEmpty()) {

                    val lastPrice = list.first().y.toDouble()
                    val currentPrice = list.last().y.toDouble()

                    if (currentPrice > lastPrice) {
                        dailychange = ((currentPrice - lastPrice) / lastPrice) * 100
                    } else {
                        dailychange = ((lastPrice - currentPrice) / lastPrice) * 100
                    }

                } else {
                    return
                }

                var changeStr = ""
                if (dailychange > 0) {
                    changeStr = "+${String.format("%.2f", dailychange)}"
                } else {
                    changeStr = "${String.format("%.2f", dailychange)}"
                }

                when (P) {
                    "1W" -> weeklyChange.text = changeStr
                    "1M" -> monthlyChange.text = changeStr
                    "6M" -> sixmonthChange.text = changeStr
                    "1Y" -> yearlyChange.text = changeStr
                }

            }

            override fun onFailureChartData() {}
        })


    }
}