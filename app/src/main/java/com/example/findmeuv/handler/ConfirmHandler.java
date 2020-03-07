package com.example.findmeuv.handler;

public class ConfirmHandler {
    private ConfirmListener confirmListener;

    public void yes() {
        confirmListener.onYesClick();
    }

    public void no() {
        confirmListener.onNoClick();
    }

    public void setConfirmListener(ConfirmListener confirmListener) {
        if (confirmListener != null) {
            this.confirmListener = confirmListener;
        }
    }

    public interface ConfirmListener {
        void onYesClick();
        void onNoClick();
    }
}
