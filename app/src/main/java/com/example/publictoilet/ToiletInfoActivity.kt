package com.example.publictoilet

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import org.w3c.dom.Text
import kotlin.math.floor

class ToiletInfoActivity : AppCompatActivity() {

    private val toiletName : TextView by lazy{
        findViewById(R.id.toilet_name)
    }

    private val s1 :ImageView by lazy{
        findViewById(R.id.star1)
    }

    private val s2 :ImageView by lazy{
        findViewById(R.id.star2)
    }

    private val s3 :ImageView by lazy{
        findViewById(R.id.star3)
    }

    private val s4 :ImageView by lazy{
        findViewById(R.id.star4)
    }

    private val s5 :ImageView by lazy{
        findViewById(R.id.star5)
    }

    private val stars : MutableList<ImageView> by lazy{
        mutableListOf(s1,s2,s3,s4,s5)
    }

    private val score : TextView by lazy{
        findViewById(R.id.score)
    }

    private val commentButton : AppCompatButton by lazy{
        findViewById(R.id.comment_button)
    }

    private val openTime : TextView by lazy{
        findViewById(R.id.toilet_opentime)
    }

    private val mw : TextView by lazy{
        findViewById(R.id.toilet_mw)
    }

    private val m1 : TextView by lazy{
        findViewById(R.id.toilet_m1)
    }
    private val m2 : TextView by lazy{
        findViewById(R.id.toilet_m2)
    }
    private val m3 : TextView by lazy{
        findViewById(R.id.toilet_m3)
    }
    private val m4 : TextView by lazy{
        findViewById(R.id.toilet_m4)
    }
    private val m5 : TextView by lazy{
        findViewById(R.id.toilet_m5)
    }
    private val m6 : TextView by lazy{
        findViewById(R.id.toilet_m6)
    }
    private val w1 : TextView by lazy{
        findViewById(R.id.toilet_w1)
    }
    private val w2 : TextView by lazy{
        findViewById(R.id.toilet_w2)
    }
    private val w3 : TextView by lazy{
        findViewById(R.id.toilet_w3)
    }

    private val tel : TextView by lazy{
        findViewById(R.id.toilet_tel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toilet_info)

        initView()
    }

    /**
     * Intent로 넘겨받은 데이터를 바탕으로 뷰를 초기화하는 함수
     */
    private fun initView(){
        val intent = intent

        val id = intent.getStringExtra("id")

        val toiletNameData = intent.getStringExtra("toiletName")
        toiletName.text = toiletNameData

        val scoreAvgData = intent.getStringExtra("score_avg")
        if(scoreAvgData == "null"){
            score.text = "평점 : 0.0"
            commentButton.text = "첫 코멘트 작성하기"
        } else {
            score.text = "평점 : $scoreAvgData"
            initStars(scoreAvgData!!)
        }

        commentButton.setOnClickListener{
            val intent = Intent(this, ToiletReviewActivity::class.java)
            // 인텐트에 정보 실어서 넘겨주기 (id, toiletName, score_avg)
            intent.putExtra("id", id)
            intent.putExtra("toiletName", toiletNameData)
            intent.putExtra("score_avg", scoreAvgData)
            startActivity(intent)
        }

        val openTimeData = intent.getStringExtra("openTime")
        if(openTimeData == "null"){
            openTime.text = "개방시간 : 확인할 수 없음"
        } else {
            openTime.text = "개방시간 : $openTimeData"
        }

        val mwData = intent.getBooleanExtra("mw", false)
        if(mwData){
            mw.text = "남녀 공용 화장실 여부 : 공용"
        } else{
            mw.text = "남녀 공용 화장실 여부 : 공용 아님"
        }

        val m1Data = intent.getStringExtra("m1")
        m1.text = "남성용 대변기 수 : $m1Data"

        val m2Data = intent.getStringExtra("m2")
        m2.text = "남성용 소변기 수 : $m2Data"

        val m3Data = intent.getStringExtra("m3")
        m3.text = "남성 장애인용 대변기 수 : $m3Data"

        val m4Data = intent.getStringExtra("m4")
        m4.text = "남성 장애인용 소변기 수 : $m4Data"

        val m5Data = intent.getStringExtra("m5")
        m5.text = "남성 어린이용 대변기 수 : $m5Data"

        val m6Data = intent.getStringExtra("m6")
        m6.text = "남성 어린이용 소변기 수 : $m6Data"

        val w1Data = intent.getStringExtra("w1")
        w1.text = "여성용 대변기 수 : $w1Data"

        val w2Data = intent.getStringExtra("w2")
        w2.text = "여성 장애인용 대변기 수 : $w2Data"

        val w3Data = intent.getStringExtra("w3")
        w3.text = "여성 어린이용 대변기 수 : $w3Data"

        val telData = intent.getStringExtra("tel")
        tel.isVisible = (telData != "null")
        tel.text = "전화번호 : $telData"
    }

    private fun initStars(score : String){
        for(i in 0 until score.toDouble().toInt()){
            stars[i].setImageResource(R.drawable.active_star_icon)
        }
        if(score.toDouble() > score.toDouble().toInt()){
            stars[score.toDouble().toInt()].setImageResource(R.drawable.half_star_icon)
        }
    }
}