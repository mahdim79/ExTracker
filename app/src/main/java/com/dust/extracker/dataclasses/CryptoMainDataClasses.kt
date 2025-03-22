package com.dust.extracker.dataclasses

data class CryptoMainDataClasses(
    var Response: String,
    var Message: String,
    var HasWarning: Boolean,
    var Type: Int
) {
}

data class CryptoMainData(
    var Id: String,
    var ImageUrl: String,
    var Name: String,
    var Symbol: String,
    var maxSupply:Double
)

