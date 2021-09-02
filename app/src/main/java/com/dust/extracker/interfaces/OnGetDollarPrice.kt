package com.dust.extracker.interfaces

import com.dust.extracker.dataclasses.DollarInfoDataClass

interface OnGetDollarPrice {
    fun onGet(price:DollarInfoDataClass)
}