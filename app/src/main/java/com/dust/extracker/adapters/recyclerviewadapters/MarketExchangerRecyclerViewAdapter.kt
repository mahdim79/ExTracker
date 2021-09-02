package com.dust.extracker.adapters.recyclerviewadapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.dataclasses.ExchangerDataClass
import com.dust.extracker.realmdb.ExchangerObject
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.acos

class MarketExchangerRecyclerViewAdapter(var context: Context , var list: List<ExchangerObject>) :
    RecyclerView.Adapter<MarketExchangerRecyclerViewAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_exchangers_recyclerview, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.exSortNum.text = list[position].id.toString()
        Picasso.get().load(list[position].imageUrl).into(holder.exImage)
        holder.exName.text = list[position].name
        holder.exDailyVol.text = "${String.format("%.2f" , list[position].trade_volume_24h_btc)} BTC"
        holder.exYearStabilised.text = "${list[position].country} - ${list[position].year_established}"
        holder.exRank.text = list[position].trust_score_rank.toString()
        holder.trust_score.text = list[position].trust_score.toString()

        holder.itemView.setOnClickListener {
            val softInputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            softInputManager.hideSoftInputFromWindow(holder.exName.windowToken , InputMethodManager.HIDE_NOT_ALWAYS)
            val intent = Intent(Intent.ACTION_VIEW , Uri.parse(list[position].url))
            context.startActivity(Intent.createChooser(intent , context.resources.getString(R.string.openUrlWith)))
        }

    }

    override fun getItemCount(): Int = list.size

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var exSortNum: TextView = itemView.findViewById(R.id.exSortNum)
        var exName: TextView = itemView.findViewById(R.id.exName)
        var exDailyVol: TextView = itemView.findViewById(R.id.exDailyVol)
        var exYearStabilised: TextView = itemView.findViewById(R.id.exYearStabilised)
        var trust_score: TextView = itemView.findViewById(R.id.trust_score)
        var exRank: TextView = itemView.findViewById(R.id.exRank)
        var exImage: CircleImageView = itemView.findViewById(R.id.exImage)
    }

}