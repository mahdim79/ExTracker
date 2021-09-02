package com.dust.extracker.adapters.recyclerviewadapters

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.NotificationDataClass
import com.dust.extracker.interfaces.OnNotificationRemoved

class NotificationRecyclerViewAdapter(var list:List<NotificationDataClass> , var onNotificationRemoved:OnNotificationRemoved , var context:Context):RecyclerView.Adapter<NotificationRecyclerViewAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val txt_id = itemView.findViewById<TextView>(R.id.txt_id)
        val txt_name = itemView.findViewById<TextView>(R.id.txt_name)
        val item_price = itemView.findViewById<TextView>(R.id.item_price)
        val txt_repeate_type = itemView.findViewById<TextView>(R.id.txt_repeate_type)
        val txt_condition = itemView.findViewById<CTextView>(R.id.txt_condition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.notification_recycler_item , parent , false))
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {

        holder.txt_id.text = list[position].id.toString()
        holder.item_price.text = String.format("%.4f" , list[position].targetPrice)
        holder.txt_name.text = list[position].symbol
        var condition = ""
        if (list[position].mode == 0)
            condition = context.resources.getString(R.string.once)
        else
            condition = context.resources.getString(R.string.always)
        holder.txt_repeate_type.text = condition

        if (list[position].lastPrice > list[position].targetPrice){
            holder.txt_condition.text = context.resources.getString(R.string.fewerThan)
            holder.txt_condition.setTextColor(Color.RED)
        }else{
            holder.txt_condition.text = context.resources.getString(R.string.moreThan)
            holder.txt_condition.setTextColor(Color.GREEN)
        }
        holder.itemView.setOnLongClickListener {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_remove_transaction)
            dialog.setCancelable(false)
            dialog.findViewById<CTextView>(R.id.txt_remove_question).text = context.resources.getString(R.string.deleteNotification)
            dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }
            dialog.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
                onNotificationRemoved.onNotificationRemoved(list[position].id)
                dialog.dismiss()
            }
            dialog.show()
            true
        }
    }
}