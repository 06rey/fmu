package com.example.findmeuv.view_model;

import android.app.Activity;
import android.util.Log;

import com.example.findmeuv.model.OkhttpModel;
import com.example.findmeuv.model.ServerEventModel;
import com.example.findmeuv.model.pojo.RouteItem;

import java.util.List;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AppViewModel extends ViewModel {

    // Server sent event
    // Success
    private MutableLiveData<List<Map<String, String>>> serverSentData = new MutableLiveData<>();
    private MutableLiveData<List<Map<String, String>>> serverSentInitData = new MutableLiveData<>();
    private MutableLiveData<List<RouteItem>> routeItemList = new MutableLiveData<>();
    // Errors
    private MutableLiveData<Boolean> sseConnectionError = new MutableLiveData<>();
    private MutableLiveData<Boolean> sseServiceError = new MutableLiveData<>();
    private MutableLiveData<Boolean> sseInitStatusError = new MutableLiveData<>();
    private MutableLiveData<Boolean> sseInitDataError = new MutableLiveData<>();
    private MutableLiveData<Boolean> sseInitJsonError = new MutableLiveData<>();

    // okhttp
    // Success
    private MutableLiveData<List<Map<String, String>>> okhttpData = new MutableLiveData<>();
    // Errors
    private MutableLiveData<Boolean> okhttpConnectionError = new MutableLiveData<>();
    private MutableLiveData<Boolean> okHttpServiceError = new MutableLiveData<>();
    private MutableLiveData<Boolean> okhttpStatusError = new MutableLiveData<>();
    private MutableLiveData<Boolean> okhttpDataError = new MutableLiveData<>();
    private MutableLiveData<Boolean> okhttpJsonError = new MutableLiveData<>();


    private ServerEventModel serverEventModel;
    private OkhttpModel okhttpModel;
    private Activity activity;

    public void initialize(Activity activity) {
        this.activity = activity;
    }

    // Live data stream using server sent event
    public void serverSentEvent(Map<String, String> mapParam, String param) {
        serverEventModel = new ServerEventModel(this, activity);
        serverEventModel.serverSentEvent(mapParam, param);
    }
    // Initializing server sent event
    public void serverSentInitRequest(Map<String, String> mapParam, String method, String param) {
        serverEventModel = new ServerEventModel(this, activity);
        serverEventModel.initRequest(mapParam, method, param);
    }
    // Closing server sent event
    public void closeServerEventConnection() {
        serverEventModel.closeConnection();
    }
    // Sending okhttp request
    public void okHttpRequest(Map<String, String> mapParam, String method, String extraParam) {
        okhttpModel = new OkhttpModel(this, activity);
        okhttpModel.okHttpRequest(mapParam, method, extraParam);
    }

    /* -----------------------------------------------------------------------
     * SERVER SENT EVENT
     * -----------------------------------------------------------------------
     */

    // OBSERVERS
    public MutableLiveData<List<Map<String, String>>> getServerSentData() {
        if (serverSentData == null) {
            serverSentData = new MutableLiveData<>();
        }
        return serverSentData;
    }
    public MutableLiveData<List<Map<String, String>>> getServerSentInitData() {
        if (serverSentInitData == null) {
            serverSentInitData = new MutableLiveData<>();
        }
        return serverSentInitData;
    }

    public MutableLiveData<List<RouteItem>> getRouteItemList() {
        if (routeItemList == null) {
            routeItemList = new MutableLiveData<>();
        }
        return routeItemList;
    }

    public MutableLiveData<Boolean> getSseConnectionError() {
        if (sseConnectionError == null) {
            sseConnectionError = new MutableLiveData<>();
        }
        return sseConnectionError;
    }

    public MutableLiveData<Boolean> getSseServiceError() {
        if (sseServiceError == null) {
            sseServiceError = new MutableLiveData<>();
        }
        return sseServiceError;
    }

    public MutableLiveData<Boolean> getSseInitStatusError() {
        if (sseInitStatusError == null) {
            sseInitStatusError = new MutableLiveData<>();
        }
        return sseInitStatusError;
    }

    public MutableLiveData<Boolean> getSseInitDataError() {
        if (sseInitDataError == null) {
            sseInitDataError = new MutableLiveData<>();
        }
        return sseInitDataError;
    }

    public MutableLiveData<Boolean> getSseInitJsonError() {
        if (sseInitJsonError == null) {
            sseInitJsonError = new MutableLiveData<>();
        }
        return sseInitJsonError;
    }
    // SETTERS

    public void setServerSentData(List<Map<String, String>> serverSentData) {
        this.serverSentData.postValue(serverSentData);
    }

    public void setServerSentInitData(List<Map<String, String>> serverSentInitData) {
        this.serverSentInitData.postValue(serverSentInitData);
    }

    public void setRouteItemList(List<RouteItem> routeItemList) {
        this.routeItemList.postValue(routeItemList);
    }

    public void setSseConnectionError() {
        this.sseConnectionError.postValue(true);
    }

    public void setSseServiceError() {
        this.sseServiceError.postValue(true);
    }

    public void setSseInitStatusError() {
        this.sseInitStatusError.postValue(false);
    }

    public void setSseInitDataError() {
        this.sseInitDataError.postValue(false);
    }

    public void setSseInitJsonError() {
        this.sseInitJsonError.postValue(false);
    }

    /* -----------------------------------------------------------------------
     * OKKTTP
     * -----------------------------------------------------------------------
     */
    // OBSERVER
    public MutableLiveData<List<Map<String, String>>> getOkhttpData() {
        if (okhttpData == null) {
            okhttpData = new MutableLiveData<>();
        }
        return okhttpData;
    }

    public MutableLiveData<Boolean> getOkhttpConnectionError() {
        if (okhttpConnectionError == null) {
            okhttpConnectionError = new MutableLiveData<>();
        }
        return okhttpConnectionError;
    }

    public MutableLiveData<Boolean> getOkHttpServiceError() {
        if (okHttpServiceError == null) {
            okHttpServiceError = new MutableLiveData<>();
        }
        return okHttpServiceError;
    }

    public MutableLiveData<Boolean> getOkhttpStatusError() {
        if (okhttpStatusError == null) {
            okhttpStatusError = new MutableLiveData<>();
        }
        return okhttpStatusError;
    }

    public MutableLiveData<Boolean> getOkhttpDataError() {
        if (okhttpDataError == null) {
            okhttpDataError = new MutableLiveData<>();
        }
        return okhttpDataError;
    }

    public MutableLiveData<Boolean> getOkhttpJsonError() {
        if (okhttpJsonError == null) {
            okhttpJsonError = new MutableLiveData<>();
        }
        return okhttpJsonError;
    }
    // SETTER
    public void setOkhttpData(List<Map<String, String>> okhttpData) {
        this.okhttpData.postValue(okhttpData);
    }

    public void setOkhttpConnectionError() {
        this.okhttpConnectionError.postValue(true);
    }

    public void setOkHttpServiceError() {
        this.okHttpServiceError.postValue(true);
    }

    public void setOkhttpStatusError() {
        this.okhttpStatusError.postValue(true);
    }

    public void setOkhttpDataError() {
        this.okhttpDataError.postValue(true);
    }

    public void setOkhttpJsonError() {
        this.okhttpJsonError.postValue(true);
    }

    // Clearing this view model
    @Override
    protected void onCleared() {
        Log.d("DebugLog", "AppViewModel->onCleared");
        super.onCleared();
    }

}
