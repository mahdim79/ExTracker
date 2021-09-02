package com.dust.extracker.interfaces

import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.realmdb.MainRealmObject

interface OnGetAllCryptoList {
    fun onGet(cryptoList:List<CryptoMainData>)
    fun onGetByName(price:Double, dataNum:Int)

}