package com.dust.extracker.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.dust.extracker.R
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class SplashActivity : AppCompatActivity() {
    private var FailCount = 0
    lateinit var tv_splash_title: TextView
    lateinit var tv_splash_slogan: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        settheme()
        super.onCreate(savedInstanceState)
        adjustFontScale()
        setContentView(R.layout.activity_splash)
        setUpViews()
        setUpAnimations()
        setUpHandler()
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

    private fun setUpBiometricAuthentication() {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    startActivity(Intent(this@SplashActivity , PasswordActivity::class.java))
                    finish()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    lunchMainActivity()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    if(FailCount == 3){
                        startActivity(Intent(this@SplashActivity , PasswordActivity::class.java))
                        finish()
                    }
                    FailCount++
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(" ")
            .setSubtitle("")
            .setNegativeButtonText("Cancel")
            .build()
        prompt.authenticate(promptInfo)
    }

    private fun lunchMainActivity() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
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

    private fun setUpHandler() {
        Handler().postDelayed(Runnable {

            if (SharedPreferencesCenter(this).getFingerPrintEnabled())
                setUpBiometricAuthentication()
            else
                lunchMainActivity()


        }, 500)
    }

    private fun setUpAnimations() {
        val alphaAnimation = AlphaAnimation(0f, 1.0f)
        alphaAnimation.fillAfter = true
        alphaAnimation.duration = 500
        tv_splash_title.startAnimation(alphaAnimation)
        tv_splash_slogan.startAnimation(alphaAnimation)
    }

    private fun setUpViews() {
        tv_splash_title = findViewById(R.id.tv_splash_title)
        tv_splash_slogan = findViewById(R.id.tv_splash_slogan)
    }
}