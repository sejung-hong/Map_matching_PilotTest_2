package com.example.map_matching_pilottest_2;

import java.util.ArrayList;

public class TBTLogic {
    // matched point기준으로 두 벡터 사이의 각도 공식 적용해서 각도 구하기 (좌표계 변환 후 적용)
    // 각도에 따라 좌/우/직진 뽑아내기
    public static ArrayList<Guidance> returnArrOfGuidance (ArrayList<Node> routeNodes) {
        ArrayList<Guidance> result = new ArrayList<>();
        Node start, center, end; // start -> center -> end의 각도 측정!

        // 추후 업데이트 예정: 연속된 직진안내 하지 않기 위해 다음 direction에 따라서 조건문 나누기 //

        boolean firstGoStraight = false;
        for (int i = 0; i < routeNodes.size() && routeNodes.size() > 2; i++) {
            start = routeNodes.get(i);
            center = routeNodes.get(i+1);
            end = routeNodes.get(i+2);

            int direction = decideDirection(start, end, center);
            /*if (i == 0) { //  첫 안내는 첫 두개 노드따져서 직진!
                result.add(new Guidance(center.getNodeID(), direction,
                        center.getName()+"까지 직진"));
                firstGoStraight = true;
                i--;
                continue;
            }*/
            if(direction == 0) { // 직진
                result.add(new Guidance(center.getNodeID(), direction,
                        center.getName()+"까지 직진"));
            } else if (direction == 1) { // 좌회전
                result.add(new Guidance(center.getNodeID(), direction,
                        center.getName()+"에서 왼쪽방향"));
            } else if (direction == 2) { // 우회전
                result.add(new Guidance(center.getNodeID(), direction,
                        center.getName()+"에서 오른쪽방향"));
            } else { // 횡단보도 미리 짜놓음 (direction == 3)
                result.add(new Guidance(center.getNodeID(), direction,
                        end.getName()+"방면으로 횡단보도 건너기"));
            }

            if (i+2 == routeNodes.size()-1) {
                break;
            }
        }
        if (routeNodes.size() == 2) {
            result.add(new Guidance (routeNodes.get(0).getNodeID(), 0,
                    routeNodes.get(0).getName()+"방면으로 직진" ));
        } else if (routeNodes.size() == 1) {
            result.add(new Guidance (-1, -1,
                    "출발지와 목적지가 동일합니다" ));
        }
        return result;
    }
    private static int decideDirection (Node node1, Node node2, Node node3) {

        GeoPoint temp;
        // 좌표계로 변경
        temp = new GeoPoint(node1.getCoordinate().getX(), node1.getCoordinate().getY());
        GeoPoint p1 = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, temp);
        temp = new GeoPoint(node2.getCoordinate().getX(), node2.getCoordinate().getY());
        GeoPoint p2 = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, temp);
        temp = new GeoPoint(node3.getCoordinate().getX(), node3.getCoordinate().getY());
        GeoPoint p3 = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, temp);

        // 각도 구하기: vector p3 -> p1 (진행방향)와 vector p3 -> p2(꺾는 방향)
        double angle = Calculation.CalAngleBetweenTwoPoint(p1, p2, p3, false);

        // 각도에 따라 direction 값 반환
        // 0: 직진, 1: 좌회전, 2: 우회전,    (추가예정) 3: 횡단보도건너기
        if (angle >= 0 && angle <= 135.0) {
            return 1; // 좌회전
        } else if (angle > 135.0 && angle <= 225.0) {
            return 0; // 직진
        } else if (angle > 225.0 && angle < 360.0) {
            return 2; // 우회전
        } else return -1; // 지우고 횡단보도 처리 로직 넣자
    }
}
