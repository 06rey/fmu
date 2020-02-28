package com.example.findmeuv.handler;

public class FragmentBackPressedHandler {
    private FragmentBackPressedListener fragmentBackPressedListener;
    private FragmentBackStateListener fragmentBackStateListener;

    public FragmentBackPressedListener getFragmentBackPressedListener() {
        return fragmentBackPressedListener;
    }

    public void setFragmentBackPressedListener(FragmentBackPressedListener fragmentBackPressedListener) {
        this.fragmentBackPressedListener = fragmentBackPressedListener;
    }

    public void doBackPressed() {
        if (fragmentBackPressedListener != null) {
            fragmentBackPressedListener.onFragmentBackPressed();
        }
    }

    public void setFragmentBackStateListener(FragmentBackStateListener fragmentBackStateListener) {
        this.fragmentBackStateListener = fragmentBackStateListener;
    }

    public void finishFragment(boolean backState) {
        if (fragmentBackStateListener != null) {
            if (backState) {
                fragmentBackPressedListener = null;
                removeFragmentBackPressedListener();
            }
            fragmentBackStateListener.onFragmentBackStateResponse(backState);
        }
    }

    public void removeFragmentBackPressedListener() {
        fragmentBackPressedListener = null;
    }

    public interface FragmentBackPressedListener {
        void onFragmentBackPressed();
    }

    public interface FragmentBackStateListener {
        void onFragmentBackStateResponse(boolean backState);
    }
}
