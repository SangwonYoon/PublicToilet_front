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
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import net.daum.mf.map.api.MapCircle
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

// 공중화장실 찾기 / 검색 결과 -> 2개의 탭을 가진 sliding drawer 구현
// 공중화장실 찾기 탭에서는 검색 범위 (Spinner로 구현) 설정 후 검색할 수 있게 구현
// 지도상에 검색 범위 표시
// 모드 선택 기능 (자유시점 모드, 트래킹 모드, 나침반 모드)
// 검색하면 지도에 검색 반경 내 공중 화장실 표시 // 검색 버튼 클릭 후 sliding drawer 닫히는 기능 추가 -> animateClose()
// 검색 결과 탭을 누르면 RecyclerView에 가까운 거리 순으로 공중화장실 정렬
// 검색 결과 item 클릭 시 해당 마커를 중심으로 지도 이동 // TODO 줌인 기능도 추가 -> mapView.setZoomLevel()
// 마커의 말풍선 클릭 시 해당 화장실 정보 화면으로 이동 -> getUserObject()로 해당 마커와 연관된 Toilet 객체 가져오기
// 리뷰 작성 화면 기능 구현

class MainActivity : AppCompatActivity(), SearchToiletFragment.OnDataPassListener, SearchResultAdapter.OnItemClickedListener {

    private val client = OkHttpClient()

    private val mapView : MapView by lazy{
        initMapView()
    }

    private val changeModeButton : ImageButton by lazy{
        findViewById(R.id.change_mode_button)
    }

    private val currentLocationButton : Button by lazy{
        findViewById(R.id.current_location_button)
    }

    private val mapContainer : ViewGroup by lazy{
        findViewById(R.id.map_view)
    }

    private val searchDrawer : SlidingDrawer by lazy{
        findViewById(R.id.search_drawer)
    }

    private val tabs : TabLayout by lazy{
        findViewById(R.id.tabs)
    }

    private val container : FrameLayout by lazy{
        findViewById(R.id.container)
    }

    private val searchToiletFragment = SearchToiletFragment()

    private val searchButton : AppCompatButton by lazy{
        findViewById(R.id.search_button)
    }

    private val resultFragment = ResultFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        setMapCenter()

        initButtons()

//        val myHome = makeMarker(37.6106656, 127.0064049, 0, "my Home")
//        mapView.addPOIItem(myHome)

        startCompassMode()

        initSlidingDrawer()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
    }

    /**
     * 검색 결과 탭에서 사용자가 선택한 화장실의 위치를 중심으로 지도 이동
     */
    override fun onItemClicked(position: Int) {
        val targetMarker = mapView.findPOIItemByTag(position)
        val coordinate = targetMarker.mapPoint
        mapView.setMapCenterPoint(coordinate, true)
        searchDrawer.animateClose()
    }

    /**
     * 화장실 위치 검색 탭의 spinner에서 선택된 검색 범위에 따라 지도상에 검색 범위를 원으로 그려주는 함수
     * SearchToiletFragment class의 onDataPass 메소드를 override
     */
    @SuppressLint("MissingPermission")
    override fun onRangeChanged(range: Int) {
//        mapView.setCurrentLocationRadius(range)
//        mapView.setCurrentLocationRadiusStrokeColor(Color.argb(128,0,0,0))
//        mapView.setCurrentLocationRadiusFillColor(Color.argb(128, 211, 211, 211))

        mapView.removeAllCircles() // 이전 검색 범위 삭제

        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val currentLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val searchRange = MapCircle( // 지도에 원으로 표시할 검색 범위
            MapPoint.mapPointWithGeoCoord(
                currentLoc!!.latitude,
                currentLoc.longitude
            ), // 원의 중심
            range, // 반지름
            Color.argb(128,0,0,0), // 테두리 색깔
            Color.argb(128, 211, 211, 211) // 내부 색깔
        )
        mapView.addCircle(searchRange)
    }

    @SuppressLint("MissingPermission")
    override fun onRangePass(range: Int) {
        //TODO 화장실 검색 API 호출
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val currentLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        val request = Request.Builder().addHeader("Content-Type","application/x-www-form-urlencoded").url("http://15.165.203.167:8080/toilets/search?latitude=${/*currentLoc!!.latitude*/37.3130672}&longitude=${/*currentLoc!!.longitude*/127.0884511}&range=$range").build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("connection error", "인터넷 연결 불안정")
                runOnUiThread{
                    Toast.makeText(
                        this@MainActivity,
                        "인터넷 연결이 불안정합니다. 다시 시도해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.code() == 200){
                    mapView.removeAllPOIItems() // 지도 화면에 있는 모든 POI(마커)를 제거한다.

                    val jsonArray = JSONArray(response.body()!!.string())

                    val bundle = Bundle()
                    bundle.putInt("array size", jsonArray.length())

                    for(idx in 0 until jsonArray.length()){
                        val tempToilet = jsonArray[idx] as JSONObject
                        val id = tempToilet.getString("id")
                        val latitude = tempToilet.getString("latitude").toDouble()
                        val longitude = tempToilet.getString("longitude").toDouble()
                        val toiletName = tempToilet.getString("toiletName")
                        val tel = tempToilet.getString("tel")
                        val openTime = tempToilet.getString("openTime")
                        val mw = tempToilet.getBoolean("mw")
                        val m1 = tempToilet.getString("m1")
                        val m2 = tempToilet.getString("m2")
                        val m3 = tempToilet.getString("m3")
                        val m4 = tempToilet.getString("m4")
                        val m5 = tempToilet.getString("m5")
                        val m6 = tempToilet.getString("m6")
                        val w1 = tempToilet.getString("w1")
                        val w2 = tempToilet.getString("w2")
                        val w3 = tempToilet.getString("w3")
                        val score_avg = tempToilet.getString("score_avg")
                        val distance = tempToilet.getString("distance").toDouble().toInt().toString()

                        val toilet = Toilet(id = id, toiletName = toiletName, tel = tel, openTime = openTime, mw = mw, m1 = m1, m2 = m2, m3 = m3, m4 = m4, m5 = m5, m6 = m6, w1 = w1, w2 = w2, w3 = w3, distance = distance, score_avg = score_avg)

                        val tempToiletMarker = makeMarker(latitude, longitude, idx, toiletName)
                        tempToiletMarker.userObject = toilet
                        mapView.addPOIItem(tempToiletMarker)

                        bundle.putString("id_$idx", id)
                        bundle.putString("toiletName_$idx", toiletName)
                        bundle.putString("tel_$idx", tel)
                        bundle.putString("openTime_$idx", openTime)
                        bundle.putBoolean("mw_$idx", mw)
                        bundle.putString("m1_$idx", m1)
                        bundle.putString("m2_$idx", m2)
                        bundle.putString("m3_$idx", m3)
                        bundle.putString("m4_$idx", m4)
                        bundle.putString("m5_$idx", m5)
                        bundle.putString("m6_$idx", m6)
                        bundle.putString("w1_$idx", w1)
                        bundle.putString("w2_$idx", w2)
                        bundle.putString("w3_$idx", w3)
                        bundle.putString("score_avg_$idx", score_avg)
                        bundle.putString("distance_$idx", distance)
                    }

                    resultFragment.arguments = bundle
                    resultFragment.changeResult()
                    searchDrawer.animateClose()
                } else{
                    Log.d("connection error", "response code is not 200")
                    runOnUiThread{
                        Toast.makeText(
                            this@MainActivity,
                            "인터넷 연결이 불안정합니다. 다시 시도해주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        val inflater = menuInflater
        inflater.inflate(R.menu.mode, menu)
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.free_mode -> stopTracking()
            R.id.tracking_mode -> startTrackingMode()
            R.id.compass_mode -> startCompassMode()
        }
        return super.onContextItemSelected(item)
    }

    /**
     * 트래킹 모드를 바꾸는 버튼과 현 위치로 이동하는 버튼을 세팅하는 함수
     */
    fun initButtons(){
        changeModeButton.setOnClickListener{
            this.registerForContextMenu(it)
            openContextMenu(it)
            unregisterForContextMenu(it)
        }
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
    fun startTrackingMode(){
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading // 트래킹 모드 On
        currentLocationButton.isVisible = false // 현 위치 버튼 안보이게  
    }

    /**
     * Compass mode를 시작시키는 함수
     */
    fun startCompassMode(){
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading // 트래킹 모드와 나침반 모드(단말의 방향에 따라 지도 회전) On
        currentLocationButton.isVisible = false // 현 위치 버튼 안보이게
    }



    /**
     * Tracking mode를 종료시키는 함수
     */
    fun stopTracking(){
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff // 트래킹 모드 Off
        currentLocationButton.isVisible = true // 현 위치 버튼 보이게
        //mapView.setShowCurrentLocationMarker(false) // 현 위치 마커 제거
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

class MarkerEventListener(val context: Context) : MapView.POIItemEventListener{
    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        //
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
        //
    }

    override fun onCalloutBalloonOfPOIItemTouched(
        mapView: MapView?,
        poiItem: MapPOIItem?,
        buttonType: MapPOIItem.CalloutBalloonButtonType?
    ) {
        // TODO
        val toilet = poiItem?.userObject as Toilet
        val intent = Intent(context, ToiletInfoActivity::class.java)
        intent.putExtra("id", toilet.id)
        intent.putExtra("toiletName", toilet.toiletName)
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
        intent.putExtra("distance", toilet.distance)
        intent.putExtra("score_avg", toilet.score_avg)

        startActivity(context, intent, null)
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
        //
    }
}