package com.dust.extracker.realmdb

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class HistoryObject:RealmObject() {
    @PrimaryKey
    var id:Int? = null

   // lateinit var transactionList:String
    lateinit var portfolioName:String
    lateinit var description:String
    var totalCapital:Double? = null
    var totalPrimaryCapital:Double? = null
    lateinit var changePct:String
    lateinit var transactionList:String

}