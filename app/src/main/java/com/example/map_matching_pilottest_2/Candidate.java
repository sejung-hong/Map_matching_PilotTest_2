package com.example.map_matching_pilottest_2;

import java.util.ArrayList;

public class Candidate {

    private Point point;
    private Link involvedLink;
    private double tp;
    private double ep;
    private double tpep;
    private double ep_median;
    private double tp_median;
    private double acc_prob;// accumulated probability (이전 최대 edge와 해당 node의 ep*tp를 곱함)

    public int getPrev_index() {
        return prev_index;
    }

    public void setPrev_index(int prev_index) {
        this.prev_index = prev_index;
    }

    private int prev_index;

    public double getAcc_prob() {
        return acc_prob;
    }

    public void setAcc_prob(double acc_prob) {
        this.acc_prob = acc_prob;
    }

    public Candidate(){
        this.point = null;
        this.involvedLink = null;
        this.tp= 0.0;
        this.ep=0.0;
        this.tpep=0.0;
        this.ep_median=0.0;
        this.tp_median=0.0;
    }

    public Candidate (Point point, Link involvedLink){
        this.point = point;
        this.involvedLink = involvedLink;
        this.tp= 0.0;
        this.ep=0.0;
        this.tpep=0.0;
        this.ep_median=0.0;
        this.tp_median=0.0;
    }

    public void setPoint(Point point){this.point=point;}

    public void setInvolvedLink(Link link){this.involvedLink=link;}

    public Point getPoint() {
        return point;
    }

    public Link getInvolvedLink(){
        return involvedLink;
    }
    //유림 혹시 몰라 push

    public void setTp(double tp) {
        this.tp = tp;
    }

    public void setEp(double ep) {
        this.ep = ep;
    }

    public double getTp() {
        return tp;
    }

    public double getEp() {
        return ep;
    }

    public void setTpep(double tpep){
        this.tpep=tpep;
    }

    public double getTpep(){return tpep;}

    public void setEp_median(double ep_median) {this.ep_median = ep_median;}

    public void setTp_median(double tp_median){this.tp_median = tp_median;}

    public double getEp_median() {
        return ep_median;
    }

    public double getTp_median() {
        return tp_median;
    }

    @Override
    public String toString() {
        return "point: "+ point + "  involvedLink: " + involvedLink + " tp/ep/tpep: "+tp+"/"+ep+"/"+tpep;//+"\n";
    }

    public String toStringOnlyPoint() {
        return point.toString();
    }
    public static ArrayList<Candidate> findRadiusCandidate(ArrayList<GPSPoint> gpsPointArrayList,
                                                           ArrayList<Candidate> matchingPointArrayList, Point center,
                                                           Integer Radius, RoadNetwork roadNetwork, int timestamp,
                                                           Emission emission, Transition transition) {
        ArrayList<Candidate> resultCandidate = new ArrayList<>();
        for (int i = 0; i < roadNetwork.linkArrayList.size(); i++) {
            double startX = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getStartNodeID()).getCoordinate().getX();
            double startY = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getStartNodeID()).getCoordinate().getY();
            double endX = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getEndNodeID()).getCoordinate().getX();
            double endY = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getEndNodeID()).getCoordinate().getY();

            Vector2D vectorFromStartToCenter = new Vector2D(center.getX() - startX, center.getY() - startY);
            Vector2D vectorFromEndToCenter = new Vector2D(center.getX() - endX, center.getY() - endY);
            Vector2D vectorFromEndToStart = new Vector2D(startX - endX, startY - endY);

            double dotProduct1 = vectorFromStartToCenter.dot(vectorFromEndToStart);
            double dotProduct2 = vectorFromEndToCenter.dot(vectorFromEndToStart);

            if (dotProduct1 * dotProduct2 <= 0) {
                //System.out.println("어허");
                //System.out.println("dot Product : "+dotProduct1+", "+dotProduct2);
                Candidate candidate = new Candidate();
                candidate.setInvolvedLink(roadNetwork.linkArrayList.get(i));
                Vector2D vectorStart = new Vector2D(startX, startY);
                Vector2D vectorC = new Vector2D(center.getX(), center.getY()); //원점에서 시작해 center로의 vector
                Vector2D vectorH = vectorStart.getAdded(vectorFromEndToStart.getMultiplied(
                        (vectorC.getSubtracted(vectorStart).dot(vectorFromEndToStart))
                                / Math.pow(vectorFromEndToStart.getLength(), 2))); //원점에서 시작해 수선의 발로의 vector
                candidate.setPoint(new Point(vectorH.getX(), vectorH.getY())); //수선의 발 vector의 x와 y값을 candidate의 point로 대입
                if (Calculation.coordDistanceofPoints(center, candidate.getPoint()) > Radius) continue;
                resultCandidate.add(candidate);
//////////////////////////////////////////
                //candidate마다 ep, tp 구하기
                Calculation.calculationEP(candidate, center, timestamp, emission);
                Calculation.calculationTP(candidate, matchingPointArrayList, center, gpsPointArrayList, timestamp, roadNetwork, transition);

                for (Candidate c: matchingPointArrayList) {
                    emission.Emission_Median(c);
                    transition.Transition_Median(c);
                }

            }
        }
        Calculation.calculationEPTP(resultCandidate, matchingPointArrayList, timestamp);

        return resultCandidate;
    }

}
