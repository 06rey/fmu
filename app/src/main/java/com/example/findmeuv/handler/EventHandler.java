package com.example.findmeuv.handler;

import java.util.List;
import java.util.Map;

public class EventHandler {

    private EventListener eventListener;
    public Button button;

    public EventHandler() {
        this.button = new Button();
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void response(List<Map<String, String>> list) {
        eventListener.onResponseReady(list);
    }

    // Use to trigger an fake button click on parent of adapter
    public class Button {
        private ClickListener clickListener;
        private ClickListenerWithParam clickListenerWithParam;

        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }
        public void doClick() {
            clickListener.onButtonClick();
        }

        public void setClickListenerWithParam(ClickListenerWithParam clickListenerWithParam) {
            this.clickListenerWithParam = clickListenerWithParam;
        }

        public void doClickWithParam(Map<String, String> data) {
            clickListenerWithParam.onButtonClick(data);
        }
    }

    public interface ClickListener {
        void onButtonClick();
    }

    public interface ClickListenerWithParam {
        void onButtonClick(Map<String, String> data);
    }
}