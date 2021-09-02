package com.dust.extracker.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.dust.extracker.R
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar


class PasswordActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var preferences: SharedPreferencesCenter
    private lateinit var edittextpassword: EditText
    private lateinit var coordinator: CoordinatorLayout
    private lateinit var vibrator: Vibrator
    private lateinit var ActualPassword: String
    private lateinit var txtcon: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        settheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)
        setUpShared()
        coordinator = findViewById(R.id.coordinator)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        ActualPassword = preferences.getPassword().toString()
        setupview()
    }

    private fun setUpShared() {
        preferences = SharedPreferencesCenter(this)
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

    private fun setupview() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar);
        setSupportActionBar(toolbar)
        val one = findViewById<CardView>(R.id.one)
        val two = findViewById<CardView>(R.id.two)
        val three = findViewById<CardView>(R.id.three)
        val four = findViewById<CardView>(R.id.four)
        val five = findViewById<CardView>(R.id.five)
        val six = findViewById<CardView>(R.id.six)
        val seven = findViewById<CardView>(R.id.seven)
        val eight = findViewById<CardView>(R.id.eight)
        val nine = findViewById<CardView>(R.id.nine)
        val zero = findViewById<CardView>(R.id.zero)
        val confirm = findViewById<CardView>(R.id.confirm)
        val backspace = findViewById<CardView>(R.id.backspace)

        one.setOnClickListener(this)
        two.setOnClickListener(this)
        three.setOnClickListener(this)
        four.setOnClickListener(this)
        five.setOnClickListener(this)
        six.setOnClickListener(this)
        seven.setOnClickListener(this)
        eight.setOnClickListener(this)
        nine.setOnClickListener(this)
        zero.setOnClickListener(this)
        confirm.setOnClickListener(this)
        backspace.setOnClickListener(this)

        edittextpassword = findViewById(R.id.edittextpassword)
        txtcon = findViewById(R.id.txtcon)

        edittextpassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (ActualPassword == edittextpassword.text.toString()) {
                    confirm.isEnabled = false
                    txtcon.setBackgroundColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.white_bottom_navigation,
                            null
                        )
                    )
                    startActivity(Intent(this@PasswordActivity, MainActivity::class.java))
                    finish()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.one -> {
                edittextpassword.setText("${edittextpassword.text}1")
            }
            R.id.two -> {
                edittextpassword.setText("${edittextpassword.text}2")
            }

            R.id.three -> {
                edittextpassword.setText("${edittextpassword.text}3")
            }

            R.id.four -> {
                edittextpassword.setText("${edittextpassword.text}4")
            }

            R.id.five -> {
                edittextpassword.setText("${edittextpassword.text}5")
            }

            R.id.six -> {
                edittextpassword.setText("${edittextpassword.text}6")
            }

            R.id.seven -> {
                edittextpassword.setText("${edittextpassword.text}7")
            }

            R.id.eight -> {
                edittextpassword.setText("${edittextpassword.text}8")
            }

            R.id.nine -> {
                edittextpassword.setText("${edittextpassword.text}9")
            }

            R.id.zero -> {
                edittextpassword.setText("${edittextpassword.text}0")
            }

            R.id.confirm -> {
                if (ActualPassword == edittextpassword.text.toString()) {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                    )
                    finish()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                100,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        vibrator.vibrate(100)
                    }
                    Snackbar.make(
                        coordinator,
                        resources.getString(R.string.wrongpin),
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                }
            }


            R.id.backspace -> {
                if (edittextpassword.text.toString() != "") {
                    edittextpassword.setText(
                        edittextpassword.text.toString()
                            .substring(0, edittextpassword.text.toString().length - 1)
                    )
                }
            }
        }
    }
}