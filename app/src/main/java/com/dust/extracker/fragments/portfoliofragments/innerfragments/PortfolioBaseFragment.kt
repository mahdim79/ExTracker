package com.dust.extracker.fragments.portfoliofragments.innerfragments

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.PortfolioCoinRecyclerViewAdapter
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.*
import com.dust.extracker.interfaces.*
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.google.android.material.snackbar.Snackbar
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import lecho.lib.hellocharts.view.PieChartView
import kotlin.math.abs

class PortfolioBaseFragment(
    var portfolioDataClass: HistoryDataClass,
    var onHistoryFragmentUpdate: OnHistoryFragmentUpdate
) : Fragment(),
    OnGetPortfolioMainData, OnGetChartData, View.OnClickListener {

    private lateinit var lineChart: LineChartView
    private lateinit var pieChart: PieChartView
    private lateinit var twentyFourtime: CTextView
    private lateinit var oneWeek: CTextView
    private lateinit var oneMonth: CTextView
    private lateinit var sixMonth: CTextView
    private lateinit var threeMonth: CTextView
    private lateinit var oneYear: CTextView
    private lateinit var all_Time: CTextView
    private lateinit var portfolio_title: CTextView
    private lateinit var portfolioAsset: CTextView
    private lateinit var portfolioTomanAsset: CTextView
    private lateinit var dateAndRate: CTextView
    private lateinit var portfolioTotalChange: CTextView
    private lateinit var portfolioTotalChangeToman: CTextView
    private lateinit var change_pct_linear: LinearLayout
    private lateinit var imageDelete: ImageView
    private lateinit var imageedit: ImageView
    private lateinit var imagedescriptions: ImageView
    private lateinit var imagehelp: ImageView
    private lateinit var chartProgressBar: ProgressBar
    private lateinit var add_Transaction: Button
    private lateinit var portfolioCoinRecyclerView: RecyclerView

    private lateinit var ondollarpriceRecieve: onDollarPriceRecieve

    private lateinit var portfolio_swiprefreshLayout: SwipeRefreshLayout
    private lateinit var realmDB: RealmDataBaseCenter
    private lateinit var apiService: ApiCenter
    private var CoinCount = 0
    private var RequestCount = 0
    private var listMain = arrayListOf<List<ChartDataClass>>()

    private var colorList: List<Int> = arrayListOf()
    private var TIME_PERIOD = "24H"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_portfolio_base, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRealmDb()
        setPrimaryDataClass()
        setUpApiService()
        setUpViews(view)
        chartProgressBar.visibility = View.VISIBLE
        if (checkNetworkConnectivity()) {
            requestCoinsData()
            UpdateChartData()
        } else {
            setData()
            chartProgressBar.visibility = View.GONE
        }
    }

    private fun setPrimaryDataClass() {
        val id = portfolioDataClass.id
        realmDB.getAllHistoryData().forEach {
            if (it.id == id)
                portfolioDataClass = it
        }
    }

    private fun checkNetworkConnectivity(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    private fun setUpRealmDb() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setData() {
        portfolio_title.text = portfolioDataClass.portfolioName
        if (!realmDB.checkDollarPriceAvailability()) {
            apiService.getDollarPrice(object : OnGetDollarPrice {
                override fun onGet(price: DollarInfoDataClass) {
                    realmDB.insertDollarPrice(price)
                    try {
                        setData()
                    }catch (e:Exception){}
                }
            })
            return
        }
        val dollarResult = realmDB.getDollarPrice()
        dateAndRate.text =
            requireActivity().resources.getString(
                R.string.DateAndRateText,
                dollarResult.date,
                dollarResult.price
            )
        portfolioTomanAsset.text = resources.getString(
            R.string.totalCapital,
            String.format("%.2f", (dollarResult.price.toDouble() * portfolioDataClass.totalCapital))
        )
        if (portfolioDataClass.changePct != null) {
            if (portfolioDataClass.changePct.toDouble() > 0) {
                portfolioTotalChange.text =
                    "+${String.format("%.3f", (portfolioDataClass.changePct.toDouble()))}"
                change_pct_linear.background = ResourcesCompat.getDrawable(
                    requireActivity().resources,
                    R.drawable.portfolio_linear_shape_green,
                    null
                )

                val diff =
                    (portfolioDataClass.totalCapital * dollarResult.price.toDouble()) - calculateTomanLastAmount()
                portfolioTotalChangeToman.text =
                    "${String.format("%.2f", diff)} ${requireActivity().resources.getString(R.string.toman)} "

            } else {
                portfolioTotalChange.text =
                    "${String.format("%.3f", (portfolioDataClass.changePct.toDouble()))}"
                change_pct_linear.background = ResourcesCompat.getDrawable(
                    requireActivity().resources,
                    R.drawable.portfolio_linear_shape_red,
                    null
                )

                val diff =
                    calculateTomanLastAmount() - (portfolioDataClass.totalCapital * dollarResult.price.toDouble())
                portfolioTotalChangeToman.text =
                    "${String.format("%.2f", diff)} ${requireActivity().resources.getString(R.string.toman)} "
            }
        }
        if (checkNetworkConnectivity()) {
            apiService.getCryptoPriceByName("BTC", 101)
        } else {
            val res = realmDB.getCryptoDataByName("BTC")
            if (res.Name != "NULL") {
                portfolioAsset.text = "$${String.format(
                    "%.3f",
                    (portfolioDataClass.totalCapital)
                )} ~ ${String.format(
                    "%.7f",
                    (portfolioDataClass.totalCapital / res.LastPrice!!)
                )} BTC"

            }
        }
        setRecyclerViewAdapter()
        setUpPieChartView()
        requireActivity().sendBroadcast(Intent("com.dust.extracker.OnUpdateTotalFund"))

    }

    private fun calculateTomanLastAmount(): Double {
        var amount = 0.toDouble()
        portfolioDataClass.transactionList.forEach {
            amount += (it.dealOpenPrice * it.dealAmount * it.dollarPrice)
        }
        return amount
    }

    private fun setRecyclerViewAdapter() {
        val list = arrayListOf<MainRealmObject>()
        portfolioDataClass.transactionList.forEach {
            list.add(realmDB.getCryptoDataByName(it.coinName))
        }
        portfolioCoinRecyclerView.adapter = PortfolioCoinRecyclerViewAdapter(
            realmDB.getDollarPrice(),
            list,
            portfolioDataClass,
            requireActivity(),
            object : OnTransactionRemoveListener {
                override fun onRemove(transactionId: Int) {
                    val newList = arrayListOf<TransactionDataClass>()
                    portfolioDataClass.transactionList.forEach {
                        if (it.id != transactionId)
                            newList.add(it)
                    }

                    if (newList.isNullOrEmpty()) {
                        if (realmDB.getHistoryDataCount() == 1) {
                            realmDB.deleteHistoryData(portfolioDataClass.id)
                            requireActivity().sendBroadcast(Intent("com.dust.extracker.DeleteFragment"))
                        } else {
                            realmDB.deleteHistoryData(portfolioDataClass.id)
                            onHistoryFragmentUpdate.onHistoryFragmentUpdate()
                        }
                    } else {
                        for (i in 0 until newList.size)
                            newList[i].id = i
                        portfolioDataClass.transactionList = newList
                        realmDB.updateHistoryData(portfolioDataClass)
                        if (checkNetworkConnectivity()) {
                            requestCoinsData()
                            UpdateChartData()
                        } else {
                            setData()
                        }
                    }
                }
            }
        )
    }

    private fun setUpApiService() {
        apiService = ApiCenter(requireActivity(), object : OnGetAllCryptoList {
            override fun onGet(cryptoList: List<CryptoMainData>) {}

            override fun onGetByName(price: Double, dataNum: Int) {
                if (dataNum == 101)
                    portfolioAsset.text = "$${String.format(
                        "%.3f",
                        (portfolioDataClass.totalCapital)
                    )} ~ ${String.format("%.7f", (portfolioDataClass.totalCapital / price))} BTC"
            }
        })
    }

    private fun UpdateChartData() {
        val transactionList = portfolioDataClass.transactionList
        val list = arrayListOf<String>()
        transactionList.forEach {
            list.add(it.coinName)
        }
        CoinCount = list.size
        list.forEach {
            apiService.getChartData(it, TIME_PERIOD, this)
        }
        list.clear()
    }

    private fun requestCoinsData() {
        val list = arrayListOf<String>()
        portfolioDataClass.transactionList.forEach {
            list.add(it.coinName)
        }
        apiService.getPortfolioMainData(list.joinToString(","), this)
        list.clear()
    }

    private fun createRandomColorList(countList: List<Int>): List<Int> {
        val list = arrayListOf<Int>()
        countList.forEach {
            val color = getARandomColor(list, countList.size)
            list.add(color)
        }
        if (list.size > 1 && list.last() == list.first())
            return createRandomColorList(countList)

        return list
    }

    private fun getARandomColor(list: List<Int>, count: Int): Int {
        val randNumber = Math.random()

        val color = if (randNumber in 0.0..0.15) {
            Color.YELLOW
        } else if (randNumber in 0.15..0.3) {
            Color.BLUE
        } else if (randNumber in 0.3..0.45) {
            Color.MAGENTA
        } else if (randNumber in 0.45..0.6) {
            Color.GRAY
        } else if (randNumber in 0.6..0.75) {
            Color.GREEN
        } else if (randNumber in 0.75..0.9) {
            Color.CYAN
        } else {
            Color.RED
        }

        if (list.isNotEmpty() && list.last() == color) {
            return getARandomColor(list, count)
        } else {
            return color
        }
    }

    private fun setUpPieChartView() {
        val list = arrayListOf<SliceValue>()
        val countList = arrayListOf<Int>()
        var totalMoney = 0.toDouble()
        portfolioDataClass.transactionList.forEach {

            if (it.dealType == "SELL") {
                if (it.currentPrice > it.dealOpenPrice) {
                    val percentage =
                        ((it.currentPrice - it.dealOpenPrice) / it.dealOpenPrice)
                    if (percentage <= 1)
                        totalMoney += (it.dealOpenPrice * it.dealAmount) - (it.dealOpenPrice * it.dealAmount * percentage)
                } else if (it.currentPrice < it.dealOpenPrice) {
                    val percentage =
                        ((it.dealOpenPrice - it.currentPrice) / it.dealOpenPrice)
                    totalMoney += (it.dealOpenPrice * it.dealAmount) + (it.dealOpenPrice * it.dealAmount * percentage)
                } else {
                    totalMoney += it.currentPrice * it.dealAmount
                }
            } else {
                totalMoney += it.dealAmount * it.currentPrice
            }
            countList.add(it.id)
        }
        if (colorList.isNullOrEmpty() || colorList.size != countList.size) {
            colorList = createRandomColorList(countList)
        }
        for (i in 0 until portfolioDataClass.transactionList.size) {
            var percentage = 0.toDouble()
            if (portfolioDataClass.transactionList[i].dealType == "SELL") {
                if (portfolioDataClass.transactionList[i].currentPrice > portfolioDataClass.transactionList[i].dealOpenPrice) {
                    val percentage1 =
                        ((portfolioDataClass.transactionList[i].currentPrice - portfolioDataClass.transactionList[i].dealOpenPrice) / portfolioDataClass.transactionList[i].dealOpenPrice)
                    if (percentage1 <= 1)
                        percentage =
                            (((portfolioDataClass.transactionList[i].dealOpenPrice * portfolioDataClass.transactionList[i].dealAmount) - (portfolioDataClass.transactionList[i].dealOpenPrice * portfolioDataClass.transactionList[i].dealAmount * percentage)) * 100) / totalMoney
                } else if (portfolioDataClass.transactionList[i].currentPrice < portfolioDataClass.transactionList[i].dealOpenPrice) {
                    val percentage1 =
                        ((portfolioDataClass.transactionList[i].dealOpenPrice - portfolioDataClass.transactionList[i].currentPrice) / portfolioDataClass.transactionList[i].dealOpenPrice)
                    percentage =
                        (((portfolioDataClass.transactionList[i].dealOpenPrice * portfolioDataClass.transactionList[i].dealAmount) + (portfolioDataClass.transactionList[i].dealOpenPrice * portfolioDataClass.transactionList[i].dealAmount * percentage1)) * 100) / totalMoney
                } else {
                    percentage =
                        ((portfolioDataClass.transactionList[i].currentPrice * portfolioDataClass.transactionList[i].dealAmount) * 100) / totalMoney
                }
            } else {
                percentage =
                    ((portfolioDataClass.transactionList[i].currentPrice * portfolioDataClass.transactionList[i].dealAmount) * 100) / totalMoney
            }

            list.add(
                SliceValue(
                    percentage.toFloat(),
                    colorList[i]
                ).setLabel(
                    "%${String.format(
                        "%.2f",
                        percentage
                    )} ${portfolioDataClass.transactionList[i].coinName}"
                )
            )
        }

        val data = PieChartData(list)
        data.setHasLabels(true)
        data.setHasLabelsOutside(true)
        pieChart.offsetTopAndBottom(20)
        pieChart.pieChartData = data
        pieChart.circleFillRatio = 0.6f
        pieChart.isChartRotationEnabled = false
    }

    private fun setUpLiveChart(list: List<PointValue>) {
        lineChart.isInteractive = true
        lineChart.isZoomEnabled = true
        lineChart.setOnClickListener {
            lineChart.setZoomLevelWithAnimation(1f, 1f, 1f)
        }
        lineChart.isValueTouchEnabled = true
        lineChart.isValueSelectionEnabled = true
        val line = Line(list)
        line.color = Color.LTGRAY
        line.pointColor = ContextCompat.getColor(requireActivity(), R.color.light_orange)
        line.isCubic = true
        line.strokeWidth = 2
        line.areaTransparency = 60
        line.pointRadius = 2
        line.setHasLabels(false)
        line.setHasPoints(true)
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
        portfolio_swiprefreshLayout.isRefreshing = false
        chartProgressBar.visibility = View.GONE
    }

    private fun setUpViews(view: View) {
        portfolio_title = view.findViewById(R.id.portfolio_title)
        imageedit = view.findViewById(R.id.imageedit)
        imagedescriptions = view.findViewById(R.id.imagedescriptions)
        imagehelp = view.findViewById(R.id.imagehelp)

        pieChart = view.findViewById(R.id.pieChart)
        portfolioAsset = view.findViewById(R.id.portfolioAsset)
        portfolioTomanAsset = view.findViewById(R.id.portfolioTomanAsset)
        dateAndRate = view.findViewById(R.id.dateAndRate)
        portfolioTotalChange = view.findViewById(R.id.portfolioTotalChange)
        portfolioTotalChangeToman = view.findViewById(R.id.portfolioTotalChangeToman)
        change_pct_linear = view.findViewById(R.id.change_pct_linear)
        imageDelete = view.findViewById(R.id.imageDelete)
        lineChart = view.findViewById(R.id.lineChart)
        portfolio_swiprefreshLayout = view.findViewById(R.id.portfolio_swiprefreshLayout)
        twentyFourtime = view.findViewById(R.id.twentyFourtime)
        oneWeek = view.findViewById(R.id.oneWeek)
        oneMonth = view.findViewById(R.id.oneMonth)
        sixMonth = view.findViewById(R.id.sixMonth)
        oneYear = view.findViewById(R.id.oneYear)
        threeMonth = view.findViewById(R.id.threeMonth)
        all_Time = view.findViewById(R.id.all_Time)
        chartProgressBar = view.findViewById(R.id.chartProgressBar)
        add_Transaction = view.findViewById(R.id.add_Transaction)
        portfolioCoinRecyclerView = view.findViewById(R.id.portfolioCoinRecyclerView)
        portfolioCoinRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        twentyFourtime.setTextColor(ContextCompat.getColor(requireActivity(), R.color.light_orange))

        twentyFourtime.setOnClickListener(this)
        oneWeek.setOnClickListener(this)
        oneMonth.setOnClickListener(this)
        threeMonth.setOnClickListener(this)
        sixMonth.setOnClickListener(this)
        oneYear.setOnClickListener(this)
        all_Time.setOnClickListener(this)

        portfolio_swiprefreshLayout.setColorSchemeColors(
            Color.BLACK,
            Color.RED,
            Color.GREEN,
            Color.YELLOW
        )
        portfolio_swiprefreshLayout.setOnRefreshListener {
            chartProgressBar.visibility = View.VISIBLE
            if (checkNetworkConnectivity()) {
                requestCoinsData()
                UpdateChartData()
            } else {
                chartProgressBar.visibility = View.GONE
                showErrorSnack(requireActivity().resources.getString(R.string.connectionFailure))
                portfolio_swiprefreshLayout.isRefreshing = false
            }
        }
        imageDelete.setOnClickListener {
            val dialog = Dialog(requireActivity())
            dialog.setContentView(R.layout.dialog_remove_transaction)
            dialog.setCancelable(false)
            dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }
            dialog.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
                dialog.dismiss()
                realmDB.deleteHistoryData(portfolioDataClass.id)
                onHistoryFragmentUpdate.onHistoryFragmentUpdate()
            }
            dialog.findViewById<CTextView>(R.id.txt_remove_question).text =
                requireActivity().resources.getString(R.string.deletePortfolio)
            dialog.show()
        }
        add_Transaction.setOnClickListener {
            val intent = Intent("com.dust.extracker.addMoreHistory")
            intent.putExtra("IS_TRANSACTION", true)
            intent.putExtra("PortfolioName", portfolioDataClass.portfolioName)
            requireActivity().sendBroadcast(intent)
        }

        imageedit.setOnClickListener {
            val intent = Intent("com.dust.extracker.OnClickTransactionData")
            intent.putExtra("PORTFOLIO_NAME_EDIT", portfolioDataClass.portfolioName)
            requireActivity().sendBroadcast(intent)
        }

        imagedescriptions.setOnClickListener {
            val dialog = Dialog(requireActivity())
            dialog.setContentView(R.layout.dialog_description)
            dialog.setCancelable(true)
            dialog.findViewById<Button>(R.id.btnClose).setOnClickListener {
                dialog.dismiss()
            }
            dialog.findViewById<CTextView>(R.id.descriptionsText).text =
                portfolioDataClass.desctiption
            dialog.show()
        }

        imagehelp.setOnClickListener {

            val dialog = Dialog(requireActivity())
            dialog.setContentView(R.layout.dialog_help)
            dialog.setCancelable(true)
            dialog.findViewById<Button>(R.id.btnClose).setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()

        }
    }

    private fun showErrorSnack(txt: String) {
        val snackBar = Snackbar.make(
            portfolio_swiprefreshLayout,
            txt,
            Snackbar.LENGTH_LONG
        ).setAction(
            requireActivity().resources.getString(R.string.connect)
        ) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting")
            requireActivity().startActivity(intent)

        }
        snackBar.setTextColor(Color.BLACK)
        snackBar.setActionTextColor(Color.BLACK)
        snackBar.view.setBackgroundColor(Color.RED)
        snackBar.show()
    }

    override fun onGetPortfolioData(data: List<PortfotioDataClass>) {
        calculateAndUpdateNewData(data)
        try {
            setData()
        }catch (e:Exception){}
    }

    private fun calculateAndUpdateNewData(data: List<PortfotioDataClass>) {

        // setNewData

        val list = portfolioDataClass.transactionList
        var totalMoney = 0.toDouble()
        for (j in 0 until list.size) {
            for (i in 0 until data.size) {
                if (data[i].coinName == list[j].coinName) {
                    portfolioDataClass.transactionList[j].currentPrice = data[i].price
                    portfolioDataClass.transactionList[j].currentDailyChange = data[i].dailyChange
                    // calculateAndSetNewCapital
                    if (portfolioDataClass.transactionList[j].dealType == "SELL") {
                        if (portfolioDataClass.transactionList[j].currentPrice > portfolioDataClass.transactionList[j].dealOpenPrice) {
                            val percentage =
                                ((portfolioDataClass.transactionList[j].currentPrice - portfolioDataClass.transactionList[j].dealOpenPrice) / portfolioDataClass.transactionList[j].dealOpenPrice)
                            if (percentage <= 1)
                                totalMoney += (portfolioDataClass.transactionList[j].dealOpenPrice * portfolioDataClass.transactionList[j].dealAmount) - (portfolioDataClass.transactionList[j].dealOpenPrice * portfolioDataClass.transactionList[j].dealAmount * percentage)
                        } else if (portfolioDataClass.transactionList[j].currentPrice < portfolioDataClass.transactionList[j].dealOpenPrice) {
                            val percentage =
                                ((portfolioDataClass.transactionList[j].dealOpenPrice - portfolioDataClass.transactionList[j].currentPrice) / portfolioDataClass.transactionList[j].dealOpenPrice)
                            totalMoney += ((portfolioDataClass.transactionList[j].dealOpenPrice * portfolioDataClass.transactionList[j].dealAmount) + (portfolioDataClass.transactionList[j].dealOpenPrice * portfolioDataClass.transactionList[j].dealAmount * percentage))
                        } else {
                            totalMoney += data[i].price * portfolioDataClass.transactionList[j].dealAmount
                        }
                    } else {
                        totalMoney += data[i].price * portfolioDataClass.transactionList[j].dealAmount
                    }
                }
            }
        }

        // updateChangePct
        val changeValue =
            abs((calculateTomanLastAmount() - (totalMoney * realmDB.getDollarPrice().price.toDouble())))
        val pct = (changeValue * 100) / calculateTomanLastAmount()
        if (calculateTomanLastAmount() > (totalMoney * realmDB.getDollarPrice().price.toDouble()))
            portfolioDataClass.changePct = "-$pct"
        else
            portfolioDataClass.changePct = "$pct"

        portfolioDataClass.totalCapital = totalMoney
        // updateNewData
        realmDB.updateHistoryData(portfolioDataClass)
    }

    override fun onGetChartData(data: List<ChartDataClass>) {
        RequestCount++
        listMain.add(data)
        if (CoinCount == RequestCount) {
            RequestCount = 0
            calculateChartData()
        }
    }

    fun calculateChartData() {
        val finList = arrayListOf<PointValue>()
        val capitalList = arrayListOf<ChartDataClass>()
        for (i in 0 until listMain[0].size) {
            var money = 0.toDouble()
            var time = 0.toDouble()
            listMain.forEach {
                var count = 0.toDouble()
                for (j in 0 until portfolioDataClass.transactionList.size)
                    if (portfolioDataClass.transactionList[j].coinName == it[i].coinName) {
                        count = portfolioDataClass.transactionList[j].dealAmount
                        time = it[i].time
                        if (portfolioDataClass.transactionList[j].dealType == "SELL") {

                            if (it[i].closePrice > portfolioDataClass.transactionList[j].dealOpenPrice) {
                                val percentage =
                                    ((it[i].closePrice - portfolioDataClass.transactionList[j].dealOpenPrice) / portfolioDataClass.transactionList[j].dealOpenPrice)
                                if (percentage <= 1)
                                    money += (portfolioDataClass.transactionList[j].dealOpenPrice * count) - (portfolioDataClass.transactionList[j].dealOpenPrice * count * percentage)
                            } else if (it[i].closePrice < portfolioDataClass.transactionList[j].dealOpenPrice) {
                                val percentage =
                                    ((portfolioDataClass.transactionList[j].dealOpenPrice - it[i].closePrice) / portfolioDataClass.transactionList[j].dealOpenPrice)
                                money += ((portfolioDataClass.transactionList[j].dealOpenPrice * portfolioDataClass.transactionList[j].dealAmount) + (portfolioDataClass.transactionList[j].dealOpenPrice * portfolioDataClass.transactionList[j].dealAmount * percentage))
                            } else {
                                money += it[i].closePrice * count
                            }
                        } else {
                            money += it[i].closePrice * count
                        }
                    }
            }
            capitalList.add(ChartDataClass("", time, money))
        }

        capitalList.forEach {
            finList.add(
                PointValue(
                    it.time.toFloat(),
                    it.closePrice.toFloat()
                )
            )
        }

        listMain.clear()
        setUpLiveChart(finList)
    }

    override fun onFailureChartData() {
        Toast.makeText(requireActivity(), requireActivity().resources.getString(R.string.errorLog), Toast.LENGTH_SHORT).show()
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
                UpdateChartData()
            }

            R.id.oneWeek -> {
                setCurrentItemTextColor(views, oneWeek)
                TIME_PERIOD = "1W"
                UpdateChartData()
            }

            R.id.oneMonth -> {
                setCurrentItemTextColor(views, oneMonth)
                TIME_PERIOD = "1M"
                UpdateChartData()
            }

            R.id.threeMonth -> {
                setCurrentItemTextColor(views, threeMonth)
                TIME_PERIOD = "3M"
                UpdateChartData()
            }

            R.id.sixMonth -> {
                setCurrentItemTextColor(views, sixMonth)
                TIME_PERIOD = "6M"
                UpdateChartData()
            }

            R.id.oneYear -> {
                setCurrentItemTextColor(views, oneYear)
                TIME_PERIOD = "1Y"
                UpdateChartData()
            }

            R.id.all_Time -> {
                setCurrentItemTextColor(views, all_Time)
                TIME_PERIOD = "ALL"
                UpdateChartData()
            }

        }
    }

    fun setCurrentItemTextColor(views: List<View>, view: View) {
        (view as CTextView).setTextColor(ContextCompat.getColor(requireActivity(), R.color.light_orange))
        views.forEach {
            if (it.id != view.id)
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

    inner class onDollarPriceRecieve : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            try {
                setData()
            }catch (e:Exception){}
        }
    }

    override fun onStart() {
        super.onStart()
        ondollarpriceRecieve = onDollarPriceRecieve()
        requireActivity().registerReceiver(
            ondollarpriceRecieve,
            IntentFilter("com.dust.extracker.onDollarPriceRecieve")
        )
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(ondollarpriceRecieve)
    }
}