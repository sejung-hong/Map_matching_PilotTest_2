package com.example.map_matching_pilottest_2;

import java.util.ArrayList;

public class Crossroad {

    //갈림길이 있는지 확인, 갈림길이 존재하면 true 존재하지 않으면 false
    public static boolean exist_Crassroad(RoadNetwork roadNetwork, Candidate candidate) {

        //candidate와 candidate가 있는 link의 시작점이나 끝점의 거리를 구함
        Node start, end;
        start = roadNetwork.getNode(candidate.getInvolvedLink().getStartNodeID());
        end = roadNetwork.getNode(candidate.getInvolvedLink().getEndNodeID());

        double start_dis, end_dis;
        start_dis = Calculation.calDistance(candidate.getPoint(), start.getCoordinate());
        end_dis = Calculation.calDistance(candidate.getPoint(), end.getCoordinate());
        //유클리드 거리 구하기

        //갈림길이 5m안에 있을 때 true
        //거리가 5m(근거 없는 5m, 더 늘릴 필요도 있음)
        if (start_dis < 5) { //start노드와의 거리
            //node가 가지는 링크가 3개 이상일 때 갈림길이라고 판단
            if (start.includingLinks(roadNetwork.linkArrayList).size() > 2) {
                return true;
            }
        }
        if (end_dis < 5) { //end노드와의 거리
            if (end.includingLinks(roadNetwork.linkArrayList).size() > 2) {
                return true;
            }
        }

        //갈림길이 5m밖에 있을 때 false
        return false;
    }


    //매칭된 링크가 같은지 같지 않은지 확인하는 함수
    public static int different_Link(RoadNetwork roadNetwork, Candidate candidate_start, Candidate candidate_end) {

        if(candidate_start.getInvolvedLink() == candidate_end.getInvolvedLink()){
            return 0; //같은 링크에 존재 할때
        }

        else{ // 다른 링크에 존재할 때
            if(candidate_start.getInvolvedLink().isLinkNextTo(roadNetwork, candidate_end.getInvolvedLink().getLinkID())){
                //node가 가지는 링크가 3개 이상일 때 갈림길이라고 판단
                Node linked_node;
                linked_node = candidate_start.getInvolvedLink().isLinkNextToPoint_Node(roadNetwork, candidate_end.getInvolvedLink());
                if(linked_node.includingLinks(roadNetwork.linkArrayList).size() > 2){
                    return 1; //candidate가 서로 다른 링크이고 두 링크가 맞닿아 있을때
                }
                else
                    return 2;
            }
            else{
                return 2; //candidate가 서로 다른 링크이고 두 링크가 맞닿아 있지 않을때
            }
        }
    }

    //갈림길에 gps 3초 후까지 확인하여 매칭
    //case1 : 평균을 이용한 방식
    public static void future_gps_case1(RoadNetwork roadNetwork, ArrayList<GPSPoint> gpsPointArrayList,  ArrayList<ArrayList<Candidate>> arrOfCandidates){
        ArrayList<Integer> link_list; //후보들의 link를 담아놓음


    }


}

