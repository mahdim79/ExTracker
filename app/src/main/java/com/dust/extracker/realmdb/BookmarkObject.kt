package com.dust.extracker.realmdb

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class BookmarkObject : RealmObject() {
    @PrimaryKey
    var id: Int? = null

    var ID: Int? = null
    lateinit var title: String
    lateinit var description: String
    var likeCount: Int? = null
    var seenCount: Int? = null
    lateinit var imageUrl: String
    lateinit var is_liked: String
    var date: Int? = null
    lateinit var url: String
    lateinit var categories: String
    lateinit var tags: String
}