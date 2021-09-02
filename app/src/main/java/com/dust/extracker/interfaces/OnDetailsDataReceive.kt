package com.dust.extracker.interfaces

import com.dust.extracker.dataclasses.DetailsDataClass

interface OnDetailsDataReceive {
    fun onReceive(data:DetailsDataClass)
}