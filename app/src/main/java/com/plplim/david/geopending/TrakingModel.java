package com.plplim.david.geopending;

import java.util.HashMap;
import java.util.Map;

public class TrakingModel {
    public String uid;
    public String destinationUid;
    public double ruleLat;
    public double ruleLong;
    public double ruleRadius;
    public double destinationLatitude;
    public double destinationLongitude;
    public Object timestamp;
    public boolean destinationCheck = false;

    public TrakingModel() {
    }

    public TrakingModel(String uid, String destinationUid, double ruleLat, double ruleLong, double ruleRadius,
                        double destinationLatitude, double destinationLongitude, Object timestamp, boolean destinationCheck) {
        this.uid = uid;
        this.destinationUid = destinationUid;
        this.ruleLat = ruleLat;
        this.ruleLong = ruleLong;
        this.ruleRadius = ruleRadius;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.timestamp = timestamp;
        this.destinationCheck = destinationCheck;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDestinationUid() {
        return destinationUid;
    }

    public void setDestinationUid(String destinationUid) {
        this.destinationUid = destinationUid;
    }

    public double getRuleLat() {
        return ruleLat;
    }

    public void setRuleLat(double ruleLat) {
        this.ruleLat = ruleLat;
    }

    public double getRuleLong() {
        return ruleLong;
    }

    public void setRuleLong(double ruleLong) {
        this.ruleLong = ruleLong;
    }

    public double getRuleRadius() {
        return ruleRadius;
    }

    public void setRuleRadius(double ruleRadius) {
        this.ruleRadius = ruleRadius;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDestinationCheck() {
        return destinationCheck;
    }

    public void setDestinationCheck(boolean destinationCheck) {
        this.destinationCheck = destinationCheck;
    }
}
