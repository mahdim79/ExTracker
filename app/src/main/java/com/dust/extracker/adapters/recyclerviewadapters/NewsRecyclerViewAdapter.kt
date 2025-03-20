package com.dust.extracker.adapters.recyclerviewadapters

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.NewsDataClass
import com.dust.extracker.fragments.blogfragments.ReadNewsFragment
import com.dust.extracker.realmdb.NewsObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.dust.extracker.utils.Utils
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.internal.Util
import java.sql.Date
import java.sql.Timestamp
import java.util.*

class NewsRecyclerViewAdapter(var list: List<NewsDataClass> ,var activity: FragmentManager , var realm:RealmDataBaseCenter , var isBookmarkPage:Boolean = false) :
    RecyclerView.Adapter<NewsRecyclerViewAdapter.MainViewHolder>() {

    lateinit var context: Context
    private lateinit var perfCenter:SharedPreferencesCenter
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        this.context = parent.context
        perfCenter = SharedPreferencesCenter(context)
        return MainViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {

        if (position == list.size-1)
            holder.divider.visibility = View.GONE

        holder.itemView.setOnClickListener {

            activity.beginTransaction()
                .replace(R.id.news_frame_holder, ReadNewsFragment(list[position]))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("ReadNewsFragment")
                .commit()

        }
        holder.title.text = list[position].title
        holder.news_desc.text = list[position].description
      //  holder.news_like_count.text = list[position].likeCount.toString()
        holder.news_seen_count.text = list[position].seenCount.toString()


        val availability = realm.checkBookmarkAvailability(list[position].url)
        if (availability)
            holder.add_to_bookmark.setImageResource(R.drawable.ic_baseline_bookmarked)

        holder.add_to_bookmark.setOnClickListener {
            if (availability){
                realm.removeBookmark(list[position].url)
                holder.add_to_bookmark.setImageResource(R.drawable.ic_baseline_bookmark)
                if (isBookmarkPage)
                    context.sendBroadcast(Intent("com.dust.extracker.RemoveBookmark"))
            }else{
                realm.insertBookmark(list[position])
                holder.add_to_bookmark.setImageResource(R.drawable.ic_baseline_bookmarked)
            }
        }


        list[position].date.let { ts ->
            holder.news_date.text = Utils.convertTimestampToDate( ts * 1000L)
        }
        Picasso.get().load(list[position].imageUrl).into(holder.news_image)

        if (!list[position].is_liked) {
            holder.news_like_count.text = "0"
            holder.news_like.setImageResource(R.drawable.ic_baseline_news_unliked)
            if (perfCenter.getNightMode())
                holder.news_like.imageTintList = ColorStateList.valueOf(Color.WHITE)
            else
                holder.news_like.imageTintList = ColorStateList.valueOf(Color.BLACK)
        }else{
            holder.news_like_count.text = "1"
            holder.news_like.setImageResource(R.drawable.ic_baseline_news_liked)
            holder.news_like.imageTintList = ColorStateList.valueOf(Color.RED)
        }

        holder.news_like_count.setOnClickListener {
            list[position].is_liked = !list[position].is_liked
            handleLikeEvent(holder,list[position])
        }

        holder.news_like.setOnClickListener {
            list[position].is_liked = !list[position].is_liked
            handleLikeEvent(holder,list[position])
        }
    }

    override fun getItemCount(): Int = list.size

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var title = itemView.findViewById<CTextView>(R.id.news_title)
        var news_desc = itemView.findViewById<CTextView>(R.id.news_desc)
        var news_date = itemView.findViewById<CTextView>(R.id.news_date)
        var news_image = itemView.findViewById<CircleImageView>(R.id.news_image)
        var news_like = itemView.findViewById<ImageView>(R.id.news_like)
        var news_like_count = itemView.findViewById<TextView>(R.id.news_like_count)
        var news_seen_count = itemView.findViewById<TextView>(R.id.news_seen_count)
        var divider = itemView.findViewById<View>(R.id.divider)
        var add_to_bookmark = itemView.findViewById<ImageView>(R.id.add_to_bookmark)
    }


    private fun handleLikeEvent(holder: MainViewHolder,data:NewsDataClass) {
        realm.setNewsIsLiked(data.ID,data.is_liked)
        if (data.is_liked){
            holder.news_like_count.text = "1"
            holder.news_like.setImageResource(R.drawable.ic_baseline_news_liked)
            holder.news_like.imageTintList = ColorStateList.valueOf(Color.RED)
        } else{
            holder.news_like_count.text = "0"
            holder.news_like.setImageResource(R.drawable.ic_baseline_news_unliked)
            holder.news_like.imageTintList = ColorStateList.valueOf(if (perfCenter.getNightMode()) Color.WHITE else Color.BLACK)
        }
    }

}