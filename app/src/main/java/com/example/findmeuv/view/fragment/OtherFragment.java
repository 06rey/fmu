package com.example.findmeuv.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.view.activity.FeedbackActivity;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class OtherFragment extends Fragment {

    private AppViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.other, container, false);


        TextView btnFeedBack = view.findViewById(R.id.btnFeedBack);

        btnFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(getContext(), FeedbackActivity.class));
            }
        });

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
