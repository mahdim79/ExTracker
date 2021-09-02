package com.dust.extracker.dataclasses

data class TransactionDataClass(var id:Int  , var coinName:String, var dealType:String ,var dealAmount:Double , var dollarPrice:Double, var dealOpenPrice:Double , var currentPrice:Double , var currentDailyChange:String) {
}