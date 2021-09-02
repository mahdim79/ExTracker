package com.dust.extracker.adapters.recyclerviewadapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.customviews.CButton
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.CommentDataClass
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentRecyclerViewAdapter(var list: List<CommentDataClass>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val NORMAL_VIEW_TYPE: Int = 0
    val REPLY_VIEW_TYPE: Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == NORMAL_VIEW_TYPE)
            return CommentViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_normal_comment, parent, false)
            )
        return CommentReplyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_reply_comment, parent, false)
        )

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CommentViewHolder){
             var Mholder: CommentViewHolder = holder as CommentViewHolder;
            Mholder.comment_name.text = list[position].userName
            Mholder.comment_content.text = list[position].commentContent
            Mholder.comment_date.text = list[position].date
            Mholder.comment_text_disslike.text = list[position].disslikesCount.toString()
            Mholder.comment_text_like.text = list[position].likesCount.toString()
            Picasso.get().load(list[position].avatarImageUrl).into(Mholder.comment_image)



        }else{
             var Mholder: CommentReplyViewHolder = holder as CommentReplyViewHolder;
            Mholder.comment_name.text = list[position].userName
            Mholder.comment_content.text = list[position].commentContent
            Mholder.comment_date.text = list[position].date
            Mholder.comment_text_disslike.text = list[position].disslikesCount.toString()
            Mholder.comment_text_like.text = list[position].likesCount.toString()
            Picasso.get().load(list[position].avatarImageUrl).into(Mholder.comment_image)


        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        if (list[position].viewtype == 0)
            return NORMAL_VIEW_TYPE
        return REPLY_VIEW_TYPE
    }

    inner class CommentViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {

        var comment_name:CTextView = itemview.findViewById(R.id.comment_name)
        var comment_content:CTextView = itemview.findViewById(R.id.comment_content)
        var comment_date:CTextView = itemview.findViewById(R.id.comment_date)
        var comment_send_reply:CButton = itemview.findViewById(R.id.comment_send_reply)
        var comment_text_disslike: TextView = itemview.findViewById(R.id.comment_text_disslike)
        var comment_text_like: TextView = itemview.findViewById(R.id.comment_text_like)
        var comment_image: CircleImageView = itemview.findViewById(R.id.comment_image)
        var comment_info: ImageView = itemview.findViewById(R.id.comment_info)
        var comment_img_disslike: ImageView = itemview.findViewById(R.id.comment_img_disslike)
        var comment_img_like: ImageView = itemview.findViewById(R.id.comment_img_like)


    }

    inner class CommentReplyViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var comment_name:CTextView = itemview.findViewById(R.id.comment_name)
        var comment_content:CTextView = itemview.findViewById(R.id.comment_content)
        var comment_date:CTextView = itemview.findViewById(R.id.comment_date)
        var comment_reply_to_text:CTextView = itemview.findViewById(R.id.comment_reply_to_text)
        var comment_send_reply:CButton = itemview.findViewById(R.id.comment_send_reply)
        var comment_text_disslike: TextView = itemview.findViewById(R.id.comment_text_disslike)
        var comment_text_like: TextView = itemview.findViewById(R.id.comment_text_like)
        var comment_image: CircleImageView = itemview.findViewById(R.id.comment_image)
        var comment_info: ImageView = itemview.findViewById(R.id.comment_info)
        var comment_img_disslike: ImageView = itemview.findViewById(R.id.comment_img_disslike)
        var comment_img_like: ImageView = itemview.findViewById(R.id.comment_img_like)
    }
}