package com.dust.extracker.fragments.portfoliofragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.R
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.customviews.CButton
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.HistoryDataClass
import com.dust.extracker.dataclasses.TransactionDataClass
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.dust.extracker.utils.Utils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class InputDataFragment : Fragment(), View.OnClickListener,OnGetAllCryptoList {
    lateinit var btnBuy: CButton
    lateinit var btnSell: CButton
    lateinit var btnAdd: CButton

    private val spinnerList = arrayListOf<String>()
    lateinit var nameRelativeLayout: RelativeLayout
    lateinit var descRelativeLayout: RelativeLayout
    lateinit var spinnerRelativeLayout: RelativeLayout

    lateinit var count: TextInputLayout
    lateinit var mainPrice: TextInputLayout
    lateinit var dollarPrice: TextInputLayout
    lateinit var descriptions: TextInputLayout
    lateinit var portfolioName: TextInputLayout

    lateinit var txt_totalAmount: CTextView
    lateinit var crypto_name: CTextView

    lateinit var portfolio_spinner: Spinner

    lateinit var backImg: ImageView

    lateinit var realmDB: RealmDataBaseCenter

    lateinit var viewMain: View

    private lateinit var apiService:ApiCenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inputdata, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewMain = view
        setUpRealmDb()
        setUpViews(view)
        syncViews()

    }

    private fun setUpRealmDb() {
        realmDB = RealmDataBaseCenter()
        apiService = ApiCenter(requireContext(),this)
    }

    private fun syncViews() {
        btnSell.setOnClickListener(this)
        btnBuy.setOnClickListener(this)
        btnAdd.setOnClickListener(this)
        backImg.setOnClickListener(this)
    }

    private fun setUpViews(view: View) {
        backImg = view.findViewById(R.id.backImg)
        portfolioName = view.findViewById(R.id.portfolioName)
        btnBuy = view.findViewById(R.id.btnBuy)
        crypto_name = view.findViewById(R.id.crypto_name)
        btnSell = view.findViewById(R.id.btnSell)
        btnAdd = view.findViewById(R.id.btnAdd)
        txt_totalAmount = view.findViewById(R.id.txt_totalAmount)
        nameRelativeLayout = view.findViewById(R.id.nameRelativeLayout)
        descRelativeLayout = view.findViewById(R.id.descRelativeLayout)
        spinnerRelativeLayout = view.findViewById(R.id.spinnerRelativeLayout)
        portfolio_spinner = view.findViewById(R.id.portfolio_spinner)
        count = view.findViewById(R.id.count)
        mainPrice = view.findViewById(R.id.price)
        dollarPrice = view.findViewById(R.id.dollarPrice)
        descriptions = view.findViewById(R.id.descriptions)

        var color = if (SharedPreferencesCenter(requireActivity()).getNightMode())
            Color.WHITE
        else
            Color.BLACK

        btnAdd.setTextColor(color)
        btnBuy.setTextColor(color)
        btnSell.setTextColor(color)

        txt_totalAmount.text =
            resources.getString(R.string.totalAmountText, requireArguments().getString("COINNAME", "BTC2"))

        if (requireArguments().getBoolean("IS_TRANSACTION", false)) {
            realmDB.getAllHistoryData().forEach {
                spinnerList.add(it.portfolioName)
            }
            var index = 0
            for (i in 0 until spinnerList.size)
                if (spinnerList[i] == requireArguments().getString(
                        "PortfolioName",
                        "SecKey=sdffgvbnmsdfghjkrtyuio"
                    )
                )
                    index = i
            portfolio_spinner.adapter = ArrayAdapter<String>(
                requireActivity(),
                R.layout.custom_spinner_item,
                R.id.textview1,
                spinnerList
            )
            portfolio_spinner.setSelection(index)

            nameRelativeLayout.visibility = View.GONE
            descRelativeLayout.visibility = View.GONE
            crypto_name.text = requireActivity().resources.getString(R.string.addDeal)
            btnAdd.text = requireActivity().resources.getString(R.string.addDeal)
        } else {
            spinnerRelativeLayout.visibility = View.GONE
        }

        val cachedPrice = realmDB.getCryptoDataByName(requireArguments().getString("COINNAME", "BTC")).LastPrice
        if (cachedPrice == null || cachedPrice == 0.0){
            apiService.getCryptoPriceByName(requireArguments().getString("COINNAME", "BTC"), 0)
        }else{
            setMainPrice(cachedPrice)
        }

        realmDB.getDollarPrice()?.price?.let {
            dollarPrice.editText!!.setText(Utils.formatPriceNumber(it.toDouble(),0, Locale.ENGLISH))
        }
    }

    private fun setMainPrice(price:Double){
        mainPrice.editText!!.setText(Utils.formatPriceNumber(price,calculateDecimal(price), Locale.ENGLISH))
    }

    private fun calculateDecimal(price:Double):Int{
        return if (price > 1){
            2
        }else if (price > 0.00001){
            7
        }else{
            12
        }
    }

    fun newInstance(
        coinName: String,
        IS_TRANSACTION: Boolean,
        PortfolioName: String
    ): InputDataFragment {
        val args = Bundle()
        args.putString("COINNAME", coinName)
        args.putBoolean("IS_TRANSACTION", IS_TRANSACTION)
        args.putString("PortfolioName", PortfolioName)
        val fragment =
            InputDataFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.btnBuy -> {
                btnSell.isEnabled = true
                btnBuy.isEnabled = false
                btnAdd.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.buy_add_deal_button_selector,
                    null
                )
            }
            R.id.btnSell -> {
                btnBuy.isEnabled = true
                btnSell.isEnabled = false
                btnAdd.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.sell_add_deal_button_selector,
                    null
                )
            }
            R.id.btnAdd -> {
                calculateAndAddData()
            }
            R.id.backImg -> fragmentManager?.popBackStack("InputDataFragment", 1)

        }
    }

    private fun calculateAndAddData() {
        val validity = checkValidity()
        if (validity == "RESULT_OK") {
            if (requireArguments().getBoolean("IS_TRANSACTION", false)) {
                val allData = realmDB.getAllHistoryData()
                allData.forEach {
                    if (it.portfolioName == spinnerList[portfolio_spinner.selectedItemPosition]) {
                        val newList = arrayListOf<TransactionDataClass>()
                        newList.addAll(it.transactionList)

                        var dealType = ""

                        if (btnBuy.isEnabled)
                            dealType = "SELL"
                        else
                            dealType = "BUY"

                        newList.add(
                            TransactionDataClass(
                                it.transactionList.size,
                                requireArguments().getString("COINNAME", "BTC"),
                                dealType,
                                count.editText!!.text.toString().toDouble(),
                                dollarPrice.editText!!.text.toString().replace(",","").toDouble(),
                                mainPrice.editText!!.text.toString().replace(",","").toDouble(),
                                0.toDouble(),
                                "null"
                            )
                        )
                        it.transactionList = newList
                        realmDB.updateHistoryData(it)
                    }
                }
                requireFragmentManager().popBackStack(
                    "SelectCtyptoFragment",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                requireFragmentManager().popBackStack(
                    "InputDataFragment",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                requireFragmentManager().beginTransaction()
                    .replace(R.id.frame_holder, PortfolioFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("PortfolioFragment")
                    .commit()
                return
            }
            realmDB.getAllHistoryData().forEach {
                if (portfolioName.editText!!.text.toString() == it.portfolioName){
                    portfolioName.error = requireActivity().resources.getString(R.string.nameUsed)
                    return
                }
            }
            val historyData = createHistoryData()
            realmDB.insertHistoryData(historyData, requireFragmentManager(), requireActivity())
        }
    }

    private fun createHistoryData(): HistoryDataClass {
        var dealType = ""

        if (btnBuy.isEnabled)
            dealType = "SELL"
        else
            dealType = "BUY"

        val transactionDataList = arrayListOf(
            TransactionDataClass(
                0,
                requireArguments().getString("COINNAME", "BTC"),
                dealType,
                count.editText!!.text.toString().toDouble(),
                dollarPrice.editText!!.text.toString().replace(",","").toDouble(),
                mainPrice.editText!!.text.toString().replace(",","").toDouble(),
                0.toDouble(),
                "null"
            )
        )
        var desc = ""
        if (descriptions.editText!!.text.toString() == "")
            desc = requireActivity().resources.getString(R.string.noDescription)
        else
            desc = descriptions.editText!!.text.toString()

        return HistoryDataClass(
            0,
            portfolioName.editText!!.text.toString(),
            transactionDataList,
            desc,
            (mainPrice.editText!!.text.toString().replace(",","").toDouble() * count.editText!!.text.toString()
                .toDouble()),
            (mainPrice.editText!!.text.toString().replace(",","").toDouble() * count.editText!!.text.toString()
                .toDouble()),
            "0"
        )
    }

    private fun checkValidity(): String {
        var result = "RESULT_NOT_OK"

        if (!requireArguments().getBoolean("IS_TRANSACTION", false)) {
            val pName = portfolioName.editText!!.text.toString()
            if (pName == "") {
                portfolioName.error = requireActivity().resources.getString(R.string.requireField)
                return result
            } else {
                portfolioName.error = ""
            }
        }

        val count1 = count.editText!!.text.toString()
        val price = mainPrice.editText!!.text.toString()
        val doPrice = dollarPrice.editText!!.text.toString()

        if (count1 != "" && count1.first().toString() != "." && count1.last().toString() != ".") {
            count.error = ""
            if (price != "" && price.first().toString() != "." && price.last().toString() != ".") {
                mainPrice.error = ""
                if (doPrice != "") {
                    dollarPrice.error = ""
                    result = "RESULT_OK"
                } else {
                    dollarPrice.error = requireActivity().resources.getString(R.string.requireField)
                }
            } else {
                if (price == "")
                    mainPrice.error = requireActivity().resources.getString(R.string.requireField)
                else
                    mainPrice.error = requireActivity().resources.getString(R.string.enterCorrectAmount)
            }
        } else {
            if (count1 == "")
                count.error = requireActivity().resources.getString(R.string.requireField)
            else
                count.error = requireActivity().resources.getString(R.string.enterCorrectAmount)
        }



        return result
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {}

    override fun onGetByName(price: Double, dataNum: Int) {
        setMainPrice(price)
    }
}