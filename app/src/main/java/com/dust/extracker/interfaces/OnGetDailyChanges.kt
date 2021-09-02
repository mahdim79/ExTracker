package com.dust.extracker.interfaces

import com.dust.extracker.dataclasses.LastChangeDataClass

interface OnGetDailyChanges {
    fun onGetDailyChanges(list:List<LastChangeDataClass>)
}