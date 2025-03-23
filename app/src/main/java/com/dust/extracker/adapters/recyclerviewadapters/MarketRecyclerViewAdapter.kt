package com.dust.extracker.adapters.recyclerviewadapters

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.utils.Utils
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.Exception

class MarketRecyclerViewAdapter(var context: Context,
    var alphaAnimation: AlphaAnimation,
    var dollarPrice: Double
) : ListAdapter<MainRealmObject,MarketRecyclerViewAdapter.MainRecyclerViewHolder>(callBack){

    companion object{
        private val callBack = object :DiffUtil.ItemCallback<MainRealmObject>(){
            override fun areItemsTheSame(
                oldItem: MainRealmObject,
                newItem: MainRealmObject
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MainRealmObject,
                newItem: MainRealmObject
            ): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.Symbol.equals(newItem.Symbol) &&
                        oldItem.ID.equals(newItem.ID) &&
                        oldItem.rank == newItem.rank
            }

        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainRecyclerViewHolder {
        return MainRecyclerViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.market_recycler_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MainRecyclerViewHolder, position: Int) {
        val itemData = getItem(position)
        Picasso.get().load(itemData.ImageUrl)
            .into(holder.item_image)
        holder.item_text.text = itemData.Name
        holder.itemView.setOnLongClickListener {
            val resultList = arrayListOf<String>()
            val sharedPreferences = context.getSharedPreferences("FAV", Context.MODE_PRIVATE)
            val rawData = sharedPreferences.getString("fav", "")
            var listRawData = listOf<String>()
            if (rawData != "") {
                listRawData = rawData!!.split(',')
                resultList.addAll(listRawData)
            }
            var text = ""
            var color: Drawable? = null
            if (resultList.contains(itemData.ID!!)) {
                resultList.remove(itemData.ID!!)
                text = context.resources.getString(R.string.deletewatch)
                color = ResourcesCompat.getDrawable(
                    context.resources!!,
                    R.drawable.dialog_remove_confirm_btn,
                    null
                )
            } else {
                resultList.add(itemData.ID!!)
                text = context.resources.getString(R.string.addwatch)
                color = ResourcesCompat.getDrawable(
                    context.resources!!,
                    R.drawable.dialog_add_confirm_btn,
                    null
                )
            }

            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_remove_transaction)
            dialog.setCancelable(true)
            dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }
            dialog.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
                sharedPreferences.edit().putString("fav", resultList.joinToString(",")).apply()
                context.sendBroadcast(Intent("com.dust.extracker.notifyDataSetChanged"))
                dialog.dismiss()
            }

            dialog.findViewById<CTextView>(R.id.txt_remove_question).text = text
            dialog.findViewById<Button>(R.id.btnConfirm).background = color
            dialog.show()
            true
        }
        try {
            val decimalCount = calculateDecimalByPrice(itemData.LastPrice!!)
            if (itemData.LastPrice!! == 0.0)
                holder.item_price.text = ""
            else
                holder.item_price.text = "$${Utils.formatPriceNumber(itemData.LastPrice!!,decimalCount, Locale.ENGLISH)}"
            holder.item_price_toman.text = "${Utils.formatPriceNumber((itemData.LastPrice!! * dollarPrice),decimalCount)} ${context.resources.getString(R.string.toman)}"

        } catch (e: Exception) {
            holder.item_price.text = ""
        }
        holder.item_price.startAnimation(alphaAnimation)

     //   holder.sort_num.text = itemData.SortOrder

        holder.item_text_coinName.text = itemData.Symbol

        holder.dailyChange.text = "${itemData.DailyChangePCT.toString()}%"

        if (itemData.DailyChangePCT != null) {
            if (itemData.DailyChangePCT!! > 0)
                holder.dailyChange.background = ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.portfolio_linear_shape_green,
                    null
                )
            else
                holder.dailyChange.background = ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.portfolio_linear_shape_red,
                    null
                )
        }

        holder.itemView.setOnClickListener {
            val intent = Intent("com.dust.extracker.OnClickMainData")
            intent.putExtra("Symbol", itemData.Symbol)
            context.sendBroadcast(intent)
        }

        class UpdatePrices : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                try {
                    val bundle = p1!!.extras
                    if (!bundle!!.isEmpty && bundle.containsKey("IS_DAILY_CHANGE") && bundle.getBoolean(
                            "IS_DAILY_CHANGE"
                        )
                    ) {
                        var dayChange = 0.0
                        try {
                            dayChange =
                                holder.dailyChange.text.toString().replace("%", "").toDouble()
                        } catch (e: Exception) {
                            dayChange = 0.0
                        }
                        if (dayChange >= 0
                        ) {
                            holder.dailyChange.text = "${bundle.getDouble(itemData.Symbol)}%"

                            holder.dailyChange.background = ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.portfolio_linear_shape_green,
                                null
                            )

                        } else {
                            holder.dailyChange.text = "${bundle.getDouble(itemData.Symbol)}%"

                            holder.dailyChange.background = ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.portfolio_linear_shape_red,
                                null
                            )
                        }
                        holder.dailyChange.startAnimation(alphaAnimation)

                    } else {
                        if (!bundle.isEmpty && bundle.containsKey(itemData.Symbol)) {
                            val price = try {
                                holder.item_price.text.toString().replace("$", "").replace(",","").toDouble()
                            } catch (e: Exception) {
                                0.0
                            }
                            val bundlePrice = bundle.getDouble(itemData.Symbol)
                            val decimalCount = calculateDecimalByPrice(bundlePrice)

                            if (price < bundlePrice) {
                                holder.item_price.text = "$${Utils.formatPriceNumber(bundlePrice,decimalCount,
                                    Locale.ENGLISH)}"
                                holder.item_price.setTextColor(Color.GREEN)

                            } else if (price == 0.0) {
                                holder.item_price.text = "$${Utils.formatPriceNumber(bundlePrice,decimalCount,
                                    Locale.ENGLISH)}"

                            } else {
                                holder.item_price.text = "$${Utils.formatPriceNumber(bundlePrice,decimalCount,
                                    Locale.ENGLISH)}"

                                holder.item_price.setTextColor(Color.RED)

                            }
                            val tomanPrice = ((holder.item_price.text.toString().replace("$", "").replace(",", "").toDouble()) * (p1!!.extras!!.getString("PRICE")!!.toDouble()))
                            val decimal = calculateDecimalByPrice(tomanPrice)
                            holder.item_price_toman.text = "${Utils.formatPriceNumber(tomanPrice,decimal)} ${context.resources.getString(R.string.toman)}"

                            holder.item_price.startAnimation(alphaAnimation)
                        }
                    }
                } catch (e: Exception) {
                    Log.i("someTag","")
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            context.registerReceiver(UpdatePrices(), IntentFilter("com.dust.extracker.UPDATE_ITEMS"),Context.RECEIVER_EXPORTED)
        }else{
            context.registerReceiver(UpdatePrices(), IntentFilter("com.dust.extracker.UPDATE_ITEMS"))
        }

        class UpdateTomanData : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val tomanPrice = (itemData.LastPrice!! * (p1!!.extras!!.getString("PRICE")!!.toDouble()))
                val decimal = calculateDecimalByPrice(tomanPrice)
                holder.item_price_toman.text = "${Utils.formatPriceNumber(tomanPrice,decimal)} تومان"
                holder.item_price_toman.startAnimation(alphaAnimation)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            context.registerReceiver(
                UpdatePrices(),
                IntentFilter("com.dust.extracker.onDollarPriceRecieve"),
                Context.RECEIVER_EXPORTED
            )
        }else{
            context.registerReceiver(
                UpdatePrices(),
                IntentFilter("com.dust.extracker.onDollarPriceRecieve")
            )
        }

    }

    private fun calculateDecimalByPrice(price:Double):Int{
        return if (price > 1){
            2
        }else if (price >0.00001){
            7
        }else{
            12
        }
    }
    
    inner class MainRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item_image = itemView.findViewById<CircleImageView>(R.id.item_image)
        var item_text = itemView.findViewById<TextView>(R.id.item_text)
        var item_price = itemView.findViewById<TextView>(R.id.item_price)
        var sort_num = itemView.findViewById<TextView>(R.id.sort_num)
        var item_text_coinName = itemView.findViewById<TextView>(R.id.item_text_coinName)
        var dailyChange = itemView.findViewById<CTextView>(R.id.dailyChange)
        var item_price_toman = itemView.findViewById<CTextView>(R.id.item_price_toman)
    }
}