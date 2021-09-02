package com.dust.extracker.dataclasses

data class NewsDataClass(
    var id: Int,
    var ID: Int,
    var title: String,
    var description: String,
    var likeCount: Int,
    var seenCount: Int,
    var imageUrl: String,
    var is_liked: Boolean,
    var date: Int,
    var url: String,
    var categories: String,
    var tags: String
) {
}