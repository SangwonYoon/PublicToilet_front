package com.example.publictoilet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment

class SearchToiletFragment() : Fragment() {

    interface OnDataPassListener{
        fun onDataPass(range: Int)
    }

    private lateinit var dataPassListener: OnDataPassListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassListener = context as OnDataPassListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_toilet_tab, container, false)
        view.isClickable = true // FrameLayout에서 뒷 화면 터치 방지
        val spinner = view.findViewById<Spinner>(R.id.spinner)
        val searchButton = view.findViewById<Button>(R.id.search_button)

        val items = resources.getStringArray(R.array.range)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos){
                    0 -> dataPassListener.onDataPass(300)
                    1 -> dataPassListener.onDataPass(500)
                    2 -> dataPassListener.onDataPass(1000)
                    3 -> dataPassListener.onDataPass(3000)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //
            }
        }

        searchButton.setOnClickListener {
            val range = when(spinner.selectedItem.toString()){
                "300m" -> 300
                "500m" -> 500
                "1km" -> 1000
                else -> 3000
            }

            //TODO 화장실 검색 API 호출
        }

        return view
    }
}