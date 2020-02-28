package com.example.findmeuv.handler;

import java.util.List;
import java.util.Map;

public interface EventListener {
    void onResponseReady(List<Map<String, String>> list);
}
