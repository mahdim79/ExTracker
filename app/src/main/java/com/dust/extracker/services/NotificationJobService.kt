package com.dust.extracker.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dust.extracker.R
import com.dust.extracker.activities.SplashActivity
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.HistoryDataClass
import com.dust.extracker.dataclasses.LastChangeDataClass
import com.dust.extracker.dataclasses.NewsDataClass
import com.dust.extracker.dataclasses.NotificationDataClass
import com.dust.extracker.dataclasses.PortfotioDataClass
import com.dust.extracker.dataclasses.TransactionDataClass
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnGetDailyChanges
import com.dust.extracker.interfaces.OnGetNews
import com.dust.extracker.interfaces.OnGetPortfolioMainData
import com.dust.extracker.realmdb.DollarObject
import com.dust.extracker.realmdb.HistoryObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.google.gson.Gson
import io.realm.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.floor
import androidx.core.net.toUri
import com.dust.extracker.utils.Utils
import java.math.BigDecimal
import java.util.Locale

class NotificationJobService : JobService(), OnGetAllCryptoList, OnGetDailyChanges, OnGetNews {

    private lateinit var data: List<NotificationDataClass>
    private lateinit var apiService: ApiCenter
    private var popularList = arrayListOf<String>()
    val tag = "NotificationJobService"
    private var notificationEnabled = false
    private var priceNotificationEnabled = false
    private var newsNotificationEnabled = false
    private lateinit var portfolioList: List<String>
    private lateinit var shared: SharedPreferencesCenter

    private val notificationJobInterval = 30 * 1000L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.i(tag, "starting NotificationJobService")
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                withContext(Dispatchers.Main) {
                    handleNotifications()
                }
                delay(notificationJobInterval)
            }
        }
        return true
    }

    override fun onNetworkChanged(params: JobParameters) {
        // super.onNetworkChanged(params)
    }

    private fun checkDayPassedForShowingNotification(lastShow: Long): Boolean =
        System.currentTimeMillis() - lastShow > 86_400_000

    private fun handleNotifications() {
        shared = SharedPreferencesCenter(this)
        priceNotificationEnabled = shared.getPriceNotificationsEnabled()
        newsNotificationEnabled = shared.getImportantNewsNotificationsEnabled()
        notificationEnabled = shared.getNotificationEnabled()
        portfolioList = shared.getEnabledPortfolioNotifications()
        data = shared.getNotificationData()
        apiService = ApiCenter(this, this)

        if (checkConn()) {
            if (notificationEnabled) {
                Log.i(tag, "Request Sending ... count: ${data.size}")
                for (i in data.indices)
                    apiService.getCryptoPriceByName(data[i].symbol, i)

                if (priceNotificationEnabled) {
                    if (checkDayPassedForShowingNotification(shared.getLastDatePriceNotified())) {
                        if (popularList.isEmpty()) {
                            val allData = RealmDataBaseCenter().getPopularCoins()
                            for (i in allData.indices) {
                                popularList.add(allData[i].Name!!)
                                if (i == 3)
                                    break
                            }
                        }

                        apiService.getDailyChanges(
                            popularList.joinToString(","),
                            this@NotificationJobService
                        )
                    }
                }

                if (newsNotificationEnabled)
                    if (checkDayPassedForShowingNotification(shared.getLastDateNewsNotified()))
                        apiService.getNews(this@NotificationJobService)


                if (portfolioList.isNotEmpty()) {
                    if (checkDayPassedForShowingNotification(shared.getLastDatePortfolioNotified())) {
                        try {
                            getAllHistoryData(Realm.getDefaultInstance()).forEach {
                                if (portfolioList.contains(it.portfolioName)) {
                                    sendPortfolioDataRequest(
                                        it.portfolioName,
                                        it.transactionList
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.i(tag, "All History Exception")

                        }
                    }
                }
            }
        }

    }

    private fun getAllHistoryData(realmDB: Realm): List<HistoryDataClass> {
        val list = arrayListOf<HistoryDataClass>()
        realmDB.executeTransaction {
            val result = it.where(HistoryObject::class.java).findAll()
            result.forEach { history ->
                val listStr = history.transactionList.split("|")

                val transactionList = arrayListOf<TransactionDataClass>()
                listStr.forEach { str ->
                    transactionList.add(Gson().fromJson(str, TransactionDataClass::class.java))
                }
                list.add(
                    HistoryDataClass(
                        history.id!!,
                        history.portfolioName,
                        transactionList,
                        history.description,
                        history.totalCapital!!,
                        history.totalPrimaryCapital!!,
                        history.changePct
                    )
                )
            }
        }
        return list
    }

    fun getDollarPrice(realmDB: Realm): DollarObject {
        var result: DollarObject? = null
        realmDB.executeTransaction {
            try {
                result = realmDB.where(DollarObject::class.java).findFirst()!!
            } catch (e: Exception) {
                result = DollarObject()
                result!!.price = "0.0"
                result!!.date = "0"
                result!!.lastDollarPrice = "0.0"
                result!!.changePct = "0.0"
                result!!.id = 1
            }
        }
        return result!!
    }

    private fun sendPortfolioDataRequest(
        name: String,
        transactionList: List<TransactionDataClass>
    ) {
        Log.i(tag, "sendingPortfolioRequest for : $name")
        val list1 = arrayListOf<String>()
        transactionList.forEach {
            list1.add(it.coinName)
        }
        apiService.getPortfolioMainData(list1.joinToString(","), object : OnGetPortfolioMainData {
            override fun onGetPortfolioData(data: List<PortfotioDataClass>) {
                var totalMoney = 0.toDouble()
                for (j in transactionList.indices) {
                    for (i in data.indices) {
                        if (data[i].coinName == transactionList[j].coinName) {
                            transactionList[j].currentPrice = data[i].price
                            transactionList[j].currentDailyChange = data[i].dailyChange
                            if (transactionList[j].dealType == "SELL") {
                                if (transactionList[j].currentPrice > transactionList[j].dealOpenPrice) {
                                    val percentage =
                                        ((transactionList[j].currentPrice - transactionList[j].dealOpenPrice) / transactionList[j].dealOpenPrice)
                                    if (percentage <= 1)
                                        totalMoney += (transactionList[j].dealOpenPrice * transactionList[j].dealAmount) - (transactionList[j].dealOpenPrice * transactionList[j].dealAmount * percentage)
                                } else if (transactionList[j].currentPrice < transactionList[j].dealOpenPrice) {
                                    val percentage =
                                        ((transactionList[j].dealOpenPrice - transactionList[j].currentPrice) / transactionList[j].dealOpenPrice)
                                    totalMoney += ((transactionList[j].dealOpenPrice * transactionList[j].dealAmount) + (transactionList[j].dealOpenPrice * transactionList[j].dealAmount * percentage))
                                } else {
                                    totalMoney += data[i].price * transactionList[j].dealAmount
                                }
                            } else {
                                totalMoney += data[i].price * transactionList[j].dealAmount
                            }
                        }
                    }
                }
                var changePct = ""
                val currentCapital =
                    totalMoney * getDollarPrice(Realm.getDefaultInstance()).price.toDouble()
                val lastCapital = calculateTomanLastAmount(transactionList)
                Log.i(tag, "$currentCapital")

                val changeValue =
                    abs((lastCapital - (currentCapital)))
                val pct = (changeValue * 100) / lastCapital
                if (lastCapital > (currentCapital))
                    changePct = "${Utils.formatPriceNumber(pct,2, Locale.ENGLISH)}"
                else
                    changePct = "+${Utils.formatPriceNumber(pct,2, Locale.ENGLISH)}"

                if (abs(pct).toInt() > 5)
                    sendPortfolioNotification(name, Utils.formatPriceNumber(currentCapital,0,
                        Locale.ENGLISH), changePct)

            }
        })
    }

    private fun createNotificationChannel(): String {
        val channelId = this.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "cryptoline_notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.lightColor = Color.BLUE
            channel.vibrationPattern = longArrayOf(0L)
            channel.enableVibration(false)
            channel.setSound(null, null)
            val notificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }

    fun sendPortfolioNotification(
        portfolioName: String,
        currentCapital: String,
        changePct: String
    ) {

        if (checkNotificationPermission()) {
            shared.setLastDatePortfolioNotified(System.currentTimeMillis())
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            val notification = NotificationCompat.Builder(this, createNotificationChannel())
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.portfolioNotification,portfolioName))
                .setContentText(getString(R.string.portfolioNotificationContent,currentCapital,changePct))
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(LongArray(1) {
                    1000
                })
                .setSound("android.resources://${this.packageName}/${R.raw.notification_rington}".toUri())
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 101, intent, PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
            val notify =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notify.notify(generateFreshNotificationId(notify), notification)
            Log.i("serviceTag", "notifying notification")
        }

    }

    private fun calculateTomanLastAmount(transactionList: List<TransactionDataClass>): Double {
        var amount = 0.toDouble()
        transactionList.forEach {
            amount += (it.dealOpenPrice * it.dealAmount * it.dollarPrice)
        }
        return amount
    }

    override fun onGetDailyChanges(list: List<LastChangeDataClass>) {
        var changes = shared.getPriceNotificationsLevel()

        when (changes) {
            0 -> changes = 10
            1 -> changes = 15
            2 -> changes = 20
        }

        loop@ for (i in list.indices) {
            if (abs(list[i].ChangePercentage) >= changes.toDouble()) {
                sendPriceNotification(list[i])
                shared.setLastDatePriceNotified(System.currentTimeMillis())
                break@loop
            }
        }
    }

    private fun sendPriceNotification(data: LastChangeDataClass) {
        if (checkNotificationPermission()) {
            var changes = ""
            changes = if (data.ChangePercentage > 0)
                "+${String.format("%.2f", data.ChangePercentage)}"
            else
                "${String.format("%.2f", data.ChangePercentage)}"
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            val notification = NotificationCompat.Builder(this, createNotificationChannel())
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.priceChanges,data.CoinName))
                .setContentText(getString(R.string.dailyPriceChangeAmount, changes))
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(LongArray(1) {
                    1000
                })
                .setSound("android.resources://${this.packageName}/${R.raw.notification_rington}".toUri())
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 101, intent, PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
            val notify =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notify.notify(generateFreshNotificationId(notify), notification)
        }
    }

    override fun onGetByName(price: Double, dataNum: Int) {
        try {
            val notificationData = data[dataNum]
            Log.i(tag, "data receiver : $price $dataNum ${notificationData.targetPrice} ${notificationData.lastUpdatedPrice}")

            val downwardNotification = notificationData.lastPrice > notificationData.targetPrice

            if (downwardNotification) {
                if (price <= notificationData.targetPrice && notificationData.lastUpdatedPrice > notificationData.targetPrice){
                    sendNotification(
                        notificationData.symbol,
                        notificationData.targetPrice,
                        notificationData.mode,
                        notificationData.id,
                        true
                    )
                    if (notificationData.mode == 0)
                        return
                }
            } else {
                if (price >= notificationData.targetPrice && notificationData.lastUpdatedPrice < notificationData.targetPrice){
                    sendNotification(
                        notificationData.symbol,
                        notificationData.targetPrice,
                        notificationData.mode,
                        notificationData.id,
                        false
                    )
                    if (notificationData.mode == 0)
                        return
                }
            }
            shared.updateNotificationData(notificationData.id,price)
        } catch (e: Exception) {
            Log.i(tag, "exception :${e.message!!}")
        }
    }

    private fun sendNotification(
        symbol: String,
        targetPrice: Double,
        mode: Int,
        id: Int,
        downwardNotification:Boolean
    ) {
        if (checkNotificationPermission()) {
            Log.i(tag, "notify")
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

            val contentText = if (downwardNotification){
                getString(R.string.downwardPriceWarning,symbol,targetPrice.toString())
            }else{
                getString(R.string.upwardPriceWarning,symbol,targetPrice.toString())
            }

            val notification = NotificationCompat.Builder(this, createNotificationChannel())
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.priceWarning))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(LongArray(1) {
                    1000
                })
                .setSound("android.resources://${this.packageName}/${R.raw.notification_rington}".toUri())
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 101, intent, PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
            val notify =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notify.notify(generateFreshNotificationId(notify), notification)

            if (mode == 0) {
                Log.i(tag, "mode : $mode")
                shared.removeNotificationData(id)
                this.sendBroadcast(Intent("com.dust.extracker.OnServiceDeleteNotification"))
            }
        }

    }

    private fun generateFreshNotificationId(notify: NotificationManager): Int {
        val id = floor(Math.random() * 200).toInt()
        notify.activeNotifications.forEach {
            if (it.id == id)
                return generateFreshNotificationId(notify)
        }
        return id
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {}
    private fun checkConn(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return info != null && info.isConnectedOrConnecting
    }


    private fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return this.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun sendNewsNotification(data: NewsDataClass) {
        if (checkNotificationPermission()) {
            val intent = Intent(Intent.ACTION_VIEW, data.url.toUri())
            val notification = NotificationCompat.Builder(this, createNotificationChannel())
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle(data.title)
                .setContentText(data.description)
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(LongArray(1) {
                    1000
                })
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 101, intent, PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .setSound("android.resources://${this.packageName}/${R.raw.notification_rington}".toUri())
                .build()
            val notify =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notify.notify(generateFreshNotificationId(notify), notification)
        }
    }

    override fun onGetNews(list: List<NewsDataClass>) {
        if (!checkDayPassedForShowingNotification(shared.getLastDateNewsNotified()))
            return
        list.find { it.categories.indexOf("BTC", 0, true) != -1 }?.let { notifyNews ->
            sendNewsNotification(notifyNews)
            shared.setLastDateNewsNotified(System.currentTimeMillis())
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }


}