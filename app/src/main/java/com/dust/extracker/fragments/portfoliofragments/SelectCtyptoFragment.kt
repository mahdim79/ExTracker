package com.dust.extracker.fragments.portfoliofragments

import android.content.Context
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
import androidx.fragment.app.FragmentTransaction
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

class SelectCtyptoFragment : Fragment(),OnGetAllCryptoList {
    private lateinit var crypto_recycler_view: RecyclerView
    private lateinit var search_pb: ProgressBar
    private lateinit var crypto_name: EditText
    private lateinit var search_nested: NestedScrollView
    lateinit var realmDB: RealmDataBaseCenter
    private lateinit var apiCenter:ApiCenter

    private var searchJob:Job? = null
    lateinit var list1: List<MainRealmObject>

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
        setUpEditText()

    }

    private fun doSearch(query:String){
        if (query.trim() == "") {
            setUpRecyclerView()
            return
        }

        apiCenter.searchCrypto(query) { result ->
            setUpRecyclerView(result)
        }
    }

    private fun setUpEditText() {
        crypto_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(1000)
                    withContext(Dispatchers.Main){
                        doSearch(crypto_name.text.toString())
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

    }

    private fun setUpDataBase() {
        realmDB = RealmDataBaseCenter()
        apiCenter = ApiCenter(requireContext(),this)
        list1 = realmDB.getPopularCoins()
    }

    private fun setUpViews(view: View) {
        crypto_recycler_view = view.findViewById(R.id.crypto_recycler_view)
        search_pb = view.findViewById(R.id.search_pb)
        crypto_name = view.findViewById(R.id.crypto_name)
        search_nested = view.findViewById(R.id.search_nested)

        crypto_recycler_view.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
    }

    private fun setUpRecyclerView(list:List<MainRealmObject>? = null) {

        val dataList = list ?: list1

        var bool = false
        var PortfolioName = "SecKey=sdffgvbnmsdfghjkrtyuio"
        if (arguments != null && requireArguments().containsKey("IS_TRANSACTION")){
            PortfolioName = requireArguments().getString("PortfolioName")!!
            bool = requireArguments().getBoolean("IS_TRANSACTION")
        }

        val adapter =
            SearchRecyclerViewAdapter(
                PortfolioName,
                bool,
                requireContext(),
                dataList,
                object :
                    SearchRecyclerViewAdapter.OnSelectItem {
                    override fun onSelect(
                        coinName: String,
                        IS_TRANSACTION: Boolean,
                        PortfolioName: String
                    ) {

                        dataList.find { it.Symbol == coinName }?.let { selectedCrypto ->
                            realmDB.insertSearchCryptoData(
                                CryptoMainData(
                                    selectedCrypto.ID!!,
                                    selectedCrypto.ImageUrl!!,
                                    selectedCrypto.Name!!,
                                    selectedCrypto.Symbol!!,
                                    selectedCrypto.maxSupply ?: 0.0,
                                    selectedCrypto.circulatingSupply ?: 0.0
                                ), object : OnRealmDataChanged {
                                    override fun onAddComplete() {

                                        fragmentManager?.beginTransaction()!!
                                            .replace(
                                                R.id.frame_holder,
                                                InputDataFragment().newInstance(
                                                    coinName,
                                                    IS_TRANSACTION,
                                                    PortfolioName
                                                )
                                            )
                                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                            .addToBackStack("InputDataFragment")
                                            .commit()

                                    }
                                })
                        }
                    }
                })
        crypto_recycler_view.adapter = adapter
    }
    private fun setUpBackImage(view: View) {

        val imageView:ImageView = view.findViewById(R.id.imageView)
        imageView.setOnClickListener {
            fragmentManager?.popBackStack("SelectCtyptoFragment" , 1)
        }
    }

    fun newInstance(IS_TRANSACTION:Boolean , PortfolioName:String): SelectCtyptoFragment {
        val args = Bundle()
        args.putBoolean("IS_TRANSACTION" , IS_TRANSACTION)
        args.putString("PortfolioName" , PortfolioName)
        val fragment =
            SelectCtyptoFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {}

    override fun onGetByName(price: Double, dataNum: Int) {}
}