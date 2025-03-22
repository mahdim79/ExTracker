package com.dust.extracker.realmdb

import java.util.*

class SortData {

    val realm = RealmDataBaseCenter()

    private val SORT_BY_REFRESH by lazy {
        0
    }
    private val SORT_BY_NAME by lazy {
        1
    }
    private val SORT_BY_PRICE by lazy {
        2
    }
    private val SORT_BY_DAILY_CHANGE by lazy {
        3
    }
    private val SORT_BY_COMMENTS by lazy {
        4
    }
   /* fun getSortedData(PaginationCount: Int, sortType: Int = SORT_BY_REFRESH, desend: Boolean = true): List<MainRealmObject> {
        // TODO: 6/6/2021 complete this ,man!
        val listMain = arrayListOf<MainRealmObject>()
        val stop = 50 * PaginationCount
        val list = realm.getAllCryptoData()

        if (sortType == SORT_BY_REFRESH) {
            for (i in 0.rangeTo(stop)) {
                try {
                    listMain.add(list[i]!!)
                } catch (e: Exception) {
                }
            }
            return listMain
        }
        val list1 = arrayListOf<MainRealmObject>()
        list1.addAll(list)
        when (sortType) {
            SORT_BY_NAME -> {
                Collections.sort(list1, kotlin.Comparator { t, t2 ->
                    t.CoinName!!.compareTo(t2.CoinName!!)
                })
            }
            SORT_BY_PRICE -> {

                Collections.sort(list1, kotlin.Comparator { t, t2 ->

                    if (t.LastPrice!!.toInt() > t2.LastPrice!!.toInt())
                        1
                    else if (t.LastPrice!!.toInt() < t2.LastPrice!!.toInt())
                        -1
                    else
                        0

                })

            }
            SORT_BY_DAILY_CHANGE -> {
                Collections.sort(list1, kotlin.Comparator { t, t2 ->
                    if (t.DailyChangePCT!!.toInt() > t2.DailyChangePCT!!.toInt())
                        1
                    else if (t.DailyChangePCT!!.toInt() < t2.DailyChangePCT!!.toInt())
                        -1
                    else
                        0
                })
            }
            SORT_BY_COMMENTS -> {
                Collections.sort(list1, kotlin.Comparator { t, t2 ->
                    if (t.CommentCount!!.toInt() > t2.CommentCount!!.toInt())
                        1
                    else if (t.CommentCount!!.toInt() < t2.CommentCount!!.toInt())
                        -1
                    else
                        0
                })
            }
        }
        if (desend) {
            list1.reverse()
        }

        for (i in 0.rangeTo(stop)) {
            try {
                listMain.add(list1[i])
            } catch (e: Exception) {
            }
        }

        return listMain
    }
*/

}