package com.example.map_matching_pilottest_2;

import java.util.ArrayList;

public class Calculation {
    /*public static Double coordDistanceofPoints(Point a, Point b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }//유클리드 거리 구하기*/

    public static double calDistance(double lat1, double lon1, double lat2, double lon2) {

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }

    public static double CalAngleBetweenTwoPoint(GeoPoint org_p1, GeoPoint org_p2, GeoPoint p3, boolean isClockWise)
    {
        // 각도 구하기: vector p1 -> p2와 vector p1 -> p3

        // p3을 원점으로 옮겨주는 의미에서 다른 점들도 모두 p3가 원점으로 갈때 이동해야하는 만큼 평행이동 해준다.
        Point p1 = new Point (org_p1.getX() -p3.getX(), org_p1.getY() - p3.getY());
        Point p2 = new Point (org_p2.getX() -p3.getX(), org_p2.getY() - p3.getY());

        //acos()나 asin()함수로 구하는 각도를 degree로 나타냈을때
        //그 범위는 0~180이다.

        //각도를 0~360으로 나타내기 위한 사전작업으로
        //기준점(ptPoint1)을 90도 회전시킨 점을 구한다.
        Point rotated90p1 = new Point (0.0,0.0);

        if(isClockWise){
            rotated90p1.setX(-p1.getY());
            rotated90p1.setY(p1.getX());
        } else {
            rotated90p1.setX(p1.getY());
            rotated90p1.setY(-p1.getX());
        }

        //앞서 계산한 점과 두번째 인자로 받은 점간의 각도를 구함.
        //이 각도가 90도보다 크다면 첫번째인자로 받은 기준점과 두번째 점간의 각도가
        //180도보다 크다는 것을 의미한다.

        double fAng = Math.acos( (rotated90p1.getX() * p2.getX() + rotated90p1.getY() * p2.getY()) /
                (Math.sqrt( rotated90p1.getX() * rotated90p1.getX() + rotated90p1.getY() * rotated90p1.getY() ) *
                        Math.sqrt(p2.getX() * p2.getX() + p2.getY() * p2.getY()) ) )
                * 360 / (2*Math.PI);

        //fAng의 크기에 따라 두점사이의 각도를 다른 방식으로 구함.
        if(fAng > 90)
            return 360 - Math.acos( (p1.getX() * p2.getX() + p1.getY() * p2.getY()) /
                    (Math.sqrt( p1.getX() * p1.getX() + p1.getY() * p1.getY() ) *
                            Math.sqrt(p2.getX() * p2.getX() + p2.getY() * p2.getY()) ) )
                    * 360 / (2*Math.PI);

        else
            return Math.acos( (p1.getX() * p2.getX() + p1.getY() * p2.getY()) /
                    (Math.sqrt( p1.getX() * p1.getX() + p1.getY() * p1.getY() ) *
                            Math.sqrt(p2.getX() * p2.getX() + p2.getY() * p2.getY()) ) )
                    * 360 / (2*Math.PI);
    }

    public static double calDistance(Point p1, Point p2) {
        return calDistance(p1.getY(), p1.getX(), p2.getY(), p2.getX());
    }

    // 주어진 도(degree) 값을 라디언으로 변환
    private static double deg2rad(double deg) {
        return (double) (deg * Math.PI / (double) 180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private static double rad2deg(double rad) {
        return (double) (rad * (double) 180d / Math.PI);
    }
}