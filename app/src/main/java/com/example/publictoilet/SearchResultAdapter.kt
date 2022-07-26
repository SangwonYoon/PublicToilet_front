package com.example.publictoilet

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class SearchResultAdapter(val mContext: Context, val searchResultList : MutableList<Toilet>) : RecyclerView.Adapter<SearchResultAdapter.CustomViewHolder>() {

    interface OnItemClickedListener{
        fun onItemClicked(position : Int)
    }

    private val itemClickedListener = mContext as OnItemClickedListener

    class CustomViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val toiletName = itemView.findViewById<TextView>(R.id.toilet_name)
        private val distance = itemView.findViewById<TextView>(R.id.distance)

        fun bind(toilet : Toilet){
            toiletName.text = toilet.toiletName
            distance.text = toilet.distance + "m"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_result_item, parent, false)
        return CustomViewHolder(view).apply{
            itemView.setOnClickListener {
                val curPos = adapterPosition
                itemClickedListener.onItemClicked(curPos)
                /*
                val toilet = searchResultList[curPos]
                // 누르면 화면 전환
                val intent = Intent(mContext, ToiletInfoActivity::class.java)
                intent.putExtra("toiletName", toilet.toiletName)
                intent.putExtra("score_avg", toilet.score_avg)
                intent.putExtra("tel", toilet.tel)
                intent.putExtra("openTime", toilet.openTime)
                intent.putExtra("mw", toilet.mw)
                intent.putExtra("m1", toilet.m1)
                intent.putExtra("m2", toilet.m2)
                intent.putExtra("m3", toilet.m3)
                intent.putExtra("m4", toilet.m4)
                intent.putExtra("m5", toilet.m5)
                intent.putExtra("m6", toilet.m6)
                intent.putExtra("w1", toilet.w1)
                intent.putExtra("w2", toilet.w2)
                intent.putExtra("w3", toilet.w3)

                mContext.startActivity(intent)

                 */
            }
        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val toilet = searchResultList[position]
        holder.apply {
            bind(toilet)
        }
    }

    override fun getItemCount(): Int {
        return searchResultList.size
    }
}