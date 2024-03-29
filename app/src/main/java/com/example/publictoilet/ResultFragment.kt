package com.example.publictoilet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ResultFragment : Fragment() {

    var searchResultList = mutableListOf<Toilet>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.result_tab, container, false)
        view.isClickable = true // FrameLayout에서 뒷 화면 터치 방지

        //TODO Recycler View adapter
        val resultContainer = view.findViewById<RecyclerView>(R.id.result_container)

        resultContainer.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        resultContainer.setHasFixedSize(true)

        resultContainer.adapter = SearchResultAdapter(requireContext(), searchResultList)

        return view
    }

    /**
     * 검색 탭에서 검색 버튼 클릭 후 검색 결과 탭의 결과를 바꿔주는 함수
     */
    fun changeResult(){
        if(arguments != null){
            searchResultList = mutableListOf()
            val numOfToilets = requireArguments().getInt("array size")
            Log.d("number", numOfToilets.toString())
            for(idx in 0 until numOfToilets){
                val id = requireArguments().getString("id_$idx")
                val toiletName = requireArguments().getString("toiletName_$idx")
                val tel = requireArguments().getString("tel_$idx")
                val openTime = requireArguments().getString("openTime_$idx")
                val mw = requireArguments().getBoolean("mw_$idx")
                val m1 = requireArguments().getString("m1_$idx")
                val m2 = requireArguments().getString("m2_$idx")
                val m3 = requireArguments().getString("m3_$idx")
                val m4 = requireArguments().getString("m4_$idx")
                val m5 = requireArguments().getString("m5_$idx")
                val m6 = requireArguments().getString("m6_$idx")
                val w1 = requireArguments().getString("w1_$idx")
                val w2 = requireArguments().getString("w2_$idx")
                val w3 = requireArguments().getString("w3_$idx")
                val distance = requireArguments().getString("distance_$idx")
                val score_avg = requireArguments().getString("score_avg_$idx")

                val toilet = Toilet(id = id!!, toiletName = toiletName!!, tel = tel!!, openTime = openTime!!, mw = mw, m1 = m1!!, m2 = m2!!, m3 = m3!!, m4 = m4!!, m5 = m5!!, m6 = m6!!, w1 = w1!!, w2 = w2!!, w3 = w3!!, distance = distance!!, score_avg = score_avg!!)
                searchResultList.add(toilet)
            }
        }
    }

    /*
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(arguments != null){
            searchResultList = mutableListOf()
            val numOfToilets = requireArguments().getInt("array size")
            Log.d("number", numOfToilets.toString())
            for(idx in 0 until numOfToilets){
                val toiletName = requireArguments().getString("toiletName_$idx")
                val tel = requireArguments().getString("tel_$idx")
                val openTime = requireArguments().getString("openTime_$idx")
                val mw = requireArguments().getBoolean("mw_$idx")
                val m1 = requireArguments().getString("m1_$idx")
                val m2 = requireArguments().getString("m2_$idx")
                val m3 = requireArguments().getString("m3_$idx")
                val m4 = requireArguments().getString("m4_$idx")
                val m5 = requireArguments().getString("m5_$idx")
                val m6 = requireArguments().getString("m6_$idx")
                val w1 = requireArguments().getString("w1_$idx")
                val w2 = requireArguments().getString("w2_$idx")
                val w3 = requireArguments().getString("w3_$idx")
                val distance = requireArguments().getString("distance_$idx")
                val score_avg = requireArguments().getString("score_avg_$idx")

                val toilet = Toilet(toiletName = toiletName!!, tel = tel!!, openTime = openTime!!, mw = mw, m1 = m1!!, m2 = m2!!, m3 = m3!!, m4 = m4!!, m5 = m5!!, m6 = m6!!, w1 = w1!!, w2 = w2!!, w3 = w3!!, distance = distance!!, score_avg = score_avg!!)
                searchResultList.add(toilet)
            }
        }
    }
     */
}