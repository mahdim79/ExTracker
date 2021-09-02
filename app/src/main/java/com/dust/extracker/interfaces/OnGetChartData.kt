package com.dust.extracker.interfaces

import com.dust.extracker.dataclasses.ChartDataClass

interface OnGetChartData {
    fun onGetChartData(data: List<ChartDataClass>)
    fun onFailureChartData()
}