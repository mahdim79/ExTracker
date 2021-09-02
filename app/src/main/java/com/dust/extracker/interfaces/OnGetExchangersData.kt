package com.dust.extracker.interfaces

import com.dust.extracker.dataclasses.ExchangerDataClass

interface OnGetExchangersData {
    fun onGetExchangerData(list:List<ExchangerDataClass>)
}