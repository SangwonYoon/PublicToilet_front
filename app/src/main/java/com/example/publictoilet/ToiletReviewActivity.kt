package com.example.publictoilet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ToiletReviewActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    private val toiletName : TextView by lazy{
        findViewById(R.id.toilet_name)
    }

    private val s1 : ImageView by lazy{
        findViewById(R.id.star1)
    }
    private val s2 : ImageView by lazy{
        findViewById(R.id.star2)
    }
    private val s3 : ImageView by lazy{
        findViewById(R.id.star3)
    }
    private val s4 : ImageView by lazy{
        findViewById(R.id.star4)
    }
    private val s5 : ImageView by lazy{
        findViewById(R.id.star5)
    }

    private val score : TextView by lazy{
        findViewById(R.id.score)
    }

    private val toiletReviewContainer : RecyclerView by lazy{
        findViewById(R.id.toilet_review_container)
    }

    private var reviewList = mutableListOf<Review>()

    private val rs1 : ImageView by lazy{
        findViewById(R.id.rating_star1)
    }

    private val rs2 : ImageView by lazy{
        findViewById(R.id.rating_star2)
    }

    private val rs3 : ImageView by lazy{
        findViewById(R.id.rating_star3)
    }

    private val rs4 : ImageView by lazy{
        findViewById(R.id.rating_star4)
    }

    private val rs5 : ImageView by lazy{
        findViewById(R.id.rating_star5)
    }

    private val spinner : Spinner by lazy{
        findViewById(R.id.rate_spinner)
    }

    private val inputReview : EditText by lazy{
        findViewById(R.id.input_review)
    }

    private val postButton : AppCompatButton by lazy{
        findViewById(R.id.post_button)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toilet_review)

        val toiletId = initView()
        initRecyclerView()
        initSpinner()
        initInputReview()
        initPostButton(toiletId)
    }

    private fun initView() : Int{
        val intent = intent

        val id = intent.getStringExtra("id")

        val toiletNameData = intent.getStringExtra("toiletName")
        toiletName.text = toiletNameData

        val scoreAvgData = intent.getStringExtra("score_avg")
        if(scoreAvgData == "null"){
            score.text = "평점 : 0.0"
        } else{
            score.text = "평점 : $scoreAvgData"
            initStars(scoreAvgData!!, mutableListOf(s1,s2,s3,s4,s5))
        }

        getReviews(id!!.toInt())

        return id.toInt()
    }

    private fun initStars(score : String, stars : MutableList<ImageView>){
        for(i in 0 until score.toDouble().toInt()){
            stars[i].setImageResource(R.drawable.active_star_icon)
        }
        if(score.toDouble() > score.toDouble().toInt()){
            stars[score.toDouble().toInt()].setImageResource(R.drawable.half_star_icon)
        }
    }

    private fun initRecyclerView(){
        toiletReviewContainer.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        toiletReviewContainer.setHasFixedSize(true)

        toiletReviewContainer.adapter = ReviewAdapter(this, reviewList)
    }

    private fun getReviews(toiletId : Int){
        val getReviewRequest = Request.Builder().addHeader("Content-Type","application/x-www-form-urlencoded").url("http://15.165.203.167:8080/toilets/$toiletId/reviews").build()
        client.newCall(getReviewRequest).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("connection error", "인터넷 연결 불안정")
                runOnUiThread{
                    Toast.makeText(
                        this@ToiletReviewActivity,
                        "인터넷 연결이 불안정합니다. 다시 시도해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.code() == 200){
                    reviewList = mutableListOf()

                    val jsonArray = JSONArray(response.body()!!.string())

                    for(idx in 0 until jsonArray.length()){
                        val tempReview = jsonArray[idx] as JSONObject
                        val comment = tempReview.getString("comment")
                        val score = tempReview.getString("score")

                        val review = Review(comment = comment, score = score)
                        reviewList.add(review)
                    }
                    reviewList.reverse() // 최신 리뷰가 상단에 오도록 reverse
                }
            }
        })
    }

    private fun initSpinner(){
        val items = resources.getStringArray(R.array.score)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        spinner.adapter = adapter

        initStars(spinner.selectedItem.toString(), mutableListOf(rs1, rs2, rs3, rs4, rs5))
    }

    private fun initInputReview(){
        inputReview.onFocusChangeListener = object : View.OnFocusChangeListener{
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if(!hasFocus){
                    if(inputReview.text.isNotEmpty()){
                        postButton.isEnabled = true
                        postButton.background = resources.getDrawable(R.drawable.active_button_bg)
                    } else{
                        postButton.isEnabled = false
                        postButton.background = resources.getDrawable(R.drawable.inactive_button_bg)
                    }
                }
            }
        }
    }

    private fun initPostButton(toiletId: Int){
        postButton.isEnabled = false

        postButton.setOnClickListener {
            val body = FormBody.Builder()
                .add("comment", inputReview.text.toString())
                .add("score", spinner.selectedItem.toString())
                .build()

            val postReviewRequest = Request.Builder().addHeader("Content-Type","application/x-www-form-urlencoded").url("http://15.165.203.167:8080/reviews/$toiletId").post(body).build()

            client.newCall(postReviewRequest).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("connection error", "인터넷 연결 불안정")
                    runOnUiThread{
                        Toast.makeText(
                            this@ToiletReviewActivity,
                            "인터넷 연결이 불안정합니다. 다시 시도해주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if(response.code() == 200){
                        Log.d("post reivew", "success")
                        Toast.makeText(
                            this@ToiletReviewActivity,
                            "리뷰가 성공적으로 등록되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else{
                        Log.d("connection error", "review post 도중 인터넷 연결 불안정")
                        runOnUiThread{
                            Toast.makeText(
                                this@ToiletReviewActivity,
                                "인터넷 연결이 불안정합니다. 다시 시도해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
        }
    }
}