package com.example.findmeuv.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.findmeuv.R;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class UserGuideFragment extends Fragment {

    private AppViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.user_guide, container, false);

        viewModel = ViewModelProviders.of(getActivity()).get(AppViewModel.class);
        viewModel.initialize(getActivity());

        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {

            }
        });

        return view;
    }
}
