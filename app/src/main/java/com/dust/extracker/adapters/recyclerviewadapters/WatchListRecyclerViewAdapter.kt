package com.dust.extracker.adapters.recyclerviewadapters

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.utils.Utils
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.internal.Util
import java.util.Locale
import kotlin.Exception

class WatchListRecyclerViewAdapter(
    var list: List<MainRealmObject>,
    var context: Context,
    var alphaAnimation: AlphaAnimation,
    var dollarPrice: Double
) :
    RecyclerView.Adapter<WatchListRecyclerViewAdapter.MainRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainRecyclerViewHolder {
        return MainRecyclerViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.market_recycler_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MainRecyclerViewHolder, position: Int) {
        Picasso.get().load(list[position].ImageUrl)
            .into(holder.item_image)
        holder.item_text.text = list[position].Name
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
            if (rawData.indexOf(list[position].ID!!) != -1) {
                resultList.remove(list[position].ID!!)
                text = context.resources.getString(R.string.deletewatch)
                color = ResourcesCompat.getDrawable(
                    context.resources!!,
                    R.drawable.dialog_remove_confirm_btn,
                    null
                )
            } else {
                resultList.add(list[position].ID!!)
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
            val decimal = calculateDecimalByPrice(list[position].LastPrice!!)
            if (list[position].LastPrice!! == 0.0)
                holder.item_price.text = ""
            else
                holder.item_price.text = "$${Utils.formatPriceNumber(list[position].LastPrice!!,decimal,
                    Locale.ENGLISH)}"
            holder.item_price_toman.text = "${Utils.formatPriceNumber((list[position].LastPrice!! * dollarPrice),decimal)} ${context.resources.getString(R.string.toman)}"

        } catch (e: Exception) {
            holder.item_price.text = ""
        }
        holder.item_price.startAnimation(alphaAnimation)

      //  holder.sort_num.text = list[position].SortOrder

        holder.item_text_coinName.text = list[position].Symbol

        holder.dailyChange.text = "${list[position].DailyChangePCT.toString()}%"

        if (list[position].DailyChangePCT != null) {
            if (list[position].DailyChangePCT!! > 0)
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
            intent.putExtra("Symbol", list[position].Symbol)
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
                            holder.dailyChange.text = "${bundle.getDouble(list[position].Symbol)}%"

                            holder.dailyChange.background = ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.portfolio_linear_shape_green,
                                null
                            )

                        } else {
                            holder.dailyChange.text = "${bundle.getDouble(list[position].Symbol)}%"

                            holder.dailyChange.background = ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.portfolio_linear_shape_red,
                                null
                            )
                        }
                        holder.dailyChange.startAnimation(alphaAnimation)

                    } else {
                        if (!bundle.isEmpty && bundle.containsKey(list[position].Symbol)) {
                            var price = 0.0
                            try {
                                price =
                                    holder.item_price.text.toString().replace("$", "").replace(",","").toDouble()
                            } catch (e: Exception) {
                                price = 0.0
                            }
                            val bundlePrice = bundle.getDouble(list[position].Symbol)
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
                            val tomanPrice = ((holder.item_price.text.toString().replace("$", "").replace(",","").toDouble()) * (p1!!.extras!!.getString("PRICE")!!.toDouble()))
                            val decimal = calculateDecimalByPrice(tomanPrice)
                            holder.item_price_toman.text = "${Utils.formatPriceNumber(tomanPrice,decimal, Locale.ENGLISH)} ${context.resources.getString(R.string.toman)}"

                            holder.item_price.startAnimation(alphaAnimation)
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            context.registerReceiver(
                UpdatePrices(),
                IntentFilter("com.dust.extracker.UPDATE_ITEMS_WatchList"),
                Context.RECEIVER_EXPORTED
            )
        }else{
            context.registerReceiver(
                UpdatePrices(),
                IntentFilter("com.dust.extracker.UPDATE_ITEMS_WatchList")
            )
        }

        class UpdateTomanData : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val tomanPrice = ((holder.item_price.text.toString().replace("$", "").toDouble()) * (p1!!.extras!!.getString("PRICE")!!.toDouble()))
                val decimal = calculateDecimalByPrice(tomanPrice)
                holder.item_price_toman.text = "${Utils.formatPriceNumber(tomanPrice,decimal)} ${context.resources.getString(R.string.toman)}"

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

    override fun getItemCount(): Int = list.size

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