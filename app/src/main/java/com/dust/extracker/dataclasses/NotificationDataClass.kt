package com.dust.extracker.dataclasses

data class NotificationDataClass(
    var id: Int,
    var symbol: String,
    var lastPrice: Double,
    var targetPrice: Double,
    var mode: Int
) {
}