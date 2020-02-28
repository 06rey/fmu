package com.example.findmeuv.model.pojo;

public class Route {

    private String origin;
    private String destination;
    private String routeName;
    private String via;

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public Route(String origin, String destination, String routeName, String via) {
        this.routeName = routeName;
        this.origin = origin;
        this.destination = destination;
        this.via = via;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getVia() {
        return via;
    }

}
