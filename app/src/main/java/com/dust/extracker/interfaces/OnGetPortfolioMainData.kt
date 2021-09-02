package com.dust.extracker.interfaces

import com.dust.extracker.dataclasses.PortfotioDataClass

interface OnGetPortfolioMainData {
    fun onGetPortfolioData(data:List<PortfotioDataClass>)
}