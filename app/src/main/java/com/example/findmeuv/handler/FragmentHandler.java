package com.example.findmeuv.handler;

import android.os.Bundle;

public class FragmentHandler {
    private ChangeFragmentListener changeFragmentListener;

    public void setChangeFragmentListener(ChangeFragmentListener changeFragmentListener) {
        this.changeFragmentListener = changeFragmentListener;
    }

    public void changeFragment(int id, Bundle bundle) {
        changeFragmentListener.onChangeFragment(id, bundle);
    }

    public interface ChangeFragmentListener {
        void onChangeFragment(int id, Bundle bundle);
    }

}
