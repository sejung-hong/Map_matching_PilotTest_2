package com.example.map_matching_pilottest_2;

public class Guidance {
    private String sentence;
    private int nodeID;
    private int direction;

    public Guidance (int nodeID, int direction, String sentence) {
        this.sentence = sentence;
        this.nodeID = nodeID;
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }


    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }
}
