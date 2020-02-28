package com.example.findmeuv.model.pojo;

public class TravelingUvExpress {

    private String tripId;
    private String companyName;
    private String destination;
    private String arrival;
    private String plateNo;
    private String model;
    private String distanceInKm;
    private String vacantSeat;
    private float distanceInMeter;

    public float getDistanceInMeter() {
        return distanceInMeter;
    }

    public void setDistanceInMeter(float distanceInMeter) {
        this.distanceInMeter = distanceInMeter;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getArrival() {
        return arrival;
    }

    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDistanceInKm() {
        return distanceInKm;
    }

    public void setDistanceInKm(String distanceInKm) {
        this.distanceInKm = distanceInKm;
    }

    public String getVacantSeat() {
        return vacantSeat;
    }

    public void setVacantSeat(String vacantSeat) {
        this.vacantSeat = vacantSeat;
    }
}
