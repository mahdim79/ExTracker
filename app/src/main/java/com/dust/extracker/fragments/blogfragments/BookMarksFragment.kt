package com.dust.extracker.fragments.blogfragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.NewsRecyclerViewAdapter
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.NewsObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter

class BookMarksFragment:Fragment() {
    private lateinit var bookmark_recyclerview:RecyclerView
    private lateinit var realmDB:RealmDataBaseCenter
    private lateinit var back_img:ImageView
    private lateinit var notation:CTextView
    private lateinit var onRemove:OnRemove

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bookmark , container , false)
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
        val list = realmDB.getBookmarks()
        if (list.isNotEmpty())
            notation.visibility = View.GONE
        bookmark_recyclerview.layoutManager = LinearLayoutManager(activity!! , LinearLayoutManager.VERTICAL , false)
        bookmark_recyclerview.adapter = NewsRecyclerViewAdapter( list, activity!!.supportFragmentManager , realmDB , true)
    }

    private fun setUpViews(view: View) {
        bookmark_recyclerview = view.findViewById(R.id.bookmark_recyclerview)
        back_img = view.findViewById(R.id.back_img)
        notation = view.findViewById(R.id.notation)

        back_img.setOnClickListener {
            activity!!.supportFragmentManager.popBackStack("BookMarksFragment" , FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    override fun onStart() {
        super.onStart()
        onRemove = OnRemove()
        activity!!.registerReceiver(onRemove , IntentFilter("com.dust.extracker.RemoveBookmark"))
    }

    override fun onStop() {
        super.onStop()
        activity!!.unregisterReceiver(onRemove)
    }

    inner class OnRemove:BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            notation.visibility = View.VISIBLE
            setUpRecyclerView()
        }
    }
}