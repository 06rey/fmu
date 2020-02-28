package com.example.findmeuv.model.pojo;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class RouteItem {

    private PolylineOptions polyLineOption;

    private PolylineOptions fromOrigin;
    private PolylineOptions toDestination;
    private PolylineOptions validPickUpPointLine;
    private LatLng originLatLng;
    private LatLng destLatLng;
    private String nearest;
    private LatLng nearestLatLng;
    private LatLng startLatLng;
    private LatLng endLatLng;
    private String origin;
    private String destination;
    private String routeId;
    private String companyName;
    private String head;
    private LatLng currentLocationLatLng;
    private LatLng pickUpLoc;

    public LatLng getPickUpLoc() {
        return pickUpLoc;
    }

    public void setPickUpLoc(LatLng pickUpLoc) {
        this.pickUpLoc = pickUpLoc;
    }

    public LatLng getCurrentLocationLatLng() {
        return currentLocationLatLng;
    }

    public void setCurrentLocationLatLng(LatLng currentLocationLatLng) {
        this.currentLocationLatLng = currentLocationLatLng;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public LatLng getStartLatLng() {
        return startLatLng;
    }

    public void setStartLatLng(LatLng startLatLng) {
        this.startLatLng = startLatLng;
    }

    public LatLng getEndLatLng() {
        return endLatLng;
    }

    public void setEndLatLng(LatLng endLatLng) {
        this.endLatLng = endLatLng;
    }

    public PolylineOptions getValidPickUpPointLine() {
        return validPickUpPointLine;
    }

    public void setValidPickUpPointLine(PolylineOptions validPickUpPointLine) {
        this.validPickUpPointLine = validPickUpPointLine;
    }

    public PolylineOptions getFromOrigin() {
        return fromOrigin;
    }

    public void setFromOrigin(PolylineOptions fromOrigin) {
        this.fromOrigin = fromOrigin;
    }

    public PolylineOptions getToDestination() {
        return toDestination;
    }

    public void setToDestination(PolylineOptions toDestination) {
        this.toDestination = toDestination;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getNearest() {
        return nearest;
    }

    public void setNearest(String nearest) {
        this.nearest = nearest;
    }

    public LatLng getNearestLatLng() {
        return nearestLatLng;
    }

    public void setNearestLatLng(LatLng nearestLatLng) {
        this.nearestLatLng = nearestLatLng;
    }

    public LatLng getDestLatLng() {
        return destLatLng;
    }

    public void setDestLatLng(LatLng destLatLng) {
        this.destLatLng = destLatLng;
    }

    public LatLng getOriginLatLng() {
        return originLatLng;
    }

    public void setOriginLatLng(LatLng originLatLng) {
        this.originLatLng = originLatLng;
    }

    public PolylineOptions getPolyLineOption() {
        return polyLineOption;
    }

    public void setPolyLineOption(PolylineOptions polyLineOption) {
        this.polyLineOption = polyLineOption;
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
}
