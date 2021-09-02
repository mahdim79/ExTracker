package com.dust.extracker.fragments.marketfragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dust.extracker.R
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class TradingViewChartFragment : Fragment(), View.OnClickListener {

    private lateinit var fullChartWebView: WebView
    private lateinit var swiprefreshLayout: SwipeRefreshLayout
    private lateinit var backImg: ImageView

    private lateinit var img1: ImageView
    private lateinit var img2: ImageView
    private lateinit var img3: ImageView
    private lateinit var img4: ImageView
    private lateinit var img5: ImageView
    private lateinit var img6: ImageView
    private lateinit var img7: ImageView
    private lateinit var img8: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tradingview_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView(view)
        setUpWebViewSettings()
        if (checkNetworkConnectivity()) {
            setUpWebView(arguments!!.getString("COIN_NAME")!!)
        } else {
            Toast.makeText(activity!!, activity!!.resources.getString(R.string.connectionFailure), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpWebViewSettings() {
        fullChartWebView.settings.javaScriptEnabled = true
        fullChartWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swiprefreshLayout.isRefreshing = false
            }
        }
    }

    private fun checkNetworkConnectivity(): Boolean {
        val connectivityManager =
            activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    private fun setUpWebView(coinName: String) {
        setCoinImages()
        fullChartWebView.loadUrl(
            "https://www.tradingview.com/chart/?symbol=BINANCE%3A${coinName}USD"
        )
    }

    private fun showErrorSnack(txt: String) {
        val snackBar = Snackbar.make(
            swiprefreshLayout,
            txt,
            Snackbar.LENGTH_LONG
        ).setAction(
            activity!!.resources.getString(R.string.connect)
        ) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting")
            activity!!.startActivity(intent)

        }
        snackBar.setTextColor(Color.BLACK)
        snackBar.setActionTextColor(Color.BLACK)
        snackBar.view.setBackgroundColor(Color.RED)
        snackBar.show()
    }


    private fun setUpView(view: View) {
        fullChartWebView = view.findViewById(R.id.fullChartWebView)
        swiprefreshLayout = view.findViewById(R.id.swiprefreshLayout)

        img1 = view.findViewById(R.id.img1)
        img2 = view.findViewById(R.id.img2)
        img3 = view.findViewById(R.id.img3)
        img4 = view.findViewById(R.id.img4)
        img5 = view.findViewById(R.id.img5)
        img6 = view.findViewById(R.id.img6)
        img7 = view.findViewById(R.id.img7)
        img8 = view.findViewById(R.id.img8)

        img1.setOnClickListener(this)
        img2.setOnClickListener(this)
        img3.setOnClickListener(this)
        img4.setOnClickListener(this)
        img5.setOnClickListener(this)
        img6.setOnClickListener(this)
        img7.setOnClickListener(this)
        img8.setOnClickListener(this)

        backImg = view.findViewById(R.id.backImg)

        swiprefreshLayout.setOnRefreshListener {
            if (checkNetworkConnectivity()) {
                fullChartWebView.reload()
            } else {
                swiprefreshLayout.isRefreshing = false
                showErrorSnack(activity!!.resources.getString(R.string.connectionFailure))
            }
        }

        backImg.setOnClickListener {
            fragmentManager?.popBackStack(
                "TradingViewChartFragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    private fun setCoinImages() {
        val list = arrayListOf("BTC", "ETH", "LTC", "XRP", "DASH", "DOGE", "ADA", "BNB")
        val listLinks = arrayListOf<String>()
        val db = RealmDataBaseCenter()
        list.forEach {
            val data = db.getCryptoDataByName(it)
            listLinks.add("${data.BaseImageUrl}${data.ImageUrl}")
        }
        Picasso.get().load(listLinks[0]).into(img1)
        Picasso.get().load(listLinks[1]).into(img2)
        Picasso.get().load(listLinks[2]).into(img3)
        Picasso.get().load(listLinks[3]).into(img4)
        Picasso.get().load(listLinks[4]).into(img5)
        Picasso.get().load(listLinks[5]).into(img6)
        Picasso.get().load(listLinks[6]).into(img7)
        Picasso.get().load(listLinks[7]).into(img8)
    }

    fun newInstance(COIN_NAME: String): TradingViewChartFragment {
        val args = Bundle()
        args.putString("COIN_NAME", COIN_NAME)
        val fragment = TradingViewChartFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onClick(p0: View?) {
        if (checkNetworkConnectivity()) {
            when (p0!!.id) {
                R.id.img1 -> setUpWebView("BTC")
                R.id.img2 -> setUpWebView("ETH")
                R.id.img3 -> setUpWebView("LTC")
                R.id.img4 -> setUpWebView("XRP")
                R.id.img5 -> setUpWebView("DASH")
                R.id.img6 -> setUpWebView("DOGE")
                R.id.img7 -> setUpWebView("ADA")
                R.id.img8 -> setUpWebView("BNB")
            }
        } else {
            Toast.makeText(activity!!, activity!!.resources.getString(R.string.connectionFailure), Toast.LENGTH_SHORT).show()
        }
    }

}