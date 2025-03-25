package com.dust.extracker.adapters.recyclerviewadapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.realmdb.MainRealmObject
import com.squareup.picasso.Picasso

class ExchangerRecyclerViewAdapter(
    var context: Context,
    var list: List<MainRealmObject>,
    var fragmentManager: FragmentManager
) : RecyclerView.Adapter<ExchangerRecyclerViewAdapter.MainViewHolder>() {

    private lateinit var onItemClick:(symbol:String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_recyclerview_search, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.cryptotext.text = "${list[position].Name}"
        holder.item_text_coin.text = " (${list[position].Symbol})"
        Picasso.get().load(list[position].ImageUrl).into(holder.cryptoImage)
        holder.itemView.setOnClickListener {
            if (::onItemClick.isInitialized){
                list[position].Symbol?.let { s ->
                    onItemClick.invoke(s)
                }
            }
        }
    }

    fun setOnItemClickListener(onItemClick:(symbol:String) -> Unit){
        this.onItemClick = onItemClick
    }

    override fun getItemCount(): Int = list.size

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var cryptoImage: ImageView = itemView.findViewById(R.id.item_image)
        var cryptotext: TextView = itemView.findViewById(R.id.item_text)
        var item_text_coin: TextView = itemView.findViewById(R.id.item_text_coin)
    }
}