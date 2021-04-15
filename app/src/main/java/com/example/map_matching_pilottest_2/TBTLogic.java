package com.example.map_matching_pilottest_2;

import java.util.ArrayList;

public class TBTLogic {
    // matched point기준으로 두 벡터 사이의 각도 공식 적용해서 각도 구하기 (좌표계 변환 후 적용)
    // 각도에 따라 좌/우/직진 뽑아내기

    // Guidance에, 양 끝에 출발과 도착을 붙여준다
    // direction이 -1이면 출발이거나 도착
    public static ArrayList<Guidance> returnFinalGuidances (ArrayList<Node> routeNodes) {
        ArrayList<Guidance> guidances = returnArrOfGuidance (routeNodes);
        guidances.add(0, new Guidance(routeNodes.get(0).getNodeID(), -1,
                "출발"));
        guidances.add(new Guidance(routeNodes.get(routeNodes.size()-1).getNodeID(), -1,
                "도착"));
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
            center = routeNodes.get(i+1);
            end = routeNodes.get(i+2);

            int direction = decideDirection(start, end, center);

            if (i+3 <= routeNodes.size()-1) { // 에러방지
                n_start = routeNodes.get(i+1);
                n_center = routeNodes.get(i+2);
                n_end = routeNodes.get(i+3);

                int n_direction = decideDirection(n_start, n_end, n_center);

                if (n_direction == 0 && n_end.getNodeID() != routeNodes.get(routeNodes.size()-1).getNodeID() && direction == 0) { // 현재와 다음 안내가 직진이라면
                    continuousStraight = true;
                    continue;
                } else if (i == 0 && n_end.getNodeID() != routeNodes.get(routeNodes.size()-1).getNodeID() && direction == 0) { // 첫 지시는 직진, 두 번째 지시는 직진이 아닌 경우
                    result.add(new Guidance(start.getNodeID(), direction,
                            end.getName()+"까지 직진"));
                } else if (i == 0) { // 첫 지시인데 이것이 직진지시가 아닌경우, 시작노드 ID 넣고 첫 지시는 직진이라고 해야 함
                    result.add(new Guidance(start.getNodeID(), direction,
                            center.getName()+"까지 직진"));
                }

                // 다음 안내가 직진은 아니지만 연속 직진으로 판단중인 상태일 때
                if (continuousStraight) {
                    start = n_start;
                    center = n_center;
                    end = n_end;
                    i++;
                }
                if (continuousStraight && n_direction != 0) {
                    continuousStraight = false;
                }

                // 연속 직진 중인데 마지막 지시일 때 반복문 나가기 (도착만 표시하면 되므로)
                if( continuousStraight &&  n_end.getNodeID() == routeNodes.get(routeNodes.size()-1).getNodeID())
                    break;
            }

            direction = decideDirection(start, end, center); // 일부러 한번 더 한거! 지우면 안돼요

            if(direction == 0) { // 직진 // continue 인 이유는 처음 한번 직진 지시를 하면 그 다음부턴 직진 지시 할 일이 없기 때문
                // 다만 이전 길이 횡단보도였다면 상황이 달라짐. (횡단보도 건넌 후 직진이 가능하므로) 그건 그때 횡단보도 구현하며 반영 예정 혹은 횡단보도 제끼자
                /*result.add(new Guidance(center.getNodeID(), direction,
                        end.getName()+"까지 직진"));*/
                continue;
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
