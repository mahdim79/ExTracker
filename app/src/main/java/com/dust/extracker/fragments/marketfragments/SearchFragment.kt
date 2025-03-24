package com.dust.extracker.fragments.marketfragments

import android.content.Intent
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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.SearchRecyclerViewAdapter
import com.dust.extracker.apimanager.ApiCenter
import com.dust.extracker.dataclasses.CryptoMainData
import com.dust.extracker.interfaces.OnGetAllCryptoList
import com.dust.extracker.interfaces.OnRealmDataChanged
import com.dust.extracker.realmdb.MainRealmObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment(), OnGetAllCryptoList {

    private lateinit var closeButton: ImageView
    private lateinit var etSearchQuery: EditText
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchProgressBar: ProgressBar

    private lateinit var realm: RealmDataBaseCenter

    private lateinit var initList: List<MainRealmObject>

    private lateinit var apiCenter: ApiCenter

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_crypto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObjects()
        initViews(view)
        setupViews()
    }

    private fun initObjects() {
        apiCenter = ApiCenter(requireContext(), this)
        realm = RealmDataBaseCenter()

        initList = realm.getPopularCoins()
    }

    private fun setupViews() {
        closeButton.setOnClickListener {
            requireFragmentManager().popBackStack(
                "SearchFragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        initRecyclerView()

        etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        doSearchJob(etSearchQuery.text.toString())
                    }
                }
            }

        })

    }

    private fun initRecyclerView(dataList: List<MainRealmObject>? = null) {
        try {
            val list = dataList ?: initList
            searchRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            searchRecyclerView.adapter = SearchRecyclerViewAdapter(
                "",
                false,
                requireContext(),
                list,
                object : SearchRecyclerViewAdapter.OnSelectItem {
                    override fun onSelect(
                        coinName: String,
                        IS_TRANSACTION: Boolean,
                        PortfolioName: String
                    ) {
                        list.find { it.Symbol == coinName }?.let { selectedCrypto ->
                            realm.insertSearchCryptoData(
                                CryptoMainData(
                                    selectedCrypto.ID!!,
                                    selectedCrypto.ImageUrl!!,
                                    selectedCrypto.Name!!,
                                    selectedCrypto.Symbol!!,
                                    selectedCrypto.maxSupply ?: 0.0,
                                    selectedCrypto.circulatingSupply ?: 0.0
                                ), object : OnRealmDataChanged {
                                    override fun onAddComplete() {
                                        goToCryptoDetailsFragment(coinName)
                                    }

                                })
                        }
                    }

                })
        }catch (e:Exception){}

    }

    private fun goToCryptoDetailsFragment(symbol: String) {
        val intent = Intent("com.dust.extracker.OnClickMainData")
        intent.putExtra("Symbol", symbol)
        requireContext().sendBroadcast(intent)
    }

    private fun doSearchJob(s: String) {
        if (s.trim() == "") {
            initRecyclerView()
            return
        }

        apiCenter.searchCrypto(s) { result ->
            initRecyclerView(result)
        }

    }

    private fun initViews(view: View) {
        closeButton = view.findViewById(R.id.img_search_close)
        etSearchQuery = view.findViewById(R.id.et_search_query)
        searchRecyclerView = view.findViewById(R.id.crypto_recycler_view)
        searchProgressBar = view.findViewById(R.id.search_pb)
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {}

    override fun onGetByName(price: Double, dataNum: Int) {}

}