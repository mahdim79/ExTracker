package com.dust.extracker.realmdb

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MainRealmObject:RealmObject() {
    @PrimaryKey
    var id:Int? = null

    var ID:String? = null //
    var BaseImageUrl: String? = null
    var BaseLinkUrl: String? = null
    var Url: String? = null
    var ImageUrl: String? = null  //
    var Name: String? = null //
    var Symbol: String? = null
    var CoinName: String? = null //
    var FullName: String? = null
    var Description: String? = null
    var Algorithm: String? = null
    var ProofType: String? = null
    var SortOrder: String? = null //
    var Sponsored: String? = null
    var IsTrading: Boolean? = null
    var BlockNumber: Double? = null
    var AssetTokenStatus: String? = null
    var MaxSupply: Double? = null
    var LastPrice: Double? = null //
    var DailyChangePCT: Double? = null //
    var NetHashesPerSecond: Double? = null
    var MktCapPenalty: Double? = null
    var PlatformType: String? = null
    var AssetLaunchDate: String? = null
    var CommentCount: Int? = null
}