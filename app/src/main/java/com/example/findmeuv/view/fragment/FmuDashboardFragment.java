package com.example.findmeuv.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.findmeuv.R;

import androidx.fragment.app.Fragment;

public class FmuDashboardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dashboard_layout, container, false);

        return view;
    }

    public interface OnFragmentInteractionListener {

    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
