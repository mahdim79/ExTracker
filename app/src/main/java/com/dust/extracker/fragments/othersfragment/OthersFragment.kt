package com.dust.extracker.fragments.othersfragment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.BuildConfig
import com.dust.extracker.R
import com.dust.extracker.activities.SplashActivity
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.application.MyApplication
import com.dust.extracker.customviews.CButton
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.UserDataClass
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.dust.extracker.utils.Constants
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.util.UUID


class OthersFragment : Fragment(), View.OnClickListener, OnGetAllCryptoList {

    private lateinit var others_profilePhoto: CircleImageView


    private lateinit var others_name: CTextView
    private lateinit var userLogInTxt: CTextView

    private lateinit var others_edit: CButton

    private lateinit var others_notificationSwitcher: SwitchCompat
    private lateinit var others_nightModeSwitcher: SwitchCompat
    private lateinit var others_languageSwitcher: SwitchCompat
    private lateinit var others_fingerPrintSwitcher: SwitchCompat

    private lateinit var others_instagram: ImageView
    private lateinit var others_telegram: ImageView
    private lateinit var others_aparat: ImageView
    private lateinit var others_facebook: ImageView
    private val realmDb = RealmDataBaseCenter()

    private lateinit var others_profile_edit: LinearLayout
    private lateinit var others_nightMode: LinearLayout
    private lateinit var others_changeLanguage: LinearLayout
    private lateinit var others_faq: LinearLayout
    private lateinit var others_firstUser: LinearLayout
    private lateinit var others_problemReport: LinearLayout
    private lateinit var others_informationAndResources: LinearLayout
    private lateinit var others_logOut: LinearLayout
    private lateinit var sharedPreferencesCenter: SharedPreferencesCenter
    private lateinit var apiCenter: ApiCenter

    private var getContent: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            val dest_uri = StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()
            result?.let {
                val ucropOption = UCrop.Options()
                ucropOption.withMaxResultSize(860, 860)
                val u = UCrop.of(it, Uri.fromFile(File(requireActivity().cacheDir, dest_uri)))
                    .withAspectRatio(1f, 1f)
                    .withOptions(ucropOption)
                resultImageCrop.launch(u.getIntent(requireContext()))
            }
        }

    private var resultImageCrop =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.let { UCrop.getOutput(it) }
                uri?.let {
                    val imageUri = it.toString()
                    setUserAvatar(imageUri)
                    val currentUserData = realmDb.getUserData()
                    realmDb.updateUserData(UserDataClass(currentUserData?.id ?: 0,currentUserData?.userName ?: "",currentUserData?.phoneNumber ?: 0.0,currentUserData?.Email ?: "",imageUri,currentUserData?.name ?: ""))
                }
            }
        }


private lateinit var onUserLogIn: OnUserLogIn

    // Consts .......................................................


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_others, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSharedPrefCenter()
        setUpApiCenter()
        setUpViews(view)
        setUpViewTypes()
        setUpClickListeners()
    }

    private fun setUpApiCenter() {
        apiCenter = ApiCenter(requireActivity(), this)
    }

    private fun setUpSharedPrefCenter() {
        sharedPreferencesCenter = SharedPreferencesCenter(requireActivity())
    }


    private fun setUpViewTypes() {
        if (sharedPreferencesCenter.checkUserLogIn()) {
            sharedPreferencesCenter.setUserLogIn(true)
            others_logOut.visibility = View.VISIBLE
            userLogInTxt.text = requireActivity().resources.getString(R.string.userProfile)
            others_name.text = sharedPreferencesCenter.getUserName()
        }
        setUpUserAvatarImageView()

    }

    private fun setUpUserAvatarImageView() {
        realmDb.getUserData()?.avatarUrl?.let { uriStr ->
            if (uriStr.isNotEmpty()){
                setUserAvatar(uriStr)
            }
        }
    }

    private fun setUserAvatar(uriStr:String){
        try {
            val uri = uriStr.toUri()
            (requireActivity().application as MyApplication).grantUriPermission(uri)
            others_profilePhoto.setImageURI(uri)
        }catch (e:Exception){}
    }

    private fun setUpClickListeners() {
        others_problemReport.setOnClickListener(this)
        others_informationAndResources.setOnClickListener(this)
        others_logOut.setOnClickListener(this)
        others_aparat.setOnClickListener(this)
        others_facebook.setOnClickListener(this)
        others_telegram.setOnClickListener(this)
        others_instagram.setOnClickListener(this)
        others_edit.setOnClickListener(this)
        others_name.setOnClickListener(this)
        others_profilePhoto.setOnClickListener(this)
        others_profile_edit.setOnClickListener(this)
        others_nightMode.setOnClickListener(this)
        others_changeLanguage.setOnClickListener(this)
        others_faq.setOnClickListener(this)
        others_firstUser.setOnClickListener(this)
        userLogInTxt.setOnClickListener(this)

        others_notificationSwitcher.setOnCheckedChangeListener { _, b ->
            sharedPreferencesCenter.setNotificationEnabled(b)
            if (b)
                others_edit.visibility = View.VISIBLE
            else
                others_edit.visibility = View.GONE

            val intent = Intent("com.dust.extracker.Update_NotificationData")
            intent.putExtra("EnabledNotifications", "")
            requireActivity().sendBroadcast(intent)

        }
        others_nightModeSwitcher.setOnCheckedChangeListener { _, b ->
            sharedPreferencesCenter.setNightMode(b)
            restartApp()
        }
        others_languageSwitcher.setOnCheckedChangeListener { _, b ->
            sharedPreferencesCenter.setEnglishLanguage(b)
            restartApp()
        }
    }

    fun restartApp() {
        try {
            requireActivity().finishAffinity()
        } catch (e: Exception) {

        } finally {
            requireActivity().startActivity(Intent(requireActivity(), SplashActivity::class.java))
        }
    }

    private fun startLogInFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.others_frame_holder, UserLogInFragment())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("UserLogInFragment")
            .commit()
    }

    private fun startProfileFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.others_frame_holder, UserProfileFragment())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("UserProfileFragment")
            .commit()
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.userLogInTxt -> {
                others_profile_edit.performClick()
            }
            R.id.others_profile_edit -> {
                if (sharedPreferencesCenter.checkUserLogIn()) {
                    startProfileFragment()
                } else {
                    startLogInFragment()
                }
            }
            R.id.others_edit -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.others_frame_holder, NotificationFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("NotificationFragment")
                    .commit()
            }
            R.id.others_profilePhoto -> {
//                try {
//                    getContent.launch("image/*")
//                }catch (e:Exception){
//                    Log.i("setUpUserAvatarImageView",e.message.toString())
//                }
            }
            R.id.others_instagram -> {
                openInstagramAccount()
            }
            R.id.others_telegram -> {
                openTelegramAccount()
            }
            R.id.others_facebook -> {
            }
            R.id.others_aparat -> {
            }
            R.id.others_logOut -> {
            }
            R.id.others_faq -> {
                openUrl("https://www.google.com")
            }
            R.id.others_firstUser -> {
                openUrl("https://www.google.com")
            }
            R.id.others_informationAndResources -> {
                openUrl("https://www.google.com")
            }
            R.id.others_problemReport -> {
                openUrl("https://www.google.com")
            }
        }
    }

    private fun openTelegramAccount(){
        val intent = Intent(Intent.ACTION_VIEW, "tg://resolve?domain=${Constants.TELEGRAM_USER_NAME}".toUri())
        startActivity(intent)
    }

    private fun openInstagramAccount(){
        val instagramUserName = Constants.INSTAGRAM_USER_NAME
        val uri = "http://instagram.com/_u/$instagramUserName".toUri()
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "http://instagram.com/$instagramUserName".toUri()
                )
            )
        }
    }

    private fun setUpViews(view: View) {
        others_profile_edit = view.findViewById(R.id.others_profile_edit)
        others_languageSwitcher = view.findViewById(R.id.others_languageSwitcher)
        others_fingerPrintSwitcher = view.findViewById(R.id.others_fingerPrintSwitcher)
        others_nightMode = view.findViewById(R.id.others_nightMode)
        others_changeLanguage = view.findViewById(R.id.others_changeLanguage)
        others_faq = view.findViewById(R.id.others_faq)
        others_firstUser = view.findViewById(R.id.others_firstUser)
        others_problemReport = view.findViewById(R.id.others_problemReport)
        others_informationAndResources = view.findViewById(R.id.others_informationAndResources)
        others_logOut = view.findViewById(R.id.others_logOut)
        others_facebook = view.findViewById(R.id.others_facebook)
        others_aparat = view.findViewById(R.id.others_aparat)
        others_telegram = view.findViewById(R.id.others_telegram)
        others_instagram = view.findViewById(R.id.others_instagram)
        others_nightModeSwitcher = view.findViewById(R.id.others_nightModeSwitcher)
        others_notificationSwitcher = view.findViewById(R.id.others_notificationSwitcher)
        others_edit = view.findViewById(R.id.others_edit)
        others_name = view.findViewById(R.id.others_name)
        others_profilePhoto = view.findViewById(R.id.others_profilePhoto)
        userLogInTxt = view.findViewById(R.id.userLogInTxt)

        others_name.text =
            requireActivity().resources.getString(R.string.version, BuildConfig.VERSION_NAME)

        others_notificationSwitcher.isChecked = sharedPreferencesCenter.getNotificationEnabled()
        others_nightModeSwitcher.isChecked = sharedPreferencesCenter.getNightMode()
        others_languageSwitcher.isChecked = sharedPreferencesCenter.getEnglishLanguage()

        if (SharedPreferencesCenter(requireActivity()).getNightMode())
            others_edit.setTextColor(Color.WHITE)
        else
            others_edit.setTextColor(Color.BLACK)

        if (others_notificationSwitcher.isChecked)
            others_edit.visibility = View.VISIBLE
        else
            others_edit.visibility = View.GONE

        setUpFingerPrint()


    }

    private fun setUpFingerPrint() {
        others_fingerPrintSwitcher.isChecked = sharedPreferencesCenter.getFingerPrintEnabled()
        others_fingerPrintSwitcher.setOnClickListener {
            others_fingerPrintSwitcher.isChecked = !others_fingerPrintSwitcher.isChecked

            val executor = ContextCompat.getMainExecutor(requireActivity())
            val prompt =
                BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        if (sharedPreferencesCenter.getFingerPrintEnabled()) {
                            sharedPreferencesCenter.setFingerPrintEnabled(false)
                            others_fingerPrintSwitcher.isChecked = false
                        } else {
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.others_frame_holder, SetPasswordFragment())
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .addToBackStack("SetPasswordFragment")
                                .commit()
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                })

            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(" ")
                .setSubtitle("")
                .setNegativeButtonText("Cancel")
                .build()
            prompt.authenticate(info)
        }
    }

    fun newInstance(): OthersFragment {
        val args = Bundle()
        val fragment =
            OthersFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().finishAffinity()
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        requireActivity().startActivity(
            Intent.createChooser(
                intent,
                requireActivity().resources.getString(R.string.openUrlWith)
            )
        )
    }

    inner class OnUserLogIn : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            others_logOut.visibility = View.VISIBLE
            userLogInTxt.text = requireActivity().resources.getString(R.string.userProfile)
            val bundle = p1!!.extras!!
            if (!bundle.isEmpty && bundle.containsKey("name") && bundle.containsKey("avatarUrl")) {
                others_name.text = bundle.getString("name")
                val avatarUrl = bundle.getString("avatarUrl")
                if (avatarUrl != "")
                    Picasso.get().load(avatarUrl).into(others_profilePhoto)
            }
        }
    }

    override fun onStart() {
        onUserLogIn = OnUserLogIn()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requireActivity().registerReceiver(onUserLogIn, IntentFilter("com.dust.extracker.USER_DATA"),Context.RECEIVER_EXPORTED)
        }else{
            requireActivity().registerReceiver(onUserLogIn, IntentFilter("com.dust.extracker.USER_DATA"))
        }
        super.onStart()
        try {
            others_fingerPrintSwitcher.isChecked = sharedPreferencesCenter.getFingerPrintEnabled()
        } catch (e: Exception) {
        }
    }

    override fun onStop() {
        requireActivity().unregisterReceiver(onUserLogIn)
        super.onStop()
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {}

    override fun onGetByName(price: Double, dataNum: Int) {}
}