package com.dust.extracker.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.dust.extracker.application.MyApplication


class CTextView : AppCompatTextView {

    constructor(context: Context) : super(context) {
        setTxtTypeface()
    }
    constructor(context: Context,attrs:AttributeSet) : super(context,attrs) {
        setTxtTypeface()
    }
    constructor(context: Context,attrs:AttributeSet,defStyleAttrs:Int) : super(context,attrs,defStyleAttrs) {
        setTxtTypeface()
    }

    private fun setTxtTypeface() {
        var myApplication = context.applicationContext as MyApplication
        typeface = myApplication.initializeTypeFace()
    }

}