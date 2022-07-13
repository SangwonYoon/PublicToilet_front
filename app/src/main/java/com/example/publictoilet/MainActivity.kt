package com.example.publictoilet

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import net.daum.mf.map.api.MapCircle
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

// 공중화장실 찾기 / 검색 결과 -> 2개의 탭을 가진 sliding drawer 구현
// TODO sliding drawer 높이 조절
// 공중화장실 찾기 탭에서는 검색 범위 (Spinner로 구현) 설정 후 검색할 수 있게 구현
// TODO BottomSheetFragment 올라오면 지도는 중심 위치 유지하면서 작아지게 구현
// TODO 검색하면 지도에 검색 반경 및 검색 반경 내 공중 화장실 표시
// TODO 검색 결과 탭을 누르면 RecyclerView에 가까운 거리 순으로 공중화장실 정렬
// TODO RecyclerView의 item 클릭 시 지도에서는 화장실 위치 표시, BottomSheetFragment에서는 화장실 정보(별점, 코멘트 등) 표시

class MainActivity : AppCompatActivity(), SearchToiletFragment.OnDataPassListener {

    private val mapView : MapView by lazy{
        initMapView()
    }

    private val currentLocationButton : Button by lazy{
        findViewById(R.id.current_location_button)
    }

    private val mapContainer : ViewGroup by lazy{
        findViewById(R.id.map_view)
    }

    private val tabs : TabLayout by lazy{
        findViewById(R.id.tabs)
    }

    private val container : FrameLayout by lazy{
        findViewById(R.id.container)
    }

    private val searchToiletFragment = SearchToiletFragment()

    private val resultFragment = ResultFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        setMapCenter()

        initCurrentLocationButton()

        val myHome = makeMarker(37.6106656, 127.0064049, 0, "my Home")
        mapView.addPOIItem(myHome)

        startTracking()

        initSlidingDrawer()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
    }

    /**
     * 화장실 위치 검색 탭의 spinner에서 선택된 검색 범위에 따라 지도상에 검색 범위를 원으로 그려주는 함수
     */
    @SuppressLint("MissingPermission")
    override fun onDataPass(range: Int) {
        mapView.removeAllCircles() // 이전 검색 범위 삭제

        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val currentLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val searchRange = MapCircle( // 지도에 원으로 표시할 검색 범위
            MapPoint.mapPointWithGeoCoord(
                currentLoc!!.latitude,
                currentLoc!!.longitude
            ), // 원의 중심
            range, // 반지름
            Color.argb(128,0,0,0), // 테두리 색깔
            Color.argb(128, 211, 211, 211) // 내부 색깔
        )
        mapView.addCircle(searchRange)
    }

    /**
     * 현 위치로 이동하는 버튼 세팅하는 함수
     */
    fun initCurrentLocationButton(){
        currentLocationButton.setOnClickListener {
            setMapCenter()
        }
    }

    /**
     * 화면에 지도를 띄우는 함수
     */
    private fun initMapView() : MapView{
        val mapView = MapView(this)

        val mapViewContainer = mapContainer
        mapViewContainer.addView(mapView)

        return mapView
    }

    /**
     * 지도의 중심을 현재 위치로 이동
     */
    @SuppressLint("MissingPermission")
    private fun setMapCenter(){
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val currentLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if(currentLoc != null) {
            mapView.setMapCenterPoint(
                MapPoint.mapPointWithGeoCoord(
                    currentLoc.latitude,
                    currentLoc.longitude
                ), true
            )
        }
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

    /**
     * 위치 권한을 사용자로부터 얻는 함수
     */
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED)
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                    .setNeutralButton("설정"
                    ) { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .setPositiveButton("확인"
                    ) { _, _ -> finish() }
                    .setCancelable(false)
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSION_STORAGE)
            }
        }
    }

    /**
     * 사용자가 위치 권한을 부여했는지 확인하는 함수
     */
    override fun onRequestPermissionsResult(requestCode:Int, @NonNull permissions:Array<String>, @NonNull grantResults:IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSION_STORAGE -> for (i in grantResults.indices)
            {
                // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                if (grantResults[i] < 0)
                {
                    Toast.makeText(this@MainActivity, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }
    }

    /**
     * Tracking mode를 시작시키는 함수
     */
    @SuppressLint("MissingPermission")
    fun startTracking(){
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading // 트래킹 모드와 나침반 모드(단말의 방향에 따라 지도 회전) On
    }

    /**
     * Tracking mode를 종료시키는 함수
     */
    fun stopTracking(){
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
    }

    /**
     * sliding drawer를 세팅하는 함수
     */
    private fun initSlidingDrawer(){
        tabs.addTab(tabs.newTab().setText("화장실 위치 검색"))
        tabs.addTab(tabs.newTab().setText("검색 결과"))

        supportFragmentManager.beginTransaction().add(R.id.container, searchToiletFragment).commit()

        tabs.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                var selected = if (position == 0) searchToiletFragment
                else resultFragment
                supportFragmentManager.beginTransaction().replace(R.id.container, selected).commit()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    companion object{
        var MY_PERMISSION_STORAGE = 1000
    }
}