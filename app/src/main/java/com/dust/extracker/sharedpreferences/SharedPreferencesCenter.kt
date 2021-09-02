package com.dust.extracker.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import com.dust.extracker.dataclasses.NotificationDataClass
import com.google.gson.Gson

class SharedPreferencesCenter(var context: Context, var preferencesName: String = "USER_INFO") {

    fun getSharedPreferences(name: String = preferencesName): SharedPreferences {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    fun getSharedPreferencesEditor(name: String = preferencesName): SharedPreferences.Editor {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
    }

    fun checkUserLogIn(): Boolean = getSharedPreferences().getBoolean("IS_USER_LOG_IN", false)

    fun setUserLogIn(logIn: Boolean) =
        getSharedPreferencesEditor().putBoolean("IS_USER_LOG_IN", logIn).apply()

    fun getUserName(): String = getSharedPreferences().getString("USER_NAME", "null")!!

    fun getAvatarUrl(): String = getSharedPreferences().getString("AVATAR_URL", "null")!!

    fun setNotificationEnabled(enabled: Boolean) =
        getSharedPreferencesEditor("SETTINGS").putBoolean("NOTIFICATIONS", enabled).apply()

    fun getNotificationEnabled(): Boolean =
        getSharedPreferences("SETTINGS").getBoolean("NOTIFICATIONS", true)

    fun setNightMode(night: Boolean) =
        getSharedPreferencesEditor("SETTINGS").putBoolean("NIGHT_MODE", night).apply()

    fun getNightMode(): Boolean = getSharedPreferences("SETTINGS").getBoolean("NIGHT_MODE", false)

    fun setEnglishLanguage(en: Boolean) =
        getSharedPreferencesEditor("SETTINGS").putBoolean("ENGLISH_LANGUAGE", en).apply()

    fun getEnglishLanguage(): Boolean =
        getSharedPreferences("SETTINGS").getBoolean("ENGLISH_LANGUAGE", false)

    fun getMarketCap(): String = getSharedPreferences("marketCap").getString("Capacity", "-")!!

    fun setMarketCap(marketCap: String) =
        getSharedPreferences("marketCap").edit().putString("Capacity", marketCap).apply()

    fun setNotificationData(data: NotificationDataClass) {
        val list = arrayListOf<NotificationDataClass>()
        list.addAll(getNotificationData())
        list.add(data)
        val rawDataList = arrayListOf<String>()
        for (i in 0 until list.size){
            list[i].id = i
            rawDataList.add(Gson().toJson(list[i], NotificationDataClass::class.java))
        }

        getSharedPreferencesEditor("NOTIFICATION_DATA").putString(
            "DATA",
            rawDataList.joinToString("|")
        ).commit()
    }

    fun getNotificationData(): List<NotificationDataClass> {
        try {
            val shaData =
                getSharedPreferences("NOTIFICATION_DATA").getString("DATA", "")!!
            if (shaData == "")
                return arrayListOf()

            val rawData = shaData.split("|")

            val list = arrayListOf<NotificationDataClass>()
            rawData.forEach {
                list.add(Gson().fromJson(it, NotificationDataClass::class.java))
            }
            return list
        } catch (e: Exception) {
            return arrayListOf()
        }

    }

    fun removeNotificationData(id: Int) {
        val result = arrayListOf<NotificationDataClass>()
        val lastData = getNotificationData()
        lastData.forEach {
            if (it.id != id)
                result.add(it)
        }
        if (result.isEmpty()){
            getSharedPreferencesEditor("NOTIFICATION_DATA").putString(
                "DATA",
                ""
            ).commit()
        }
        for (i in 0 until result.size)
            result[i].id = i

        val finList = arrayListOf<String>()
        result.forEach {
            finList.add(Gson().toJson(it , NotificationDataClass::class.java))
        }

        getSharedPreferencesEditor("NOTIFICATION_DATA").putString(
            "DATA",
            finList.joinToString("|")
        ).commit()
    }

    fun setPriceNotificationsEnabled(enabled:Boolean) = getSharedPreferencesEditor("SETTINGS").putBoolean("PRICE_NOTIFICATIONS" , enabled).apply()

    fun getPriceNotificationsEnabled():Boolean = getSharedPreferences("SETTINGS").getBoolean("PRICE_NOTIFICATIONS" , true)

    fun setPriceNotificationsLevel(pctLevel:Int) = getSharedPreferencesEditor("SETTINGS").putInt("PRICE_NOTIFICATIONS_LEVEL" , pctLevel).apply()

    fun getPriceNotificationsLevel():Int = getSharedPreferences("SETTINGS").getInt("PRICE_NOTIFICATIONS_LEVEL" , 1)

    fun setLastDatePriceNotified(day:Int) = getSharedPreferencesEditor("SETTINGS").putInt("PRICE_NOTIFICATIONS_DAY" , day).apply()

    fun getLastDatePriceNotified():Int = getSharedPreferences("SETTINGS").getInt("PRICE_NOTIFICATIONS_DAY" , 1)

    fun setImportantNewsNotificationsEnabled(enabled:Boolean) = getSharedPreferencesEditor("SETTINGS").putBoolean("NEWS_NOTIFICATIONS" , enabled).apply()

    fun getImportantNewsNotificationsEnabled():Boolean = getSharedPreferences("SETTINGS").getBoolean("NEWS_NOTIFICATIONS" , false)

    fun getLastDateNewsNotified():Int = getSharedPreferences("SETTINGS").getInt("NEWS_NOTIFICATIONS_DAY" , 1)

    fun setLastDateNewsNotified(day:Int) = getSharedPreferencesEditor("SETTINGS").putInt("NEWS_NOTIFICATIONS_DAY" , day).apply()

    fun setEnabledPortfolioNotifications(name:String){
        val data = getEnabledPortfolioNotifications()
        val list = arrayListOf<String>()
        list.addAll(data)
        list.add(name)
        getSharedPreferencesEditor("PORTFOLIO_NOTIFICATIONS").putString("PORTFOLIOS" , list.joinToString(",")).apply()
    }

    fun getEnabledPortfolioNotifications():List<String>{
        val rawData = getSharedPreferences("PORTFOLIO_NOTIFICATIONS").getString("PORTFOLIOS" , "")
        if (rawData == "")
            return arrayListOf()
        return rawData!!.split(",")
    }

    fun removeEnabledPortfolioNotifications(name:String){
        val data = getEnabledPortfolioNotifications()
        val list = arrayListOf<String>()
        data.forEach {
            if (it != name)
                list.add(it)
        }
        getSharedPreferencesEditor("PORTFOLIO_NOTIFICATIONS").putString("PORTFOLIOS" , list.joinToString(",")).apply()
    }

    fun setLastDatePortfolioNotified(day:Int) = getSharedPreferencesEditor("SETTINGS").putInt("PORTFOLIO_NOTIFICATIONS_DAY" , day).apply()

    fun getLastDatePortfolioNotified():Int = getSharedPreferences("SETTINGS").getInt("PORTFOLIO_NOTIFICATIONS_DAY" , 1)

    fun setFingerPrintEnabled(enabled: Boolean) = getSharedPreferencesEditor("SETTINGS").putBoolean("FINGERPRINT_ENABLED" , enabled).apply()

    fun getFingerPrintEnabled():Boolean =  getSharedPreferences("SETTINGS").getBoolean("FINGERPRINT_ENABLED" , false)

    fun setPassword(pass:Int) = getSharedPreferencesEditor("SETTINGS").putInt("PASSWORD" , pass).apply()

    fun getPassword():Int = getSharedPreferences("SETTINGS").getInt("PASSWORD" , 0)

}