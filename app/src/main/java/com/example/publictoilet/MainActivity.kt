package com.example.publictoilet

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class MainActivity : AppCompatActivity() {

    private val mapContainer : ViewGroup by lazy{
        findViewById(R.id.map_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapView = MapView(this)

        val mapViewContainer = mapContainer
        mapViewContainer.addView(mapView)

        val myHome = makeMarker(37.6106656, 127.0064049, 0)

        mapView.addPOIItem(myHome)
    }

    /**
     * 마커 객체를 만들어 리턴해주는 함수
     * @param latitude 마커의 경도
     * @param longitude 마커의 위도
     * @param tagNo 마커의 식별 번호
     * @param tagName 마커의 이름
     */
    private fun makeMarker(latitude : Double, longitude : Double, tagNo : Int, tagName : String) : MapPOIItem{
        val marker = MapPOIItem()
        marker.itemName = tagName
        marker.tag = tagNo
        marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
        marker.markerType = MapPOIItem.MarkerType.BluePin
        marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin

        return marker
    }
}