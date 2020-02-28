package com.example.findmeuv.model.pojo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TripItinerary implements Serializable {

    private int temp = 1;

    private String tripID;
    private String tripDate;
    private String departTime;
    private String arriveTime;
    private String transportService;
    private String origin;
    private String destination;
    private String via;
    private String fare;
    private String noOfPass;
    private String bookMode;
    private String queueId;
    private String availableSeat;
    private String totalFare;
    private Map<String, String> selectedSeat = new HashMap<>();
    private String passengerJson;
    private boolean isImAlso;
    private String note;
    private String boardingPoint;
    private String locLat;
    private String locLng;
    private String deviceId = "none";
    private String bookId;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getSelectedSeatSize() {
        return selectedSeat.size();
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTransportService() {
        return transportService;
    }

    public void setTransportService(String transportService) {
        this.transportService = transportService;
    }

    public String getLocLat() {
        return locLat;
    }

    public void setLocLat(String locLat) {
        this.locLat = locLat;
    }

    public String getLocLng() {
        return locLng;
    }

    public void setLocLng(String locLng) {
        this.locLng = locLng;
    }

    public String getBoardingPoint() {
        return boardingPoint;
    }

    public void setBoardingPoint(String boardingPoint) {
        this.boardingPoint = boardingPoint;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean getIsImAlso() {
        return isImAlso;
    }

    public void setImAlso(boolean imAlso) {
        isImAlso = imAlso;
    }

    public String getPassengerJson() {
        return passengerJson;
    }

    public void setPassengerJson(String passengerJson) {
        this.passengerJson = passengerJson;
    }

    public void addSeat(String seatNo) {
        selectedSeat.put("seat"+String.valueOf(temp), seatNo);
        temp++;
    }

    public String getSeat(String key) {
        return selectedSeat.get(key);
    }

    public String getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(String totalFare) {
        this.totalFare = totalFare;
    }

    public String getAvailableSeat() {
        return availableSeat;
    }

    public void setAvailableSeat(String availableSeat) {
        this.availableSeat = availableSeat;
    }

    public String getTripID() {
        return tripID;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }

    public String getTripDate() {
        return tripDate;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }

    public String getDepartTime() {
        return departTime;
    }

    public void setDepartTime(String departTime) {
        this.departTime = departTime;
    }

    public String getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public String getNoOfPass() {
        return noOfPass;
    }

    public void setNoOfPass(String noOfPass) {
        this.noOfPass = noOfPass;
    }

    public String getBookMode() {
        return bookMode;
    }

    public void setBookMode(String bookMode) {
        this.bookMode = bookMode;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }
}
