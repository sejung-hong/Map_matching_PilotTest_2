package com.example.map_matching_pilottest_2;

import java.util.ArrayList;

public class TBTLogic {
    // matched point기준으로 두 벡터 사이의 각도 공식 적용해서 각도 구하기 (좌표계 변환 후 적용)
    // 각도에 따라 좌/우/직진 뽑아내기

    // Guidance에, 양 끝에 출발과 도착을 붙여준다
    // direction이 -1이면 출발이거나 도착
    public static ArrayList<Guidance> returnFinalGuidances (ArrayList<Node> routeNodes) {
        ArrayList<Guidance> guidances = returnArrOfGuidance (routeNodes);
        /*guidances.add(0, new Guidance(routeNodes.get(0).getNodeID(), -1,
                "[출발] " + RoadNetwork.getPOI(RoadNetwork.getPOIID(-1, routeNodes.get(0).getNodeID(), -1)).getName()));*/
        guidances.add(new Guidance(routeNodes.get(routeNodes.size()-1).getNodeID(), -1,
                "이건나오면안되는뎅!"));
        return guidances;
    }
    public static ArrayList<Guidance> returnArrOfGuidance (ArrayList<Node> routeNodes) {
        ArrayList<Guidance> result = new ArrayList<>();

        Node start, center, end; // start -> center -> end의 각도 측정!
        Node n_start, n_center, n_end; // next 안내 파악을 위한 next start, next center, next end
        // 추후 업데이트 예정: 연속된 직진안내 하지 않기 위해 다음 direction에 따라서 조건문 나누기 //

        boolean continuousStraight = false;
        for (int i = 0; i < routeNodes.size() && routeNodes.size() > 2; i++) {

            start = routeNodes.get(i);
            center = routeNodes.get(i + 1);
            end = routeNodes.get(i + 2);

            int direction = decideDirection(start, end, center);

            // Array out of Bound 에러방지, 현재 index의 -3이 배열의 마지막 index보다 작거나 같을 때만
            if (i + 3 <= routeNodes.size() - 1) {
                n_start = routeNodes.get(i + 1);
                n_center = routeNodes.get(i + 2);
                n_end = routeNodes.get(i + 3);

                // next direction: 다음 start-center-end의 각도 측정
                int n_direction = decideDirection(n_start, n_end, n_center);

                // 다음 각도도 직진이면, 연속 직진인거 표시한 뒤에 안내하지 않고 continue
                if (n_direction == 0 && direction == 0) {
                    continuousStraight = true;

                    // 이와중에 처음이면? 무조건 찍어야 하므로 일단 더미 안내 생성
                    if (i == 0) {
                        result.add(new Guidance(start.getNodeID(), -1, "아직모르쥬?"));
                    }

                    // 이와중에 node 4개로 이루어진 직선경로였다면..? ##까지 직진으로 뽑고 종료
                    if (n_end.getNodeID() == routeNodes.get(routeNodes.size() - 1).getNodeID() ) {
                        result.add(new Guidance(start.getNodeID(), 0, genSentence(0, n_center.getNodeID(), n_end.getNodeID(), -1)));
                        break;
                    }
                    continue;
                }

                // [첫 지시는 무조건 직진] 첫 지시고, 이것이 직진지시지만 두 번째 지시는 직진이 아닌 경우 -> "end까지 직진"
                else if (i == 0 && n_end.getNodeID() != routeNodes.get(routeNodes.size() - 1).getNodeID() && direction == 0) {
                    // genSentence에 n_~~~ 으로 처리한 이유: end까지 직진을 구현하기 위해, 다음의 end가 n_center인 것을 이용!
                    result.add(new Guidance(start.getNodeID(), direction,
                            genSentence(direction, n_start.getNodeID(), n_center.getNodeID(), n_end.getNodeID())));
                }
                // [첫 지시는 무조건 직진] 첫 지시인데 이것이 직진지시가 아닌경우, 시작노드 ID 넣고 첫 지시는 직진이라고 해야 함 -> "center까지 직진"
                else if (i == 0) {
                    result.add(new Guidance(start.getNodeID(), direction,
                            genSentence(0, start.getNodeID(), center.getNodeID(), end.getNodeID())));
                }

                // 연속 직진이라고 표시되어있으면, start,center,end 를 next s, c, e로 업데이트
                if (continuousStraight) {
                    start = n_start;
                    center = n_center;
                    end = n_end;
                    i++;
                }
                // 다음 안내가 직진은 아니지만 이전까지 연속 직진으로 판단중인 상태일 때 연속직진 상태를 false로 변환
                if (continuousStraight && n_direction != 0) {

                    // 첫번째 상태부터 쭉 직진이었다면
                    if(result.get(0).getSentence().equals("아직모르쥬?")) {
                        result.set(0, new Guidance(result.get(0).getNodeID(), 0, genSentence(0, start.getNodeID(), center.getNodeID(), end.getNodeID())));
                    }
                    continuousStraight = false;
                }

                // 연속 직진 중인데 마지막 지시일 때 반복문 나가기 (도착만 표시하면 되므로)
                if (continuousStraight && n_end.getNodeID() == routeNodes.get(routeNodes.size() - 1).getNodeID()) {
                    // 첫번째 상태부터 쭉 직진이었다면
                    if(result.get(0).getSentence().equals("아직모르쥬?")) {
                        result.set(0, new Guidance(result.get(0).getNodeID(), 0, genSentence(0, n_center.getNodeID(), n_end.getNodeID(), -1)));
                    }
                    break;
                }

            }

            // 연속 직진인 상태라면 업데이트된 s, e, c로 계산!
            // 아니면 원래 걸로 한번 더 계산
            direction = decideDirection(start, end, center); // 일부러 한번 더 한거! 지우면 안돼요

            // 직진 상황에 continue 인 이유는 처음 한번 직진 지시를 하면 그 다음부턴 직진 지시 할 일이 없기 때문
            if (direction == 0) {
                // 다만 이전 길이 횡단보도였다면 상황이 달라짐. (횡단보도 건넌 후 직진이 가능하므로) 그건 그때 횡단보도 구현하며 반영 예정
                // >> 횡단보도 제끼자
                /*result.add(new Guidance(center.getNodeID(), direction, end.getName()+"까지 직진"));*/
                continue;
            } else if (direction == 1) { // 좌회전
                result.add(new Guidance(center.getNodeID(), direction,
                        genSentence(direction, start.getNodeID(), center.getNodeID(), end.getNodeID())));
            } else if (direction == 2) { // 우회전
                result.add(new Guidance(center.getNodeID(), direction,
                        genSentence(direction, start.getNodeID(), center.getNodeID(), end.getNodeID())));
            } else { // 에러!
                result.add(new Guidance(center.getNodeID(), direction,
                        "Guidance 생성 오류"));
            }
            if (i + 2 == routeNodes.size() - 1) {

                break;
            }
        }

        // 예외처리1: 루트 노드가 2개인 경우
        if (routeNodes.size() == 2) {
            result.add(new Guidance(routeNodes.get(0).getNodeID(), 0,
                    genSentence(0, routeNodes.get(0).getNodeID(), routeNodes.get(1).getNodeID(), -1)));
        }
        // 예외처리2: 루트 노드가 1개인 경우
        else if (routeNodes.size() == 1) {
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
    
    // sentence를 생성하는 메서드
    private static String genSentence (int direction, int start, int center, int end) {
        boolean hasPOIname=  true;
        String POIname = RoadNetwork.getPOI(RoadNetwork.getPOIID(start, center, end)).getName();
        if (POIname.equals("")) hasPOIname = false;

        String directionSentence = "";
        if (direction == 0) {
            if (hasPOIname) directionSentence += "방면으로 ";
            directionSentence += "직진";
        } else if (direction == 1) {
            if (hasPOIname) directionSentence += "에서 ";
            directionSentence += "왼쪽 방향";
        } else if (direction == 2) {
            if (hasPOIname) directionSentence += "에서 ";
            directionSentence += "오른쪽 방향";
        } else {
            directionSentence =  "direction 잘못된 수 들어옴";
        }
        return POIname + directionSentence;
    }
}
