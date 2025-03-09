package com.dust.extracker.fragments.othersfragment

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.R
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.UserDataClass
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnUpdateUserData
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.realmdb.UserObject
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserProfileFragment : Fragment() , OnGetAllCryptoList , OnUpdateUserData , View.OnClickListener{
    private lateinit var others_profilePhoto: CircleImageView
    private lateinit var name_text: EditText
    private lateinit var image_back: ImageView
    private lateinit var email_text: EditText
    private lateinit var phone_number: EditText
    private lateinit var change_phoneNumber: Button
    private lateinit var submit_save: Button
    private lateinit var apiCenter: ApiCenter
    private lateinit var dialog: Dialog
    private lateinit var coordinatorlayout:CoordinatorLayout
    private lateinit var onUpdatePhoneNumber:OnUpdatePhoneNumber
    private var IS_PHOTO_CHANGED = false

    private lateinit var realmDB: RealmDataBaseCenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpApiService()
        setUpRealmDB()
        setDefaultData()
        setUpButtons()
        setUpBackButton()
    }

    override fun onStart() {
        super.onStart()
        val onUpdatePhoneNumber = OnUpdatePhoneNumber()
        requireActivity().registerReceiver(onUpdatePhoneNumber , IntentFilter("com.dust.extracker.UPDATE_PHONE_NUMBER"))
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onUpdatePhoneNumber)
    }

    inner class OnUpdatePhoneNumber:BroadcastReceiver()
    {
        override fun onReceive(p0: Context?, p1: Intent?) {
            setDefaultData()
        }
    }

    private fun setUpBackButton() {
        image_back.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack("UserProfileFragment" , FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    private fun setUpApiService() {
        apiCenter = ApiCenter(requireActivity() , this)
    }

    private fun setUpButtons() {
        submit_save.setOnClickListener {
            val userObject = realmDB.getUserData()
            if (IS_PHOTO_CHANGED || name_text.text.toString() != userObject.name || email_text.text.toString() != userObject.Email){
                dialog = Dialog(requireActivity())
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.dialog_login_wait)
                val message = dialog.findViewById<CTextView>(R.id.dialog_message)
                message.text = requireActivity().resources.getString(R.string.sendingInformation)
                dialog.show()
                if (IS_PHOTO_CHANGED){
                    // TODO: 6/7/2021 upload new photo to server
                }
                val userData = UserDataClass(userObject.id!! , userObject.userName!! , phone_number.text.toString().toDouble() , email_text.text.toString() , "" , name_text.text.toString())
                apiCenter.updateUserData(userData , userObject.phoneNumber!!, this)
            }
        }
    }

    private fun setDefaultData() {
        val userObject = realmDB.getUserData()
        if (userObject.avatarUrl != "")
            Picasso.get().load(userObject.avatarUrl).into(others_profilePhoto)
        name_text.setText(userObject.name)
        email_text.setText(userObject.Email)
        phone_number.setText(userObject.phoneNumber.toString())
    }

    private fun setUpRealmDB() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setUpViews(view: View) {
        submit_save = view.findViewById(R.id.submit_save)
        change_phoneNumber = view.findViewById(R.id.change_phoneNumber)
        phone_number = view.findViewById(R.id.phone_number)
        email_text = view.findViewById(R.id.email_text)
        name_text = view.findViewById(R.id.name_text)
        others_profilePhoto = view.findViewById(R.id.others_profilePhoto)
        coordinatorlayout = view.findViewById(R.id.coordinatorlayout)
        image_back = view.findViewById(R.id.image_back)
        change_phoneNumber.setOnClickListener(this)
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {}

    override fun onGetByName(price: Double, dataNum: Int) {}


    private fun showSnackBar(message: String, view: View) {
        val snackBar: Snackbar = Snackbar.make(
            view,
            message,
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.BLACK)

        snackBar.view.setBackgroundColor(Color.BLUE)
        snackBar.show()

    }

    private fun showErrorSnackBar(message: String, view: View) {
        val snackBar: Snackbar = Snackbar.make(
            view,
            message,
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)

        snackBar.view.setBackgroundColor(Color.RED)
        snackBar.show()
    }

    override fun onUpdateUserData(userData: UserDataClass) {
        realmDB.updateUserData(userData)
        setDefaultData()
        dialog.dismiss()
        showSnackBar(requireActivity().resources.getString(R.string.updateInfoSucces) , coordinatorlayout)
    }

    override fun onFailure() {
        dialog.dismiss()
        showErrorSnackBar(requireActivity().resources.getString(R.string.loadingError)  , coordinatorlayout)
    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.change_phoneNumber ->{
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.others_frame_holder , ChangePhoneNumberFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("ChangePhoneNumberFragment")
                    .commit()
            }
        }
    }
}