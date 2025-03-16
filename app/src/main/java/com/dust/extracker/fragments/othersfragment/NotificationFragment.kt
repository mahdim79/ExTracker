package com.dust.extracker.fragments.othersfragment

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.activities.MainActivity
import com.dust.extracker.adapters.recyclerviewadapters.NotificationRecyclerViewAdapter
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.NotificationDataClass
import com.dust.extracker.interfaces.OnNotificationRemoved
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter

class NotificationFragment : Fragment(), View.OnClickListener {
    private lateinit var image_back: ImageView
    private lateinit var notifications_switcher: SwitchCompat
    private lateinit var notifications_news: SwitchCompat
   // private lateinit var notification_tick_seekBar: TickSeekBar
    private lateinit var create_customization: Button
    private lateinit var add_Image: ImageView
    private lateinit var container_linear: LinearLayout
    private lateinit var shared: SharedPreferencesCenter
    private lateinit var notification_customized_recycler: RecyclerView
    private lateinit var notification_Linear: LinearLayout
    private lateinit var onServiceDeleteNotification: OnServiceDeleteNotification
    private lateinit var realm: RealmDataBaseCenter
    private lateinit var viewGroup: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewGroup = container!!
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpRealmDB()
        setUpSharedPreferences()
        setUpContainerLinear()
     //   setUpNotificationSwitcher()
        setUpBackButton()
        setUpViewType()
        setUpImportantNewsSwitcher()
    }

    private fun setUpRealmDB() {
        realm = RealmDataBaseCenter()
    }

    private fun setUpContainerLinear() {

        val data = realm.getAllHistoryData()
        if (data.isNotEmpty()) {
            data.forEach {
                val part =
                    LayoutInflater.from(requireActivity()).inflate(R.layout.part_portfolio, viewGroup, false)
                val text = part.findViewById<CTextView>(R.id.portfolioName)
                val compat = part.findViewById<SwitchCompat>(R.id.notifications_my)
                container_linear.addView(part)
                text.text = it.portfolioName
                if (shared.getEnabledPortfolioNotifications().contains(it.portfolioName)) {
                    compat.isChecked = true
                }
                compat.setOnCheckedChangeListener { _, b ->
                    if (b) {
                        shared.setEnabledPortfolioNotifications(it.portfolioName)
                    } else {
                        shared.removeEnabledPortfolioNotifications(it.portfolioName)
                    }
                    val intent = Intent("com.dust.extracker.Update_NotificationData")
                    intent.putExtra("EnabledPortfolio" , "")
                    requireActivity().sendBroadcast(intent)
                }
            }
        }
    }

    private fun setUpImportantNewsSwitcher() {
        notifications_news.isChecked = shared.getImportantNewsNotificationsEnabled()
        notifications_news.setOnCheckedChangeListener { _, b ->
            shared.setImportantNewsNotificationsEnabled(b)
            val intent = Intent("com.dust.extracker.Update_NotificationData")
            if (b)
                intent.putExtra("newsData", true)
            else
                intent.putExtra("newsData", false)
            requireActivity().sendBroadcast(intent)
        }
    }

    /*private fun setUpNotificationSwitcher() {
        notification_tick_seekBar.min = 0f
        notification_tick_seekBar.max = 2f
        notifications_switcher.isChecked = shared.getPriceNotificationsEnabled()
        if (notifications_switcher.isChecked) {
            notification_tick_seekBar.visibility = View.VISIBLE
            notification_tick_seekBar.setProgress(shared.getPriceNotificationsLevel().toFloat())
        }

        notifications_switcher.setOnCheckedChangeListener { _, b ->
            shared.setPriceNotificationsEnabled(b)
            val intent = Intent("com.dust.extracker.Update_NotificationData")
            if (b) {
                notification_tick_seekBar.visibility = View.VISIBLE
                intent.putExtra("PriceData", true)
            } else {
                notification_tick_seekBar.visibility = View.GONE
                intent.putExtra("PriceData", false)
            }
            requireActivity().sendBroadcast(intent)
        }

        notification_tick_seekBar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {}

            override fun onStartTrackingTouch(seekBar: TickSeekBar?) {}

            override fun onStopTrackingTouch(seekBar: TickSeekBar?) {
                shared.setPriceNotificationsLevel(seekBar!!.progress)
            }
        }
    }*/

    private fun setUpSharedPreferences() {
        shared = SharedPreferencesCenter(requireActivity())
    }

    private fun setUpViewType() {
        val data = shared.getNotificationData()
        if (data.isNotEmpty()) {
            create_customization.visibility = View.GONE
            notification_Linear.visibility = View.VISIBLE
            setUpNotificationRecyclerView(data)
        } else {
            create_customization.visibility = View.VISIBLE
            notification_Linear.visibility = View.GONE
        }
    }

    private fun setUpNotificationRecyclerView(data: List<NotificationDataClass>) {
        notification_customized_recycler.adapter =
            NotificationRecyclerViewAdapter(data, object : OnNotificationRemoved {
                override fun onNotificationRemoved(id: Int) {
                    shared.removeNotificationData(id)
                    setUpViewType()
                }
            }, requireActivity())
    }

    private fun setUpBackButton() {
        image_back.setOnClickListener {
            fragmentManager?.popBackStack(
                "NotificationFragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    private fun setUpViews(view: View) {
        image_back = view.findViewById(R.id.image_back)
        add_Image = view.findViewById(R.id.add_Image)
        notifications_news = view.findViewById(R.id.notifications_news)
        notification_customized_recycler = view.findViewById(R.id.notification_customized_recycler)
        notification_Linear = view.findViewById(R.id.notification_Linear)
        notifications_switcher = view.findViewById(R.id.notifications_switcher)
      //  notification_tick_seekBar = view.findViewById(R.id.notification_tick_seekBar)
        create_customization = view.findViewById(R.id.create_customization)
        container_linear = view.findViewById(R.id.container_linear)

        notification_customized_recycler.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        create_customization.setOnClickListener(this)
        add_Image.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        requireFragmentManager().beginTransaction()
            .replace(
                R.id.others_frame_holder,
                NotificationChooseCryptoFragment()
            )
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("NotificationChooseCryptoFragment")
            .commit()
    }

    override fun onStart() {
        super.onStart()
        onServiceDeleteNotification = OnServiceDeleteNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requireActivity().registerReceiver(
                onServiceDeleteNotification,
                IntentFilter("com.dust.extracker.OnServiceDeleteNotification"),
                Context.RECEIVER_EXPORTED
            )
        }else{
            requireActivity().registerReceiver(
                onServiceDeleteNotification,
                IntentFilter("com.dust.extracker.OnServiceDeleteNotification")
            )        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onServiceDeleteNotification)
    }

    inner class OnServiceDeleteNotification : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            setUpViewType()
        }
    }

}