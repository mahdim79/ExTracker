package com.dust.extracker.realmdb

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MainRealmObject:RealmObject() {
    @PrimaryKey
    var id:Int? = null

    var ID:String? = null //
    var rank:Int? = null
    var ImageUrl: String? = null  //
    var Name: String? = null //
    var Symbol: String? = null
    var LastPrice: Double? = null //
    var DailyChangePCT: Double? = null //
    var maxSupply:Double? = null
    var circulatingSupply:Double? = null
}