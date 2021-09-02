package com.dust.extracker.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.dust.extracker.R
import com.dust.extracker.application.MyApplication

class CButton : AppCompatButton {
    constructor(context: Context) : super(context) {
        setTxtTypeface()
        setTxtColor()
    }


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTxtTypeface()
        setTxtColor()

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttrs: Int) : super(
        context,
        attrs,
        defStyleAttrs
    ) {
        setTxtTypeface()
        setTxtColor()

    }

    private fun setTxtTypeface() {
        var myApplication = context.applicationContext as MyApplication
        typeface = myApplication.initializeTypeFace()
    }

    private fun setTxtColor() {
        setTextColor(ContextCompat.getColor(context , R.color.light_orange))
    }

}