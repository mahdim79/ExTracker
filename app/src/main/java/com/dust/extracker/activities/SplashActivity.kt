package com.dust.extracker.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.dust.extracker.R
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class SplashActivity : AppCompatActivity() {
    private var FailCount = 0
    lateinit var imageView: CircleImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        settheme()
        setLanguage()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setUpViews()
        setUpAnimations()
        setUpHandler()
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

    private fun setLanguage() {
        var localeStr = ""
        if (SharedPreferencesCenter(this).getEnglishLanguage())
            localeStr = "en"
        else
            localeStr = "fa"
        val locale = Locale(localeStr)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
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


        }, 1500)
    }

    private fun setUpAnimations() {
        val alphaAnimation = AlphaAnimation(0f, 1.0f)
        alphaAnimation.fillAfter = true
        alphaAnimation.duration = 1000
        imageView.startAnimation(alphaAnimation)
    }

    private fun setUpViews() {
        imageView = findViewById(R.id.img_splash)
    }
}