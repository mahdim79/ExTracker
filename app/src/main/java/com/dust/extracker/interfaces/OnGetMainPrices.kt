package com.dust.extracker.interfaces

import com.dust.extracker.dataclasses.PriceDataClass

interface OnGetMainPrices {
    fun onGetPrices(priceList:List<PriceDataClass>)
}