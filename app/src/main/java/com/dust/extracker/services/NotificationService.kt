package com.dust.extracker.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dust.extracker.R
import com.dust.extracker.activities.SplashActivity
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.dataclasses.*
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnGetDailyChanges
import com.dust.extracker.interfaces.OnGetNews
import com.dust.extracker.interfaces.OnGetPortfolioMainData
import com.dust.extracker.realmdb.DollarObject
import com.dust.extracker.realmdb.HistoryObject
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import io.realm.Realm
import java.sql.Date
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

class NotificationService : Service(), OnGetAllCryptoList, OnGetDailyChanges, OnGetNews {
    private lateinit var data: List<NotificationDataClass>
    private lateinit var apiService: ApiCenter
    private lateinit var timer: Timer
    private var popularList = arrayListOf<String>()
    val tag = "ServiceTag"
    private var notificationEnabled = false
    private var priceNotificationEnabled = false
    private var newsNotificationEnabled = false
    private lateinit var portfolioList: List<String>
    private lateinit var shared: SharedPreferencesCenter
    private lateinit var updateData: UpdateData
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(tag, "started")

        shared = SharedPreferencesCenter(applicationContext)
        priceNotificationEnabled = shared.getPriceNotificationsEnabled()
        newsNotificationEnabled = shared.getImportantNewsNotificationsEnabled()
        notificationEnabled = shared.getNotificationEnabled()
        portfolioList = shared.getEnabledPortfolioNotifications()
        data = shared.getNotificationData()
        apiService = ApiCenter(applicationContext, this)
        updateData = UpdateData()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            registerReceiver(updateData, IntentFilter("com.dust.extracker.Update_NotificationData"),Context.RECEIVER_EXPORTED)
        }else{
            registerReceiver(updateData, IntentFilter("com.dust.extracker.Update_NotificationData"))
        }

        timer = Timer()
        timer.schedule(NewTimerTask(), 0, 30000)
        return Service.START_STICKY
    }

    inner class NewTimerTask : TimerTask() {
        override fun run() {
            if (checkConn()) {
                if (notificationEnabled) {
                    Log.i(tag, "Request Sending ...")
                    for (i in 0 until data.size)
                        apiService.getCryptoPriceByName(data[i].symbol, i)

                    if (priceNotificationEnabled) {
                        if (shared.getLastDatePriceNotified() != Date().day) {
                            if (popularList.isEmpty()) {
                                val allData = RealmDataBaseCenter().getPopularCoins()
                                for (i in 0 until allData.size) {
                                    popularList.add(allData[i].Name!!)
                                    if (i == 3)
                                        break
                                }
                            }

                            apiService.getDailyChanges(
                                popularList.joinToString(","),
                                this@NotificationService
                            )
                        }
                    }

                    if (newsNotificationEnabled)
                        if (shared.getLastDateNewsNotified() != Date().day)
                            apiService.getNews(this@NotificationService)


                    if (portfolioList.isNotEmpty()) {
                        if (shared.getLastDatePortfolioNotified() != Date().day) {
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
    }

    fun getAllHistoryData(realmDB: Realm): List<HistoryDataClass> {
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
        val list1 = arrayListOf<String>()
        transactionList.forEach {
            list1.add(it.coinName)
        }
        apiService.getPortfolioMainData(list1.joinToString(","), object : OnGetPortfolioMainData {
            override fun onGetPortfolioData(data: List<PortfotioDataClass>) {
                var totalMoney = 0.toDouble()
                for (j in 0 until transactionList.size) {
                    for (i in 0 until data.size) {
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
                    changePct = "${String.format("%.2f", pct)}"
                else
                    changePct = "+${String.format("%.2f", pct)}"

                if (abs(pct).toInt() > 10)
                    sendPortfolioNotification(name, currentCapital, changePct)

            }
        })
    }

    private fun createNotificationChannel(): String {
        val channelId = packageName
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
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }

    fun sendPortfolioNotification(
        portfolioName: String,
        currentCapital: Double,
        changePct: String
    ) {

        if (checkNotificationPermission()){
            shared.setLastDatePortfolioNotified(Date().day)
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            val notification = NotificationCompat.Builder(applicationContext,createNotificationChannel())
                .setOngoing(false)
                .setContentTitle("اطلاعیه پرتفولیو: $portfolioName")
                .setContentText("ارزش$currentCapital تومان | تغییر$changePct درصد")
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(LongArray(1) {
                    1000
                })
                .setSound(Uri.parse("android.resources://${this.packageName}/${R.raw.notification_rington}"))
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 101, intent
                        , PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
            val notify =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notify.notify(generateFreshNotificationId(notify), notification)
            Log.i("serviceTag","notifying notification")
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

        loop@ for (i in 0 until list.size) {
            if (abs(list[i].ChangePercentage) >= changes.toDouble()) {
                sendPriceNotification(list[i])
                shared.setLastDatePriceNotified(Date().day)
                break@loop
            }
        }
    }

    private fun sendPriceNotification(data: LastChangeDataClass) {
        if (checkNotificationPermission()){
            var changes = ""
            if (data.ChangePercentage > 0)
                changes = "+${String.format("%.2f", data.ChangePercentage)}"
            else
                changes = "${String.format("%.2f", data.ChangePercentage)}"
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            val notification = NotificationCompat.Builder(applicationContext,createNotificationChannel())
                .setOngoing(false)
                .setContentTitle("تغییرات قیمت ${data.CoinName}")
                .setContentText("میزان تغییر روزانه قیمت: $changes")
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(LongArray(1) {
                    1000
                })
                .setSound(Uri.parse("android.resources://${this.packageName}/${R.raw.notification_rington}"))
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 101, intent
                        , PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
            val notify =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notify.notify(generateFreshNotificationId(notify), notification)
        }
    }

    override fun onGetByName(price: Double, dataNum: Int) {
        Log.i(tag, "data receiver : $price $dataNum ${data[dataNum].targetPrice}")
        try {
            if (data[dataNum].lastPrice > data[dataNum].targetPrice) {
                if (price <= data[dataNum].targetPrice)
                    sendNotification(
                        data[dataNum].symbol,
                        data[dataNum].lastPrice,
                        data[dataNum].mode,
                        data[dataNum].id
                    )
            } else {
                if (price >= data[dataNum].targetPrice)
                    sendNotification(
                        data[dataNum].symbol,
                        data[dataNum].lastPrice,
                        data[dataNum].mode,
                        data[dataNum].id
                    )
            }
        } catch (e: Exception) {
            Log.i(tag, "exception :${e.message!!}")
        }
    }

    private fun sendNotification(
        symbol: String,
        lastPrice: Double,
        mode: Int,
        id: Int
    ) {
        if (checkNotificationPermission()){
            Log.i(tag, "notify")
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            val notification = NotificationCompat.Builder(applicationContext,createNotificationChannel())
                .setOngoing(false)
                .setContentTitle("قیمت به مقدار مد نظر رسید")
                .setContentText("قیمت$symbol از$lastPrice عبور کرد!")
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(LongArray(1) {
                    1000
                })
                .setSound(Uri.parse("android.resources://${this.packageName}/${R.raw.notification_rington}"))
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 101, intent
                        , PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
            val notify =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notify.notify(generateFreshNotificationId(notify), notification)

            if (mode == 0) {
                Log.i(tag, "mode : $mode")
                shared.removeNotificationData(id)
                sendBroadcast(Intent("com.dust.extracker.OnServiceDeleteNotification"))
                try {
                    this.stopSelf()
                } catch (e: Exception) {
                    Log.i(tag, "mode exception ${e.message!!}")
                } finally {
                    startService(Intent(this, NotificationService::class.java))
                }
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
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return info != null && info.isConnectedOrConnecting
    }

    inner class UpdateData : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1 != null && p1.extras != null) {
                if (p1.extras!!.containsKey("updateData"))
                    data = shared.getNotificationData()

                if (p1.extras!!.containsKey("PriceData"))
                    priceNotificationEnabled = p1.extras!!.getBoolean("PriceData")

                if (p1.extras!!.containsKey("newsData"))
                    newsNotificationEnabled = p1.extras!!.getBoolean("newsData")

                if (p1.extras!!.containsKey("EnabledPortfolio"))
                    portfolioList = shared.getEnabledPortfolioNotifications()

                if (p1.extras!!.containsKey("EnabledNotifications"))
                    notificationEnabled = shared.getNotificationEnabled()

            }
        }
    }

    override fun onDestroy() {
        Log.i(tag, "stoped")
        if (::timer.isInitialized){
            timer.purge()
            timer.cancel()
        }
        unregisterReceiver(updateData)
        super.onDestroy()
    }

    private fun checkNotificationPermission():Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun sendNewsNotification(data: NewsDataClass) {
        if (checkNotificationPermission()){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(data.url))
            val notification = NotificationCompat.Builder(applicationContext,createNotificationChannel())
                .setOngoing(false)
                .setContentTitle("${data.title}")
                .setContentText("${data.description}")
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(LongArray(1) {
                    1000
                })
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 101, intent
                        , PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .setSound(Uri.parse("android.resources://${this.packageName}/${R.raw.notification_rington}"))
                .build()
            val notify =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notify.notify(generateFreshNotificationId(notify), notification)
        }
    }

    override fun onGetNews(list: List<NewsDataClass>) {
        loopN@ for (i in 0 until list.size) {
            if (list[i].categories.indexOf("Bitcoin", 0, true) != -1) {
                sendNewsNotification(list[i])
                shared.setLastDateNewsNotified(Date().day)
                break@loopN
            }
        }
    }
}