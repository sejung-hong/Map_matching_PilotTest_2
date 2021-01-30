package com.example.map_matching_pilottest_2;

import java.util.Random;

public class GPSPoint {
    private Point coordinate;
    private int timeStamp; // 일단 쉽게 짜려고 int형으로 생성. 필요시 수정 가능 (String형 혹은 다른 Time관련 클래스로)

    GPSPoint (int timeStamp, Point orgCoordinate, int mode, int opt, String itLooksLike) { // org means origin
        this.timeStamp = timeStamp;
        double gps_x=0, gps_y=0;
        Random random = new Random();
        switch (mode) {
            case 1: { // 1: 원래 하던대로 (표준편차 4)
                while(true) {
                    //10m 안쪽의 오차가 약 95% 가량 나도록 표준편차 4로 설정
                    gps_x = 4 * random.nextGaussian() + orgCoordinate.getX();
                    //if(gps_x<0) gps_x=orgCoordinate.getX();
                    gps_y = 4 * random.nextGaussian() + orgCoordinate.getY();
                    //if(gps_y<0) gps_y=orgCoordinate.getY();
                    if (gps_x<0 && gps_y<0 || gps_x>100 && gps_y>100)
                        continue;
                    else
                        break;
                }
                break;
            }
            case 2: { // 2: x혹은 y좌표만 uniform하게 (hor, ver, dia에 따라서)
                if (itLooksLike.equals("hor")) { // 가로
                    while(true) {
                        gps_x = orgCoordinate.getX();
                        gps_y = 4 * random.nextGaussian() + orgCoordinate.getY() + 5; // 평균 위로 5, 표준편차 4.
                        if (gps_x<0 && gps_y<0 || gps_x>100 && gps_y>100)
                            continue;
                        else
                            break;
                    }
                } else if (itLooksLike.equals("ver")) { // 세로
                    while(true) {
                        gps_x = 4 * random.nextGaussian() + orgCoordinate.getX();// 평균 오른쪽으로 5, 표준편차 4.
                        gps_y = orgCoordinate.getY();
                        if (gps_x<0 && gps_y<0 || gps_x>100 && gps_y>100)
                            continue;
                        else
                            break;
                    }
                } else if (itLooksLike.equals("dia")) { // "dia" 대각선
                    while(true) {
                        // uniform하게 x+=10
                        gps_x = orgCoordinate.getX() < 100 ? opt + orgCoordinate.getX() : orgCoordinate.getX();
                        // 10m 안쪽의 오차가 약 95% 가량 나도록 표준편차 4로 설정
                        gps_y = orgCoordinate.getY();
                        //if(gps_y<0) gps_y=orgCoordinate.getY();
                        if (gps_x<0 && gps_y<0 || gps_x>100 && gps_y>100)
                            continue;
                        else
                            break;
                    }
                } else  {
                    System.out.println("에러");
                    System.out.println("Point: " + orgCoordinate);
                }

                break;
            }
            case 3: { // 3: x, y 모두 uniform 하게
                while(true) {
                    // uniform하게 x+=10
                    gps_x = orgCoordinate.getX() < 100 ? opt + orgCoordinate.getX() : orgCoordinate.getX();
                    // uniform하게 y+=10
                    gps_y = orgCoordinate.getY() < 100 ? opt + orgCoordinate.getY() : orgCoordinate.getY();
                    //if(gps_y<0) gps_y=orgCoordinate.getY();
                    if (gps_x<0 && gps_y<0 || gps_x>100 && gps_y>100)
                        continue;
                    else
                        break;
                }
                break;
            }
            case 4: { // 4: 교수님이 말한 평균 4 방식
                while(true) {
                    //10m 안쪽의 오차가 약 95% 가량 나도록 표준편차 4로 설정
                    gps_x = opt + 2 * random.nextGaussian() + orgCoordinate.getX();
                    //if(gps_x<0) gps_x=orgCoordinate.getX();
                    gps_y = opt + 2 * random.nextGaussian() + orgCoordinate.getY();
                    //if(gps_y<0) gps_y=orgCoordinate.getY();
                    if (gps_x<0 && gps_y<0 || gps_x>100 && gps_y>100)
                        continue;
                    else
                        break;
                }
                break;
            } default : {
                System.out.println("ERROR. Invalid GPSGenMode");
                break;
            }
        }

        /*//오차 uniform하게 생성
        gps_x = (orgCoordinate.getX() > 95)? orgCoordinate.getX() : orgCoordinate.getX() + 2;
        gps_y = (orgCoordinate.getX() > 95)? orgCoordinate.getX() : orgCoordinate.getY() + 2;*/
        coordinate = new Point (gps_x, gps_y);


    }

    public String toString() {
        return "[" + timeStamp+ "] " + coordinate;
    }
    public Point getPoint() {
        return coordinate;
    }

    public Double getX(){return coordinate.getX();}
    public Double getY(){return coordinate.getY();}

}
