package com.dust.extracker.dataclasses

import android.graphics.Bitmap

data class SearchDataClass(
    var id:Int,
    var name:String,
    var coinName:String,
    var image:Bitmap,
    var marketCap:Double
) {
}