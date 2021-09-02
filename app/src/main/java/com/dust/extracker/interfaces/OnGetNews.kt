package com.dust.extracker.interfaces

import com.dust.extracker.dataclasses.NewsDataClass

interface OnGetNews {
    fun onGetNews(list:List<NewsDataClass>)
}