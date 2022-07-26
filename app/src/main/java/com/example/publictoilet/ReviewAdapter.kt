package com.example.publictoilet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(val mContext: Context, val reviewList : MutableList<Review>) : RecyclerView.Adapter<ReviewAdapter.CustomViewHolder>() {

    class CustomViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val review = itemView.findViewById<TextView>(R.id.review)
        private val rate = itemView.findViewById<TextView>(R.id.rate)

        fun bind(reviewObj : Review){
            review.text = reviewObj.comment
            rate.text = reviewObj.score
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val review = reviewList[position]
        holder.apply{
            bind(review)
        }
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
}