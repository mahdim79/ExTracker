package com.dust.extracker.adapters.recyclerviewadapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.AccessibilityDataClass
import de.hdodenhof.circleimageview.CircleImageView

class MarketAccessibilityRecyclerViewAdapter(var list: List<AccessibilityDataClass> , var context: Context) :
    RecyclerView.Adapter<MarketAccessibilityRecyclerViewAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.accessibility_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.text.text = list[position].name
        holder.image.setImageDrawable(ResourcesCompat.getDrawable(context.resources , list[position].resId , null))
        holder.itemView.setOnClickListener {
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW , Uri.parse(list[position].url)) , context.resources.getString(R.string.openUrlWith)))
        }
    }

    override fun getItemCount(): Int = list.size

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var image = itemView.findViewById<CircleImageView>(R.id.access_Image)
        var text = itemView.findViewById<CTextView>(R.id.access_Text)

    }

}