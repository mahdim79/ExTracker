package com.dust.extracker.interfaces

import com.dust.extracker.realmdb.MainRealmObject

interface OnRealmDataChanged {
    fun onAddComplete(list :List<MainRealmObject>)
}