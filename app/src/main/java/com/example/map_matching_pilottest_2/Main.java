/*
package com.example.map_matching_pilottest_2;

//import com.sun.deploy.util.SyncAccess;
import javafx.util.Pair;
*/
/*import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import javax.swing.SwingWorker;*//*


import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Emission emission = new Emission();
    private static Transition transition = new Transition();
    private static int wSize = 3; //윈도사이즈는 3!!!!!!
    //MySwingWorker mySwingWorker;
    */
/*그림 그려 주는 tool
    private static SwingWrapper<XYChart> sw;
    private static XYChart chart;*//*


    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("===== [YSY] Map-matching PilotTest 1-2 =====");
        int testNo = 2; // 여기만 바꿔주면 됨 (1-세정, 2-유네, 3-유림)
        FileIO fileIO = new FileIO(testNo);
        // 파일에서 읽어와 도로네트워크 생성
        RoadNetwork roadNetwork = fileIO.generateRoadNetwork();

        ///////////// Transition probability matrix 구하기 (yh_tp)////////////////
        int n = roadNetwork.getLinksSize();
        double [][] tp_matrix = new double[n][n];
        for (int i = 0; i < n;i++) {
            // 여기에서 link[i]가 몇개의 link와 맞닿아있는지 int 변수 선언해서 저장
            int m = roadNetwork.getLink(i).nextLinksNum(roadNetwork);
            // 알고리즘대로 tp 지정
            for (int j = 0; j < n; j++) {
                if (i == j) tp_matrix[i][j] = 0.5;
                else if (roadNetwork.getLink(i).isLinkNextTo(roadNetwork, j))
                    tp_matrix[i][j] = 1.0/m;
                else tp_matrix[i][j] = 0.0;
            }
        }

        // Link와 Node를 바탕으로 Adjacent List 구축
        ArrayList<AdjacentNode> heads = new ArrayList<>();
        for (int i = 0; i < roadNetwork.nodeArrayList.size(); i++) {
            AdjacentNode headNode = new AdjacentNode(roadNetwork.nodeArrayList.get(i));
            heads.add(headNode);

            List<Pair<Link, Integer>> adjacentLink = roadNetwork.getLink1(headNode.getNode().getNodeID());
            if (adjacentLink.size() == 0) continue;
            AdjacentNode ptr = headNode;
            for (int j = 0; j < adjacentLink.size(); j++) {
                AdjacentNode addNode = new AdjacentNode(roadNetwork.getNode(adjacentLink.get(j).getValue()), adjacentLink.get(j).getKey());
                ptr.setNextNode(addNode);
                ptr = ptr.getNextNode();
            }
        }

        // GPS points와 routePoints를 저장할 ArrayList생성
        ArrayList<GPSPoint> gpsPointArrayList = new ArrayList<>();
        ArrayList<Point> routePointArrayList; // 실제 경로의 points!
        ArrayList<Candidate> matchingCandiArrayList = new ArrayList<>();

        // test 번호에 맞는 routePoints생성
        routePointArrayList = roadNetwork.routePoints(testNo);

        */
/*
        for(int i=0; i<gpsPointArrayList.size(); i++){
            emission.Emission_Median(gpsPointArrayList.get(i), routePointArrayList.get(i));
            if(i>0){
                transition.Transition_Median(gpsPointArrayList.get(i-1), gpsPointArrayList.get(i),routePointArrayList.get(i-1), routePointArrayList.get(i));
            }//매칭된 point로 해야하나.. 실제 point로 해야하나.. 의문?
            //중앙값 저장
        }
        *//*


        // window size만큼의 t-window, ... , t-1, t에서의 candidates의 arrayList
        ArrayList<ArrayList<Candidate>> arrOfCandidates = new ArrayList<>();
        ArrayList<GPSPoint> subGPSs = new ArrayList<>();
        // ArrayList<Point> subRPA = new ArrayList<>(); // 비터비 내부 보려면 이것도 주석 해제해야! (subRoadPointArrayList)
        // GPSPoints 생성
        int timestamp = 0;
        //System.out.println("여기부터 생성된 gps point~~");
        System.out.println("Fixed Sliding Window Viterbi (window size: 3)");
        for (Point point : routePointArrayList) {
            GPSPoint gpsPoint = new GPSPoint(timestamp, point);
            gpsPointArrayList.add(gpsPoint);
            timestamp++;
            //System.out.println(gpsPoint); //gps point 제대로 생성 되는지 확인차 넣음
            ArrayList<Candidate> candidates = new ArrayList<>();
            candidates.addAll(findRadiusCandidate(gpsPointArrayList, matchingCandiArrayList, gpsPoint.getPoint(), 20, roadNetwork, timestamp));

            /////////matching print/////////////
            //System.out.println("매칭완료 " + matchingPointArrayList.get(timestamp-1));

            //System.out.println();

            emission.Emission_Median(matchingCandiArrayList.get(timestamp-1));
            if(timestamp > 1){
                transition.Transition_Median(matchingCandiArrayList.get(timestamp-1));
            }
            //median값 저장

            ///////////// FSW VITERBI /////////////
            subGPSs.add(gpsPoint);
            arrOfCandidates.add(candidates);
            // subRPA.add(point); // 비터비 내부 보려면 이것도 주석 해제해야!
            if (subGPSs.size() == wSize) {
                FSWViterbi.generateMatched_yhtp(wSize, arrOfCandidates, tp_matrix); // 윤혜tp 비터비
                FSWViterbi.generateMatched_sjtp(wSize, arrOfCandidates, gpsPointArrayList, transition, timestamp, roadNetwork); // 세정tp로 비터비
                subGPSs.clear();
                arrOfCandidates.clear();
                // subRPA.clear(); // 비터비 내부 보려면 이것도 주석 해제해야!
                subGPSs.add(gpsPoint);
                arrOfCandidates.add(candidates);
                // subRPA.add(point); // 비터비 내부 보려면 이것도 주석 해제해야!
            }
            ///////////////////////////////////////
        }
        // yhtp 이용해서 구한 subpath 출력
        FSWViterbi.printSubpath_yhtp (wSize);

        // sjtp 이용해서 구한 subpath 출력
        FSWViterbi.printSubpath_sjtp (wSize);

        // origin->생성 gps-> yhtp 이용해서 구한 matched 출력 및 정확도 확인
        FSWViterbi.test_data2_yhtp(routePointArrayList, gpsPointArrayList);

        // origin->생성 gps-> sjtp 이용해서 구한 matched 출력 및 정확도 확인
        FSWViterbi.test_data2_sjtp(routePointArrayList, gpsPointArrayList);

        // 윤혜tp와 세정tp비교!
        FSWViterbi.compareYhtpAndSjtp();
    }

    public static Double coordDistanceofPoints(Point a, Point b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }//유클리드 거리 구하기

    public static ArrayList<Candidate> findRadiusCandidate(ArrayList<GPSPoint> gpsPointArrayList, ArrayList<Candidate> matchingPointArrayList, Point center, Integer Radius, RoadNetwork roadNetwork, int timestamp) {
        ArrayList<Candidate> resultCandidate = new ArrayList<>();
        for (int i = 0; i < roadNetwork.linkArrayList.size(); i++) {
            double startX = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getStartNodeID()).getCoordinate().getX();
            double startY = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getStartNodeID()).getCoordinate().getY();
            double endX = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getEndNodeID()).getCoordinate().getX();
            double endY = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getEndNodeID()).getCoordinate().getY();

            Vector2D vectorFromStartToCenter = new Vector2D(center.getX() - startX, center.getY() - startY);
            Vector2D vectorFromEndToCenter = new Vector2D(center.getX() - endX, center.getY() - endY);
            Vector2D vectorFromEndToStart = new Vector2D(startX - endX, startY - endY);

            double dotProduct1 = vectorFromStartToCenter.dot(vectorFromEndToStart);
            double dotProduct2 = vectorFromEndToCenter.dot(vectorFromEndToStart);

            if (dotProduct1 * dotProduct2 <= 0) {
                //System.out.println("어허");
                //System.out.println("dot Product : "+dotProduct1+", "+dotProduct2);
                Candidate candidate = new Candidate();
                candidate.setInvolvedLink(roadNetwork.linkArrayList.get(i));
                Vector2D vectorStart = new Vector2D(startX, startY);
                Vector2D vectorC = new Vector2D(center.getX(), center.getY()); //원점에서 시작해 center로의 vector
                Vector2D vectorH = vectorStart.getAdded(vectorFromEndToStart.getMultiplied(
                        (vectorC.getSubtracted(vectorStart).dot(vectorFromEndToStart))
                                / Math.pow(vectorFromEndToStart.getLength(), 2))); //원점에서 시작해 수선의 발로의 vector
                candidate.setPoint(new Point(vectorH.getX(), vectorH.getY())); //수선의 발 vector의 x와 y값을 candidate의 point로 대입
                if (coordDistanceofPoints(center, candidate.getPoint()) > Radius) continue;
                resultCandidate.add(candidate);
//////////////////////////////////////////
                //candidate마다 ep, tp 구하기
                calculationEP(candidate, center, timestamp);
                calculationTP(candidate, matchingPointArrayList, center, gpsPointArrayList, timestamp, roadNetwork);

                for (Candidate c: matchingPointArrayList) {
                    emission.Emission_Median(c);
                    transition.Transition_Median(c);
                }

            }
        }
        calculationEPTP(resultCandidate, matchingPointArrayList, timestamp);

        return resultCandidate;
    }

    // EP클래스 가서 캔디데이트 마다 값 구하고 저장
    public static void calculationEP(Candidate cand, Point center, int timestamp) {
        cand.setEp(emission.Emission_pro(cand, center, cand.getPoint(), timestamp)); //ep 구하기
        return;
    }

    // TP클래스 가서 캔디데이트 마다 값 구하고 저장
    public static void calculationTP(Candidate cand, ArrayList<Candidate> matchingPointArrayList, Point center, ArrayList<GPSPoint> gpsPointArrayList, int timestamp,  RoadNetwork roadNetwork) {
        if (timestamp == 1 || timestamp == 2) {
            cand.setTp(0);
            return;
        }
        Candidate matching_pre = matchingPointArrayList.get(timestamp - 2);
        cand.setTp(transition.Transition_pro(gpsPointArrayList.get(timestamp - 2).getPoint(), center, matching_pre, cand, roadNetwork)); //tp 구하기
        return;

    }

    // 곱해진 eptp저장하고 후보들 중 가장 높은 eptp를 가지는 후보를 matchingPointArrayList에 저장하고
    // tp median과 ep median을 저장
    public static Candidate calculationEPTP(ArrayList<Candidate> resultCandidate, ArrayList<Candidate> matchingPointArrayList, int timestamp) {
        Candidate matchingCandidate = new Candidate();

        if (timestamp == 2 || timestamp == 1) {
            double min_ep = 0;
            for (int i = 0; i < resultCandidate.size(); i++) {
                if (i == 0) {
                    min_ep = resultCandidate.get(i).getEp();
                    matchingCandidate = resultCandidate.get(i);
                } else if (min_ep > resultCandidate.get(i).getEp()) {
                    min_ep = resultCandidate.get(i).getEp();
                    matchingCandidate = resultCandidate.get(i);
                }
            }
            matchingPointArrayList.add(matchingCandidate);

            return matchingCandidate;
        }

        double maximum_tpep = 0;

        for(int i=0; i < resultCandidate.size(); i++){
            double tpep=0;
            tpep = resultCandidate.get(i).getEp() * resultCandidate.get(i).getTp();
            resultCandidate.get(i).setTpep(tpep);

            if(maximum_tpep < tpep){
                maximum_tpep = tpep;
                matchingCandidate = resultCandidate.get(i);

            }
        }
        matchingPointArrayList.add(matchingCandidate);

        return matchingCandidate;

    }

    public static ArrayList<Link> AdjacentLink(Link mainLink,RoadNetwork roadNetwork,ArrayList<AdjacentNode> heads){
        int startNode=mainLink.getStartNodeID();
        int endNode = mainLink.getEndNodeID();
        ArrayList<Link> secondLink = new ArrayList<>();
        //ArrayList<Node> startAdjacentNode = new ArrayList<>();
        //ArrayList<Node> endAdjacentNode = new ArrayList<>();
        AdjacentNode pointer = heads.get(roadNetwork.nodeArrayList.get(startNode).getNodeID()).getNextNode();
        while(true){
            if(pointer==null) break;
            secondLink.add(roadNetwork.getLink(pointer.getNode().getNodeID(),startNode));
            pointer=pointer.getNextNode();
        }
        pointer = heads.get(roadNetwork.nodeArrayList.get(endNode).getNodeID()).getNextNode();
        while(true){
            if(pointer==null) break;
            secondLink.add(roadNetwork.getLink(pointer.getNode().getNodeID(),endNode));
            pointer=pointer.getNextNode();
        }
        return secondLink;
    }

}*/
