package com.dust.extracker.fragments.portfoliofragments.innerfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dust.extracker.R
import com.dust.extracker.dataclasses.HistoryDataClass
import com.dust.extracker.realmdb.HistoryObject
import com.dust.extracker.realmdb.RealmDataBaseCenter
import com.google.android.material.textfield.TextInputLayout

class EditHistoryNameFragment : Fragment() {

    private lateinit var portfolioName: TextInputLayout
    private lateinit var saveButton: Button
    private lateinit var backImg: ImageView
    private lateinit var realmDB:RealmDataBaseCenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRealmDB()
        setUpViews(view)
    }

    private fun setUpRealmDB() {
        realmDB = RealmDataBaseCenter()
    }

    private fun setUpViews(view: View) {
        portfolioName = view.findViewById(R.id.portfolioName)
        saveButton = view.findViewById(R.id.saveButton)
        backImg = view.findViewById(R.id.backImg)

        portfolioName.editText!!.setText(arguments!!.getString("NAME"))
        backImg.setOnClickListener {
            fragmentManager?.popBackStack(
                "EditHistoryNameFragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        saveButton.setOnClickListener {
            saveData()
        }
    }

    private fun saveData() {

        if (portfolioName.editText!!.text.toString() != "" && portfolioName.editText!!.text.toString() != arguments!!.getString(
                "NAME"
            )
        ) {
            var lastData:HistoryDataClass? = null
            realmDB.getAllHistoryData().forEach {
                if (it.portfolioName == arguments!!.getString("NAME"))
                    lastData = it
            }
            lastData!!.portfolioName = portfolioName.editText!!.text.toString()
            realmDB.updateHistoryData(lastData!!)
            backImg.performClick()
            return
        }

        if (portfolioName.editText!!.text.toString() == arguments!!.getString("NAME"))
            portfolioName.editText!!.error = requireActivity().resources.getString(R.string.enterNewParameter)
        else
            portfolioName.editText!!.error = requireActivity().resources.getString(R.string.requireField)
    }

    fun newInstance(name: String): EditHistoryNameFragment {
        val args = Bundle()
        args.putString("NAME", name)
        val fragment = EditHistoryNameFragment()
        fragment.arguments = args
        return fragment
    }
}