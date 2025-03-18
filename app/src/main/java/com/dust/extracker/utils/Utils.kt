package com.dust.extracker.utils

import com.dust.extracker.R
import saman.zamani.persiandate.PersianDate
import java.text.DateFormatSymbols
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Utils {
    fun convertTimestampToDate(timeStamp:Long):String{
        val date = PersianDate(timeStamp)
        var hour = date.hour.toString()
        var minute = date.minute.toString()
        if (hour.length == 1)
            hour = "0$hour"
        if (minute.length == 1)
            minute = "0$minute"
        return "${date.shYear}/${date.shMonth}/${date.shDay}  $hour:$minute"
    }

    fun formatPriceNumber(num: Double,decimalCount:Int,locale:Locale? = null):String{
        var format = "#,###"
        if (decimalCount > 0){
            format += "."
            for (i in 0..decimalCount)
                format += "#"
        }

        var symbol = DecimalFormatSymbols()
        locale?.let { l ->
            symbol = DecimalFormatSymbols(l)
        }

        return try {
            DecimalFormat(format,symbol)
                .format(num)
        }catch (e:Exception){
            String.format("%.${decimalCount}f", num)
        }

    }

}