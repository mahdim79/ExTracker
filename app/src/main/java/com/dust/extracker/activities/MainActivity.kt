package com.dust.extracker.activities

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.android.volley.toolbox.Volley
import com.dust.extracker.R
import com.dust.extracker.adapters.viewpagersadapters.MainViewPagerAdapter
import com.dust.extracker.customviews.CViewPager
import com.dust.extracker.services.NotificationService
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class
MainActivity : AppCompatActivity() {
    private lateinit var onPageChange: OnPageChange
    private var lastFragment: Int = 2
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewPager: CViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        settheme()
        setLanguage()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpViews()
        setUpViewPager()
        setUpBottomNavigationView()
        checkNotificationService()
    }

    private fun setLanguage() {
        var localeStr = "fa"
        localeStr = if (SharedPreferencesCenter(this).getEnglishLanguage())
            "en"
        else
            "fa"
        val locale = Locale(localeStr)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun checkNotificationService() {
        if (!checkServiceRunning())
            startService(Intent(this, NotificationService::class.java))
    }


    fun checkServiceRunning(): Boolean {
        val activityManager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getRunningServices(Integer.MAX_VALUE).forEach {
            if (it.service.className == NotificationService::class.java.name)
                return true
        }
        return false
    }

    private fun settheme() {
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
                finishAffinity()
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

                finishAffinity()
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

                finishAffinity()

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

                finishAffinity()
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

                finishAffinity()
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
        registerReceiver(onPageChange, IntentFilter("com.dust.extracker.OnPageChange"))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(onPageChange)
    }


}
