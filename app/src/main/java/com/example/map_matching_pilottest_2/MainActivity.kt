package com.example.map_matching_pilottest_2

import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Pair
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class MainActivity : FragmentActivity(), OnMapReadyCallback {

    val permission_request = 99

    //private val candidate: Candidate = Candidate()
    private lateinit var naverMap: NaverMap
    private val emission: Emission = Emission()
    private val transition = Transition()
    private val wSize = 3 //윈도사이즈는 3!!!!!!

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

    fun startProcess() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            } //권한
        mapFragment.getMapAsync(this) //안드로이드 연결 //onMapReady연결
    }//권한이 있다면 onMapReady연결


    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        val cameraPosition = CameraPosition(
            LatLng(37.618235, 127.061945),  // 위치 지정
            16.0 // 줌 레벨
        )
        naverMap.cameraPosition = cameraPosition
        this.naverMap = naverMap
        /*val marker = Marker()
        marker.position = LatLng(37.618235, 127.061945)
        marker.map = naverMap*/
        main() //file
        /*fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this) //gps 자동으로 받아오기

        setUpdateLocationListner() //내위치를 가져오는 코드*/

    }
    //맵을 생성할 준비가 되었을 때 가장 먼저 호출되는 오버라이드 메소드

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            permission_request -> {
                var check = true
                for (grant in grantResults) {
                    if (grant != PERMISSION_GRANTED) {
                        check = false
                        break
                    }
                }
                if (check) {
                    startProcess()
                } else {
                    Toast.makeText(this, "권한을 승인해아지만 앱을사용가능", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }//권한 승인


    //내 위치를 가져오는 코드
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient //자동으로 gps값을 받아온다.
    lateinit var locationCallback: LocationCallback //gps응답 값을 가져온다.
    //lateinit: 나중에 초기화 해주겠다는 의미

    @SuppressLint("MissingPermission")
    fun setUpdateLocationListner() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //높은 정확도
            interval = 1000 //1초에 한번씩 GPS 요청
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for ((i, location) in locationResult.locations.withIndex()) {
                    Log.d("location: ", "${location.latitude}, ${location.longitude}")
                    setLastLocation(location)
                }
            }
        }
        //location 요청 함수 호출 (locationRequest, locationCallback)

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }//좌표계를 주기적으로 갱신

    fun setLastLocation(location: Location) {
        val myLocation = LatLng(37.618235, 127.061945)
        val marker = Marker()
        marker.position = myLocation
        //marker.captionText = "위도: ${location.latitude}, 경도: ${location.longitude}"
        marker.map = naverMap
        //마커
        val cameraUpdate = CameraUpdate.scrollTo(myLocation)
        naverMap.moveCamera(cameraUpdate)
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 5.0

        //marker.map = null
    }


    //자바 복붙하면 자동으로 코틀린으로 바꿔줌.. 신기해
    // pilot test 1-2의 main 가져옴
    fun main() {
        System.out.println("===== [YSY] Map-matching PilotTest 2 =====")

        val testNo = 1 // 여기만 바꿔주면 됨 (PilotTest 2는 data 1만 존재)

        val dir = filesDir.absolutePath //파일절대경로
        val fileIO = FileIO(dir)
        // 파일에서 읽어와 도로네트워크 생성
        val roadNetwork = fileIO.generateRoadNetwork()

        ///////////// Transition probability matrix 구하기 (yh_tp)////////////////
        val n = roadNetwork.linksSize
        val tp_matrix = Array(n) {
            DoubleArray(
                n
            )
        }
        for (i in 0 until n) {
            // 여기에서 link[i]가 몇개의 link와 맞닿아있는지 int 변수 선언해서 저장
            val m = roadNetwork.getLink(i).nextLinksNum(roadNetwork)
            // 알고리즘대로 tp 지정
            for (j in 0 until n) {
                if (i == j) tp_matrix[i][j] = 0.5 else if (roadNetwork.getLink(i)
                        .isLinkNextTo(roadNetwork, j)
                ) tp_matrix[i][j] = 1.0 / m else tp_matrix[i][j] = 0.0
            }
        }

        // Link와 Node를 바탕으로 Adjacent List 구축
        val heads: ArrayList<AdjacentNode> = ArrayList()
        for (i in roadNetwork.nodeArrayList.indices) {
            val headNode = AdjacentNode(roadNetwork.nodeArrayList[i])
            heads.add(headNode)
            val adjacentLink: MutableList<Pair<Link, Int>>? =
                roadNetwork.getLink1(headNode.node.nodeID) //mutableList?
            if (adjacentLink != null) { //안전하게 하기 위함
                if (adjacentLink.size == 0) continue
            }
            var ptr = headNode
            if (adjacentLink != null) { //안전하게 하기 위함
                for (j in adjacentLink.indices) {
                    val addNode = AdjacentNode(
                        roadNetwork.getNode(adjacentLink[j].second), adjacentLink[j].first
                    )
                    ptr.nextNode = addNode
                    ptr = ptr.nextNode
                }
            }
        }
        //신기한 사실 = get,set 함수를 불러오지 않아도 알아서 척척박사님 알아맞춰보세요
        //여기까지 도로네트워크 생성

        // GPS points와 routePoints를 저장할 ArrayList생성
        val gpsPointArrayList: ArrayList<GPSPoint> = ArrayList()
        val routePointArrayList: ArrayList<Point> // 실제 경로의 points!

        val matchingCandiArrayList: ArrayList<Candidate> = ArrayList()

        // test 번호에 맞는 routePoints생성

        // test 번호에 맞는 routePoints생성
        routePointArrayList = roadNetwork.routePoints(testNo)


        // window size만큼의 t-window, ... , t-1, t에서의 candidates의 arrayList


        // window size만큼의 t-window, ... , t-1, t에서의 candidates의 arrayList
        val arrOfCandidates: ArrayList<ArrayList<Candidate>> = ArrayList()
        val subGPSs: ArrayList<GPSPoint> = ArrayList()
        //ArrayList<Point> subRPA = new ArrayList<>(); // 비터비 내부 보려면 이것도 주석 해제해야! (subRoadPointArrayList)
        // GPSPoints 생성
        //ArrayList<Point> subRPA = new ArrayList<>(); // 비터비 내부 보려면 이것도 주석 해제해야! (subRoadPointArrayList)
        // GPSPoints 생성
        var timestamp = 0

        // 1: 원래 하던대로 (표준편차 4)  | 2: x혹은 y좌표만 uniform하게(hor, ver, dia에 따라서)
        // 3: x, y 모두 uniform하게     | 4: 교수님이 말한 평균 4 방식
        val gpsGenMode = 2
        println("Fixed Sliding Window Viterbi (window size: 3)")
        for (i in routePointArrayList.indices step (5)) {
            var point: Point = routePointArrayList.get(i)
            println("routePoint: " + point)
            printPoint(point, Color.YELLOW)
        }

        for (i in routePointArrayList.indices step (5)) {
            // 오래 걸리는 작업 수행부분
            var point: Point = routePointArrayList.get(i)
            val gpsPoint = GPSPoint(
                timestamp,
                point,
                gpsGenMode,
                3,
                roadNetwork.getLink(point.linkID).itLooksLike
            )
            printPoint(gpsPoint.point, Color.RED) // 생성된 GPS출력(빨간색)

            println("[MAIN] GPS: $gpsPoint")
            gpsPointArrayList.add(gpsPoint)
            timestamp++
            //System.out.println(gpsPoint); //gps point 제대로 생성 되는지 확인차 넣음
            val candidates: ArrayList<Candidate> = ArrayList()
            candidates.addAll(
                Candidate.findRadiusCandidate(
                    gpsPointArrayList, matchingCandiArrayList,
                    gpsPoint.point, 50, roadNetwork, timestamp, emission, transition
                )
            )
            println(">>>> [MAIN] candidates <<<<")
            for (candidate in candidates) {
                println("  $candidate")
            }
            println(">>>>>>>>>>>>>><<<<<<<<<<<<<")
            /*emission.Emission_Median(matchingCandiArrayList[timestamp - 1])
            if (timestamp > 1) {
                transition.Transition_Median(matchingCandiArrayList[timestamp - 1])
            }*/
            //median값 저장

            ///////////// FSW VITERBI /////////////
            subGPSs.add(gpsPoint)
            arrOfCandidates.add(candidates)

            //subRPA.add(point); // 비터비 내부 보려면 이것도 주석 해제해야!
            if (subGPSs.size == wSize) {
                println("===== VITERBI start ====")
                println("----- yhtp ------")
                FSWViterbi.generateMatched(
                    tp_matrix,
                    wSize,
                    arrOfCandidates,
                    gpsPointArrayList, /* subRPA, subGPSs,*/
                    transition,
                    timestamp,
                    roadNetwork,
                    "yh"
                )
                println("----- sjtp ------")
                FSWViterbi.generateMatched(
                    tp_matrix,
                    wSize,
                    arrOfCandidates,
                    gpsPointArrayList, /* subRPA, subGPSs, */
                    transition,
                    timestamp,
                    roadNetwork,
                    "sj"
                )
                subGPSs.clear()
                arrOfCandidates.clear()
                //subRPA.clear(); // 비터비 내부 보려면 이것도 주석 해제해야!
                subGPSs.add(gpsPoint)
                arrOfCandidates.add(candidates)
                //subRPA.add(point); // 비터비 내부 보려면 이것도 주석 해제해야!

                println("===== VITERBI end ====")
            }
            ///////////////////////////////////////
        }
        // yhtp 이용해서 구한 subpath 출력
        //FSWViterbi.printSubpath(wSize, "yh")

        // sjtp 이용해서 구한 subpath 출력
        //FSWViterbi.printSubpath(wSize, "sj")

        // origin->생성 gps-> yhtp 이용해서 구한 matched 출력 및 정확도 확인
        FSWViterbi.test(roadNetwork, "yh")

        // origin->생성 gps-> sjtp 이용해서 구한 matched 출력 및 정확도 확인
        FSWViterbi.test(roadNetwork, "sj")

        // 윤혜tp와 세정tp비교!
        FSWViterbi.compareYHandSJ()

        // 어차피 결과가 같아서 출력은 하나만
        printMatched(FSWViterbi.getMatched_yhtp(), Color.BLUE, 50) // 윤혜 매칭: 파란색
        printMatched(FSWViterbi.getMatched_sjtp(), Color.GREEN, 30) // 세정 매칭: 초록색
        /*var i: Int = 0;
        *//*for (c in FSWViterbi.getMatched_sjtp()) {
            println("$i] matched: $c")
            printPoint(c.point, Color.BLUE);
            i++;
        }*/

        /*for (c in FSWViterbi.getMatched_sjtp()) {
            printPoint(c.point);
        }

*/
    }

    fun printPoint(point: Point, COLOR: Int) {
        val marker = Marker() //좌표
        marker.position = LatLng(
            point.y,
            point.x
        )
        marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
        marker.iconTintColor = COLOR //색 덧입히기
        marker.width = 30
        marker.height = 30
        // 마커가 너무 커서 크기 지정해줌
        marker.map = naverMap //navermap에 출력
        var cameraUpdate = CameraUpdate.scrollAndZoomTo(
            LatLng(
                point.y,
                point.x
            ), 17.0
        )
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동

    }

    // 생성된 GPS를 지도 위에 출력하는 함수
    fun printsubGPSs(subGPSs: ArrayList<GPSPoint>) {
        for (i in subGPSs.indices) { //indices 또는 index사용
            val marker = Marker() //좌표
            marker.position = LatLng(
                subGPSs.get(i).x,
                subGPSs.get(i).y
            ) //node 좌표 출력
            marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
            marker.iconTintColor = Color.RED //색 덧입히기
            marker.width = 20
            marker.height = 20
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
        } //모든 노드 출력

        var cameraUpdate = CameraUpdate.scrollTo(
            LatLng(
                subGPSs.get(0).x,
                subGPSs.get(0).y
            )
        )
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동
    }

    fun printMatched(matched: ArrayList<Candidate>, COLOR: Int, SIZE: Int) {
        for (i in matched.indices) { //indices 또는 index사용
            val marker = Marker() //좌표
            marker.position = LatLng(
                matched.get(i).point.y,
                matched.get(i).point.x
            ) //node 좌표 출력
            marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
            marker.iconTintColor = COLOR //색 덧입히기
            marker.width = SIZE
            marker.height = SIZE
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
        } //모든 노드 출력

        var cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(
            matched.get(0).point.y,
            matched.get(0).point.x
        ),18.0)
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동
    }


    //Node(좌표)를 지도위에 출력하는 함수
    fun getNodePrint(roadNetwork: RoadNetwork) {
        for (i in roadNetwork.nodeArrayList.indices) { //indices 또는 index사용
            val marker = Marker() //좌표
            marker.position = LatLng(
                roadNetwork.getNode(i).coordinate.x,
                roadNetwork.getNode(i).coordinate.y
            ) //node 좌표 출력
            marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
            marker.iconTintColor = Color.BLACK //색 덧입히기
            marker.width = 30
            marker.height = 50
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
        } //모든 노드 출력

        var cameraUpdate = CameraUpdate.scrollTo(
            LatLng(
                roadNetwork.getNode(0).coordinate.x, roadNetwork.getNode(
                    0
                ).coordinate.y
            )
        )
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동
    }

    fun getLinkPrint(roadNetwork: RoadNetwork) {

        for (i in roadNetwork.linkArrayList.indices) {

        }

    }

}