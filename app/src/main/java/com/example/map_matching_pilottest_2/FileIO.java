package com.example.map_matching_pilottest_2;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.os.Bundle;

import java.io.*;

public class FileIO {
    // pilot test 2
    String directoryName;

    public FileIO (String dir) {
        directoryName = dir;
    }

    RoadNetwork generateRoadNetwork () throws IOException {

        RoadNetwork roadNetwork = new RoadNetwork();

        /*=======Node.txt 파일읽어오기 작업========*/
        //파일 객체 생성
        File file1 = new File(directoryName + "/data1/Node.txt");

        System.out.println("경로 출력 : "+file1.getAbsolutePath());

        if(!file1.exists()){
            System.out.println("파일을 읽지 못함");
            return roadNetwork;
            //파일을 읽지 못하는 경우
            //emulator에서 data->data->com.example.map_matching->files에 data1(Node.txt, Link.txt)를 추가해주어야함
        }
        else {
            System.out.println("파일을 읽음");
        }

        //입력 스트림 생성
        FileReader fileReader1 = new FileReader(file1);
        //BufferedReader 클래스 이용하여 파일 읽어오기


        BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
        System.out.println("======== Node 정보 =======");
        while (bufferedReader1.ready()) {
            String line = bufferedReader1.readLine();
            String[] lineArray = line.split("\t");
            Point coordinate = new Point(lineArray[1], lineArray[2]);
            Node node = new Node(lineArray[0], coordinate); // 노드생성
            roadNetwork.nodeArrayList.add(node); // nodeArrayList에 생성한 노드 추가
            System.out.println(node);
        }
        // close the bufferedReader
        bufferedReader1.close();

        /*=======Link.txt 파일읽어오기 작업========*/
        //파일 객체 생성
        File file2 = new File(directoryName + "/data1/Link.txt");
        //입력 스트림 생성
        FileReader fileReader2 = new FileReader(file2);
        //BufferedReader 클래스 이용하여 파일 읽어오기
        BufferedReader bufferedReader2 = new BufferedReader(fileReader2);
        System.out.println("======== Link 정보 =======");
        while (bufferedReader2.ready()) {
            String line = bufferedReader2.readLine();
            String[] lineArray = line.split("\t");
            //Point coordinate = new Point (lineArray[1], lineArray[2]);
            // weight 구하기 - 피타고라스법칙 적용
            // a=밑변 b=높이 weight=(a제곱+b제곱)의 제곱근의 반올림값
            Double a = roadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[1])).getCoordinate().getX()
                    - roadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[2])).getCoordinate().getX();
            Double b = roadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[1])).getCoordinate().getY()
                    - roadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[2])).getCoordinate().getY();
            Double weight = (double) Math.round(Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2)));

            // link 생성
            Link link = new Link(lineArray[0], lineArray[1], lineArray[2], weight);

            roadNetwork.linkArrayList.add(link); // linkArrayList에 생성한 노드 추가
            System.out.println(link);
//            System.out.print("involving points:");
//            System.out.println(link.getInvolvingPointList());
        }
        // close the bufferedReader
        bufferedReader2.close();

        return roadNetwork;
    }
//유림 혹시 몰라 push
}
