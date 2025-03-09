package com.dust.extracker.fragments.blogfragments

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.NewsRecyclerViewAdapter
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.customviews.CTextView
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.dataclasses.NewsDataClass
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnGetNews
import com.dust.extracker.interfaces.OnNewsDataAdded
import com.dust.extracker.realmdb.NewsObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.dust.extracker.sharedpreferences.SharedPreferencesCenter

class SearchNewsFragment() : Fragment() {

    lateinit var news_recycler_view: RecyclerView
    lateinit var news_search_notation: CTextView
    lateinit var news_back_btn: ImageView
    lateinit var news_name: EditText
    private lateinit var realmDB: RealmDataBaseCenter
    private lateinit var apiService: ApiCenter
    lateinit var news_progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpBackButton()
        setUpRealmDB()
        setUpApiService()
        setUpSearchViews()

    }

    private fun setUpApiService() {
        apiService = ApiCenter(requireActivity(), object : OnGetAllCryptoList {
            override fun onGet(cryptoList: List<CryptoMainData>) {
            }

            override fun onGetByName(price: Double, dataNum: Int) {
            }

        })
    }

    private fun setUpRealmDB() {
        realmDB = RealmDataBaseCenter()

    }


    private fun setUpSearchViews() {

        news_recycler_view.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        news_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (news_name.text.toString() == "") {
                    news_progressBar.visibility = View.GONE
                    news_search_notation.visibility = View.VISIBLE
                    news_recycler_view.adapter =
                        NewsRecyclerViewAdapter(
                            arrayListOf(),
                            requireActivity().supportFragmentManager, realmDB
                        )
                } else {
                    news_progressBar.visibility = View.VISIBLE
                    news_search_notation.visibility = View.GONE

                    if (realmDB.getNewsDataCount() == 0) {
                        if (checkConnection()) {
                            apiService.getNews(object : OnGetNews {
                                override fun onGetNews(list: List<NewsDataClass>) {
                                    realmDB.insertNews(list, object : OnNewsDataAdded {
                                        override fun onNewsDataAdded() {
                                            performSearchAction()
                                        }
                                    })
                                }
                            })
                        }
                    } else {
                        performSearchAction()
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

    }

    private fun performSearchAction() {
        val allData = realmDB.getNews("ALL")
        val data = arrayListOf<NewsObject>()
        allData.forEach {
            if (it.title.indexOf(news_name.text.toString() , ignoreCase = true) != -1 || it.tags.indexOf(news_name.text.toString() , ignoreCase = true) != -1)
                data.add(it)
        }
        news_progressBar.visibility = View.GONE
        news_recycler_view.adapter =
            NewsRecyclerViewAdapter(
                data,
                requireActivity().supportFragmentManager, realmDB
            )
    }

    private fun checkConnection(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return info != null && info.isConnectedOrConnecting
    }

    private fun setUpBackButton() {

        news_back_btn.setOnClickListener {
            fragmentManager?.popBackStack("fragment_search_news", 1)
        }
    }

    private fun setUpViews(view: View) {
        news_recycler_view = view.findViewById(R.id.news_recycler_view)
        news_search_notation = view.findViewById(R.id.news_search_notation)
        news_back_btn = view.findViewById(R.id.news_back_btn)
        news_name = view.findViewById(R.id.news_name)
        news_progressBar = view.findViewById(R.id.news_progressBar)

    }

    fun newInstance(): SearchNewsFragment {
        val args = Bundle()
        val fragment = SearchNewsFragment()
        fragment.arguments = args
        return fragment
    }
}