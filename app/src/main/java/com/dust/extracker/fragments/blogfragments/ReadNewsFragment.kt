package com.dust.extracker.fragments.blogfragments

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.CommentRecyclerViewAdapter
import com.dust.extracker.customviews.CButton
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.CommentDataClass
import com.dust.extracker.dataclasses.NewsDataClass
import com.dust.extracker.realmdb.NewsObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter
import com.squareup.picasso.Picasso

class ReadNewsFragment(var newsData: NewsDataClass) : Fragment() {
    lateinit var new_img: ImageView
    lateinit var news_like: ImageView
    lateinit var add_to_bookmark: ImageView
    lateinit var image_back: ImageView
    lateinit var news_content: CTextView
    lateinit var news_title: CTextView
    lateinit var news_view_count: TextView
    lateinit var news_tag: TextView
    lateinit var news_main_source: TextView
    lateinit var send_comment: CButton
    lateinit var comment_progressBar: ProgressBar
    lateinit var comment_recyclerview: RecyclerView
    lateinit var read_news_nestedScrollView: NestedScrollView
    private lateinit var realm: RealmDataBaseCenter

    private var isLiked = false

    private lateinit var shared: SharedPreferencesCenter
    var COMMENT_PAGINATION_COUNTER = 1

    var commentList = arrayListOf<CommentDataClass>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_read_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpRealm()
        setUpSharedPreferencesService()
        setUpBackButton()
        setNew()
        setUpComments()

    }

    private fun setUpRealm() {
        realm = RealmDataBaseCenter()
    }

    private fun setUpSharedPreferencesService() {
        shared = SharedPreferencesCenter(requireContext())
    }

    private fun setUpComments() {
        comment_recyclerview.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        // TODO: 5/30/2021 get comments from server
        doApiCallAndSync(COMMENT_PAGINATION_COUNTER)

        // set up pagination
        read_news_nestedScrollView.setOnScrollChangeListener { v: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY == v!!.getChildAt(0).measuredHeight - v.measuredHeight) {

                comment_progressBar.visibility = View.VISIBLE
                COMMENT_PAGINATION_COUNTER++
                doApiCallAndSync(COMMENT_PAGINATION_COUNTER)

            }
        }
    }

    private fun doApiCallAndSync(commentPaginationCounter: Int) {
        // TODO: 5/30/2021 call api manager in order to get data
        // TODO: 5/30/2021 below must be in interface
        Handler().postDelayed({
            commentList.addAll(fakeServer(commentPaginationCounter))
            comment_recyclerview.adapter =
                CommentRecyclerViewAdapter(
                    commentList
                )
            comment_progressBar.visibility = View.INVISIBLE
        }, 2000)

    }

    private fun setNew() {
        news_title.text = newsData.title
        isLiked = newsData.is_liked
        setIsLikedOnUi()
        news_content.text = newsData.description
        Picasso.get().load(newsData.imageUrl).into(new_img)
        send_comment.setTextColor(Color.BLACK)
        news_view_count.text =
            requireContext().resources.getString(R.string.view_count_news, newsData.likeCount, newsData.seenCount)
        news_tag.text = "#${newsData.tags.replace(" ", "").replace("|", " #")}"
        news_main_source.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newsData.url))
            requireActivity().startActivity(Intent.createChooser(intent, requireActivity().resources.getString(R.string.openUrlWith)))
        }

        val availability = realm.checkBookmarkAvailability(newsData.url)
        if (availability)
            add_to_bookmark.setImageResource(R.drawable.ic_baseline_bookmarked)

        add_to_bookmark.setOnClickListener {
            val availability2 = realm.checkBookmarkAvailability(newsData.url)
            if (availability2) {
                realm.removeBookmark(newsData.url)
                add_to_bookmark.setImageResource(R.drawable.ic_baseline_bookmark)
            } else {
                realm.insertBookmark(newsData)
                add_to_bookmark.setImageResource(R.drawable.ic_baseline_bookmarked)
            }
        }

        news_like.setOnClickListener {
            newsData.ID.let { nId ->
                isLiked = !isLiked
                setIsLikedOnUi()
                realm.setNewsIsLiked(nId,isLiked)
            }
        }
    }

    private fun setIsLikedOnUi(){
        if (isLiked){
            news_like.setImageResource(R.drawable.ic_baseline_news_liked)
            news_like.imageTintList = ColorStateList.valueOf(Color.RED)
        }else{
            news_like.setImageResource(R.drawable.ic_baseline_news_unliked)
            news_like.imageTintList = ColorStateList.valueOf(if(shared.getNightMode()) Color.WHITE else Color.BLACK)
        }
    }

    private fun setUpBackButton() {

        image_back.setOnClickListener {
            fragmentManager?.popBackStack(
                "ReadNewsFragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    private fun setUpViews(view: View) {
        new_img = view.findViewById(R.id.new_img)
        news_like = view.findViewById(R.id.news_like)
        news_view_count = view.findViewById(R.id.news_view_count)
        add_to_bookmark = view.findViewById(R.id.add_to_bookmark)
        news_main_source = view.findViewById(R.id.news_main_source)
        news_tag = view.findViewById(R.id.news_tag)
        image_back = view.findViewById(R.id.image_back)
        news_content = view.findViewById(R.id.news_content)
        news_title = view.findViewById(R.id.news_title)
        comment_recyclerview = view.findViewById(R.id.comment_recyclerview)
        send_comment = view.findViewById(R.id.comment_send_reply)
        comment_progressBar = view.findViewById(R.id.comment_progressBar)
        read_news_nestedScrollView = view.findViewById(R.id.read_news_nestedScrollView)
    }

    fun fakeServer(commentPaginationCounter: Int): List<CommentDataClass> {

        var list = arrayListOf<CommentDataClass>()

        for (i in 0..10) {
            var randViewType = if (Math.random() > 0.7) 1 else 0
            list.add(
                CommentDataClass(
                    i,
                    randViewType,
                    "user $i",
                    "comment line content $i",
                    "date $i",
                    "https://images.all-free-download.com/images/graphiclarge/autumn_background_208731.jpg",
                    i + 1,
                    i + 2
                )
            )
        }

        return list
    }

}