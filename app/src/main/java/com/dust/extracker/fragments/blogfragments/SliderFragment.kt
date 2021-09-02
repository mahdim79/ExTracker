package com.dust.extracker.fragments.blogfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dust.extracker.R
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.SliderDataClass
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.squareup.picasso.Picasso

class SliderFragment(var list: List<SliderDataClass> , var position:Int):Fragment() ,View.OnClickListener {
    lateinit var imageView: ImageView
    lateinit var desc:CTextView
    lateinit var date:CTextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_slider , container , false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpSliderView()

    }

    private fun setUpSliderView() {
        // setUpImage
        Picasso.get().load(list[position].url)
            .into(imageView)

        // setUpDescription
        desc.text = list[position].description

        // setUpDate
        date.text = list[position].date

        // setSliderOnClickListener
        imageView.setOnClickListener(this)
        desc.setOnClickListener(this)
        date.setOnClickListener(this)

    }

    private fun setUpViews(view: View) {
        imageView = view.findViewById(R.id.img_slider)
        desc = view.findViewById(R.id.slider_description)
        date = view.findViewById(R.id.slider_date)
    }

    override fun onClick(p0: View?) {
        val data = RealmDataBaseCenter().getNewsDataById(list[position].id)

        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.news_frame_holder, ReadNewsFragment(data))
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("ReadNewsFragment")
            .commit()
    }

}