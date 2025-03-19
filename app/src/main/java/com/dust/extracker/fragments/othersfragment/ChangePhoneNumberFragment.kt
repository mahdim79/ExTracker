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
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.UserDataClass
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnSmsSend
import com.dust.extracker.interfaces.OnUpdateUserData
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt

class ChangePhoneNumberFragment:Fragment() , View.OnClickListener , OnGetAllCryptoList, OnSmsSend ,OnUpdateUserData{
    private lateinit var phone_number: EditText
    private lateinit var verification_code: EditText
    private lateinit var submit_login: Button
    private lateinit var edit_number: Button
    private lateinit var timer_txt: TextView
    private lateinit var backImage: ImageView
    private lateinit var coordinatorlayout: CoordinatorLayout
    private lateinit var phoneChangeLinear: LinearLayout
    private lateinit var apiCenter: ApiCenter
    private lateinit var dialog: Dialog
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var realm: RealmDataBaseCenter
    private lateinit var realmDB: RealmDataBaseCenter
    private lateinit var sharedPreferencesCenter: SharedPreferencesCenter

    private var VERIFICATION_CODE = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_changephonenumber , container , false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpApiService()
        setUpRealmDB()
        setUpViews(view)

    }

    private fun setUpApiService() {
        apiCenter = ApiCenter(requireActivity() , this)
    }
    private fun setUpRealmDB() {
        realmDB = RealmDataBaseCenter()
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

        backImage.setOnClickListener(this)
        timer_txt.setOnClickListener(this)
        edit_number.setOnClickListener(this)
        submit_login.setOnClickListener(this)
        verification_code.setOnClickListener(this)
        phone_number.setOnClickListener(this)
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
                        dialog = Dialog(requireActivity())
                        dialog.setCancelable(false)
                        dialog.setContentView(R.layout.dialog_login_wait)
                        dialog.show()
                        val obj = realm.getUserData()
                        apiCenter.updateUserData(UserDataClass(obj?.id!! , obj.userName!! , phone_number.text.toString().toDouble() , obj.Email!! , obj.avatarUrl!! , obj.name!!),
                            obj.phoneNumber!! , this)

                    } else {
                        showErrorSnackBar(requireActivity().resources.getString(R.string.invalidCode), coordinatorlayout)
                    }
                }
            }
            R.id.verification_code -> {
            }
            R.id.phone_number -> {
            }
            R.id.image_back ->{
                requireActivity().supportFragmentManager.popBackStack("ChangePhoneNumberFragment" , FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
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

    private fun checkValidNumber(number: String): Boolean =
        number.startsWith("09") && number.length == 11

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

    private fun generateRandomVerificationCode(): Int =
        "${(Math.random() * 10).roundToInt()}${(Math.random() * 10).roundToInt()}${(Math.random() * 10).roundToInt()}${(Math.random() * 10).roundToInt()}${(Math.random() * 10).roundToInt()}${(Math.random() * 10).roundToInt()}".toInt()

    private fun calculateCountDownTimer(millis: Long): String {
        var second = millis / 1000
        val minute = second / 60
        second %= 60
        return String.format("%02d" , minute) + String.format("%02d" , second)
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {}

    override fun onGetByName(price: Double, dataNum: Int) {}

    override fun onSuccess() {
        dialog.dismiss()
        showSnackBar(requireActivity().resources.getString(R.string.sendingSuccess), coordinatorlayout)
    }


    override fun onUpdateUserData(userData: UserDataClass) {
        realmDB.updateUserData(userData)
        dialog.dismiss()
        requireActivity().sendBroadcast(Intent("com.dust.extracker.UPDATE_PHONE_NUMBER"))
        showSnackBar(requireActivity().resources.getString(R.string.updateComplete) , coordinatorlayout)
        requireActivity().supportFragmentManager.popBackStack("ChangePhoneNumberFragment" , FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun onFailure() {
        dialog.dismiss()
        showErrorSnackBar(requireActivity().resources.getString(R.string.errorOrder), coordinatorlayout)
    }
}