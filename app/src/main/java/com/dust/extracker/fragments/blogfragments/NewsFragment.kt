package com.dust.extracker.fragments.blogfragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.NewsRecyclerViewAdapter
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter

class NewsFragment : Fragment() {
    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var realmDB: RealmDataBaseCenter
    private lateinit var updateNewsViewPagerRecycler: UpdateNewsViewPagerRecycler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpRealmDB()
        setUpRecyclerView()

    }

    private fun setUpRealmDB() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setUpRecyclerView() {
        newsRecyclerView.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
        newsRecyclerView.adapter = when (arguments?.getInt("position")) {
            0 -> {
                val data = realmDB.getNews("ALL")
                NewsRecyclerViewAdapter(
                    data,
                    activity!!.supportFragmentManager, realmDB
                )
            }

            1 -> {
                val data = realmDB.getNews("BTC")
                NewsRecyclerViewAdapter(
                    data,
                    activity!!.supportFragmentManager, realmDB
                )
            }

            2 -> {
                val data = realmDB.getNews("ETH")
                NewsRecyclerViewAdapter(
                    data,
                    activity!!.supportFragmentManager, realmDB
                )
            }

            3 -> {
                val data = realmDB.getNews("Trading")
                NewsRecyclerViewAdapter(
                    data,
                    activity!!.supportFragmentManager, realmDB
                )
            }

            else -> {
                val data = realmDB.getNews("Altcoin")
                NewsRecyclerViewAdapter(
                    data,
                    activity!!.supportFragmentManager, realmDB
                )
            }
        }
    }

    private fun setUpViews(view: View) {
        newsRecyclerView = view.findViewById(R.id.news_recyclerview)
    }

    fun newInstance(position: Int): NewsFragment {
        val args = Bundle()
        args.putInt("position", position)
        val fragment = NewsFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onStart() {
        super.onStart()
        updateNewsViewPagerRecycler = UpdateNewsViewPagerRecycler()
        activity!!.registerReceiver(updateNewsViewPagerRecycler , IntentFilter("com.dust.extracker.UpdateNewsViewPagerRecycler"))
    }

    override fun onStop() {
        super.onStop()
        activity!!.unregisterReceiver(updateNewsViewPagerRecycler)
    }

    inner class UpdateNewsViewPagerRecycler:BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            setUpRecyclerView()
        }
    }

}