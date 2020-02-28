package com.example.findmeuv.utility;

import android.util.Log;

import com.example.findmeuv.model.pojo.RouteItem;
import com.example.findmeuv.view_model.AppViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseBodyJsonArrayParser {

    public List<Map<String, String>> parseJsonArray(JSONArray arr, AppViewModel viewModel) {

        List<RouteItem> routeItemList = new ArrayList<>();
        List<Map<String, String>> list = new ArrayList<>();
        try {
            for (int a=0; a<arr.length(); a++) {

                RouteItem routeItem = new RouteItem();
                JSONObject obj = arr.getJSONObject(a);
                Map<String, String> value = new HashMap<>();
                int len =obj.names().length();

                for (int b=0; b<len; b++) {

                    switch (obj.names().getString(b)) {
                        case "way_point":

                            JSONArray wayPointArr = obj.getJSONArray("way_point");
                            if (wayPointArr.length() > 0) {
                                int jsonArrayLen = wayPointArr.length();
                                PolylineOptions polylineOptions = new PolylineOptions();
                                for (int i=0; i<jsonArrayLen; i++) {
                                    JSONObject jsonObject = wayPointArr.getJSONObject(i);
                                    double lat = Double.parseDouble(jsonObject.getString("lat"));
                                    double lng = Double.parseDouble(jsonObject.getString("lng"));
                                    LatLng latLng = new LatLng(lat, lng);
                                    polylineOptions.add(latLng);
                                }
                                routeItem.setPolyLineOption(polylineOptions);
                            }
                            break;

                        case "pick_up_point_line":

                            JSONArray pickUpPointArr = obj.getJSONArray("pick_up_point_line");
                            int pickUpPointSize = pickUpPointArr.length();
                            PolylineOptions pickUpPointPolylineOptions = new PolylineOptions();
                            for (int i=0; i<pickUpPointSize; i++) {
                                JSONObject jsonObject =pickUpPointArr.getJSONObject(i);
                                double lat = Double.parseDouble(jsonObject.getString("lat"));
                                double lng = Double.parseDouble(jsonObject.getString("lng"));
                                LatLng latLng = new LatLng(lat, lng);
                                pickUpPointPolylineOptions.add(latLng);
                            }
                            routeItem.setValidPickUpPointLine(pickUpPointPolylineOptions);
                            break;

                        case "from_origin":

                            JSONArray fromOriginArr = obj.getJSONArray("from_origin");
                            int length = fromOriginArr.length();
                            PolylineOptions fromOriginPolylineOptions = new PolylineOptions();
                            for (int i=0; i<length; i++) {
                                JSONObject jsonObject = fromOriginArr.getJSONObject(i);
                                double lat = Double.parseDouble(jsonObject.getString("lat"));
                                double lng = Double.parseDouble(jsonObject.getString("lng"));
                                LatLng latLng = new LatLng(lat, lng);
                                fromOriginPolylineOptions.add(latLng);
                            }
                            routeItem.setFromOrigin(fromOriginPolylineOptions);
                            break;

                        case "to_destination":

                            JSONArray toDestinationArr = obj.getJSONArray("to_destination");
                            int leng = toDestinationArr.length();
                            PolylineOptions toDestinationPolylineOptions = new PolylineOptions();
                            for (int i=0; i<leng; i++) {
                                JSONObject jsonObject = toDestinationArr.getJSONObject(i);
                                double lat = Double.parseDouble(jsonObject.getString("lat"));
                                double lng = Double.parseDouble(jsonObject.getString("lng"));
                                LatLng latLng = new LatLng(lat, lng);
                                toDestinationPolylineOptions.add(latLng);
                            }
                            routeItem.setToDestination(toDestinationPolylineOptions);
                            break;

                        case "origin_lat_lng":

                            JSONObject originLatLngObj = obj.getJSONObject("origin_lat_lng");
                            double originLat = Double.parseDouble(originLatLngObj.getString("lat"));
                            double originLng = Double.parseDouble(originLatLngObj.getString("lng"));
                            LatLng originLatLng = new LatLng(originLat, originLng);
                            routeItem.setOriginLatLng(originLatLng);
                            break;

                        case "destination_lat_lng":

                            JSONObject destinationLatLngObj = obj.getJSONObject("destination_lat_lng");
                            double destinationLat = Double.parseDouble(destinationLatLngObj.getString("lat"));
                            double destinationLng = Double.parseDouble(destinationLatLngObj.getString("lng"));
                            LatLng destinationLatLng = new LatLng(destinationLat, destinationLng);
                            routeItem.setDestLatLng(destinationLatLng);
                            break;

                        case "nearest_lat_lng":

                            JSONObject nearestLatLngObj = obj.getJSONObject("nearest_lat_lng");
                            double nearestLat = Double.parseDouble(nearestLatLngObj.getString("lat"));
                            double nearestLng = Double.parseDouble(nearestLatLngObj.getString("lng"));
                            LatLng nearestLatLng = new LatLng(nearestLat, nearestLng);
                            routeItem.setNearestLatLng(nearestLatLng);
                            break;

                        case "current_location":
                            JSONObject currentLocationLatLngObj = obj.getJSONObject("current_location");
                            double currentLocationLat = Double.parseDouble(currentLocationLatLngObj.getString("lat"));
                            double currentLocationLng = Double.parseDouble(currentLocationLatLngObj.getString("lng"));
                            LatLng currentLocationLatLng = new LatLng(currentLocationLat, currentLocationLng);
                            routeItem.setCurrentLocationLatLng(currentLocationLatLng);
                            break;
                        case "pick_up_loc":

                            if (obj.getString("pick_up_loc").contains("Terminal")) {
                                value.put(obj.names().getString(b), obj.getString(obj.names().getString(b)));
                            } else {
                                JSONObject pickUpLatLngObj = obj.getJSONObject("pick_up_loc");
                                double pickUpLocLat = Double.parseDouble(pickUpLatLngObj.getString("lat"));
                                double pickUpLocLng = Double.parseDouble(pickUpLatLngObj.getString("lng"));
                                LatLng pickUpLatLng = new LatLng(pickUpLocLat, pickUpLocLng);
                                routeItem.setPickUpLoc(pickUpLatLng);
                            }
                            break;

                        default:
                            value.put(obj.names().getString(b), obj.getString(obj.names().getString(b)));
                            break;
                    }
                }

                routeItemList.add(routeItem);
                list.add(value);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("DebugLog", "ERROR: DATA->parseJsonArray MSG: " + e.getMessage());

            Map<String, String> value = new HashMap<>();
            value.put("json_error", "Json Object Error");
            list.add(value);
        }
        viewModel.setRouteItemList(routeItemList);
        return list;
    }
}
