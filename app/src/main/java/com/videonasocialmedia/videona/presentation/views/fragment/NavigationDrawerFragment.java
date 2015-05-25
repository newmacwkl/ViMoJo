/*
 * Copyright (c) 2015. Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 *
 * Authors:
 * Juan Javier Cabanas Abascal
 * Verónica Lago Fominaya
 */

package com.videonasocialmedia.videona.presentation.views.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.videonasocialmedia.videona.R;
import com.videonasocialmedia.videona.VideonaApplication;
import com.videonasocialmedia.videona.presentation.views.activity.AboutActivity;
import com.videonasocialmedia.videona.presentation.views.activity.GalleryActivity;
import com.videonasocialmedia.videona.presentation.views.activity.RecordActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This class is used to show the right panel of the video fx menu
 */
public class NavigationDrawerFragment extends Fragment {

    /*CONFIG*/
    /**
     * Tracker google analytics
     */
    private Tracker tracker;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.common_fragment_navigator, container, false);
        ButterKnife.inject(this, view);

        VideonaApplication app = (VideonaApplication) getActivity().getApplication();
        tracker = app.getTracker();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.fragment_navigator_record_button)
    public void navigateToRecord() {
        Intent record = new Intent(this.getActivity(), RecordActivity.class);
        startActivity(record);
    }

    @OnClick(R.id.fragment_navigator_edit_button)
    public void navigateToEdit() {
        Intent gallery = new Intent(this.getActivity(), GalleryActivity.class);
        gallery.putExtra("SHARE", false);
        startActivity(gallery);
    }


    @OnClick(R.id.fragment_navigator_share_button)
    public void navigateToShare() {

        Intent share = new Intent(this.getActivity(), GalleryActivity.class);
        share.putExtra("SHARE", true);
        startActivity(share);
    }

    @OnClick(R.id.fragment_navigator_settings_button)
    public void navigateToSettings() {
        Intent intent = new Intent(this.getActivity(), AboutActivity.class);
        startActivity(intent);
    }

    @OnClick({R.id.fragment_navigator_record_button, R.id.fragment_navigator_edit_button,
            R.id.fragment_navigator_share_button, R.id.fragment_navigator_settings_button})
    public void trackClicks(View view) {
        sendButtonTracked(view.getId());
    }


    /**
     * Sends button clicks to Google Analytics
     *
     * @param id the identifier of the clicked button
     */
    private void sendButtonTracked(int id) {
        String label;
        switch (id) {
            case R.id.fragment_navigator_record_button:
                label = "Go to Record from " + this.getActivity().getLocalClassName();
                break;
            case R.id.fragment_navigator_edit_button:
                label = "Go to edit " + this.getActivity().getLocalClassName();
                break;
            case R.id.fragment_navigator_share_button:
                label = "Go to share " + this.getActivity().getLocalClassName();
                break;
            case R.id.fragment_navigator_settings_button:
                label = "Go to settings " + this.getActivity().getLocalClassName();
                break;
            default:
                label = "Other";
        }
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Navigation Drawer")
                .setAction("button clicked")
                .setLabel(label)
                .build());
        GoogleAnalytics.getInstance(this.getActivity().getApplication().getBaseContext()).dispatchLocalHits();
    }

}
