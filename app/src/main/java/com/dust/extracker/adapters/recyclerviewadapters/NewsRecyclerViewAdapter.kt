package com.dust.extracker.adapters.recyclerviewadapters

import android.content.Context
import android.content.Intent
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
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.sql.Date
import java.sql.Timestamp
import java.util.*

class NewsRecyclerViewAdapter(var list: List<NewsObject> ,var activity: FragmentManager , var realm:RealmDataBaseCenter , var isBookmarkPage:Boolean = false) :
    RecyclerView.Adapter<NewsRecyclerViewAdapter.MainViewHolder>() {

    var localIs_liked:Boolean = true
    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        this.context = parent.context
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
        holder.news_like_count.text = list[position].likeCount.toString()
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


        holder.news_date.text = "${convertTimeStamp(list[position].date!!)}"
        Picasso.get().load(list[position].imageUrl).into(holder.news_image)

        if (list[position].is_liked == "false") {
            holder.news_like.setImageResource(R.drawable.ic_baseline_news_unliked)
            localIs_liked = false
        }

        holder.news_like_count.setOnClickListener {
            handleLikeEvent(holder)
        }

        holder.news_like.setOnClickListener {
            handleLikeEvent(holder)
        }
    }

    override fun getItemCount(): Int = list.size

    private fun convertTimeStamp(stampTime:Int):String{
        var millis = stampTime * 1000
        val s = Timestamp(millis.toLong())
        val date = java.util.Date(s.time)
        val time = "${date.date}/${date.month}/${date.year} ${date.hours}:${date.minutes}"
        return time
    }

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


    private fun handleLikeEvent(holder: MainViewHolder)
    {
        if (localIs_liked){
            localIs_liked = false
            holder.news_like.setImageResource(R.drawable.ic_baseline_news_unliked)
            // TODO: 5/28/2021 save like status in db and server
        } else{
            localIs_liked = true
            holder.news_like.setImageResource(R.drawable.ic_baseline_news_liked)
            // TODO: 5/28/2021 save like status in db and server
        }
    }

}