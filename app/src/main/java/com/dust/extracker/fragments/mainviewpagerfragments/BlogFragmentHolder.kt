package com.dust.extracker.fragments.mainviewpagerfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.R
import com.dust.extracker.fragments.blogfragments.BlogFragment

class BlogFragmentHolder():Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blog_holder , container , false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentManager?.beginTransaction()!!.replace(R.id.news_frame_holder ,
            BlogFragment()
        )
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("fragment_main_news")
            .commit()

    }

    fun newInstance(): BlogFragmentHolder{
        val args = Bundle()

        val fragment = BlogFragmentHolder()
        fragment.arguments = args
        return fragment
    }
}