package com.dust.extracker.dataclasses

data class HistoryDataClass(var id:Int ,var portfolioName:String , var transactionList:List<TransactionDataClass> , var desctiption:String , var totalCapital:Double ,var totalPrimaryCapital:Double ,var changePct:String) {
}