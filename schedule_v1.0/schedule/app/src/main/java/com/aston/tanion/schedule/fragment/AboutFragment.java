package com.aston.tanion.schedule.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.activity.TutorialActivity;

/**
 * Created by Aston Tanion on 27/07/2016.
 */
public class AboutFragment extends Fragment{
    public static final String TAG = "AboutFragment";


    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        Button tutorialButton = (Button) view.findViewById(R.id.about_tutorial);
        tutorialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the tutorial activity
                Intent intent = TutorialActivity.newIntent(getActivity());
                startActivity(intent);
            }
        });
        return view;
    }
}