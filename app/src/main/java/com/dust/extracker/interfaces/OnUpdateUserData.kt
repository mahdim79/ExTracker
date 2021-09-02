package com.dust.extracker.interfaces

import com.dust.extracker.dataclasses.UserDataClass

interface OnUpdateUserData {
    fun onUpdateUserData(userData:UserDataClass)
    fun onFailure()
}