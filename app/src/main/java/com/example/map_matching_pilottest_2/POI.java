package com.example.map_matching_pilottest_2;

public class POI {
    private int POIID; // ID
    private Point coordinate;
    private String name;

    // 아래 노드 3개 설명은 POI 엑셀 파일 참고
    private int node_end1; // POI를 중앙에 끼는 끝노드1 OR -1
    private int node_end2; // POI를 중앙에 끼는 끝노드2
    private int node_mid; // POI와 가장 가까운 노드

    public POI() {
        POIID = -1;
        coordinate = null;
        name = "";
        node_end1 = -1;
        node_end2 = -1;
        node_mid = -1;
    }

    // 특수한 경우 (한 코너로 POI가 정의될 수 없음
    public POI (String POIID, Point coordinate, String name, String node_mid) {
        this.POIID = Integer.parseInt(POIID);
        this.coordinate = coordinate;
        this.name = name;
        this.node_mid = Integer.parseInt(node_mid);
        this.node_end1 = -1; // null대신
        this.node_end2 = -1; // null대신
    }
    
    // 일반적인 경우
    public POI (String POIID, Point coordinate, String name, String node_end1, String node_mid, String node_end2) {
        this.POIID = Integer.parseInt(POIID);
        this.coordinate = coordinate;
        this.name = name;
        this.node_end1 = Integer.parseInt(node_end1);
        this.node_mid = Integer.parseInt(node_mid);
        this.node_end2 = Integer.parseInt(node_end2);
    }

    @Override
    public String toString () {
        return "[" + POIID + "]\t" + "(" +coordinate.getX().toString() +", "
                + coordinate.getY().toString()+") \t\"" + name + "\"\t" + node_end1 + "\t" + node_mid + "\t" + node_end2;
    }

    public int getPOIID() {
        return POIID;
    }

    public void setPOIID(int POIID) {
        this.POIID = POIID;
    }

    public Point getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Point coordinate) {
        this.coordinate = coordinate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNode_end1() {
        return node_end1;
    }

    public void setNode_end1(int node_end1) {
        this.node_end1 = node_end1;
    }

    public int getNode_end2() {
        return node_end2;
    }

    public void setNode_end2(int node_end2) {
        this.node_end2 = node_end2;
    }

    public int getNode_mid() {
        return node_mid;
    }

    public void setNode_mid(int node_mid) {
        this.node_mid = node_mid;
    }
}
