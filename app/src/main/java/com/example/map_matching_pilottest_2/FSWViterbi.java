package com.example.map_matching_pilottest_2;

import java.util.ArrayList;
import java.util.Arrays;

public class FSWViterbi {
    private static final ArrayList<Candidate[]> subpaths_yhtp = new ArrayList<>();
    private static final ArrayList<Candidate> matched_yhtp = new ArrayList<>();
    private static double correctness_yhtp;

    public static ArrayList<Candidate> getMatched_yhtp() {
        return matched_yhtp;
    }
    public static ArrayList<Candidate> getMatched_sjtp() {
        return matched_sjtp;
    }
    public static void setMatched_sjtp(Candidate candidate){
        matched_sjtp.add(candidate);
    }
    public static void setMatched_yhtp(Candidate candidate){
        matched_sjtp.add(candidate);
    }


    private static final ArrayList<Candidate[]> subpaths_sjtp = new ArrayList<>();
    private static final ArrayList<Candidate> matched_sjtp = new ArrayList<>();
    private static double correctness_sjtp;
    public static final Candidate[][] remainCandidates = new Candidate[2][];

    // gps 받아올때마다 FSW비터비로 매칭하는 메서드 -윤혜tp
    public static ArrayList<Candidate> generateMatched(double[][] tp_matrix ,int wSize,int wNext, ArrayList<ArrayList<Candidate>> arrOfCandidates,
                                                       ArrayList<GPSPoint> gpsPointArrayList, Transition transition,
                                                       ArrayList<ArrayList<Candidate>> remainCandidate ,int timeStamp, RoadNetwork roadNetwork,
                                                       String tp_type, boolean viterbifirst) {
        // arrOfCandidates를 순회하며 찾은 path의 마지막을 matching_success에 추가하는 loop
        // t는 timestamp를 의미
        // subpath 생성 및 matched arraylist에 저장
        // 현재 candidates와 다음 candidates로 가는 t.p와 e.p곱 중 최대 값을 가지는 curr와 그 index를 maximum_tpep[현재]에 저장

        /*
        Vector2D test1 = new Vector2D(2,2);
        Vector2D test2 = new Vector2D(4,2);
        Vector2D test3 = new Vector2D(2,5);
        Vector2D test4 = new Vector2D(-4,3);
        System.out.print("유림:test결과\n");
        System.out.print("restult1: "+test1.getAngle(test2)+"\n");
        System.out.print("restult2: "+test3.getAngle(test2)+"\n");
        System.out.print("restult3: "+test4.getAngle(test2)+"\n");
*/

        double alpha = 0.5;
        double maximum_prob = 0;
        Candidate[] subpath = new Candidate[wSize-wNext]; // path의 길이를 도출할 길이로 설정
        //System.out.println("yhtp debugging");
        int i=1;
        if(viterbifirst) i=0;
        viterbifirst=false;
        for (; i < wSize-1 ; i++) { // i moves in window
            ArrayList<Candidate> curr_candidates = arrOfCandidates.get(i);
            ArrayList<Candidate> next_candidates = arrOfCandidates.get(i+1);
            //System.out.println("☆origin point:" + subRPA.get(i+1));// 테스트 하려면 메서드 인자에 subGPSs추가해야함
            //System.out.println("☆GPS point: " + subGPSs.get(i));// 테스트 하려면 메서드 인자에 subRPA추가해야함
            // 다음 candidate를 하나씩 순회
            for (Candidate nc : next_candidates) {
                maximum_prob = 0;
                nc.setEp(Emission.Emission_pro(nc, gpsPointArrayList.get(timeStamp-1).getPoint(), nc.getPoint(), timeStamp));
                System.out.println("  nc: " + nc.getPoint() + "/ ep: " + nc.getEp());
                // 현재 candidate를 하나씩 순회하며
                for (Candidate cc : curr_candidates) {
                    GeoPoint in_pt = new GeoPoint(nc.getPoint().getX(), nc.getPoint().getY());
                    GeoPoint gps_t = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);
                    Vector2D gps_v = new Vector2D(gps_t.getX(),gps_t.getY());
                    in_pt = new GeoPoint(cc.getPoint().getX(), cc.getPoint().getY());
                    GeoPoint cc_t = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);
                    Vector2D cc_v = new Vector2D(cc_t.getX(),cc_t.getY());
                    cc.setVector(Vector2D.subtract(gps_v,cc_v));
                    double tp;
                    if (tp_type.equals("yh")) {
                        tp = tp_matrix[cc.getInvolvedLink().getLinkID()][nc.getInvolvedLink().getLinkID()];
                        cc.setTp(tp); //tp 저장 cc -> nc로 이동할 확률을 cc에 저장 // 굳이 tp 저장할 필요 없음.
                    } else {
                        //System.out.println("[FSWViterbi] cc:" + cc);
                        tp = Transition.Transition_pro(gpsPointArrayList.get(timeStamp-1).getPoint(), gpsPointArrayList.get(timeStamp-3).getPoint(), cc, nc, roadNetwork);
                        //tp = cc.getTp();
                        cc.setTp(tp);
                    }
                    //cc.setTp(tp); //tp 저장 cc -> nc로 이동할 확률을 cc에 저장 // 굳이 tp 저장할 필요 없음.
                    //cc.setEp(Emission.Emission_pro(cc, gpsPointArrayList.get(timeStamp-2).getPoint(), nc.getPoint(), timeStamp));
                    //이미 저장되어있으므로 저장할 필요 없음.

                    double prob = tp * nc.getEp();

                    cc.setTpep(prob);
                    System.out.println("    cc: " + cc);

                    if (i == 0) { // window내 window의 시작 부분
                        if (maximum_prob < prob * cc.getEp()) { // 최대의 acc_prob를 갱신하며 이전전
                            maximum_prob = prob * cc.getEp();// window의 시작부분이므로 현재의 ep * 다음의 ep * 현재->다음의tp를 Acc_prob에 축적한다
                            nc.setPrev_index(curr_candidates.indexOf(cc));
                            nc.setAcc_prob(maximum_prob);
                            nc.setMax_tp_median(nc.getTp_median()); // 확률이 제일 높은 median값을 저장
                            //System.out.println("    MAX!");
                        }
                    } else { // window 내 그 외의 부분
                        if (maximum_prob < prob * cc.getAcc_prob()) {
                            maximum_prob = prob * cc.getAcc_prob(); // 현재의 acc_prob * 다음의 ep * 현재->다음의 tp를 Acc_prob에 축적한다
                            nc.setPrev_index(curr_candidates.indexOf(cc));
                            nc.setAcc_prob(maximum_prob);
                            nc.setMax_tp_median(nc.getTp_median()); // 확률이 제일 높은 median값을 저장
                            //System.out.println("    MAX!");
                        }
                    }
                }
            }
        }
        System.out.print("유림 : 윈도우\n");
        for(int y = 0; y<arrOfCandidates.size();y++){
            System.out.print("[Window Number : "+y+"]\n");
            for(int r = 0; r<arrOfCandidates.get(y).size();r++)
                System.out.print(arrOfCandidates.get(y).get(r).toStringyr());
        }

        /* 각도 계산해서 넣는 부분 */
        GeoPoint in_pt = new GeoPoint(gpsPointArrayList.get(timeStamp-2).getX(), gpsPointArrayList.get(timeStamp-2).getY());
        GeoPoint start_t = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);
        in_pt = new GeoPoint(gpsPointArrayList.get(timeStamp-1).getX(), gpsPointArrayList.get(timeStamp-1).getY());
        GeoPoint end_t = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);
        Vector2D start_v = new Vector2D(start_t.getX(),start_t.getY());
        Vector2D end_v = new Vector2D(end_t.getX(),end_t.getY());
        //마지막 두 벡터를 더해서 구한 벡터 = cal_v -> 이 친구와 전 값들을 비교!
        Vector2D cal_v = Vector2D.add(start_v,end_v);


        System.out.print("유림 : 벡터누적\n");
        for(int j=0;j<wSize-wNext;j++){//앞의 3개의 window의 candidate들만 벡터값 누적해서 acc 계산
            System.out.print("유림 : 벡터누적 윈도우"+j+"\n");
            for(Candidate c : arrOfCandidates.get(j)){
                System.out.print("TP*EP: " + c.getAcc_prob()+"\n");
                c.setAcc_prob((1-alpha)*(1/(cal_v.getAngle(c.getVector())))
                        +alpha*c.getAcc_prob());
                //(1/(c.getangle & vectorA 차이))의 이유는 각도 차이가 작을 수록 더 확률이 높아야 하기 때문!
                System.out.print("각도: " + (1/(cal_v.getAngle(c.getVector())))+"\n");
                System.out.print(c.toStringyr());
            }
        }

        // (wSize - wNext)번째 candidates 중 acc_prob가 가장 높은 것 max_last_candi에 저장
        Candidate max_last_candi = new Candidate();
        double max_prob = 0;
        for (Candidate candidate : arrOfCandidates.get(wSize - wNext)) {
            if (max_prob < candidate.getAcc_prob()) {
                max_prob = candidate.getAcc_prob();
                max_last_candi = candidate;
            }
        }
        // max_last_candi를 시작으로 back tracing하여 subpath구하기
        //System.out.println("")
        Candidate tempCandi = arrOfCandidates.get(wSize - wNext -1).get(max_last_candi.getPrev_index());
        subpath[subpath.length - 1] = tempCandi;
        //int i = subpath.length - 1;
        for (int j = subpath.length - 2; j >= 0; j--) {
            tempCandi = arrOfCandidates.get(j).get(tempCandi.getPrev_index());
            subpath[j] = tempCandi;
        }

        ArrayList<Candidate> subpathArrayList;
        // 생성된 subpath를 subpaths에 추가가
        if (tp_type.equals("yh")) {
            subpaths_yhtp.add(subpath);
            subpathArrayList = new ArrayList<>(Arrays.asList(subpath));
            // subpath를 모두 매칭!!
            matched_yhtp.addAll(subpathArrayList);
        } else {
            subpaths_sjtp.add(subpath);
            subpathArrayList = new ArrayList<>(Arrays.asList(subpath));
            // subpath를 모두 매칭!!
            matched_sjtp.addAll(subpathArrayList);

            for(Candidate c:subpath){
                Emission.Emission_Median(c);
                Transition.Transition_Median(c);
            } //emission_median값, transition median 저장

        }
        System.out.print("유림 : 서브패스\n");
        for(int y = 0; y<subpathArrayList.size();y++){
            System.out.print(subpathArrayList.get(y).toStringyr());
        }
        remainCandidate.clear();
        remainCandidate.add(arrOfCandidates.get(wSize-2));
        remainCandidate.add(arrOfCandidates.get(wSize-1));
        System.out.print("유림 : 리메인\n");
        for(int y = 0; y<remainCandidate.size();y++){
            System.out.print("[Number : "+y+"]\n");
            for(int r=0;r<remainCandidate.get(y).size();r++){
                System.out.print(remainCandidate.get(y).get(r).toStringyr());
            }
        }
        return subpathArrayList;
    }
    // subpath출력 메서드 (테스트용) -윤혜tp
    public static void printSubpath(int wSize, String tp_type) {
        // subpath 출력
        int t = wSize - 2;
        ArrayList<Candidate[]> subpaths = new ArrayList<>();
        if (tp_type.equals("yh")) {
            subpaths = subpaths_yhtp;
        } else {
            subpaths = subpaths_sjtp;
        }

        for (Candidate[] subpath : subpaths) {
            System.out.print(t + "] ");
            for (int i = 0; i < subpath.length; i++) {
                System.out.print("[" + subpath[i].getInvolvedLink().getLinkID() + "]");
                if (i != subpath.length - 1)
                    System.out.print(" ㅡ ");
            }
            System.out.println();
            t++;
        }
    }

    public static void test (RoadNetwork rn, String tp_mode) {

        ArrayList<Candidate> matched;
        if (tp_mode.equals ("yh")) {
            matched = matched_yhtp;
        } else {
            matched = matched_sjtp;
        }
        int i = 0;
        double correctness = 0, total = 0;
        for (i = 0 ; i < matched_yhtp.size(); i++) {
            //System.out.println("origin link: " + rn.routePointArrayList.get(i).getLinkID() + ", matched link: " + c.getInvolvedLink().getLinkID() );
            Candidate c = matched_yhtp.get(i);
            Point point = rn.routePointArrayList.get(5*i);
            System.out.println("Point: " + point + " -> Matched: " + c);
            if (point.getLinkID() == matched.get(i).getInvolvedLink().getLinkID()) {
                correctness ++ ;
            }
            total ++;
            i++;
        }
        if (tp_mode.equals ("yh")) {
            correctness_yhtp = 100*(correctness/total);
            System.out.println("yhtp matched: " + matched_yhtp.size() + ", origin points: " + rn.routePointArrayList.size());
            System.out.println(tp_mode + "tp correctness: " + correctness_yhtp);
        } else {
            correctness_sjtp = 100*(correctness/total);
            System.out.println("sjtp matched: " + matched_yhtp.size() + ", origin points: " + rn.routePointArrayList.size());
            System.out.println(tp_mode + "tp correctness: " + correctness_sjtp);

        }

    }

    // yhtp와 sjtp 비교용 메서드 (테스트용)
    public static void compareYhtpAndSjtp () {
        System.out.println("========= Compare correctness =========\nCorrectness(yhtp) = " + correctness_yhtp);
        System.out.println("Correctness(sjtp) = " + correctness_sjtp+"\n=======================================");
    }

    public static void compareYHandSJ() {
        int i = 0;
        int j = 0;
        for (Candidate c: matched_yhtp) {
            if (c.getPoint().getX().compareTo(matched_sjtp.get(i).getPoint().getX()) != 0 ||
                    c.getPoint().getY().compareTo(matched_sjtp.get(i).getPoint().getY()) != 0) {
                System.out.println(i + "] NOT SAME!");
                System.out.println("   yh: " + c + "\n   sj:" + matched_sjtp.get(i).getPoint());
                j++;
            }
            i++;
        }
        double prob = ((double)j/(double)i)*100;
        System.out.println("yhtp의 정확도:" + correctness_yhtp);
        System.out.println("sjtp의 정확도:" + correctness_sjtp);
        //System.out.println("=> 두 tp의 매칭 결과가 다른 확률:" + prob);
    }

}