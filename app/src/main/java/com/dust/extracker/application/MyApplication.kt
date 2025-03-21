package com.dust.extracker.application

import android.app.Application
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import com.dust.extracker.utils.Constants
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import io.realm.Realm
import io.realm.RealmConfiguration


class MyApplication:Application() {
    override fun onCreate() {
        initializeTypeFace()
        setUpRealmDataBase()
        super.onCreate()
        initAppCenter()
    }

    private fun initAppCenter() {
        AppCenter.start(
            this, Constants.APP_CENTER_DSN,
            Analytics::class.java, Crashes::class.java
        )
    }

    private fun setUpRealmDataBase() {
        Realm.init(this)
        val realmConfiguration = RealmConfiguration.Builder()
            .name("MainDB")
            .schemaVersion(1)
            .allowWritesOnUiThread(true)
            .build()
        Realm.setDefaultConfiguration(realmConfiguration)
    }

    fun initializeTypeFace():Typeface {
        return Typeface.createFromAsset(assets , "fonts/iraniansans.ttf")
    }

    fun grantUriPermission(uri: Uri){
        grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}