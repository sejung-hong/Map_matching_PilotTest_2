package com.example.map_matching_pilottest_2;

import java.util.ArrayList;

public class Second_2 {
    public static void Second_2(ArrayList<GPSPoint> gpsPointArrayList, int timestamp){

        if(timestamp == 1 || timestamp == 2)
            return;

        timestamp = timestamp -1; //편하게 보기 위해, timestamp = 0이면 gps가 하나 존재

        //gps값 tm좌표로 변경
        double X_later2 = gpsPointArrayList.get(timestamp).getX();
        double Y_later2 = gpsPointArrayList.get(timestamp).getY(); // 0초 전 GPS, 현재 GPS
        double X_later1 = gpsPointArrayList.get(timestamp-1).getX();
        double Y_later1 = gpsPointArrayList.get(timestamp-1).getY(); // 1초 전 GPS
        double X_later0 = gpsPointArrayList.get(timestamp-2).getX();
        double Y_later0 = gpsPointArrayList.get(timestamp-2).getY(); // 2초 전 GPS

        GeoPoint later2_geo = new GeoPoint(X_later2, Y_later2);
        GeoPoint GPS_later2 = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, later2_geo);
        GeoPoint later1_geo = new GeoPoint(X_later1, Y_later1);
        GeoPoint GPS_later1 = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, later1_geo);
        GeoPoint later0_geo = new GeoPoint(X_later0, Y_later0);
        GeoPoint GPS_later0 = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, later0_geo);

        //GPS_later1이 가운데 점
        float degree = getAngleFromThreePoints(GPS_later2,GPS_later1,GPS_later0);
        System.out.println(degree); //각도 출력
        //return 1-(degree/180);
    }

    public static float getAngleFromThreePoints(GeoPoint p1, GeoPoint p2, GeoPoint p3){
        float p12 = (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
        float p23 = (float) Math.sqrt(Math.pow(p2.x - p3.x, 2) + Math.pow(p2.y - p3.y, 2));
        float p31 = (float) Math.sqrt(Math.pow(p3.x - p1.x, 2) + Math.pow(p3.y - p1.y, 2));
        float radian = (float) Math.acos((p12*p12 + p23*p23 - p31*p31) / (2 * p12 * p23));
        float degree = (float) (radian / Math.PI * 180);
        return degree;
    }
    //출처: https://www.crocus.co.kr/1635 [Crocus]
}

