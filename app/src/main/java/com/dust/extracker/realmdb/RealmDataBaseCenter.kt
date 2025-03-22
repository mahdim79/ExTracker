package com.dust.extracker.realmdb

import android.content.Context
import android.content.Intent
import android.service.autofill.UserData
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.R
import com.dust.extracker.dataclasses.*
import com.dust.extracker.fragments.portfoliofragments.EmptyPortfolioFragment
import com.dust.extracker.fragments.portfoliofragments.PortfolioFragment
import com.dust.extracker.interfaces.OnExchangerRealmDataAdded
import com.dust.extracker.interfaces.OnNewsDataAdded
import com.dust.extracker.interfaces.OnRealmDataChanged
import com.dust.extracker.interfaces.OnRealmDataSorted
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import io.realm.Realm
import io.realm.RealmResults
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.acos

class RealmDataBaseCenter() {
    var realmDB = Realm.getDefaultInstance()


    fun insertAllCryptoData(
        list: List<CryptoMainData>,
        onRealmDataChanged: OnRealmDataChanged,
        context: Context
    ) {
        realmDB.executeTransactionAsync({
            val dataToInsert = arrayListOf<MainRealmObject>()
            for ((id, i) in list.withIndex()) {
                val objectmain = MainRealmObject()
                objectmain.id = id
                objectmain.ID = i.Id
                objectmain.rank = id + 1
                objectmain.ImageUrl = i.ImageUrl
                objectmain.Name = i.Name
                objectmain.Symbol = i.Symbol
                objectmain.maxSupply = i.maxSupply
                objectmain.LastPrice = 0.0
                objectmain.DailyChangePCT = 0.0
                dataToInsert.add(objectmain)
            }
            it.copyToRealmOrUpdate(dataToInsert)
        }, {
            onRealmDataChanged.onAddComplete()
        }, {

        })
    }

    fun getCryptoDataCount(): Int = realmDB.where(MainRealmObject::class.java).findAll().size

    fun getCryptoData(PaginationCount: Int): List<MainRealmObject> {
        val list = arrayListOf<MainRealmObject>()
        val start = 50 * (PaginationCount - 1)
        val stop = 50 * PaginationCount
        for (i in start.rangeTo(stop)) {
            val data1 = realmDB.where(MainRealmObject::class.java).equalTo("id", i).findFirst()
            data1?.let { d ->
                list.add(d)
            }
        }
        return list
    }

    fun getPopularCoins(): List<MainRealmObject> {
        val list = arrayListOf<MainRealmObject>()
        try {
            val data = realmDB.where(MainRealmObject::class.java).findAll()
            list.addAll(data)
        } catch (e: Exception) {
        }
        return list
    }

    fun getCryptoDataByIds(list: List<String>): List<MainRealmObject> {
        val result = arrayListOf<MainRealmObject>()
        list.forEach {
            val data = realmDB.where(MainRealmObject::class.java).equalTo("ID", it).findFirst()
            result.add(data!!)
        }
        return result
    }

    fun getCryptoDataByName(name: String): MainRealmObject {
        var result: MainRealmObject? = null
        realmDB.executeTransaction {
            try {
                result = it.where(MainRealmObject::class.java).equalTo("Symbol", name).findFirst()!!
            } catch (e: Exception) {
                result = MainRealmObject()
                result!!.Name = "NULL"
            }
        }
        return result!!
    }

    fun getAllCryptoData(): RealmResults<MainRealmObject> =
        realmDB.where(MainRealmObject::class.java).findAll()

    fun insertDollarPrice(data: DollarInfoDataClass) {
        realmDB.executeTransaction {
            val obj = DollarObject()
            obj.id = 0
            obj.price = data.price
            obj.date = data.date
            try {
                val lastData = realmDB.where(DollarObject::class.java).findAll()
                if (lastData.size != 0) {
                    val lastPrice = lastData[0]
                    if (lastPrice!!.price.toDouble() > data.price.toDouble())
                        obj.changePct =
                            "-${(((lastPrice.price.toDouble() - data.price.toDouble()) / lastPrice.price.toDouble()) * 100)}"
                    else
                        obj.changePct =
                            (((data.price.toDouble() - lastPrice.price.toDouble()) / lastPrice.price.toDouble()) * 100).toString()
                    obj.lastDollarPrice = data.price
                } else {
                    obj.lastDollarPrice = data.price
                    obj.changePct = "0"
                }
            } catch (e: Exception) {
                obj.changePct = "0"
            }
            it.copyToRealmOrUpdate(obj)
        }
    }

    fun getDollarPrice(): DollarObject? {
        var result: DollarObject? = null
        realmDB.executeTransaction {
            try {
                result = realmDB.where(DollarObject::class.java).findFirst()!!
            } catch (e: Exception) {}
        }
        return result
    }

    fun checkDollarPriceAvailability(): Boolean {
        try {
            realmDB.where(DollarObject::class.java).findFirst()!!.price
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun checkDataAvailability(): Boolean {
        try {
            realmDB.where(MainRealmObject::class.java).findAll()!![0]!!.Name
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun updatePrices(realmObjects: List<MainRealmObject>, prices: List<PriceDataClass>) {
        Log.i("updatePrices","start")
        realmDB.executeTransaction {
            for (i in realmObjects.indices) {
                for (j in prices.indices) {
                    if (realmObjects[i].Symbol == prices[j].name)
                        realmObjects[i].LastPrice = prices[j].price
                }
            }
            it.copyToRealmOrUpdate(realmObjects)
        }
        Log.i("updatePrices","end")
    }

    fun updateDailyChanges(
        realmObjects: List<MainRealmObject>,
        changes: List<LastChangeDataClass>
    ) {
        realmDB.executeTransaction {
            for (i in realmObjects.indices) {
                for (j in changes.indices) {
                    if (realmObjects[i].Symbol == changes[j].CoinName)
                        realmObjects[i].DailyChangePCT = changes[j].ChangePercentage
                }
            }
            it.copyToRealmOrUpdate(realmObjects)
        }
    }

    fun getPricesByIds(idList: List<String>): List<PriceDataClass> {
        val list = arrayListOf<PriceDataClass>()
        realmDB.executeTransaction { realm ->
            for (element in idList) {
                try {
                    val obj = realm.where(MainRealmObject::class.java).equalTo("ID", element)
                        .findFirst()
                    list.add(PriceDataClass(obj!!.LastPrice!!, obj.Symbol!!))
                } catch (e: Exception) {
                }
            }
        }
        return list
    }

    fun getLastDailyChangesByIds(idList: List<String>): List<LastChangeDataClass> {
        val list = arrayListOf<LastChangeDataClass>()
        realmDB.executeTransaction { realm ->
            for (i in 0 until idList.size) {
                try {
                    val obj = realm.where(MainRealmObject::class.java).equalTo("ID", idList[i])
                        .findFirst()
                    list.add(LastChangeDataClass(obj!!.Symbol!!, obj.DailyChangePCT!!))
                } catch (e: Exception) {
                }
            }
        }
        return list
    }

    fun insertUserData(userData: UserDataClass) {
        realmDB.executeTransactionAsync(
            {
                val obj = it.createObject(UserObject::class.java, userData.id)
                obj.name = userData.name
                obj.userName = userData.userName
                obj.Email = userData.Email
                obj.phoneNumber = userData.phoneNumber
                obj.avatarUrl = userData.avatarUrl
            },
            {},
            {}
        )
    }

    fun deleteUserData() = realmDB.where(UserObject::class.java).findAll().deleteAllFromRealm()

    fun getUserData(): UserObject? = realmDB.where(UserObject::class.java).findFirst()

    fun updateUserData(userData: UserDataClass) {
        realmDB.executeTransaction {
            val obj = UserObject()
            obj.id = userData.id
            obj.name = userData.name
            obj.userName = userData.userName
            obj.Email = userData.Email
            obj.phoneNumber = userData.phoneNumber
            obj.avatarUrl = userData.avatarUrl

            it.copyToRealmOrUpdate(obj)
        }
    }

    fun insertHistoryData(
        historyDataClass: HistoryDataClass,
        fragmentManager: FragmentManager,
        context: Context
    ) {
        realmDB.executeTransactionAsync(
            {
                var id = 0
                try {
                    id = it.where(HistoryObject::class.java).max("id")!!.toInt() + 1
                } catch (e: Exception) {
                }
                val obj = it.createObject(HistoryObject::class.java, id)
                val listStr = arrayListOf<String>()
                historyDataClass.transactionList.forEach {
                    val dataStr = Gson().toJson(it, TransactionDataClass::class.java)
                    listStr.add(dataStr)
                }
                obj.changePct = historyDataClass.changePct
                obj.totalCapital = historyDataClass.totalCapital
                obj.description = historyDataClass.desctiption
                obj.portfolioName = historyDataClass.portfolioName
                obj.totalPrimaryCapital = historyDataClass.totalCapital
                obj.transactionList = listStr.joinToString("|")
            }
            ,
            {
                fragmentManager.apply {
                    popBackStack("InputDataFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    popBackStack("SelectCtyptoFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
                if (getAllHistoryData().size == 1) {
                    fragmentManager.beginTransaction()
                        .replace(R.id.frame_holder, PortfolioFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("PortfolioFragment")
                        .commit()
                }
            }
            ,
            {

            }
        )
    }

    fun updateHistoryData(historyDataClass: HistoryDataClass) {
        realmDB.executeTransaction {
            val historyObject = HistoryObject()
            historyObject.id = historyDataClass.id
            historyObject.totalCapital = historyDataClass.totalCapital
            historyObject.description = historyDataClass.desctiption
            historyObject.portfolioName = historyDataClass.portfolioName
            historyObject.changePct = historyDataClass.changePct
            val listStr = arrayListOf<String>()
            var primaryAmount = 0.toDouble()
            historyDataClass.transactionList.forEach {
                val dataStr = Gson().toJson(it, TransactionDataClass::class.java)
                listStr.add(dataStr)
                primaryAmount += (it.dealAmount * it.dealOpenPrice)
            }
            historyObject.transactionList = listStr.joinToString("|")
            historyObject.totalPrimaryCapital = primaryAmount
            it.copyToRealmOrUpdate(historyObject)
        }
    }

    fun getAllHistoryData(): List<HistoryDataClass> {
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

    fun getHistoryDataCount(): Int {
        var count = 0L
        realmDB.executeTransaction {
            count = it.where(HistoryObject::class.java).count()
        }
        return count.toInt()
    }

    fun deleteHistoryData(id: Int) {
        realmDB.executeTransaction {
            try {
                it.where(HistoryObject::class.java).equalTo("id", id).findFirst()!!
                    .deleteFromRealm()
            } catch (e: Exception) {
            }
        }
    }

    fun updatePrice(data: PriceDataClass) {
        realmDB.executeTransaction {
            val obj = it.where(MainRealmObject::class.java).equalTo("Symbol", data.name).findFirst()
            obj!!.LastPrice = data.price
            it.copyToRealmOrUpdate(obj)
        }
    }

    fun updateDailyChange(data: LastChangeDataClass) {
        realmDB.executeTransaction {
            val obj =
                it.where(MainRealmObject::class.java).equalTo("Symbol", data.CoinName).findFirst()
            obj!!.DailyChangePCT = data.ChangePercentage
            it.copyToRealmOrUpdate(obj)
        }
    }

    fun insertExchangerData(
        data: List<ExchangerDataClass>,
        onExchangerRealmDataAdded: OnExchangerRealmDataAdded
    ) {
        realmDB.executeTransactionAsync(
            {
                for (i in 0 until data.size) {
                    val obj = it.createObject(ExchangerObject::class.java, i)
                    obj.name = data[i].name
                    obj.year_established = data[i].year_established
                    obj.country = data[i].country
                    obj.url = data[i].url
                    obj.imageUrl = data[i].imageUrl
                    obj.trust_score = data[i].trust_score
                    obj.trust_score_rank = data[i].trust_score_rank
                    obj.trade_volume_24h_btc = data[i].trade_volume_24h_btc
                }
            },
            {
                onExchangerRealmDataAdded.onExchangerDataAdded()
            },
            {})
    }

    fun getExchangersData(PaginationCount: Int): List<ExchangerObject> {
        val resultMain = arrayListOf<ExchangerObject>()
        realmDB.executeTransaction {
            var start = (30 * (PaginationCount - 1))
            if (PaginationCount > 1)
                start++
            val stop = 30 * PaginationCount
            try {
                for (i in start.rangeTo(stop)) {
                    val data1 =
                        realmDB.where(ExchangerObject::class.java).equalTo("id", i).findFirst()
                    resultMain.add(data1!!)
                }
            } catch (e: Exception) {
            }
        }
        return resultMain
    }

    fun getAllExchangerData(): RealmResults<ExchangerObject> =
        realmDB.where(ExchangerObject::class.java).findAll()

    fun getExchangerDataByIds(idList: List<String>): List<ExchangerObject> {
        val result = arrayListOf<ExchangerObject>()
        idList.forEach {
            try {
                result.add(
                    realmDB.where(ExchangerObject::class.java).equalTo("id", it.toInt())
                        .findFirst()!!
                )
            } catch (e: Exception) {
            }
        }
        return result
    }

    fun updateExchangersData(data: List<ExchangerDataClass>) {
        realmDB.executeTransaction {
            val list = arrayListOf<ExchangerObject>()
            for (i in 0 until data.size) {
                val obj = ExchangerObject()
                obj.id = (data[i].trust_score_rank - 1)
                obj.name = data[i].name
                obj.year_established = data[i].year_established
                obj.country = data[i].country
                obj.url = data[i].url
                obj.imageUrl = data[i].imageUrl
                obj.trust_score = data[i].trust_score
                obj.trust_score_rank = data[i].trust_score_rank
                obj.trade_volume_24h_btc = data[i].trade_volume_24h_btc
                list.add(obj)
            }
            it.copyToRealmOrUpdate(list)
        }
    }

    fun getExchangersDataCount(): Int {
        var result = 0
        realmDB.executeTransaction {
            try {
                result = it.where(ExchangerObject::class.java).findAll().size
            } catch (e: Exception) {
            }
        }
        return result
    }

    fun insertNews(list: List<NewsDataClass>, onNewsDataAdded: OnNewsDataAdded) {
        realmDB.executeTransactionAsync({ realm ->
            list.forEach {
                val obj = realm.createObject(NewsObject::class.java, it.id)
                obj.ID = it.ID
                obj.title = it.title
                obj.description = it.description
                obj.likeCount = it.likeCount
                obj.seenCount = it.seenCount
                obj.imageUrl = it.imageUrl
                obj.is_liked = "${it.is_liked}"
                obj.date = it.date
                obj.url = it.url
                obj.categories = it.categories
                obj.tags = it.tags
            }

        }, {
            onNewsDataAdded.onNewsDataAdded()
        }, {})
    }

    fun getNews(category: String): List<NewsDataClass> {
        val resultList = arrayListOf<NewsDataClass>()
        realmDB.executeTransaction { realm ->
            try {
                val allData = realm.where(NewsObject::class.java).findAll()!!
                allData.forEach {
                    val newsData = NewsDataClass(it.id ?: 0,it.ID ?: "",it.title,it.description,it.likeCount ?: 0,it.seenCount ?: 0,it.imageUrl,it.is_liked == "true",it.date ?: 0,it.url,it.categories,it.tags)
                    if (category == "ALL")
                        resultList.add(newsData)
                    else
                        if (it.categories.indexOf(category) != -1)
                            resultList.add(newsData)
                }

            } catch (e: Exception) {
            }
        }
        return resultList
    }

    fun getNewsDataCount(): Int = realmDB.where(NewsObject::class.java).findAll().size

    fun updateNewsData(list: List<NewsDataClass>) {
        realmDB.executeTransaction { realm ->
            val dataList = arrayListOf<NewsObject>()
            val allCurrentNews = realm.where(NewsObject::class.java).findAll()
            list.forEach {
                try {
                    val obj = NewsObject()
                    obj.id = it.id
                    obj.ID = it.ID
                    obj.title = it.title
                    obj.description = it.description
                    obj.likeCount = it.likeCount
                    obj.seenCount = it.seenCount
                    obj.imageUrl = it.imageUrl
                    obj.is_liked = "${it.is_liked}"
                    obj.date = it.date
                    obj.url = it.url
                    obj.categories = it.categories
                    obj.tags = it.tags

                    allCurrentNews.find { o -> o.ID == it.ID }?.let { currentNews ->
                        obj.is_liked = currentNews.is_liked
                    }

                    dataList.add(obj)
                } catch (e: Exception) {
                }
            }
            realm.copyToRealmOrUpdate(dataList)
        }
    }

    fun setNewsIsLiked(newsId:String,isLiked:Boolean){
        realmDB.executeTransactionAsync { realm ->
            realm.where(NewsObject::class.java).equalTo("ID",newsId).findFirst()?.let { news ->
                news.is_liked = if (isLiked) "true" else "false"
                realm.copyToRealmOrUpdate(news)
            }
            realm.where(BookmarkObject::class.java).equalTo("ID",newsId).findFirst()?.let{ news ->
                news.is_liked = if (isLiked) "true" else "false"
                realm.copyToRealmOrUpdate(news)
            }
        }
    }

    fun getNewsDataById(id: Int): NewsDataClass {
        val data = realmDB.where(NewsObject::class.java).equalTo("id", id).findFirst()!!
        return NewsDataClass(data.id ?: 0,data.ID ?: "",data.title,data.description,data.likeCount ?: 0,data.seenCount ?: 0,data.imageUrl,data.is_liked == "true",data.date ?: 0,data.url,data.categories,data.tags)
    }

    fun insertBookmark(data: NewsDataClass) {
        realmDB.executeTransactionAsync({ realm ->
            var max = (realm.where(BookmarkObject::class.java).max("id") ?: -1).toInt()
            max += 1
            val obj = BookmarkObject()
            obj.id = max
            obj.ID = data.ID
            obj.title = data.title
            obj.description = data.description
            obj.url = data.url
            obj.date = data.date
            obj.imageUrl = data.imageUrl
            obj.is_liked = data.is_liked.toString()
            obj.seenCount = data.seenCount
            obj.likeCount = data.likeCount
            obj.categories = data.categories
            obj.tags = data.tags
            realm.copyToRealmOrUpdate(obj)

        }, {}, {})
    }

    fun getBookmarks(): List<NewsDataClass> {
        val list = arrayListOf<NewsDataClass>()
        realmDB.executeTransaction { realm ->
            realm.where(BookmarkObject::class.java).findAll().forEach {
                val data = NewsDataClass(0,it.ID ?: "",it.title,it.description,it.likeCount ?: 0,it.seenCount ?: 0,it.imageUrl,it.is_liked == "true",it.date ?: 0,it.url,it.categories,it.tags)
                list.add(data)
            }
        }
        return list
    }

    fun checkBookmarkAvailability(url: String): Boolean {
        var result = false
        realmDB.executeTransaction { realm ->
            try {
                realm.where(BookmarkObject::class.java).findAll().forEach {
                    if (it.url == url)
                        result = true
                }
            } catch (e: Exception) {
                result = false
            }
        }
        return result
    }

    fun removeBookmark(url: String) {
        realmDB.executeTransaction { realm ->
            try {
                realm.where(BookmarkObject::class.java).equalTo("url" , url).findFirst()!!.deleteFromRealm()
            }catch (e:Exception){}
        }
    }

}