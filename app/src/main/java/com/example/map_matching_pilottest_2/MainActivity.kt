package com.example.map_matching_pilottest_2

import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Pair
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.MarkerIcons
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter


class MainActivity : FragmentActivity(), OnMapReadyCallback {

    val permission_request = 99
    private lateinit var naverMap: NaverMap

    val emission = Emission()
    val transition = Transition()
    val wSize = 3 //윈도사이즈는 3!!!!!!

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
        this.naverMap = naverMap

        //main() //file

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this) //gps 자동으로 받아오기

        setUpdateLocationListner() //내위치를 가져오는 코드

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
        val dir = filesDir.absolutePath //내부저장소 절대 경로
        val filename = "실제 GPS.txt"
        writeTextFile(dir, filename, "애플리케이션 시작!\n")

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
                    val contents = location.latitude.toString() + "\t" + location.longitude.toString() + "\n"
                    writeTextFile(dir, filename, contents)
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
        val myLocation = LatLng(location.latitude, location.longitude)
        val marker = Marker()
        marker.position = myLocation
        marker.width = 30
        marker.height = 50
        marker.captionText = "위도: ${location.latitude}, 경도: ${location.longitude}"
        marker.map = naverMap
        //마커
        val cameraUpdate = CameraUpdate.scrollTo(myLocation)
        naverMap.moveCamera(cameraUpdate)
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 5.0
        //카메라
    }

    fun writeTextFile(directory:String, filename:String, content:String){
        val dir = File(directory)

        if(!dir.exists()){ //dir이 존재 하지 않을때
            dir.mkdirs() //mkdirs : 중간에 directory가 없어도 생성됨
        }

        val writer = FileWriter(directory + "/" + filename, true)
        //true는 파일을 이어쓰는 것을 의미

        //쓰기 속도 향상
        val buffer = BufferedWriter(writer)
        buffer.write(content)
        buffer.close()

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
            DoubleArray(n)
        }
        for (i in 0 until n) {
            // 여기에서 link[i]가 몇개의 link와 맞닿아있는지 int 변수 선언해서 저장
            val m = roadNetwork.getLink(i).nextLinksNum(roadNetwork)
            // 알고리즘대로 tp 지정
            for (j in 0 until n) {
                if (i == j) tp_matrix[i][j] = 0.5
                else if (roadNetwork.getLink(i).isLinkNextTo(roadNetwork, j))
                    tp_matrix[i][j] = 1.0 / m
                else tp_matrix[i][j] = 0.0
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
        routePointArrayList = roadNetwork.routePoints(testNo)
        //routePointArray 생성되지 않음..
        //기울기나 방향이 맞지 않아서 생성이 안되는 문제가 있음
        //pilot 1-2에서는 가능했지만 실제 좌표를 가지고 하는 pilot 2에서는 불가능..
        //->세정 해결

/*
        // window size만큼의 t-window, ... , t-1, t에서의 candidates의 arrayList
        val arrOfCandidates: ArrayList<ArrayList<Candidate>> = ArrayList()
        val subGPSs: ArrayList<GPSPoint> = ArrayList()


        // GPSPoints 생성
        var timestamp = 0
        System.out.println("Fixed Sliding Window Viterbi (window size: 3)");
        for (point in routePointArrayList) {
            val gpsPoint = GPSPoint(timestamp, point)
            gpsPointArrayList.add(gpsPoint)
            timestamp++
            //System.out.println(gpsPoint); //gps point 제대로 생성 되는지 확인차 넣음
            val candidates: ArrayList<Candidate> = ArrayList()
            candidates.addAll(
                findRadiusCandidate(
                    gpsPointArrayList,
                    matchingCandiArrayList,
                    gpsPoint.point,
                    20,
                    roadNetwork,
                    timestamp
                )
            )

            ////////////median값 저장 - 세정 tp에서 필요//////////
            emission.Emission_Median(matchingCandiArrayList[timestamp - 1])
            if (timestamp > 1) {
                transition.Transition_Median(matchingCandiArrayList[timestamp - 1])
            }

            ///////////// FSW VITERBI /////////////
            subGPSs.add(gpsPoint)
            arrOfCandidates.add(candidates)
            // subRPA.add(point); // 비터비 내부 보려면 이것도 주석 해제해야!
            if (subGPSs.size == wSize) {
                FSWViterbi.generateMatched_yhtp(wSize, arrOfCandidates, tp_matrix) // 윤혜tp 비터비
                FSWViterbi.generateMatched_sjtp(
                    wSize,
                    arrOfCandidates,
                    gpsPointArrayList,
                    transition,
                    timestamp,
                    roadNetwork
                ) // 세정tp로 비터비
                subGPSs.clear()
                arrOfCandidates.clear()
                // subRPA.clear(); // 비터비 내부 보려면 이것도 주석 해제해야!
                subGPSs.add(gpsPoint)
                arrOfCandidates.add(candidates)
                // subRPA.add(point); // 비터비 내부 보려면 이것도 주석 해제해야!
            }
            ///////////////////////////////////////
        }

        // yhtp 이용해서 구한 subpath 출력
        FSWViterbi.printSubpath_yhtp(wSize);

        // sjtp 이용해서 구한 subpath 출력
        FSWViterbi.printSubpath_sjtp(wSize);

        // origin->생성 gps-> yhtp 이용해서 구한 matched 출력 및 정확도 확인
        FSWViterbi.test_data2_yhtp(routePointArrayList, gpsPointArrayList);

        // origin->생성 gps-> sjtp 이용해서 구한 matched 출력 및 정확도 확인
        FSWViterbi.test_data2_sjtp(routePointArrayList, gpsPointArrayList);

        // 윤혜tp와 세정tp비교!
        FSWViterbi.compareYhtpAndSjtp();*/



        getNodePrint(roadNetwork) //좌표 지도에 표시

        getLinkPrint(roadNetwork) //링크 지도에 표시

/*        //지도에 루트 표시
        System.out.println("리스트 사이즈: "+ routePointArrayList.size)
        for(i in 0..352){
            val path = PathOverlay() //path 오버레이
            path.coords = listOf(
                LatLng(routePointArrayList[i].x, routePointArrayList[i].y),
                LatLng(routePointArrayList[i+1].x, routePointArrayList[i+1].y)
            )
            path.color = Color.BLUE
            path.map = naverMap
        }*/

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
            marker.iconTintColor = Color.RED //색 덧입히기
            marker.width = 30
            marker.height = 50
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
        } //모든 노드 출력

        var cameraUpdate = CameraUpdate.scrollTo(
            LatLng(roadNetwork.getNode(0).coordinate.x, roadNetwork.getNode(0).coordinate.y)
        )
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동

    }

    fun getLinkPrint(roadNetwork: RoadNetwork) {

        for(i in roadNetwork.linkArrayList.indices) { //indices 또는 index사용
            val path = PathOverlay() //path 오버레이
            path.coords = listOf(
                LatLng(
                    roadNetwork.getNode(roadNetwork.getLink(i).startNodeID).coordinate.x, //link의 startNodeID의 위도
                    roadNetwork.getNode(roadNetwork.getLink(i).startNodeID).coordinate.y  //link의 startNodeID의 경도
                ),
                LatLng(
                    roadNetwork.getNode(roadNetwork.getLink(i).endNodeID).coordinate.x, //link의 endNodeID의 위도
                    roadNetwork.getNode(roadNetwork.getLink(i).endNodeID).coordinate.y  //link의 endNodeID의 경도
                )
            )
            path.map = naverMap //navermap에 출력
        }
    }

    fun coordDistanceofPoints(a: Point, b: Point): Double? {
        return Math.sqrt(Math.pow(a.x - b.x, 2.0) + Math.pow(a.y - b.y, 2.0))
    } //유클리드 거리 구하기 함수

    fun findRadiusCandidate(
        gpsPointArrayList: ArrayList<GPSPoint>,
        matchingPointArrayList: ArrayList<Candidate>,
        center: Point?, Radius: Int?, roadNetwork: RoadNetwork?, timestamp: Int
    ): ArrayList<Candidate> {
        val resultCandidate: ArrayList<Candidate> = ArrayList()
        for (i in 0 until roadNetwork!!.linkArrayList.size) {
            val startX =
                roadNetwork!!.nodeArrayList[roadNetwork.linkArrayList[i].startNodeID].coordinate.x
            val startY =
                roadNetwork.nodeArrayList[roadNetwork.linkArrayList[i].startNodeID].coordinate.y
            val endX =
                roadNetwork.nodeArrayList[roadNetwork.linkArrayList[i].endNodeID].coordinate.x
            val endY =
                roadNetwork.nodeArrayList[roadNetwork.linkArrayList[i].endNodeID].coordinate.y
            val vectorFromStartToCenter = Vector2D(center!!.x - startX, center.y - startY)
            val vectorFromEndToCenter = Vector2D(center.x - endX, center.y - endY)
            val vectorFromEndToStart = Vector2D(startX - endX, startY - endY)
            val dotProduct1 = vectorFromStartToCenter.dot(vectorFromEndToStart)
            val dotProduct2 = vectorFromEndToCenter.dot(vectorFromEndToStart)
            if (dotProduct1 * dotProduct2 <= 0) {
                //System.out.println("어허");
                //System.out.println("dot Product : "+dotProduct1+", "+dotProduct2);
                val candidate = Candidate()
                candidate.involvedLink = roadNetwork.linkArrayList.get(i)
                val vectorStart = Vector2D(startX, startY)
                val vectorC = Vector2D(center.x, center.y) //원점에서 시작해 center로의 vector
                val vectorH = vectorStart.getAdded(
                    vectorFromEndToStart.getMultiplied(
                        vectorC.getSubtracted(vectorStart).dot(vectorFromEndToStart)
                                / Math.pow(vectorFromEndToStart.length, 2.0)
                    )
                ) //원점에서 시작해 수선의 발로의 vector
                candidate.point = Point(
                    vectorH.getX(),
                    vectorH.getY()
                ) //수선의 발 vector의 x와 y값을 candidate의 point로 대입
                if (coordDistanceofPoints((center), candidate.point)!! > (Radius)!!) continue
                resultCandidate.add(candidate)
                //////////////////////////////////////////
                //candidate마다 ep, tp 구하기
                calculationEP(candidate, center, timestamp)
                calculationTP(
                    candidate,
                    matchingPointArrayList,
                    center,
                    gpsPointArrayList,
                    timestamp,
                    roadNetwork
                )
                for (c: Candidate? in matchingPointArrayList) {
                    emission.Emission_Median(c)
                    transition.Transition_Median(c)
                }
            }
        }
        calculationEPTP(resultCandidate, matchingPointArrayList, timestamp)

        return resultCandidate
    }

    // EP클래스 가서 캔디데이트 마다 값 구하고 저장
    fun calculationEP(cand: Candidate, center: Point?, timestamp: Int) {
        cand.ep = emission.Emission_pro(cand, center, cand.point, timestamp) //ep 구하기
        return
    }

    // TP클래스 가서 캔디데이트 마다 값 구하고 저장
    fun calculationTP(
        cand: Candidate,
        matchingPointArrayList: ArrayList<Candidate>,
        center: Point?,
        gpsPointArrayList: ArrayList<GPSPoint>,
        timestamp: Int,
        roadNetwork: RoadNetwork?
    ) {
        if (timestamp == 1 || timestamp == 2) {
            cand.tp = 0.0
            return
        }
        val matching_pre = matchingPointArrayList[timestamp - 2]
        cand.tp = Transition.Transition_pro(
            gpsPointArrayList[timestamp - 2].point, center, matching_pre, cand, roadNetwork
        ) //tp 구하기
        return
    }

    // 곱해진 eptp저장하고 후보들 중 가장 높은 eptp를 가지는 후보를 matchingPointArrayList에 저장하고
    // tp median과 ep median을 저장
    fun calculationEPTP(
        resultCandidate: ArrayList<Candidate>,
        matchingPointArrayList: ArrayList<Candidate>,
        timestamp: Int
    ): Candidate? {
        var matchingCandidate = Candidate()
        if (timestamp == 2 || timestamp == 1) {
            var min_ep = 0.0
            resultCandidate.size
            for (i in 0 until resultCandidate.size) {
                if (i == 0) {
                    min_ep = resultCandidate[i].ep
                    matchingCandidate = resultCandidate[i]
                } else if (min_ep > resultCandidate[i].ep) {
                    min_ep = resultCandidate[i].ep
                    matchingCandidate = resultCandidate[i]
                }
            }
            matchingPointArrayList.add(matchingCandidate)
            return matchingCandidate
        }
        var maximum_tpep = 0.0
        for (i in 0 until resultCandidate.size) {
            var tpep = 0.0
            tpep = resultCandidate[i].ep * resultCandidate[i].tp
            resultCandidate[i].tpep = tpep
            if (maximum_tpep < tpep) {
                maximum_tpep = tpep
                matchingCandidate = resultCandidate[i]
            }
        }
        matchingPointArrayList.add(matchingCandidate)
        return matchingCandidate
    }

}