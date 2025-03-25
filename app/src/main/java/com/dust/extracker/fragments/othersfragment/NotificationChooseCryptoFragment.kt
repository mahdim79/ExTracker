package com.dust.extracker.fragments.othersfragment

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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dust.extracker.R
import com.dust.extracker.adapters.recyclerviewadapters.NotificationChooseRecyclerViewAdapter
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

class NotificationChooseCryptoFragment : Fragment(), OnGetAllCryptoList {
    private lateinit var crypto_recycler_view: RecyclerView
    private lateinit var search_pb: ProgressBar
    private lateinit var crypto_name: EditText
    private lateinit var imageView: ImageView
    private lateinit var search_nested: NestedScrollView
    lateinit var realmDB: RealmDataBaseCenter
    private lateinit var apiCenter: ApiCenter

    private var searchJob: Job? = null
    lateinit var list1: List<MainRealmObject>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification_choose_crypto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        setUpDataBase()
        setUpRecyclerView()
        setUpBackImage()
        setUpEditText()

    }

    private fun setUpEditText() {
        val l = realmDB.getAllCryptoData()
        crypto_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(1000)
                    withContext(Dispatchers.Main) {
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

    private fun doSearch(query: String) {
        if (query.trim() == "") {
            setUpRecyclerView()
            return
        }

        apiCenter.searchCrypto(query) { result ->
            setUpRecyclerView(result)
        }
    }

    private fun setUpDataBase() {
        realmDB = RealmDataBaseCenter()
        apiCenter = ApiCenter(requireContext(), this)
        list1 = realmDB.getPopularCoins()
    }

    private fun setUpViews(view: View) {
        crypto_recycler_view = view.findViewById(R.id.crypto_recycler_view)
        search_pb = view.findViewById(R.id.search_pb)
        crypto_name = view.findViewById(R.id.crypto_name)
        search_nested = view.findViewById(R.id.search_nested)
        imageView = view.findViewById(R.id.imageView)

        crypto_recycler_view.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun setUpRecyclerView(list: List<MainRealmObject>? = null) {

        val dataList = list ?: list1

        var index = 0
        index = try {
            requireArguments().getInt("INDEX")
        } catch (e: Exception) {
            0
        }

        crypto_recycler_view.adapter = NotificationChooseRecyclerViewAdapter(dataList).apply {
            setOnItemClickListener { symbol ->

                dataList.find { it.Symbol == symbol }?.let { selectedCrypto ->
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
                                if (index == 0){
                                    requireFragmentManager().beginTransaction()
                                        .replace(
                                            R.id.others_frame_holder,
                                            NotificationCustomizeFragment().newInstance(symbol)
                                        )
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                        .addToBackStack("NotificationCustomizeFragment")
                                        .commit()
                                }else{
                                    requireFragmentManager().beginTransaction()
                                        .replace(
                                            R.id.main_frame,
                                            NotificationCustomizeFragment().newInstance(symbol)
                                        )
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                        .addToBackStack("NotificationCustomizeFragment")
                                        .commit()
                                }
                            }
                        })
                }
            }
        }
    }

    private fun setUpBackImage() {
        imageView.setOnClickListener {
            fragmentManager?.popBackStack(
                "NotificationChooseCryptoFragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    fun newInstance(index: Int = 1): NotificationChooseCryptoFragment {
        val args = Bundle()
        args.putInt("INDEX", index)
        val fragment = NotificationChooseCryptoFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onGet(cryptoList: List<CryptoMainData>) {}

    override fun onGetByName(price: Double, dataNum: Int) {}

}