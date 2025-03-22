package com.dust.extracker.adapters.recyclerviewadapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.fragments.othersfragment.NotificationCustomizeFragment
import com.dust.extracker.realmdb.MainRealmObject
import com.squareup.picasso.Picasso

class NotificationChooseRecyclerViewAdapter(
    var list: List<MainRealmObject>,
    var fragmentManager: FragmentManager,
    var index:Int = 0
) :
    RecyclerView.Adapter<NotificationChooseRecyclerViewAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recyclerview_search, parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.cryptotext.text = list[position].Name
        Picasso.get().load(list[position].ImageUrl)
            .into(holder.cryptoImage)
        holder.itemView.setOnClickListener {
            if (index == 0){
                fragmentManager.beginTransaction()
                    .replace(
                        R.id.others_frame_holder,
                        NotificationCustomizeFragment().newInstance(list[position].Symbol!!)
                    )
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("NotificationCustomizeFragment")
                    .commit()
            }else{
                fragmentManager.beginTransaction()
                    .replace(
                        R.id.main_frame,
                        NotificationCustomizeFragment().newInstance(list[position].Symbol!!)
                    )
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("NotificationCustomizeFragment")
                    .commit()
            }

        }
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cryptoImage: ImageView = itemView.findViewById(R.id.item_image)
        var cryptotext: TextView = itemView.findViewById(R.id.item_text)
    }

}