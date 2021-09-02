package com.dust.extracker.realmdb

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DollarObject:RealmObject() {
    @PrimaryKey
    var id:Int? = null

    lateinit var price:String
    lateinit var date:String
    lateinit var changePct:String
    lateinit var lastDollarPrice:String
}