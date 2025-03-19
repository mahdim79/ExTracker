package com.dust.extracker.application

import android.app.Application
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
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

    fun grantUriPermission(uri: Uri){
        grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}