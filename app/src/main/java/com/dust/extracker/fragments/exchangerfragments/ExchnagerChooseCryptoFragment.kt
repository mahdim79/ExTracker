package com.dust.extracker.fragments.exchangerfragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.ExchangerRecyclerViewAdapter
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import io.realm.RealmResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExchnagerChooseCryptoFragment(
    var positionMain: Int,
    var CR1: String = "BTC",
    var CR2: String = "ETH"
) : Fragment() {

    private lateinit var crypto_recycler_view: RecyclerView
    private lateinit var search_pb: ProgressBar
    private lateinit var crypto_name: EditText
    private lateinit var search_nested: NestedScrollView
    lateinit var realmDB: RealmDataBaseCenter
    private var datalist = arrayListOf<MainRealmObject>()

    var MODE: Int = 0
    private lateinit var tempList: List<MainRealmObject>
    lateinit var list1: List<MainRealmObject>

    var PaginationCount: Int = 1

    var searchJob:Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_ctypto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpDataBase()
        setUpRecyclerView()
        setUpBackImage(view)
        setUpPagination()
        setUpEditText()
    }

    private fun setUpEditText() {
        val l = realmDB.getAllCryptoData()
        crypto_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(1000)
                    doSearch(l)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

    }

    private fun doSearch(l: RealmResults<MainRealmObject>){
        if (crypto_name.text.toString() == "") {
            MODE = 0
            PaginationCount = 1
            setUpRecyclerView(PaginationCount)
            return
        }
        MODE = 1
        val listTemp = arrayListOf<MainRealmObject>()
        for (i in 0 until l.size){
            if (l[i]!!.FullName!!.indexOf(crypto_name.text.toString() , ignoreCase = true) != -1)
                listTemp.add(l[i]!!)
        }
        tempList = listTemp
        setUpRecyclerView(PaginationCount)
    }

    private fun setUpDataBase() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setUpViews(view: View) {
        crypto_recycler_view = view.findViewById(R.id.crypto_recycler_view)
        search_pb = view.findViewById(R.id.search_pb)
        crypto_name = view.findViewById(R.id.crypto_name)
        search_nested = view.findViewById(R.id.search_nested)

        crypto_recycler_view.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
    }

    private fun setUpPagination() {
        search_nested.setOnScrollChangeListener { v: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY == v!!.getChildAt(0).measuredHeight - v.measuredHeight) {
                PaginationCount++
                setUpRecyclerView(PaginationCount)
            }
        }
    }

    private fun setUpRecyclerView(PaginationCount: Int = 1) {

        if (MODE == 0) {
            list1 = realmDB.getCryptoData(PaginationCount)
            if (PaginationCount == 1) {
                datalist.clear()
            }
            datalist.addAll(list1)
        } else {
            if (PaginationCount == 1)
                datalist.clear()

            val start = 50 * (PaginationCount - 1)
            val stop = 50 * PaginationCount
            try {

                for (i in start.rangeTo(stop)) {
                    datalist.add(tempList[i])
                }

            } catch (e: Exception) {
            }
        }

        crypto_recycler_view.adapter =
            ExchangerRecyclerViewAdapter(
                positionMain,
                requireActivity(),
                datalist,
                requireFragmentManager()
            )
    }

    private fun setUpBackImage(view: View) {

        var imageView: ImageView = view.findViewById(R.id.imageView)
        imageView.setOnClickListener {
            requireFragmentManager().popBackStack("ExchnagerChooseCryptoFragment", 1)
        }
    }



}