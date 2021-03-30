package com.example.map_matching_pilottest_2;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Node {
    private int nodeID; // nodeID
    private Point coordinate; // Node의 좌표
    // POI 데이터
    private String name; // 대륙명-장소명
    private int location_num; // 대륙번호 (1:아시아 2:아프리카 3:북미 4:남미 5:유럽)

    //////생성자, getter, setter, toString//////
    // ID를 String형으로 받는 Node 생성자/// 수정!! 인자에 name, location_num 추가
    public Node (String nodeID, Point coordinate, String name, String location_num) {
        this.nodeID = Integer.parseInt(nodeID);
        this.coordinate = coordinate;
        this.name = name;
        this.location_num = Integer.parseInt(location_num);
    }

    // ID를 int형으로 받는 Node 생성자
    public Node (int nodeID, Point coordinate) {
        this.nodeID = nodeID;
        this.coordinate = coordinate;
    }

    // Getters and Setters
    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public Point getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Point coordinate) {
        this.coordinate = coordinate;
    }

    public String toString() {
        return "[" + nodeID + "]\t" + "(" +coordinate.getX().toString() +", "
                + coordinate.getY().toString()+")\t" + name;
    }
    //////////////////////////////////////////////

    // [NOT VERIFIED] 이 노드를 startNode 혹은 endNode로 가지는 link의 arraylist 반환
    public ArrayList<Link> includingLinks ( ArrayList<Link> linkArrayList) {
        ArrayList<Link> resultLinks = new ArrayList<>();
        for (Link link : linkArrayList) {
            if (link.getStartNodeID() == nodeID || link.getEndNodeID() == nodeID)
                resultLinks.add(link);
        }
        return  resultLinks;
    }
}