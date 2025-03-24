package com.dust.extracker.fragments.marketfragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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
import com.dust.extracker.utils.Utils
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import java.util.*
import androidx.core.net.toUri
import com.dust.extracker.application.MyApplication
import com.dust.extracker.utils.Constants
import kotlin.math.abs

class CryptoDetailsFragment : Fragment(), OnGetChartData, View.OnClickListener,
    OnDetailsDataReceive {

    lateinit var image_back: ImageView
    lateinit var coinImg: CircleImageView
    lateinit var details_title: CTextView
    lateinit var details_fullName: TextView
    lateinit var details_text: TextView
    lateinit var tomanPrice: CTextView
    lateinit var dollarPrice: CTextView
    lateinit var date: CTextView
    lateinit var coinPrice: CTextView
    lateinit var coin_btcBase_Price: CTextView
    lateinit var foreign_date: CTextView
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

    lateinit var rank: CTextView
    lateinit var price: CTextView
    lateinit var marketCap: CTextView
    lateinit var dailyVolume: CTextView
    lateinit var dailyChange: CTextView
    lateinit var weeklyChange: CTextView
    lateinit var monthlyChange: CTextView
    lateinit var sixmonthChange: CTextView
    lateinit var yearlyChange: CTextView
    lateinit var totalSupply: CTextView
    lateinit var marketCoins: CTextView
    lateinit var dominance: TextView
    lateinit var img_cryptoDetails_avatar: CircleImageView

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
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        softInputManager.hideSoftInputFromWindow(
            total_frame.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }


    private fun setUpApiService() {
        apiService = ApiCenter(requireActivity(), object : OnGetAllCryptoList {
            override fun onGet(cryptoList: List<CryptoMainData>) {}

            override fun onGetByName(price: Double, dataNum: Int) {}
        })
    }

    private fun checkNetworkConnectivity(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    private fun requestChartData() {
        if (checkNetworkConnectivity()) {
            apiService.getChartData(mainObject.Symbol!!, TIME_PERIOD, this)
            chartProgressBar.visibility = View.VISIBLE
        }
    }

    private fun setDollarPrice() {
        realmDB.getDollarPrice()?.let {
            freshDollarPrice = it
        }
    }

    private fun setUpCoinList() {
        mainObject = realmDB.getCryptoDataByName(requireArguments().getString("Symbol")!!)
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
        weeklyChange = view.findViewById(R.id.weeklyChange)
        monthlyChange = view.findViewById(R.id.monthlyChange)
        sixmonthChange = view.findViewById(R.id.sixmonthChange)
        yearlyChange = view.findViewById(R.id.yearlyChange)
        marketCap = view.findViewById(R.id.marketCap)
        dailyVolume = view.findViewById(R.id.dailyVolume)
        foreign_date = view.findViewById(R.id.foreign_date)
        marketCoins = view.findViewById(R.id.marketCoins)
        dominance = view.findViewById(R.id.dominance)
        img_cryptoDetails_avatar = view.findViewById(R.id.img_cryptoDetails_avatar)
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
        twentyFourtime.setTextColor(ContextCompat.getColor(requireActivity(), R.color.green_primary))

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
                showErrorSnack(requireActivity().resources.getString(R.string.connectionFailure))
                swiprefreshLayout.isRefreshing = false
            }
        }

        shareImg.setOnClickListener {
            val txtBody =
                "ارز دیجیتال${mainObject.Name}در بیست و چهار ساعت گذشته %${mainObject.DailyChangePCT!!} بازدهی داشته! میتونی از طریق این لینک جزییاتش رو ببینی.  www.ExSite.ir/coins/${mainObject.Name}"
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, txtBody)
            requireActivity().startActivity(Intent.createChooser(intent, "ارسال با ..."))
        }

        addToeWatchListbtn.setTextColor(Color.WHITE)
        addTransactionbtn.setTextColor(Color.WHITE)
        fullChart.setTextColor(Color.WHITE)

        val sharedPreferences1 = requireActivity().getSharedPreferences("FAV", Context.MODE_PRIVATE)
        if (sharedPreferences1.getString("fav", "")!!.split(',').contains(mainObject.ID)) {
            addToeWatchListbtn.text = requireActivity().resources.getString(R.string.deleteFromWatchlist)
            addToeWatchListbtn.background = ResourcesCompat.getDrawable(
                requireActivity().resources,
                R.drawable.details_addtowatch_added,
                null
            )
        } else {
            addToeWatchListbtn.text = requireActivity().resources.getString(R.string.addToWatchlist)
            addToeWatchListbtn.background = ResourcesCompat.getDrawable(
                requireActivity().resources,
                R.drawable.details_addtowatch_not_added,
                null
            )
        }

        addToeWatchListbtn.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("FAV", Context.MODE_PRIVATE)
            val rawData = sharedPreferences.getString("fav", "")
            val resultList = arrayListOf<String>()
            var listRawData = listOf<String>()
            if (rawData != "") {
                listRawData = rawData!!.split(',')
                resultList.addAll(listRawData)
            }
            if (resultList.contains(mainObject.ID)) {
                resultList.remove(mainObject.ID!!)
                addToeWatchListbtn.text = requireActivity().resources.getString(R.string.addToWatchlist)
                addToeWatchListbtn.background = ResourcesCompat.getDrawable(
                    requireActivity().resources,
                    R.drawable.details_addtowatch_not_added,
                    null
                )

            } else {
                resultList.add(mainObject.ID!!)
                addToeWatchListbtn.text = requireActivity().resources.getString(R.string.deleteFromWatchlist)
                addToeWatchListbtn.background = ResourcesCompat.getDrawable(
                    requireActivity().resources,
                    R.drawable.details_addtowatch_added,
                    null
                )

            }
            sharedPreferences.edit().putString("fav", resultList.joinToString(",")).apply()
            requireActivity().sendBroadcast(Intent("com.dust.extracker.notifyDataSetChanged"))
        }

        addTransactionbtn.setOnClickListener {
            val intent = Intent("com.dust.extracker.OnPageChange")
            intent.putExtra("PAGE", 1)
            requireActivity().sendBroadcast(intent)
            val coinName = mainObject.Symbol!!
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
            requireActivity().sendBroadcast(intent1)

            val intent = Intent("com.dust.extracker.OnClickMainData")
            intent.putExtra("COIN", mainObject.Symbol)
            requireActivity().sendBroadcast(intent)

        }

        setupUserAvatar()

    }

    private fun setupUserAvatar(){
        realmDB.getUserData()?.avatarUrl.let {
            if (!it.isNullOrEmpty()){
                val uri = it.toUri()
                (requireActivity().application as MyApplication).grantUriPermission(uri)
                img_cryptoDetails_avatar.setImageURI(uri)
            }
        }
    }

    private fun showErrorSnack(txt: String) {
        val snackBar = Snackbar.make(
            swiprefreshLayout,
            txt,
            Snackbar.LENGTH_LONG
        )
        snackBar.setTextColor(Color.WHITE)
        snackBar.setActionTextColor(Color.WHITE)
        snackBar.view.setBackgroundColor(Color.BLACK)
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
                dailychange = (abs((currentPrice - lastPrice)) / lastPrice) * 100
                if (currentPrice < lastPrice)
                    dailychange *= -1

            } else {
                return
            }
        }
        var changeStr = ""
        var color = Color.GREEN
        if (dailychange > 0) {
            changeStr = "+${Utils.formatPriceNumber(dailychange,2)}"
        } else {
            changeStr = "${Utils.formatPriceNumber(dailychange,2)}"
            color = Color.RED
        }
        TotalCoinDailyChange.text = changeStr
        if (color == Color.RED)
            total_frame.background = ResourcesCompat.getDrawable(
                requireActivity().resources,
                R.drawable.cryptodetails_frame_shape_red,
                null
            )
        else
            total_frame.background = ResourcesCompat.getDrawable(
                requireActivity().resources,
                R.drawable.cryptodetails_frame_shape_green,
                null
            )
    }

    private fun setUpLiveChart(list: List<PointValue>) {
        setUpTotalChange(list)
        lineChart.isInteractive = true
        lineChart.isZoomEnabled = false
        lineChart.setOnClickListener {
            lineChart.setZoomLevelWithAnimation(1f, 1f, 1f)
        }
        lineChart.isValueTouchEnabled = true
        lineChart.isValueSelectionEnabled = true
        val line = Line(list)
        line.color = ContextCompat.getColor(requireActivity(), R.color.dark_green)
        line.pointColor = ContextCompat.getColor(requireActivity(), R.color.green)
        line.isCubic = true
        line.strokeWidth = 2
        line.areaTransparency = 80
        line.pointRadius = 1
        line.setHasLabels(false)
        line.setHasLabelsOnlyForSelected(false)
        line.setHasPoints(true)

        val lineList = arrayListOf(line)
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
            changeStr = "+${Utils.formatPriceNumber(dailychange,2)}"
        } else {
            dailychange = ((oldValue - newValue) / oldValue) * 100
            changeStr = "-${Utils.formatPriceNumber(dailychange,2)}"
            color = Color.RED
        }

        val str = "${Utils.formatPriceNumber(newValue,6)} BTC | $changeStr"
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

        date.text = "${requireActivity().resources.getString(R.string.time)} ${freshDollarPrice.date}"

        dollarPrice.text = "${requireActivity().resources.getString(R.string.dollarPrice)} ${Utils.formatPriceNumber(freshDollarPrice.price.toDouble(),0)}"

        mainObject.LastPrice?.let { lp ->
            val decimal = if (lp > 1){
                2
            }else if (lp > 0.0001){
                7
            }else{
                13
            }
            price.text = "$${Utils.formatPriceNumber(lp,decimal)}"
            coinPrice.text = price.text.toString()
            tomanPrice.text = "${Utils.formatPriceNumber((lp * freshDollarPrice.price.toDouble()),decimal)} ${requireActivity().resources.getString(R.string.toman)}"
        }

        mainObject.DailyChangePCT?.let { pct ->
            dailyChange.text = "${Utils.formatPriceNumber(pct,2)}"
            if (pct > 0)
                dailyChange.text = "+${dailyChange.text}"
        }

        details_text.text = requireActivity().getString(R.string.details, mainObject.Name)

        details_fullName.text = mainObject.Name

        mainObject.rank?.let { ra ->
            rank.text = "#${Utils.formatPriceNumber(ra.toDouble(),0)}"
        }

        totalSupply.text = "${Utils.formatPriceNumber(mainObject.maxSupply ?: 0.0,2)} ${mainObject.Symbol}"
        marketCoins.text = "${Utils.formatPriceNumber(mainObject.circulatingSupply ?: 0.0,2)} ${mainObject.Symbol}"

        Picasso.get().load(mainObject.ImageUrl).into(coinImg)

        apiService.getDetails(mainObject.Symbol!!, this)

        calculateChangesPCT("1W")
        calculateChangesPCT("1M")
        calculateChangesPCT("6M")
        calculateChangesPCT("1Y")

        image_back.setOnClickListener {

            var str = "CryptoDetailsFragment"
            if (requireArguments().getString(
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

        details_title.text = requireActivity().resources.getString(
            R.string.priceAndChart,
            mainObject.Name
        )

    }

    fun newInstance(
        Symbol: String,
        Type: String = "CryptoDetailsFragment"
    ): CryptoDetailsFragment {
        val args = Bundle()
        args.putString("Symbol", Symbol)
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
            showErrorSnack(requireActivity().resources.getString(R.string.errorLog))
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

    private fun setCurrentItemTextColor(views: List<View>, view: View) {
        (view as CTextView).setTextColor(ContextCompat.getColor(requireActivity(), R.color.green_primary))
        views.forEach {
            if (it.id != view.id) {
                if (SharedPreferencesCenter(requireActivity()).getNightMode())
                    (it as CTextView).setTextColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.white
                        )
                    )
                else
                    (it as CTextView).setTextColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.black
                        )
                    )
            }
        }
    }

    private fun requestCoinsData() {
        if (checkNetworkConnectivity()) {
            apiService.getCoinFullDetails("BTC,${mainObject.Symbol}"){ detailsList ->

                detailsList.forEach {
                    realmDB.updateCryptoDetails(it)
                }

                apiService.getDailyChanges(
                    "BTC,${mainObject.Symbol}",
                    object : OnGetDailyChanges {
                        override fun onGetDailyChanges(list: List<LastChangeDataClass>) {
                            list.forEach {
                                realmDB.updateDailyChange(it)
                            }
                            setUpCoinList()
                            try {
                                setData()
                            }catch (e:Exception){}
                            val time = Calendar.getInstance().time.toString()
                            foreign_date.text = "${time.substring(0, 10)}-${time.substring(
                                (time.length - 5),
                                time.length
                            )}"
                        }
                    })
            }
        }
    }

    override fun onReceive(data: DetailsDataClass) {
        marketCap.text = "$ ${Utils.formatPriceNumber(data.marketCap.toDouble(),2)}"
        dailyVolume.text =
            "${Utils.formatPriceNumber(data.dailyVolume.toDouble(),2)} ${mainObject.Symbol}"
    }

    private fun calculateChangesPCT(P: String) {
        apiService.getChartData(mainObject.Symbol!!, P, object : OnGetChartData {
            override fun onGetChartData(data: List<ChartDataClass>) {
                val list = arrayListOf<PointValue>()
                list.add(PointValue(data.first().time.toFloat(), data.first().closePrice.toFloat()))
                list.add(PointValue(data.last().time.toFloat(), data.last().closePrice.toFloat()))

                var changeStr = ""
                if (list.isNotEmpty()) {
                    val lastPrice = list.first().y.toDouble()
                    val currentPrice = list.last().y.toDouble()
                    val changePercentage = (abs((currentPrice - lastPrice)) / lastPrice) * 100
                    changeStr = if (currentPrice > lastPrice) {
                        "+${Utils.formatPriceNumber(changePercentage,2)}"
                    } else if (currentPrice < lastPrice){
                        "-${Utils.formatPriceNumber(changePercentage,2)}"
                    }else{
                        "${Utils.formatPriceNumber(changePercentage,2)}"
                    }
                } else {
                    return
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