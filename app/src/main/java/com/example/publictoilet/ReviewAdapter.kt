package com.example.publictoilet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(val mContext: Context, val reviewList : MutableList<Review>) : RecyclerView.Adapter<ReviewAdapter.CustomViewHolder>() {

    class CustomViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val review = itemView.findViewById<TextView>(R.id.review)
        private val s1 = itemView.findViewById<ImageView>(R.id.star1)
        private val s2 = itemView.findViewById<ImageView>(R.id.star2)
        private val s3 = itemView.findViewById<ImageView>(R.id.star3)
        private val s4 = itemView.findViewById<ImageView>(R.id.star4)
        private val s5 = itemView.findViewById<ImageView>(R.id.star5)

        fun bind(reviewObj : Review){
            review.text = reviewObj.comment
            initStars(reviewObj.score, mutableListOf(s1,s2,s3,s4,s5))
        }

        /**
         * 리뷰 평점 값에 맞게 별 모양을 바꿔주는 함수
         * @param score 평점 평균 값
         * @param stars 변경할 별 이미지들을 담고 있는 리스트
         */
        private fun initStars(score : String, stars : MutableList<ImageView>){
            for(i in 0 until score.toDouble().toInt()){
                stars[i].setImageResource(R.drawable.active_star_icon)
            }
            for(i in score.toDouble().toInt() until 5){
                stars[i].setImageResource(R.drawable.inactive_star_icon)
            }
            if(score.toDouble() > score.toDouble().toInt()){
                stars[score.toDouble().toInt()].setImageResource(R.drawable.half_star_icon)
            }
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