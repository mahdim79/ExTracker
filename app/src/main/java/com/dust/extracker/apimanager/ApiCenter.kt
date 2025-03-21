package com.dust.extracker.apimanager

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dust.extracker.dataclasses.ChartDataClass
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.DetailsDataClass
import com.dust.extracker.dataclasses.DollarInfoDataClass
import com.dust.extracker.dataclasses.ExchangerDataClass
import com.dust.extracker.dataclasses.LastChangeDataClass
import com.dust.extracker.dataclasses.NewsDataClass
import com.dust.extracker.dataclasses.PortfotioDataClass
import com.dust.extracker.dataclasses.PriceDataClass
import com.dust.extracker.dataclasses.UserDataClass
import com.dust.extracker.interfaces.OnDetailsDataReceive
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnGetChartData
import com.dust.extracker.interfaces.OnGetDailyChanges
import com.dust.extracker.interfaces.OnGetDollarPrice
import com.dust.extracker.interfaces.OnGetExchangersData
import com.dust.extracker.interfaces.OnGetMainPrices
import com.dust.extracker.interfaces.OnGetNews
import com.dust.extracker.interfaces.OnGetPortfolioMainData
import com.dust.extracker.interfaces.OnGetTotalMarketCap
import com.dust.extracker.interfaces.OnSmsSend
import com.dust.extracker.interfaces.OnUpdateSortOrder
import com.dust.extracker.interfaces.OnUpdateUserData
import com.dust.extracker.interfaces.OnUserDataReceived
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ApiCenter(var context: Context, var onGetAllCryptoList: OnGetAllCryptoList) {

    val cryptoCompareApiKey = "45719568c9ac5d836ebe26a7067647e37b7194a8864d8656372156ea3cb5c7c4"
    val cryptoCompareApiKey2 = "1ee7d718b849a41ecb9a844ae4d9b8167837294ac46ceeb04538125533bc5982"

    fun getAllCryptoList() {
        Log.i("InitLog","sending first request...")
        val request = JsonObjectRequest(
            Request.Method.GET,
            "https://min-api.cryptocompare.com/data/all/coinlist?$cryptoCompareApiKey",
            null,
            {
                Log.i("InitLog","get all crypto success")
                if (it.getString("Response") == "Success") {
                    val parseMainData = ParseMainData()
                    parseMainData.execute(it)
                }
            },
            {})
        request.retryPolicy = DefaultRetryPolicy(
            20000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }

    fun getCryptoPriceByName(name: String, dataNum: Int) {

        val request = JsonObjectRequest(
            Request.Method.GET,
            "https://min-api.cryptocompare.com/data/price?fsym=$name&tsyms=USD&api_key={$cryptoCompareApiKey}",
            null,
            {
                it?.keys()?.forEach { key ->
                    if (key == "USD")
                        onGetAllCryptoList.onGetByName(it.getDouble("USD"), dataNum)
                }
            },
            {

            })
        request.retryPolicy = DefaultRetryPolicy(
            6000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)

    }

    fun getDollarPrice(onGetDollarPrice: OnGetDollarPrice) {

        val request = JsonObjectRequest(
            Request.Method.GET,
            "https://brsapi.ir/FreeTsetmcBourseApi/Api_Free_Gold_Currency_v2.json",
            null,
            {
                val currencies = it!!.getJSONArray("currency")
                val res = currencies.getJSONObject(0)
                onGetDollarPrice.onGet(
                    DollarInfoDataClass(
                        res.getDouble("price").toString(),
                        res.getString("date")
                    )
                )
            },
            {

            })
        request.retryPolicy = DefaultRetryPolicy(
            6000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }

    fun getMainPrices(coins: String, onGetMainPrices: OnGetMainPrices) {
        var url =
            "https://min-api.cryptocompare.com/data/pricemulti?fsyms=$coins&tsyms=USD&api_key={$cryptoCompareApiKey}"

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val list = arrayListOf<PriceDataClass>()
                response!!.keys().forEach {
                    try {
                        val object1 = response.getJSONObject(it)
                        val price = object1.getDouble("USD")
                        list.add(PriceDataClass(price, "$it"))
                    } catch (e: Exception) {
                        list.add(PriceDataClass(0.toDouble(), "$it"))
                    }
                }
                onGetMainPrices.onGetPrices(list)
            },
            {
            }
        )
        request.retryPolicy = DefaultRetryPolicy(
            6000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)

    }

    fun getDailyChanges(coinNames: String, onGetDailyChanges: OnGetDailyChanges, type: Int = 1) {
        var url =
            "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=$coinNames&tsyms=USD&api_key={$cryptoCompareApiKey}"

        if (type == 2)
            url =
                "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=$coinNames&tsyms=USD&api_key={$cryptoCompareApiKey2}"
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            {
                parseDailyData(it!!, onGetDailyChanges)
            },
            {

            }
        )
        request.retryPolicy = DefaultRetryPolicy(
            6000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }


    private fun parseDailyData(response: JSONObject, onGetDailyChanges: OnGetDailyChanges) {

        val list = arrayListOf<LastChangeDataClass>()
        var DISPLAY: JSONObject? = null
        try {
            DISPLAY = response.getJSONObject("DISPLAY")
        } catch (e: Exception) {
        }
        DISPLAY?.keys()?.forEach {
            try {
                val coinObject = DISPLAY.getJSONObject(it)
                val mainData = coinObject.getJSONObject("USD")
                list.add(LastChangeDataClass("$it", mainData.getString("CHANGEPCTDAY").toDouble()))
            } catch (e: Exception) {
                list.add(LastChangeDataClass("$it", 0.toDouble()))
            }
        }
        onGetDailyChanges.onGetDailyChanges(list)
    }

    fun sendVerificationCodeByMessage(code: Int, phoneNumber: Double, onSmsSend: OnSmsSend) {
        val message = "کاربر عزیز خوش آمدید!\nکد فعال سازی شما:\n$code"
        val request = JsonObjectRequest(
            Request.Method.GET,
            "https://raygansms.com/SendMessageWithCode.ashx?Username=Mahdim79&Password=6520062847Mahdi&Mobile=$phoneNumber&Message=$message",
            null,
            {
                // TODO: 6/7/2021 parsing data returned from server
                onSmsSend.onSuccess()
            },
            {
                onSmsSend.onFailure()
            })
        request.retryPolicy = DefaultRetryPolicy(
            6000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }

    fun getUserData(phoneNumber: Double, onUserDataReceived: OnUserDataReceived) {
        val jsonReqObject = JSONArray()
        jsonReqObject.put(phoneNumber)

        val request = JsonArrayRequest(
            Request.Method.POST,
            "",
            jsonReqObject,
            {
                // TODO: 6/7/2021 parsing data base on datatype received
            },
            {
                onUserDataReceived.onReceiveFailure()
            })
    }

    fun updateUserData(
        data: UserDataClass,
        oldPhoneNumber: Double,
        onUpdateUserData: OnUpdateUserData
    ) {
        val jsonArray = JSONArray()
        jsonArray.put(data.phoneNumber)
        jsonArray.put(data.Email)
        jsonArray.put(data.name)

        val request = JsonArrayRequest(
            Request.Method.POST,
            "",
            jsonArray,
            {

            },
            {
                onUpdateUserData.onFailure()
            })
        request.retryPolicy = DefaultRetryPolicy(
            6000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)

    }

    inner class ParseMainData : AsyncTask<JSONObject, String, List<CryptoMainData>>() {
        override fun doInBackground(vararg obj: JSONObject?): List<CryptoMainData> {
            val mainObject = obj[0]?.getJSONObject("Data")
            val list = arrayListOf<CryptoMainData>()

            val gson = Gson()
            mainObject?.keys()?.forEach {
                val data = gson.fromJson(
                    mainObject.getJSONObject(it).toString(),
                    CryptoMainData::class.java
                )
                data.SortOrder = "111"
                data.BaseImageUrl = "https://www.cryptocompare.com"
                data.BaseLinkUrl = "https://www.cryptocompare.com"
                list.add(data)

            }
            return list
        }

        override fun onPostExecute(result: List<CryptoMainData>?) {
            super.onPostExecute(result)
            onGetAllCryptoList.onGet(result!!)
        }
    }

    fun getChartData(coinName: String, format: String, onGetChartData: OnGetChartData) {
        var url = ""
        when (format) {
            "24H" -> url =
                "https://min-api.cryptocompare.com/data/v2/histohour?fsym=$coinName&tsym=USD&limit=23&api_key={$cryptoCompareApiKey}"

            "1W" -> url =
                "https://min-api.cryptocompare.com/data/v2/histoday?fsym=$coinName&tsym=USD&limit=6&api_key={$cryptoCompareApiKey}"

            "1M" -> url =
                "https://min-api.cryptocompare.com/data/v2/histoday?fsym=$coinName&tsym=USD&limit=29&api_key={$cryptoCompareApiKey}"

            "3M" -> url =
                "https://min-api.cryptocompare.com/data/v2/histoday?fsym=$coinName&tsym=USD&limit=89&api_key={$cryptoCompareApiKey}"

            "6M" -> url =
                "https://min-api.cryptocompare.com/data/v2/histoday?fsym=$coinName&tsym=USD&limit=179&api_key={$cryptoCompareApiKey}"

            "1Y" -> url =
                "https://min-api.cryptocompare.com/data/v2/histoday?fsym=$coinName&tsym=USD&limit=364&api_key={$cryptoCompareApiKey}"

            "ALL" -> url =
                "https://min-api.cryptocompare.com/data/v2/histoday?fsym=$coinName&tsym=USD&limit=1900&api_key={$cryptoCompareApiKey}"
        }

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            {
                try {
                    if (it!!.getString("Response") == "Success") {

                        val data = it.getJSONObject("Data").getJSONArray("Data")
                        val list = arrayListOf<ChartDataClass>()
                        for (i in 0 until data.length())
                            list.add(
                                ChartDataClass(
                                    coinName,
                                    data.getJSONObject(i).getDouble("time"),
                                    data.getJSONObject(i).getDouble("close")
                                )
                            )
                        onGetChartData.onGetChartData(list)
                    } else {
                        Log.i("apiResponse", "log1")
                        onGetChartData.onFailureChartData()
                    }
                } catch (e: Exception) {
                    onGetChartData.onFailureChartData()
                }
            },
            {
                Log.i("apiResponse", "log3")
                onGetChartData.onFailureChartData()
            })
        request.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }

    fun getPortfolioMainData(coinNames: String, onGetPortfolioMainData: OnGetPortfolioMainData) {
        val request = JsonObjectRequest(
            Request.Method.GET,
            "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=$coinNames&tsyms=USD&api_key={$cryptoCompareApiKey}",
            null,
            {
                val list = arrayListOf<PortfotioDataClass>()
                val DISPLAY = it.getJSONObject("DISPLAY")
                DISPLAY.keys().forEach {
                    try {
                        val coinObject = DISPLAY.getJSONObject(it)
                        val mainData = coinObject.getJSONObject("USD")
                        val str = mainData.getString("PRICE")
                        val strBuilder = StringBuilder(str)
                        strBuilder.deleteCharAt(str.indexOf("$ "))
                        if (str.indexOf(",") != -1)
                            strBuilder.deleteCharAt(str.indexOf(",") - 1)
                        list.add(
                            PortfotioDataClass(
                                "$it",
                                strBuilder.toString().toDouble(),
                                mainData.getString("CHANGEPCTDAY")
                            )
                        )
                    } catch (e: Exception) {
                        list.add(PortfotioDataClass("$it", 0.toDouble(), "0"))
                    }
                }
                onGetPortfolioMainData.onGetPortfolioData(list)
            },
            {

            }
        )
        request.retryPolicy = DefaultRetryPolicy(
            6000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }

    fun getDetails(coinName: String, onDetailsDataReceive: OnDetailsDataReceive) {
        val request = JsonObjectRequest(
            Request.Method.GET,
            "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=$coinName&tsyms=USD&api_key={$cryptoCompareApiKey}",
            null,
            {
                try {
                    val obj =
                        it.getJSONObject("RAW").getJSONObject("$coinName").getJSONObject("USD")
                    val result = DetailsDataClass(
                        obj.getString("MKTCAP"),
                        obj.getString("VOLUMEDAY"),
                        obj.getString("SUPPLY")
                    )
                    onDetailsDataReceive.onReceive(result)
                } catch (e: Exception) {
                }
            },
            {

            })
        request.retryPolicy = DefaultRetryPolicy(
            9000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }

    fun getTotalMarketCap(onGetTotalMarketCap: OnGetTotalMarketCap) {
        Log.i("coingeckoRequest", "getTotalMarketCap")
        val request = JsonObjectRequest(
            Request.Method.GET,
            "https://api.coingecko.com/api/v3/global",
            null,
            {
                try {
                    val data = it.getJSONObject("data").getJSONObject("total_market_cap")
                    onGetTotalMarketCap.onGetMarketCap(data.getDouble("usd"))
                } catch (e: Exception) {
                }
            },
            {})
        request.retryPolicy = DefaultRetryPolicy(
            6000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }

    fun getExchangersData(onGetExchangersData: OnGetExchangersData) {
        Log.i("coingeckoRequest", "getExchangersData")
        val request = JsonArrayRequest(
            Request.Method.GET,
            "https://api.coingecko.com/api/v3/exchanges?per_page=249",
            null,
            {
                val list = arrayListOf<ExchangerDataClass>()
                for (i in 0 until it.length()) {
                    val obj = it.getJSONObject(i)
                    try {
                        list.add(
                            ExchangerDataClass(
                                obj.getString("name"),
                                obj.getInt("year_established"),
                                obj.getString("country"),
                                obj.getString("url"),
                                obj.getString("image"),
                                obj.getInt("trust_score"),
                                obj.getInt("trust_score_rank"),
                                obj.getDouble("trade_volume_24h_btc")
                            )
                        )
                    } catch (e: Exception) {
                        continue
                    }
                }
                onGetExchangersData.onGetExchangerData(list)
            },
            {})
        request.retryPolicy = DefaultRetryPolicy(
            6000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }

    fun getMarketCapSortOrder(onUpdateSortOrder: OnUpdateSortOrder) {
        Log.i("InitLog","sending sort request...")

        val request = JsonArrayRequest(
            Request.Method.GET,
            "https://api.coingecko.com/api/v3/coins/markets?vs_currency=Usd&order=market_cap_desc&per_page=110&page=1&sparkline=false",
            null,
            {
                Log.i("InitLog","sort request success...")
                val list = arrayListOf<Pair<String, Int>>()
                for (i in 0 until it.length())
                    list.add(
                        Pair(
                            it.getJSONObject(i).getString("name"),
                            it.getJSONObject(i).getInt("market_cap_rank")
                        )
                    )
                onUpdateSortOrder.onUpdateSortOrder(list)
            },
            {})
        request.retryPolicy = DefaultRetryPolicy(
            6000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }

    fun getNews(onGetNews: OnGetNews) {
        val request = JsonObjectRequest(Request.Method.GET,
            "https://newsdata.io/api/1/news?apikey=pub_75401313d1b4e67864e7abdcdbc857925d56a&q=بیت%20کوین&language=fa",
            null,
            {
                try {
                    val data = it.getJSONArray("results")
                    val resultList = arrayListOf<NewsDataClass>()
                    for (i in 0 until data.length()) {
                        try {
                            val obj = data.getJSONObject(i)!!

                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            formatter.timeZone = TimeZone.getTimeZone("UTC")

                            var date = 0
                            formatter.parse(obj.getString("pubDate"))?.let { newsDate ->
                                date = (newsDate.time / 1000).toInt()
                            }

                            resultList.add(
                                NewsDataClass(
                                    i,
                                    obj.getString("article_id"),
                                    obj.getString("title"),
                                    obj.getString("description"),
                                    0,
                                    0,
                                    obj.getString("image_url"),
                                    false,
                                    date,
                                    obj.getString("link"),
                                    getNewsCategory(obj.getString("source_name"),obj.getString("title")),
                                    obj.getString("source_id")
                                )
                            )
                        } catch (e: Exception) {
                            Log.i("afd","adsf")
                        }
                    }
                    onGetNews.onGetNews(resultList)
                } catch (e: Exception) {
                }
            },
            {
                Log.i("cryptocompareNews",it.message.toString())
            })
        request.retryPolicy = DefaultRetryPolicy(
            5000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }

    private fun getNewsCategory(defaultCategory:String,text:String): String {
        var category = defaultCategory
        if (category.isEmpty())
            category = "any"
        if (text.contains("بیت کوین") || text.contains("بیت"))
            category += ",BTC"
        if (text.contains("اتریوم"))
            category += ",ETH"
        if (text.contains("تحلیل"))
            category += ",Trading"
        if (text.contains("آلتکوین"))
            category += ",Altcoin"
        return category
    }

}