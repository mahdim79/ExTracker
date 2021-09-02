package com.dust.extracker.fragments.othersfragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dust.extracker.R
import com.dust.extracker.activities.MainActivity
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class SetPasswordFragment : Fragment(), View.OnClickListener {

    private lateinit var preferences: SharedPreferencesCenter
    private lateinit var edittextpassword: EditText
    private lateinit var coordinator: CoordinatorLayout
    private lateinit var vibrator: Vibrator
    private lateinit var ActualPassword: String
    private lateinit var txtcon: TextView
    private lateinit var pass:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpShared()
        coordinator = view.findViewById(R.id.coordinator)
        vibrator = activity!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        setupview(view)
    }

    private fun setUpShared() {
        preferences = SharedPreferencesCenter(activity!!)
    }

    private fun setupview(view: View) {
        val one = view.findViewById<CardView>(R.id.one)
        val two = view.findViewById<CardView>(R.id.two)
        val three = view.findViewById<CardView>(R.id.three)
        val four = view.findViewById<CardView>(R.id.four)
        val five = view.findViewById<CardView>(R.id.five)
        val six = view.findViewById<CardView>(R.id.six)
        val seven = view.findViewById<CardView>(R.id.seven)
        val eight = view.findViewById<CardView>(R.id.eight)
        val nine = view.findViewById<CardView>(R.id.nine)
        val zero = view.findViewById<CardView>(R.id.zero)
        val confirm = view.findViewById<CardView>(R.id.confirm)
        val backspace = view.findViewById<CardView>(R.id.backspace)
        val txtMessage = view.findViewById<TextView>(R.id.txtMessage)
        val image_back = view.findViewById<ImageView>(R.id.image_back)
        txtcon = view.findViewById(R.id.txtcon)

        image_back.visibility = View.VISIBLE
        image_back.setOnClickListener {
            activity!!.supportFragmentManager.popBackStack("SetPasswordFragment" , FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        txtMessage.text = activity!!.resources.getString(R.string.enterPassword)

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
        confirm.isEnabled = false
        txtcon.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.white_bottom_navigation,
                null
            )
        )

        edittextpassword = view.findViewById(R.id.edittextpassword)


        edittextpassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (!confirm.isEnabled){
                    if (edittextpassword.text.toString().length == 7){
                        confirm.isEnabled = true
                        pass = edittextpassword.text.toString()
                        edittextpassword.setText("")
                        txtMessage.text = activity!!.resources.getString(R.string.enterPasswordAgain)
                        txtcon.setBackgroundColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.purple_500,
                                null
                            )
                        )
                    }
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
                if (pass == edittextpassword.text.toString()) {
                    preferences.setFingerPrintEnabled(true)
                    preferences.setPassword(pass.toInt())
                    activity!!.supportFragmentManager.popBackStack("SetPasswordFragment" , FragmentManager.POP_BACK_STACK_INCLUSIVE)
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