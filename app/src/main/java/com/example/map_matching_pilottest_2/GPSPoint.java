package com.example.map_matching_pilottest_2;

import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Random;

public class GPSPoint {
    private Point coordinate;
    private int timeStamp; // 일단 쉽게 짜려고 int형으로 생성. 필요시 수정 가능 (String형 혹은 다른 Time관련 클래스로)

    GPSPoint(int timeStamp, Point orgCoordinate, int mode, int opt, String itLooksLike, FileReader fileReader,String directoryName) throws IOException { // org means origin
        this.timeStamp = timeStamp;
        double gps_x = 0, gps_y = 0;
        long filepos = 0;
        Random random = new Random();

        switch (mode) {
            case 1: { // 1: 원래 하던대로 (표준편차 4)
                while (true) {
                    //10m 안쪽의 오차가 약 95% 가량 나도록 표준편차 4로 설정
                    gps_x = 4;// * random.nextGaussian() + orgCoordinate.getX();
                    //if(gps_x<0) gps_x=orgCoordinate.getX();
                    gps_y = 4;// * random.nextGaussian() + orgCoordinate.getY();
                    //if(gps_y<0) gps_y=orgCoordinate.getY();
                    if (gps_x < 0 && gps_y < 0 || gps_x > 100 && gps_y > 100)
                        continue;
                    else
                        break;
                }
                break;
            }
            case 2: { // 2: x혹은 y좌표만 uniform하게 (hor, ver, dia에 따라서)
                if (itLooksLike.equals("hor")) { // 가로
                    while (true) {
                        gps_x = orgCoordinate.getX();
                        gps_y = 4 * random.nextGaussian() * 0.00001 + orgCoordinate.getY();
                        if (gps_x < 0 && gps_y < 0 || gps_x > 100 && gps_y > 100)
                            continue;
                        else
                            break;
                    }
                } else if (itLooksLike.equals("ver")) { // 세로
                    while (true) {
                        /*GeoPoint in_pt = new GeoPoint(orgCoordinate.getX(), orgCoordinate.getY());
                        GeoPoint tm_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);
                        tm_pt.setX(tm_pt.getX() + 20); // x좌표에 1 더하는 오차 (일단 매칭 되게 해보려고)
                        GeoPoint gps_pt = GeoTrans.convert(GeoTrans.TM, GeoTrans.GEO, tm_pt);
                        gps_x = gps_pt.getX();
                        gps_y =  gps_pt.getY();;
                        *//*if (gps_x<0 && gps_y<0 || gps_x>100 && gps_y>100)
                            continue;
                        else*/
                        break;
                    }
                } else if (itLooksLike.equals("dia")) { // "dia" 대각선
                    // 테스트에서는 범위 밖을 벗어날 일이 없어서 while 문 없앴다 ~~^_^
                    GeoPoint in_pt = new GeoPoint(orgCoordinate.getX(), orgCoordinate.getY());
                    GeoPoint tm_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);

                    tm_pt.setX(tm_pt.getX() + 10 + 2 * random.nextGaussian()); // x좌표에 1 더하는 오차 (일단 매칭 되게 해보려고)
                    GeoPoint gps_pt = GeoTrans.convert(GeoTrans.TM, GeoTrans.GEO, tm_pt);

                    gps_x = gps_pt.getX();
                    gps_y = gps_pt.getY();

                    /*if (gps_x<0 && gps_y<0 || gps_x>100 && gps_y>100)
                           continue;
                    else*/

                } else {
                    System.out.println("에러");
                    System.out.println("Point: " + orgCoordinate);
                }

                break;
            }
            case 3: { // 3: x, y 모두 uniform 하게
                while (true) {
                    // uniform하게 x+=10
                    gps_x = orgCoordinate.getX() < 100 ? opt + orgCoordinate.getX() : orgCoordinate.getX();
                    // uniform하게 y+=10
                    gps_y = orgCoordinate.getY() < 100 ? opt + orgCoordinate.getY() : orgCoordinate.getY();
                    //if(gps_y<0) gps_y=orgCoordinate.getY();
                    if (gps_x < 0 && gps_y < 0 || gps_x > 100 && gps_y > 100)
                        continue;
                    else
                        break;
                }
                break;
            }
            case 4: { // 4: 교수님이 말한 평균 4 방식
                while (true) {
                    //10m 안쪽의 오차가 약 95% 가량 나도록 표준편차 4로 설정
                    gps_x = opt + 2 * random.nextGaussian() + orgCoordinate.getX();
                    //if(gps_x<0) gps_x=orgCoordinate.getX();
                    gps_y = opt + 2 * random.nextGaussian() + orgCoordinate.getY();
                    //if(gps_y<0) gps_y=orgCoordinate.getY();
                    if (gps_x < 0 && gps_y < 0 || gps_x > 100 && gps_y > 100)
                        continue;
                    else
                        break;
                }
                break;
            }
            case 5:{ // 일정하게 출력하기 위해서
                if(timeStamp%3==0){
                    gps_x = orgCoordinate.getX() + 3 * 0.00001;
                    gps_y = orgCoordinate.getY() + 3 * 0.00001;
                }
                else if(timeStamp%3==1){
                    gps_x = orgCoordinate.getX() + 4 * 0.00001;
                    gps_y = orgCoordinate.getY() + 4 * 0.00001;
                }
                else{
                    gps_x = orgCoordinate.getX() + 5 * 0.00001;
                    gps_y = orgCoordinate.getY() + 5 * 0.00001;
                }
                break;
            }
            case 6 :{ // 파일 읽기
                RandomAccessFile fileGPS = new RandomAccessFile(directoryName+"/data1/GPS.txt","r");
                FileReader fr = new FileReader(String.valueOf(fileGPS));
                BufferedReader bufferedReader1 = new BufferedReader(fr);
                String line = bufferedReader1.readLine();
                String[] lineArray = line.split("\t");
                gps_x = Double.parseDouble(lineArray[0]);
                gps_y = Double.parseDouble(lineArray[1]);
                break;
            }
            case 7 :{ // 파일 쓰기
                /* 기존 3번 방식 */
                if (itLooksLike.equals("hor")) { // 가로
                    while (true) {
                        gps_x = orgCoordinate.getX();
                        gps_y = 4 * random.nextGaussian() * 0.00001 + orgCoordinate.getY();
                        if (gps_x < 0 && gps_y < 0 || gps_x > 100 && gps_y > 100)
                            continue;
                        else
                            break;
                    }
                } else if (itLooksLike.equals("dia")) { // "dia" 대각선
                    // 테스트에서는 범위 밖을 벗어날 일이 없어서 while 문 없앴다 ~~^_^
                    GeoPoint in_pt = new GeoPoint(orgCoordinate.getX(), orgCoordinate.getY());
                    GeoPoint tm_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);

                    tm_pt.setX(tm_pt.getX() + 10 + 2 * random.nextGaussian()); // x좌표에 1 더하는 오차 (일단 매칭 되게 해보려고)
                    GeoPoint gps_pt = GeoTrans.convert(GeoTrans.TM, GeoTrans.GEO, tm_pt);

                    gps_x = gps_pt.getX();
                    gps_y = gps_pt.getY();

                } else {
                    System.out.println("에러");
                    System.out.println("Point: " + orgCoordinate);
                }
                File file2 = new File(directoryName+"/data1/GPS.txt");
                try{
                    BufferedWriter bw = new BufferedWriter(new FileWriter(file2,true));
                    PrintWriter pw = new PrintWriter(bw,true);
                    pw.write(gps_x+"\t"+gps_y+"\n");
                    pw.flush();
                    pw.close();
                    System.out.print("나 들어와쪄\n");
                }catch (Exception e){e.printStackTrace();}
                break;
            }
            default: {
                System.out.println("ERROR. Invalid GPSGenMode");
                break;
            }
        }

        /*//오차 uniform하게 생성
        gps_x = (orgCoordinate.getX() > 95)? orgCoordinate.getX() : orgCoordinate.getX() + 2;
        gps_y = (orgCoordinate.getX() > 95)? orgCoordinate.getX() : orgCoordinate.getY() + 2;*/
        coordinate = new Point(gps_x, gps_y);
    }

    GPSPoint(int timeStamp, Point coordinate) {
        this.timeStamp = timeStamp;
        this.coordinate = coordinate;
    }

    public String toString() {
        return "[" + timeStamp + "] " + coordinate;
    }

    public Point getPoint() {
        return coordinate;
    }

    public Double getX() {
        return coordinate.getX();
    }

    public Double getY() {
        return coordinate.getY();
    }

}
