package com.example.findmeuv.model.pojo;

public class Trip {

    private String tripID;
    private String uvRoute;
    private String tripDate;
    private String transService;
    private String plateNo;
    private String vehicleModel;
    private String status;
    private String departTime;
    private String arriveTime;
    private String vacantSeat;
    private String fare;
    private String distance;

    public Trip(String tripId, String tripDate, String uvRoute, String transService, String plateNo, String vehicleModel, String status, String departTime, String arriveTime, String vacantSeat, String fare) {
        this.tripID = tripId;
        this.uvRoute = uvRoute;
        this.tripDate = tripDate;
        this.transService = transService;
        this.plateNo = plateNo;
        this.vehicleModel = vehicleModel;
        this.status = status;
        this.departTime = departTime;
        this.arriveTime = arriveTime;
        this.vacantSeat = vacantSeat;
        this.fare = fare;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setVacantSeat(String vacantSeat) {
        this.vacantSeat = vacantSeat;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }

    public void setUvRoute(String uvRoute) {
        this.uvRoute = uvRoute;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }

    public void setTransService(String transService) {
        this.transService = transService;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDepartTime(String departTime) {
        this.departTime = departTime;
    }

    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public String getTripID() { return tripID; }

    public String getTripDate() { return tripDate; }

    public String getTransService() {
        return transService;
    }

    public String getPlateNo() {
        return plateNo;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public String getStatus() {
        return status;
    }

    public String getDepartTime() {
        return departTime;
    }

    public String getArriveTime() {
        return arriveTime;
    }

    public String getVacantSeat() {
        return vacantSeat;
    }

    public String getFare() {
        return fare;
    }
}
