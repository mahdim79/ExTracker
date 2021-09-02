package com.dust.extracker.realmdb

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ExchangerObject:RealmObject() {
    @PrimaryKey
    var id:Int? = null
    lateinit var name: String
    var year_established: Int? = null
    lateinit var country: String
    lateinit var url: String
    lateinit var imageUrl: String
    var trust_score: Int? = null
    var trust_score_rank: Int? = null
    var trade_volume_24h_btc: Double? = null
}