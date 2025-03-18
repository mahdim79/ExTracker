package com.dust.extracker.adapters.recyclerviewadapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.HistoryDataClass
import com.dust.extracker.interfaces.OnTransactionRemoveListener
import com.dust.extracker.realmdb.DollarObject
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.utils.Utils
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.internal.Util
import java.util.*

class PortfolioCoinRecyclerViewAdapter(
    var dollarObject: DollarObject,
    var list: List<MainRealmObject>,
    var historyDataClass: HistoryDataClass,
    var context: Context,
    var onTransactionRemoveListener: OnTransactionRemoveListener
) : RecyclerView.Adapter<PortfolioCoinRecyclerViewAdapter.MainViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_portfolio_coin_recyclerview, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return historyDataClass.transactionList.size
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        Picasso.get().load("${list[position].BaseImageUrl}${list[position].ImageUrl}")
            .error(R.drawable.ic_baseline_error_24).into(holder.coin_img)
        holder.coin_amount.text = historyDataClass.transactionList[position].dealAmount.toString()
        holder.coinName.text = historyDataClass.transactionList[position].coinName
        holder.dollarAggregation.text = "${Utils.formatPriceNumber(calculateDollarCapital(position),4)} ~ ${Utils.formatPriceNumber((historyDataClass.transactionList[position].currentPrice),6)}"
        holder.tomanAggregation.text = Utils.formatPriceNumber((calculateDollarCapital(position) * dollarObject.price.toDouble()),2)
        val dollarResult = calculateDollarChange(position)
        holder.portfolioChangeDollar.text = dollarResult.first
        holder.portfolioChangeDollarPct.text = dollarResult.second

        val tomanData = calculateTomanData(position)

        var tomanChange = ""
        var tomanChangePct = ""
        if (tomanData.first.toDouble() > 0) {
            tomanChange = "+${Utils.formatPriceNumber(tomanData.first.toDouble(),2)}"
            tomanChangePct = "+${Utils.formatPriceNumber(tomanData.second.toDouble(),2)}"
        } else {
            tomanChange = "${Utils.formatPriceNumber(tomanData.first.toDouble(),2)}"
            tomanChangePct = "-${Utils.formatPriceNumber(tomanData.second.toDouble(),2)}"
        }
        holder.portfolioChangeToman.text = tomanChange
        holder.portfolioChangeTomanPct.text = tomanChangePct

        if (tomanData.first.toDouble() > 0) {
            holder.tomanChangeLinear.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.portfolio_linear_shape_green,
                null
            )
        } else {
            holder.tomanChangeLinear.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.portfolio_linear_shape_red,
                null
            )
        }

        if (dollarResult.first.contains("+")) {
            holder.dollarChangeLinear.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.portfolio_linear_shape_green,
                null
            )
        } else {
            holder.dollarChangeLinear.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.portfolio_linear_shape_red,
                null
            )
        }

        holder.itemView.setOnLongClickListener {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_remove_transaction)
            dialog.setCancelable(false)
            dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }
            dialog.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
                onTransactionRemoveListener.onRemove(historyDataClass.transactionList[position].id)
                dialog.dismiss()
            }
            dialog.show()
            true
        }

        holder.itemView.setOnClickListener {
            val intent = Intent("com.dust.extracker.OnClickTransactionData")
            intent.putExtra("COIN_NAME", historyDataClass.transactionList[position].coinName)
            context.sendBroadcast(intent)
        }
    }

    private fun calculateDollarChange(position: Int): Pair<String, String> {
        val currentRate = calculateDollarCapital(position)

        val lastRate =
            historyDataClass.transactionList[position].dealAmount * historyDataClass.transactionList[position].dealOpenPrice

        val diffrence = currentRate - lastRate

        var first = ""
        var second = ""
        if (diffrence > 0) {
            second = "+${Utils.formatPriceNumber(((diffrence) / lastRate) * 100,2)}"
            first = "+${Utils.formatPriceNumber(diffrence,2)}"
        } else {
            second = "-${Utils.formatPriceNumber(((lastRate - currentRate) / lastRate) * 100,2)}"
            first = Utils.formatPriceNumber(diffrence,2)
        }
        return Pair(first, second)
    }

    private fun calculateTomanData(position: Int): Pair<String, String> {
        val lastRate =
            historyDataClass.transactionList[position].dealAmount * historyDataClass.transactionList[position].dealOpenPrice * historyDataClass.transactionList[position].dollarPrice
        val currentRate =
            calculateDollarCapital(position) * dollarObject.price.toDouble()

        val changePct = if (currentRate > lastRate)
            ((currentRate - lastRate) / lastRate) * 100
        else
            ((lastRate - currentRate) / lastRate) * 100

        // first is toman change
        // second is toman Pct
        return Pair(
            "${String.format(Locale.ENGLISH , "%.2f", (currentRate - lastRate))}",
            "${String.format(Locale.ENGLISH ,"%.2f", changePct)}"
        )
    }

    private fun calculateDollarCapital(position: Int): Double {
        var result = 0.toDouble()
        if (historyDataClass.transactionList[position].dealType == "SELL") {
            if (historyDataClass.transactionList[position].currentPrice > historyDataClass.transactionList[position].dealOpenPrice) {
                val percentage =
                    ((historyDataClass.transactionList[position].currentPrice - historyDataClass.transactionList[position].dealOpenPrice) / historyDataClass.transactionList[position].dealOpenPrice)
                if (percentage <= 1)
                    result =
                        (historyDataClass.transactionList[position].dealOpenPrice * historyDataClass.transactionList[position].dealAmount) - (historyDataClass.transactionList[position].dealOpenPrice * historyDataClass.transactionList[position].dealAmount * percentage)
            } else if (historyDataClass.transactionList[position].currentPrice < historyDataClass.transactionList[position].dealOpenPrice) {
                val percentage =
                    ((historyDataClass.transactionList[position].dealOpenPrice - historyDataClass.transactionList[position].currentPrice) / historyDataClass.transactionList[position].dealOpenPrice)
                result =
                    ((historyDataClass.transactionList[position].dealOpenPrice * historyDataClass.transactionList[position].dealAmount) + (historyDataClass.transactionList[position].dealOpenPrice * historyDataClass.transactionList[position].dealAmount * percentage))
            } else {
                result =
                    (historyDataClass.transactionList[position].dealAmount * historyDataClass.transactionList[position].currentPrice)
            }
        } else {
            result =
                (historyDataClass.transactionList[position].dealAmount * historyDataClass.transactionList[position].currentPrice)
        }
        return result
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coin_img = itemView.findViewById<CircleImageView>(R.id.coin_img)
        val dollarChangeLinear = itemView.findViewById<LinearLayout>(R.id.dollarChangeLinear)
        val tomanChangeLinear = itemView.findViewById<LinearLayout>(R.id.tomanChangeLinear)
        val coin_amount = itemView.findViewById<TextView>(R.id.coin_amount)
        val coinName = itemView.findViewById<TextView>(R.id.coinName)
        val dollarAggregation = itemView.findViewById<CTextView>(R.id.dollarAggregation)
        val tomanAggregation = itemView.findViewById<CTextView>(R.id.tomanAggregation)
        val portfolioChangeToman = itemView.findViewById<CTextView>(R.id.portfolioChangeToman)
        val portfolioChangeDollar = itemView.findViewById<CTextView>(R.id.portfolioChangeDollar)
        val portfolioChangeDollarPct =
            itemView.findViewById<CTextView>(R.id.portfolioChangeDollarPct)
        val portfolioChangeTomanPct = itemView.findViewById<CTextView>(R.id.portfolioChangeTomanPct)
    }
}