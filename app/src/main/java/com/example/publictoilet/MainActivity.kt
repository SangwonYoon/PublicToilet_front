package com.example.publictoilet

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

// TODO 공중화장실 찾기 / 검색 결과 -> 2개의 탭을 가진 BottomSheetFragment 구현
// TODO 공중화장실 찾기 탭에서는 검색 범위 (Spinner로 구현) 설정 후 검색할 수 있게 구현
// TODO BottomSheetFragment 올라오면 지도는 중심 위치 유지하면서 작아지게 구현
// TODO 검색하면 지도에 검색 반경 및 검색 반경 내 공중 화장실 표시
// TODO 검색 결과 탭을 누르면 RecyclerView에 가까운 거리 순으로 공중화장실 정렬
// TODO RecyclerView의 item 클릭 시 지도에서는 화장실 위치 표시, BottomSheetFragment에서는 화장실 정보(별점, 코멘트 등) 표시

class MainActivity : AppCompatActivity() {

    private val mapContainer : ViewGroup by lazy{
        findViewById(R.id.map_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapView = initMapView()
        setMapCenter(mapView)

        val myHome = makeMarker(37.6106656, 127.0064049, 0, "my Home")
        mapView.addPOIItem(myHome)
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
    private fun setMapCenter(mapView : MapView){
        checkPermission()
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locCurrent = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if(locCurrent != null) {
            mapView.setMapCenterPoint(
                MapPoint.mapPointWithGeoCoord(
                    locCurrent.latitude,
                    locCurrent.longitude
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

    companion object{
        var MY_PERMISSION_STORAGE = 1000
    }
}