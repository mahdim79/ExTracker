package com.dust.extracker.fragments.exchangerfragments

import android.content.*
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.R
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.utils.Utils
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ExchangerFragment : Fragment(), View.OnClickListener, OnGetAllCryptoList {

    lateinit var first_crypto_linear: LinearLayout
    lateinit var last_crypto_linear: LinearLayout
    lateinit var first_linear_text: TextView
    lateinit var last_linear_text: TextView
    lateinit var change_linears: CircleImageView
    lateinit var first_img: ImageView
    lateinit var last_img: ImageView
    lateinit var realmDB: RealmDataBaseCenter
    lateinit var ex_nested: NestedScrollView
    lateinit var apiCenter: ApiCenter
    lateinit var dollar_text: CTextView
    lateinit var dollarPricetxt: CTextView
    lateinit var text_total_price: CTextView
    lateinit var resultOne_edittext: EditText
    lateinit var resultTwo_textview: CTextView
    lateinit var exchanger_progressBar: ProgressBar
    private lateinit var ondataRecieve: onDataRecieve
    private lateinit var ondollarpriceRecieve: onDollarPriceRecieve

    var price1: Double? = null
    var price2: Double? = null
    var CRYPTO_ONE = ""
    var CRYPTO_TWO = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exchanger, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var date = Calendar.getInstance().time
        Log.i("date", date.date.toString())

        setUpDataBase()
        setUpApiCenter()
        setUpViews(view)
        setUpEditText()
        setUpLinearLayouts()
        if (realmDB.checkDataAvailability())
            setUpFirstExchange()
        setDollarPrice()
    }

    private fun setDollarPrice() {

        if (realmDB.checkDollarPriceAvailability()) {
            realmDB.getDollarPrice()?.price?.toDouble()?.let { dollarPrice ->
                dollarPricetxt.text = Utils.formatPriceNumber(dollarPrice,0)
            }
        }
    }

    private fun setUpEditText() {
        resultOne_edittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (resultOne_edittext.text.toString() != "") {
                    if (resultOne_edittext.text.toString()
                            .indexOf(".") == 0 || resultOne_edittext.text.toString()
                            .lastIndexOf(".") == resultOne_edittext.text.toString().length - 1
                    )
                        return
                    startExchange()
                    return
                }
                resultTwo_textview.text = ""
                text_total_price.text = "..."
                dollar_text.text = "..."


            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    private fun setUpApiCenter() {
        apiCenter = ApiCenter(requireActivity(), this)
    }

    private fun setUpDataBase() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setUpFirstExchange() {
        CRYPTO_ONE =
            requireContext().getSharedPreferences("CRS", Context.MODE_PRIVATE).getString("CR1", "BTC")!!
        CRYPTO_TWO =
            requireContext().getSharedPreferences("CRS", Context.MODE_PRIVATE).getString("CR2", "ETH")!!

        // setting texts
        first_linear_text.text = CRYPTO_ONE
        last_linear_text.text = CRYPTO_TWO

        // setting images
        Picasso.get().load(realmDB.getCryptoDataByName(CRYPTO_ONE).ImageUrl).into(first_img)
        Picasso.get().load(realmDB.getCryptoDataByName(CRYPTO_TWO).ImageUrl).into(last_img)
    }

    private fun setUpLinearLayouts() {
        first_crypto_linear.setOnClickListener(this)
        last_crypto_linear.setOnClickListener(this)
        change_linears.setOnClickListener { changeLinears() }

    }

    private fun changeLinears() {
        // changing texts
        var tempText = CRYPTO_ONE
        CRYPTO_ONE = CRYPTO_TWO
        CRYPTO_TWO = tempText
        requireActivity().getSharedPreferences("CRS", Context.MODE_PRIVATE).edit()
            .putString("CR1", CRYPTO_ONE).apply()
        requireActivity().getSharedPreferences("CRS", Context.MODE_PRIVATE).edit()
            .putString("CR2", CRYPTO_TWO).apply()
        first_linear_text.text = CRYPTO_ONE
        last_linear_text.text = CRYPTO_TWO

        // changing images
        var tempdrawable = first_img.drawable
        first_img.setImageDrawable(last_img.drawable)
        last_img.setImageDrawable(tempdrawable)

        //start exchange
        if (resultOne_edittext.text.toString() != "")
            startExchange()

    }

    private fun setUpViews(view: View) {
        first_crypto_linear = view.findViewById(R.id.first_crypto_linear)
        last_crypto_linear = view.findViewById(R.id.last_crypto_linear)
        first_linear_text = view.findViewById(R.id.first_linear_text)
        last_linear_text = view.findViewById(R.id.last_linear_text)
        change_linears = view.findViewById(R.id.change_linears)
        first_img = view.findViewById(R.id.first_img)
        last_img = view.findViewById(R.id.last_img)
        resultOne_edittext = view.findViewById(R.id.resultOne_edittext)
        resultTwo_textview = view.findViewById(R.id.resultTwo_textview)
        dollar_text = view.findViewById(R.id.dollar_text)
        dollarPricetxt = view.findViewById(R.id.dollarPricetxt)
        exchanger_progressBar = view.findViewById(R.id.exchanger_progressBar)
        ex_nested = view.findViewById(R.id.ex_nested)
        text_total_price = view.findViewById(R.id.text_total_price)

        resultTwo_textview.setOnLongClickListener {
            if (resultTwo_textview.text.toString() != ""){
                val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText("Price" , resultTwo_textview.text.toString()))
                Toast.makeText(requireActivity(), requireActivity().resources.getString(R.string.Copied), Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    fun newInstance(): ExchangerFragment {
        val args = Bundle()
        val fragment =
            ExchangerFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.last_crypto_linear -> {
                startSearchFragment(2)
            }
            R.id.first_crypto_linear -> {
                startSearchFragment(1)
            }
        }
    }

    private fun startSearchFragment(postition: Int) {
        requireFragmentManager().beginTransaction()
            .replace(
                R.id.exchanger_holder,
                ExchnagerChooseCryptoFragment(postition, CRYPTO_ONE, CRYPTO_TWO)
            )
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("ExchnagerChooseCryptoFragment")
            .commit()
    }

    private fun checkNetworkConnection():Boolean{
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return info != null && info.isConnectedOrConnecting
    }

    private fun startExchange() {

        if (CRYPTO_ONE.isEmpty() || CRYPTO_TWO.isEmpty())
            return

        if (checkNetworkConnection()) {
            apiCenter.getCryptoPriceByName(CRYPTO_ONE, 1)
            apiCenter.getCryptoPriceByName(CRYPTO_TWO, 2)
        } else {
            val snackBar = Snackbar.make(
                ex_nested,
                requireActivity().resources.getString(R.string.connectionFailure),
                Snackbar.LENGTH_LONG
            )
            snackBar.setTextColor(Color.WHITE)
            snackBar.setActionTextColor(Color.WHITE)
            snackBar.view.setBackgroundColor(Color.BLACK)
            snackBar.show()
            exchanger_progressBar.visibility = View.GONE

        }

        exchanger_progressBar.visibility = View.VISIBLE

    }

    private fun calculate() {
        if (resultOne_edittext.text.toString() == "")
            return

        if (price1 == null || price2 == null) {
            if (price1 == null)
                apiCenter.getCryptoPriceByName(CRYPTO_ONE, 1)
            else
                apiCenter.getCryptoPriceByName(CRYPTO_TWO, 2)

            return
        }

        val result = (price1!! * resultOne_edittext.text.toString().toDouble()) / price2!!
        resultTwo_textview.text = Utils.formatPriceNumber(result,7)
        dollar_text.text = Utils.formatPriceNumber((price1!! * resultOne_edittext.text.toString().toDouble()),7)
        if (dollarPricetxt.text.toString() != "") {
            realmDB.getDollarPrice()?.price?.toDouble()?.let { dollarPrice ->
                text_total_price.text = Utils.formatPriceNumber((dollarPrice * (price1!! * resultOne_edittext.text.toString().toDouble())),0)
            }
        }
        exchanger_progressBar.visibility = View.GONE

    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().finishAffinity()
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {}

    override fun onGetByName(price: Double, dataNum: Int) {

        if (dataNum == 1)
            price1 = price
        else
            price2 = price

        calculate()
    }

    inner class onDataRecieve : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            setUpFirstExchange()
        }
    }

    inner class onDollarPriceRecieve : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            realmDB.getDollarPrice()?.price?.toDouble()?.let { dollarPrice ->
                dollarPricetxt.text = Utils.formatPriceNumber(dollarPrice,0)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ondataRecieve = onDataRecieve()
        ondollarpriceRecieve = onDollarPriceRecieve()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requireActivity().registerReceiver(ondataRecieve, IntentFilter("com.dust.extracker.onGetMainData"),Context.RECEIVER_EXPORTED)
            requireActivity().registerReceiver(
                ondollarpriceRecieve,
                IntentFilter("com.dust.extracker.onDollarPriceRecieve")
                ,Context.RECEIVER_EXPORTED
            )
        }else{
            requireActivity().registerReceiver(ondataRecieve, IntentFilter("com.dust.extracker.onGetMainData"))
            requireActivity().registerReceiver(
                ondollarpriceRecieve,
                IntentFilter("com.dust.extracker.onDollarPriceRecieve")
            )
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(ondataRecieve)
        requireActivity().unregisterReceiver(ondollarpriceRecieve)
    }

}