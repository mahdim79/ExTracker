package com.dust.extracker.fragments.blogfragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.dust.extracker.R
import com.dust.extracker.adapters.viewpagersadapters.NewsViewPagerAdapter
import com.dust.extracker.adapters.viewpagersadapters.SliderViewPagerAdapter
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.application.MyApplication
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.NewsDataClass
import com.dust.extracker.dataclasses.SliderDataClass
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnGetNews
import com.dust.extracker.interfaces.OnNewsDataAdded
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.google.android.material.tabs.TabLayout
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import java.util.*

class BlogFragment : Fragment(), OnGetNews {

    private lateinit var sliderViewPager: ViewPager
    private lateinit var newsTabLayout: TabLayout
    private lateinit var newsViewPager: ViewPager
    private lateinit var dotsIndicator: DotsIndicator
    private lateinit var swiprefreshLayout: SwipeRefreshLayout
    private lateinit var searchImg: ImageView
    private lateinit var bookmark_img: ImageView
    private lateinit var timer: Timer
    private lateinit var realmDB: RealmDataBaseCenter
    private lateinit var apiService: ApiCenter
    private var list = arrayListOf<SliderDataClass>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpRealmDB()
        setUpApiService()
        requestDataFeed()
        setUpSearchButton()
        setUpBookmarkButton()
    }

    private fun setUpBookmarkButton() {
        bookmark_img.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.news_frame_holder , BookMarksFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("BookMarksFragment")
                .commit()
        }
    }

    private fun setUpApiService() {
        apiService = ApiCenter(requireActivity(), object : OnGetAllCryptoList {
            override fun onGet(cryptoList: List<CryptoMainData>) {
            }

            override fun onGetByName(price: Double, dataNum: Int) {
            }

        })
    }

    private fun setUpRealmDB() {
        realmDB = RealmDataBaseCenter()

    }

    private fun requestDataFeed() {
        if (realmDB.getNewsDataCount() != 0) {
            setUpViewPagers()
            if (checkNetworkConnectivity())
                updateNewsData()
        } else {
            if (checkNetworkConnectivity())
                apiService.getNews(this)
        }

    }

    private fun updateNewsData() {
        apiService.getNews(object : OnGetNews {
            override fun onGetNews(list: List<NewsDataClass>) {
                realmDB.updateNewsData(list)
                requireActivity().sendBroadcast(Intent("com.dust.extracker.UpdateNewsViewPagerRecycler"))
                swiprefreshLayout.isRefreshing = false
            }
        })
    }

    private fun setUpSearchButton() {
        searchImg.setOnClickListener {
            fragmentManager?.beginTransaction()!!
                .replace(R.id.news_frame_holder, SearchNewsFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("fragment_search_news")
                .commit()
        }
    }

    private fun setUpViewPagers() {
        setUpSliderViewPager()
        setUpNewsViewPager()
    }

    private fun setUpNewsViewPager() {
        newsViewPager.adapter =
            NewsViewPagerAdapter(
                childFragmentManager,requireActivity()
            )
        newsViewPager.offscreenPageLimit = 4
        newsTabLayout.setupWithViewPager(newsViewPager)

        for (i in 0 until newsTabLayout.childCount) {
            val viewGroup: ViewGroup = newsTabLayout.getChildAt(i) as ViewGroup
            for (j in 0 until viewGroup.childCount) {
                val viewGroup2: ViewGroup = viewGroup.getChildAt(j) as ViewGroup
                for (k in 0 until viewGroup2.childCount) {
                    if (viewGroup2.getChildAt(k) is TextView) {
                        (viewGroup2.getChildAt(k) as TextView).typeface =
                            (requireActivity().applicationContext as MyApplication).initializeTypeFace()
                        (viewGroup2.getChildAt(k) as TextView).textSize = 8.0f
                    }
                }
            }
        }

    }

    private fun setUpSliderViewPager() {
        list.clear()

        val sliderData = realmDB.getNews("ALL")
        for (i in 0 until sliderData.size) {
            list.add(
                SliderDataClass(
                    sliderData[i].id!!,
                    sliderData[i].imageUrl,
                    sliderData[i].title,
                    sliderData[i].date.toString()
                )
            )
            if (list.size == 3)
                break
        }

        sliderViewPager.adapter =
            SliderViewPagerAdapter(
                childFragmentManager,
                list
            )
        sliderViewPager.offscreenPageLimit = list.size - 1
        dotsIndicator.setViewPager(sliderViewPager)

    }

    private fun setUpTimer(list: List<SliderDataClass>) {

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                this@BlogFragment.activity?.runOnUiThread {
                    if (sliderViewPager.currentItem == list.size - 1) {
                        sliderViewPager.currentItem = 0
                    } else {
                        sliderViewPager.currentItem = sliderViewPager.currentItem + 1
                    }
                }
            }
        }, 0, 2000)

    }

    private fun setUpViews(view: View) {
        sliderViewPager = view.findViewById(R.id.slider_viewpager)
        newsTabLayout = view.findViewById(R.id.news_tablayout)
        newsViewPager = view.findViewById(R.id.news_viewpager)
        dotsIndicator = view.findViewById(R.id.dots_indicator)
        searchImg = view.findViewById(R.id.search_img)
        bookmark_img = view.findViewById(R.id.bookmark_img)
        swiprefreshLayout = view.findViewById(R.id.swiprefreshLayout)

        swiprefreshLayout.setColorSchemeColors(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.CYAN
        )
        swiprefreshLayout.setOnRefreshListener {
            if (checkConnection()) {
                updateNewsData()
            } else {
                swiprefreshLayout.isRefreshing = false
                Toast.makeText(requireActivity(), "No Connection!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun checkConnection(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return info != null && info.isConnectedOrConnecting
    }

    fun newInstance(): BlogFragment {
        val args = Bundle()
        val fragment =
            BlogFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onStop() {
        timer.purge()
        timer.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().finishAffinity()
    }

    override fun onStart() {
        setUpTimer(list)
        super.onStart()
    }

    override fun onGetNews(list: List<NewsDataClass>) {
        realmDB.insertNews(list, object : OnNewsDataAdded {
            override fun onNewsDataAdded() {
                setUpViewPagers()
            }
        })
    }

    private fun checkNetworkConnectivity(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }
}