package com.example.findmeuv.handler;

import java.util.Map;

public class AdapterHandler {

    private AdapterListener adapterListener;

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public void doAction(Map<String, String> data) {
        adapterListener.onAdapterAction(data);
    }

    public interface AdapterListener {
        void onAdapterAction(Map<String, String> data);
    }

}
