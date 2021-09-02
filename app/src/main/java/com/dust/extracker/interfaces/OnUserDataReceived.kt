package com.dust.extracker.interfaces

import com.dust.extracker.dataclasses.UserDataClass

interface OnUserDataReceived {
    fun onReceive(data:UserDataClass)
    fun onReceiveFailure()
}