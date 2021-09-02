package com.dust.extracker.realmdb

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class UserObject : RealmObject() {
    @PrimaryKey
    var id: Int? = null

    var userName: String? = null
    var phoneNumber: Double? = null
    var Email: String? = null
    var avatarUrl: String? = null
    var name: String? = null
}