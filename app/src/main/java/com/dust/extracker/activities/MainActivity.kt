package com.dust.extracker.activities

import android.Manifest
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.dust.extracker.R
import com.dust.extracker.adapters.viewpagersadapters.MainViewPagerAdapter
import com.dust.extracker.customviews.CViewPager
import com.dust.extracker.receivers.NotificationServiceStarter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var onPageChange: OnPageChange
    private var lastFragment: Int = 2
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewPager: CViewPager

    private var lastCloseAttemptTime:Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        super.onCreate(savedInstanceState)
        adjustFontScale()
        setContentView(R.layout.activity_main)
        setUpViews()
        setUpViewPager()
        setUpBottomNavigationView()
        startNotificationJobService()
    }

    private fun adjustFontScale() {
        val configuration = resources.configuration
        if (configuration.fontScale != 0.82f) {
            configuration.fontScale = 0.82f
            val metrics = resources.displayMetrics
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(metrics)
            metrics.scaledDensity = configuration.fontScale * metrics.density
            baseContext.resources.updateConfiguration(configuration, metrics)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val localeStr = if (SharedPreferencesCenter(newBase!!).getEnglishLanguage())
            "en"
        else
            "fa"

        val newLocale = Locale(localeStr)
        Locale.setDefault(newLocale)
        val config = newBase.resources?.configuration
        var myContext = newBase
        config?.let {
            it.setLayoutDirection(Locale(localeStr))
            it.setLocale(newLocale)
            myContext = newBase.createConfigurationContext(it) ?: newBase
        }
        super.attachBaseContext(myContext)
    }

    private fun startNotificationJobService() {
        sendBroadcast(Intent(this, NotificationServiceStarter::class.java))
    }

    private fun setTheme() {
        if (SharedPreferencesCenter(this).getNightMode()) {
            setTheme(R.style.Theme_ExTracker_dark)
            window.statusBarColor = Color.BLACK
        } else {
            setTheme(R.style.Theme_ExTracker_light)
            window.statusBarColor = ContextCompat.getColor(this, R.color.white_varient)
        }
    }

    private fun setUpBottomNavigationView() {

        bottomNavigationView.selectedItemId = R.id.marketWatch
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.marketWatch -> {
                    viewPager.currentItem = 2
                    if (lastFragment == 2) {
                        try {

                                supportFragmentManager.popBackStack(
                                    "TradingViewChartFragment",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )


                                supportFragmentManager.popBackStack(
                                    "CryptoDetailsFragment_MAIN",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )


                                supportFragmentManager.popBackStack(
                                    "NotificationCustomizeFragment",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )


                                supportFragmentManager.popBackStack(
                                    "NotificationChooseCryptoFragment",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )


                        } catch (e: Exception) {
                        }
                    }
                    lastFragment = 2
                }
                R.id.portfolio -> {
                    viewPager.currentItem = 1
                    if (lastFragment == 1) {
                        try {
                            supportFragmentManager.popBackStack(
                                "InputDataFragment",
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                            supportFragmentManager.popBackStack(
                                "SelectCtyptoFragment",
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                        } catch (e: Exception) {
                        }
                    }
                    lastFragment = 1
                }
                R.id.exchanger -> {
                    viewPager.currentItem = 0
                    if (lastFragment == 0) {
                        try {
                            supportFragmentManager.popBackStack(
                                "ExchnagerChooseCryptoFragment",
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                        } catch (e: Exception) {
                        }
                    }
                    lastFragment = 0

                }
                R.id.blog -> {
                    viewPager.currentItem = 3
                    if (lastFragment == 3) {
                        try {
                            supportFragmentManager.popBackStack(
                                "ReadNewsFragment",
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                            supportFragmentManager.popBackStack(
                                "BookMarksFragment",
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                            supportFragmentManager.popBackStack(
                                "fragment_search_news",
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                        } catch (e: Exception) {
                        }
                    }
                    lastFragment = 3

                }
                R.id.others -> {
                    viewPager.currentItem = 4
                    if (lastFragment == 4) {
                        try {
                            supportFragmentManager.popBackStack(
                                "SetPasswordFragment",
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )

                                supportFragmentManager.popBackStack(
                                    "NotificationCustomizeFragment",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )


                                supportFragmentManager.popBackStack(
                                    "NotificationChooseCryptoFragment",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )

                                supportFragmentManager.popBackStack(
                                    "NotificationFragment",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )

                                supportFragmentManager.popBackStack(
                                    "UserProfileFragment",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )


                                supportFragmentManager.popBackStack(
                                    "ChangePhoneNumberFragment",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )


                                supportFragmentManager.popBackStack(
                                    "UserProfileFragment",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )

                                supportFragmentManager.popBackStack(
                                    "UserLogInFragment",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )
                        } catch (e: Exception) {
                        }
                    }
                    lastFragment = 4

                }
            }
            true
        }
    }

    private fun setUpViewPager() {
        viewPager.adapter =
            MainViewPagerAdapter(
                supportFragmentManager
            )
        viewPager.offscreenPageLimit = 20
        viewPager.currentItem = 2
    }

    private fun setUpViews() {
        bottomNavigationView = findViewById(R.id.bottomnavigation_main)
        viewPager = findViewById(R.id.viewpager_main)
    }

    private fun testCloseApp(){

        if (System.currentTimeMillis() - lastCloseAttemptTime < 2000){
            finishAffinity()
        }else{
            lastCloseAttemptTime = System.currentTimeMillis()
            Toast.makeText(this,getString(R.string.testClose),Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {

        when (viewPager.currentItem) {
            0 -> {
                if (checkFragmentAvailability("ExchnagerChooseCryptoFragment")) {
                    supportFragmentManager.popBackStack(
                        "ExchnagerChooseCryptoFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }
                testCloseApp()
            }
            1 -> {
                if (checkFragmentAvailability("InputDataFragment")) {
                    supportFragmentManager.popBackStack(
                        "InputDataFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }
                if (checkFragmentAvailability("SelectCtyptoFragment")) {
                    supportFragmentManager.popBackStack(
                        "SelectCtyptoFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("EditHistoryNameFragment")) {
                    supportFragmentManager.popBackStack(
                        "EditHistoryNameFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("CryptoDetailsFragment")) {
                    supportFragmentManager.popBackStack(
                        "CryptoDetailsFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                testCloseApp()
            }
            2 -> {
                if (checkFragmentAvailability("TradingViewChartFragment")) {
                    supportFragmentManager.popBackStack(
                        "TradingViewChartFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("CryptoDetailsFragment_MAIN")) {
                    supportFragmentManager.popBackStack(
                        "CryptoDetailsFragment_MAIN",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("NotificationCustomizeFragment")) {
                    supportFragmentManager.popBackStack(
                        "NotificationCustomizeFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("NotificationChooseCryptoFragment")) {
                    supportFragmentManager.popBackStack(
                        "NotificationChooseCryptoFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("SearchFragment")) {
                    supportFragmentManager.popBackStack(
                        "SearchFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                testCloseApp()

            }
            3 -> {
                if (checkFragmentAvailability("ReadNewsFragment")) {
                    supportFragmentManager.popBackStack(
                        "ReadNewsFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }
                if (checkFragmentAvailability("fragment_search_news")) {
                    supportFragmentManager.popBackStack(
                        "fragment_search_news",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("BookMarksFragment")) {
                    supportFragmentManager.popBackStack(
                        "BookMarksFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                testCloseApp()
            }
            4 -> {

                if (checkFragmentAvailability("SetPasswordFragment")) {
                    supportFragmentManager.popBackStack(
                        "SetPasswordFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("NotificationCustomizeFragment")) {
                    supportFragmentManager.popBackStack(
                        "NotificationCustomizeFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("NotificationChooseCryptoFragment")) {
                    supportFragmentManager.popBackStack(
                        "NotificationChooseCryptoFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("NotificationFragment")) {
                    supportFragmentManager.popBackStack(
                        "NotificationFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }
                if (checkFragmentAvailability("UserProfileFragment")) {
                    supportFragmentManager.popBackStack(
                        "UserProfileFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("ChangePhoneNumberFragment")) {
                    supportFragmentManager.popBackStack(
                        "ChangePhoneNumberFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                if (checkFragmentAvailability("UserProfileFragment")) {
                    supportFragmentManager.popBackStack(
                        "UserProfileFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }
                if (checkFragmentAvailability("UserLogInFragment")) {
                    supportFragmentManager.popBackStack(
                        "UserLogInFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                testCloseApp()
            }
        }
    }

    private fun checkFragmentAvailability(name: String): Boolean {
        for (i in (supportFragmentManager.backStackEntryCount - 1).downTo(0))
            if (supportFragmentManager.getBackStackEntryAt(i).name == name)
                return true
        return false
    }

    inner class OnPageChange : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1!!.extras != null && p1.extras!!.containsKey("PAGE")) {
                when (p1.extras!!.getInt("PAGE", 1)) {
                    1 -> bottomNavigationView.selectedItemId = R.id.portfolio
                    2 -> bottomNavigationView.selectedItemId = R.id.marketWatch

                }
            }

        }
    }

    override fun onStart() {
        super.onStart()
        onPageChange = OnPageChange()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            registerReceiver(onPageChange, IntentFilter("com.dust.extracker.OnPageChange"),Context.RECEIVER_EXPORTED)
        }else{
            registerReceiver(onPageChange, IntentFilter("com.dust.extracker.OnPageChange"))
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(onPageChange)
    }



}
