package com.dust.extracker.fragments.othersfragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dust.extracker.R
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.UserDataClass
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnSmsSend
import com.dust.extracker.interfaces.OnUserDataReceived
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.google.android.material.snackbar.Snackbar
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

class UserLogInFragment : Fragment(), View.OnClickListener, OnGetAllCryptoList, OnSmsSend,
    OnUserDataReceived {

    private lateinit var phone_number: EditText
    private lateinit var verification_code: EditText
    private lateinit var submit_login: Button
    private lateinit var edit_number: Button
    private lateinit var timer_txt: TextView
    private lateinit var backImage: ImageView
    private lateinit var others_profilePhoto: CircleImageView
    private lateinit var coordinatorlayout: CoordinatorLayout
    private lateinit var phoneChangeLinear: LinearLayout
    private lateinit var apiCenter: ApiCenter
    private lateinit var dialog: Dialog
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var realm: RealmDataBaseCenter
    private lateinit var sharedPreferencesCenter: SharedPreferencesCenter
    private var VERIFICATION_CODE = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSharedPreferences()
        setUpRealmDb()
        setUpApiCenter()
        setUpViews(view)
        setUpBackImage()
    }

    private fun setUpRealmDb() {
        realm = RealmDataBaseCenter()
    }

    private fun setUpSharedPreferences() {
        sharedPreferencesCenter = SharedPreferencesCenter(requireActivity())
    }

    private fun setUpApiCenter() {
        apiCenter = ApiCenter(requireActivity(), this)
    }

    private fun setUpBackImage() {
        backImage.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack(
                "UserLogInFragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    private fun setUpViews(view: View) {
        backImage = view.findViewById(R.id.image_back)
        timer_txt = view.findViewById(R.id.timer_txt)
        edit_number = view.findViewById(R.id.edit_number)
        submit_login = view.findViewById(R.id.submit_login)
        verification_code = view.findViewById(R.id.verification_code)
        phone_number = view.findViewById(R.id.phone_number)
        coordinatorlayout = view.findViewById(R.id.coordinatorlayout)
        phoneChangeLinear = view.findViewById(R.id.phoneChangeLinear)
        others_profilePhoto = view.findViewById(R.id.others_profilePhoto)

        if (sharedPreferencesCenter.getNightMode())
            others_profilePhoto.setImageResource(R.drawable.ic_launcher_black)

        timer_txt.setOnClickListener(this)
        edit_number.setOnClickListener(this)
        submit_login.setOnClickListener(this)
        verification_code.setOnClickListener(this)
        phone_number.setOnClickListener(this)
    }

    private fun startSendCode(){
        timer_txt.isEnabled = false
        phone_number.isEnabled = false
        phoneChangeLinear.visibility = View.VISIBLE
        submit_login.text = requireActivity().resources.getString(R.string.confirm)
        verification_code.visibility = View.VISIBLE
        VERIFICATION_CODE = generateRandomVerificationCode()
        apiCenter.sendVerificationCodeByMessage(
            VERIFICATION_CODE,
            phone_number.text.toString().toDouble(),
            this
        )
        countDownTimer = object : CountDownTimer(120000, 1000) {
            override fun onFinish() {
                timer_txt.text = requireActivity().resources.getString(R.string.sendAgain)
                timer_txt.isEnabled = true
            }

            override fun onTick(p0: Long) {
                timer_txt.text = calculateCountDownTimer(p0)
            }

        }
        countDownTimer.start()
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.timer_txt -> {
                startSendCode()
            }
            R.id.edit_number -> {
                phone_number.isEnabled = true
                countDownTimer.cancel()
                timer_txt.text = requireActivity().resources.getString(R.string.sendAgain)
                timer_txt.isEnabled = true

            }
            R.id.submit_login -> {
                if (verification_code.text.toString() == "") {
                    if (checkValidNumber(phone_number.text.toString())) {
                        startSendCode()
                    } else {
                        showErrorSnackBar(requireActivity().resources.getString(R.string.invalidNumber), coordinatorlayout)
                    }
                } else {

                    if (VERIFICATION_CODE == verification_code.text.toString().toInt()) {
                        VERIFICATION_CODE = 0
                        logInUser(phone_number.text.toString().toDouble())

                    } else {
                        showErrorSnackBar(requireActivity().resources.getString(R.string.invalidCode), coordinatorlayout)
                    }
                }
            }
            R.id.verification_code -> {
            }
            R.id.phone_number -> {
            }
        }
    }

    private fun calculateCountDownTimer(millis: Long): String {
        var second = millis / 1000
        val minute = second / 60
        second %= 60
        return String.format("%02d" , minute) +":"+ String.format("%02d" , second)
    }

    private fun logInUser(phoneNumber: Double) {
        dialog = Dialog(requireActivity())
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_login_wait)
        dialog.show()
        apiCenter.getUserData(phoneNumber, this)
        sharedPreferencesCenter.setUserLogIn(true)

    }

    override fun onReceive(data: UserDataClass) {
        realm.insertUserData(data)
        val intent = Intent("com.dust.extracker.USER_DATA")
        intent.putExtra("name", data.name)
        intent.putExtra("avatarUrl", data.avatarUrl)
        requireActivity().sendBroadcast(intent)
        dialog.dismiss()
        requireActivity().supportFragmentManager.popBackStack(
            "UserLogInFragment",
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    override fun onReceiveFailure() {
        dialog.dismiss()
        showErrorSnackBar( requireActivity().resources.getString(R.string.errorLog), coordinatorlayout)
    }

    private fun generateRandomVerificationCode(): Int =
        "${(Math.random() * 10).roundToInt()}${(Math.random() * 10).roundToInt()}${(Math.random() * 10).roundToInt()}${(Math.random() * 10).roundToInt()}${(Math.random() * 10).roundToInt()}${(Math.random() * 10).roundToInt()}".toInt()

    private fun checkValidNumber(number: String): Boolean =
        number.startsWith("09") && number.length == 11

    override fun onGet(cryptoList: List<CryptoMainData>) {}

    override fun onGetByName(price: Double, dataNum: Int) {}
    override fun onSuccess() {
        showSnackBar(requireActivity().resources.getString(R.string.sendingSuccess), coordinatorlayout)
    }

    override fun onFailure() {
        showErrorSnackBar(requireActivity().resources.getString(R.string.errorOrder), coordinatorlayout)
    }

    private fun showErrorSnackBar(message: String, view: View) {
        val snackBar: Snackbar = Snackbar.make(
            view,
            message,
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)

        snackBar.view.setBackgroundColor(Color.BLACK)
        snackBar.show()
    }

    private fun showSnackBar(message: String, view: View) {
        val snackBar: Snackbar = Snackbar.make(
            view,
            message,
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)

        snackBar.view.setBackgroundColor(Color.BLACK)
        snackBar.show()

    }


}