package com.dust.extracker.fragments.othersfragment

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dust.extracker.R
import com.dust.extracker.activities.MainActivity
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.customviews.CButton
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.NotificationDataClass
import com.dust.extracker.dataclasses.PriceDataClass
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.squareup.picasso.Picasso
import java.util.*

class NotificationCustomizeFragment : Fragment(), OnGetAllCryptoList {
    private lateinit var image_back: ImageView
    private lateinit var cryptoImage: ImageView
    private lateinit var cryptoName: CTextView
    private lateinit var lowerThanButton: CButton
    private lateinit var HigherThanButton: CButton
    private lateinit var notif_save: Button
    private lateinit var timeingNotificationSpinner: Spinner
    private lateinit var resultOne_edittext: EditText
    private lateinit var pbar: ProgressBar

    private lateinit var data: MainRealmObject
    private lateinit var realmDB: RealmDataBaseCenter
    private lateinit var apiService: ApiCenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification_customize, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpApiService()
        setUpRealmDB()
        setUpBackImage()
        setUpSpinner()
        setData()
    }

    private fun setUpApiService() {
        apiService = ApiCenter(requireActivity(), this)
    }

    private fun setUpSpinner() {
        val data = listOf(requireActivity().resources.getString(R.string.once), requireActivity().resources.getString(R.string.always))

        timeingNotificationSpinner.adapter = ArrayAdapter<String>(
            requireActivity(),
            R.layout.custom_spinner_item,
            R.id.textview1,
            data
        )
        timeingNotificationSpinner.setSelection(0)

    }

    private fun setUpRealmDB() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setData() {
        // val pData = realmDB.getCryptoDataByName(requireArguments().getString("CoinName", "BTC"))
        doUpdateAndGetData()
    }

    private fun doUpdateAndGetData() {
        apiService.getCryptoPriceByName(requireArguments().getString("CoinName", "BTC"), 0)
    }

    private fun setUpBackImage() {
        image_back.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack(
                "NotificationCustomizeFragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    private fun setUpViews(view: View) {
        image_back = view.findViewById(R.id.image_back)
        pbar = view.findViewById(R.id.pbar)
        notif_save = view.findViewById(R.id.notif_save)
        resultOne_edittext = view.findViewById(R.id.resultOne_edittext)
        cryptoImage = view.findViewById(R.id.cryptoImage)
        cryptoName = view.findViewById(R.id.cryptoName)
        timeingNotificationSpinner = view.findViewById(R.id.timeingNotificationSpinner)
        HigherThanButton = view.findViewById(R.id.HigherThanButton)
        lowerThanButton = view.findViewById(R.id.lowerThanButton)
        val color = if (SharedPreferencesCenter(requireActivity()).getNightMode())
            Color.WHITE
        else
            Color.BLACK

        lowerThanButton.setTextColor(color)
        HigherThanButton.setTextColor(color)

    }

    fun newInstance(CoinName: String): NotificationCustomizeFragment {
        val args = Bundle()
        args.putString("CoinName", CoinName)
        val fragment = NotificationCustomizeFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {}

    override fun onGetByName(price: Double, dataNum: Int) {
        realmDB.updatePrice(PriceDataClass(price, requireArguments().getString("CoinName", "BTC")))
        startAction()
    }

    private fun startAction() {
        notif_save.isEnabled = true
        pbar.visibility = View.GONE
        data = realmDB.getCryptoDataByName(requireArguments().getString("CoinName", "BTC"))
        Picasso.get().load("${data.BaseImageUrl}${data.ImageUrl}").into(cryptoImage)
        cryptoName.text = data.Name
        resultOne_edittext.setText(
            String.format(
                Locale.ENGLISH,
                "%.3f",
                (data.LastPrice!! + (data.LastPrice!! * 0.05))
            )
        )
        resultOne_edittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                try {
                    if (resultOne_edittext.text.toString().toDouble() >= data.LastPrice!!) {
                        HigherThanButton.isEnabled = false
                        lowerThanButton.isEnabled = true
                    } else {
                        HigherThanButton.isEnabled = true
                        lowerThanButton.isEnabled = false
                    }
                } catch (e: Exception) {
                    lowerThanButton.isEnabled = true
                    HigherThanButton.isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })
        notif_save.setOnClickListener {
            if (!lowerThanButton.isEnabled || !HigherThanButton.isEnabled) {
                SharedPreferencesCenter(requireActivity()).setNotificationData(
                    NotificationDataClass(
                        0,
                        data.Name!!,
                        data.LastPrice!!,
                        resultOne_edittext.text.toString().toDouble(),
                        timeingNotificationSpinner.selectedItemPosition
                    )
                )
            }

            requireActivity().supportFragmentManager.popBackStack(
                "NotificationChooseCryptoFragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }
}