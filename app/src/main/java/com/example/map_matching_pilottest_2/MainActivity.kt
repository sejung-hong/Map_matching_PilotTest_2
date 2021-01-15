package com.example.map_matching_pilottest_2

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.Utmk
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource

class MainActivity : FragmentActivity(), OnMapReadyCallback {

    val permission_request = 99
    private lateinit var naverMap: NaverMap

    var permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    )// 권한 가져오기

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        if (isPermitted()) {
            startProcess()
        } else {
            ActivityCompat.requestPermissions(this, permissions, permission_request)
        }//권한 확인

    }

    fun isPermitted(): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }//권한을 허락 받아야함

    fun startProcess(){
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
                ?: MapFragment.newInstance().also {
                    fm.beginTransaction().add(R.id.map, it).commit()
                } //권한
        mapFragment.getMapAsync(this) //안드로이드 연결 //onMapReady연결
    }//권한이 있다면 onMapReady연결


    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this) //gps 자동으로 받아오기

        setUpdateLocationListner() //내위치를 가져오는 코드

    }
    //맵을 생성할 준비가 되었을 때 가장 먼저 호출되는 오버라이드 메소드

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when(requestCode){
            permission_request-> {
                var check = true
                for(grant in grantResults){
                    if(grant != PERMISSION_GRANTED){
                        check = false
                        break
                    }
                }
                if(check){
                    startProcess()
                }
                else{
                    Toast.makeText(this,"권한을 승인해아지만 앱을사용가능", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
/*
        when(requestCode){
            permission_request->{
                var check = true
            }
        }
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
                grantResults)) {
            if (!locationSource.isCompassEnabled) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
*/
    }//권한 승인


    //내 위치를 가져오는 코드
    lateinit var fusedLocationProviderClient:FusedLocationProviderClient //자동으로 gps값을 받아온다.
    lateinit var locationCallback:LocationCallback //gps응답 값을 가져온다.
    //lateinit: 나중에 초기화 해주겠다는 의미

    @SuppressLint("MissingPermission")
    fun setUpdateLocationListner(){
        val locationRequest =LocationRequest.create()
        locationRequest.run{
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //높은 정확도
            interval = 1000 //1초에 한번씩 GPS 요청
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for ((i,location) in locationResult.locations.withIndex()){
                    Log.d("location: ", "${location.latitude}, ${location.longitude}")
                    setLastLocation(location)
                }
            }
        }
        //location 요청 함수 호출 (locationRequest, locationCallback)

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }//좌표계를 주기적으로 갱신

    fun setLastLocation(location: Location){
        val myLocation = LatLng(location.latitude, location.longitude)
        val marker = Marker()
        marker.position = myLocation
        marker.captionText = "위도: ${location.latitude}, 경도: ${location.longitude}"
        marker.map = naverMap
        //마커
        val cameraUpdate = CameraUpdate.scrollTo(myLocation)
        naverMap.moveCamera(cameraUpdate)
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 5.0
        //카메라

        //marker.map = null

    }

}