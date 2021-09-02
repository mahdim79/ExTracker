package com.dust.extracker.adapters.recyclerviewadapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.realmdb.MainRealmObject
import com.squareup.picasso.Picasso

class SearchRecyclerViewAdapter(var PortfolioName:String ,var IS_TRANSACTION:Boolean , var context: Context , var list:List<MainRealmObject> , var onSelectItem: OnSelectItem):RecyclerView.Adapter<SearchRecyclerViewAdapter.MainViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recyclerview_search,parent , false))
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        if (position == list.size-1)
            holder.divider.visibility = View.INVISIBLE
        holder.cryptotext.text = list[position].FullName
        Picasso.get().load("${list[position].BaseImageUrl}${list[position].ImageUrl}").into(holder.cryptoImage)
        holder.itemView.setOnClickListener {
            onSelectItem.onSelect(list[position].Name!! , IS_TRANSACTION , PortfolioName)
        }
    }

    override fun getItemCount(): Int = list.size

    inner class MainViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

        var cryptoImage:ImageView = itemView.findViewById(R.id.item_image)
        var cryptotext:TextView = itemView.findViewById(R.id.item_text)
        var divider:View = itemView.findViewById(R.id.divider)
    }

    interface OnSelectItem{
        fun onSelect(coinName:String , IS_TRANSACTION:Boolean , PortfolioName:String)
    }
}