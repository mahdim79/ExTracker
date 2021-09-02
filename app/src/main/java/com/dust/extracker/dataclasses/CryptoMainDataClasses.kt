package com.dust.extracker.dataclasses

data class CryptoMainDataClasses(
    var Response: String,
    var Message: String,
    var HasWarning: Boolean,
    var Type: Int
) {
}

data class CryptoMainData(
    var BaseImageUrl: String,
    var BaseLinkUrl: String,
    var Id: String,
    var Url: String,
    var ImageUrl: String,
    var Name: String,
    var Symbol: String,
    var CoinName: String,
    var FullName: String,
    var Description: String,
    var Algorithm: String,
    var AssetTokenStatus: String,
    var ProofType: String,
    var SortOrder: String,
    var Sponsored: String,
    var IsTrading: Boolean,
    var BlockNumber: Double,
    var NetHashesPerSecond: Double,
    var MaxSupply: Double,
    var MktCapPenalty: Double,
    var PlatformType: String,
    var AssetLaunchDate: String
) {
}

