package com.dust.extracker.application

import android.app.Application
import android.graphics.Typeface
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.Locale

class MyApplication:Application() {
    override fun onCreate() {
        initializeTypeFace()
        setUpRealmDataBase()
        super.onCreate()
    }

    private fun setUpRealmDataBase() {
        Realm.init(this)
        var realmConfiguration = RealmConfiguration.Builder()
            .name("MainDB")
            .schemaVersion(1)
            .allowWritesOnUiThread(true)
            .build()
        Realm.setDefaultConfiguration(realmConfiguration)
    }

    fun initializeTypeFace():Typeface {
        return Typeface.createFromAsset(assets , "fonts/iraniansans.ttf")
    }
}