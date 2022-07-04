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

class SearchToiletFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_toilet_tab, container, false)
        val spinner = view.findViewById<Spinner>(R.id.spinner)
        val searchButton = view.findViewById<Button>(R.id.search_button)

        val items = resources.getStringArray(R.array.range)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)

        spinner.adapter = adapter

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