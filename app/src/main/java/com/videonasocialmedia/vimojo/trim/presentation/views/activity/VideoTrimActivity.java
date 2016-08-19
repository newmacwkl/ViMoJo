package com.videonasocialmedia.vimojo.trim.presentation.views.activity;
/*
 * Copyright (C) 2015 Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 *
 * Authors:
 * Álvaro Martínez Marco
 *
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.BubbleThumbRangeSeekbar;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.videonasocialmedia.vimojo.BuildConfig;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Video;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.TrimPreviewPresenter;
import com.videonasocialmedia.vimojo.presentation.mvp.views.TrimView;
import com.videonasocialmedia.vimojo.presentation.views.activity.EditActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.GalleryActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.SettingsActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.VimojoActivity;
import com.videonasocialmedia.vimojo.presentation.views.customviews.VideonaPlayer;
import com.videonasocialmedia.vimojo.presentation.views.listener.VideonaPlayerListener;
import com.videonasocialmedia.vimojo.utils.Constants;
import com.videonasocialmedia.vimojo.utils.TimeUtils;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;

import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VideoTrimActivity extends VimojoActivity implements TrimView,
        OnRangeSeekbarChangeListener, VideonaPlayerListener {

    @Bind(R.id.text_start_trim)
    TextView startTimeTag;
    @Bind(R.id.text_end_trim)
    TextView stopTimeTag;
    @Bind(R.id.text_time_trim)
    TextView durationTag;
    @Bind(R.id.trim_rangeSeekBar)
    BubbleThumbRangeSeekbar trimmingRangeSeekBar;
    @Bind(R.id.videona_player)
    VideonaPlayer videonaPlayer;

    int videoIndexOnTrack;
    private TrimPreviewPresenter presenter;
    private Video video;
    private int videoDuration = 1;
    private int videoStartFile = 1;
    private int startTimeMs = 0;
    private int finishTimeMs = 100;
    private String TAG = "VideoTrimActivity";
    private int currentPosition = 0;
    private String VIDEO_POSITION = "video_position";
    private String VIDEO_DURATION = "video_duration";
    private String START_TIME_TAG = "start_time_tag";
    private String STOP_TIME_TAG = "stop_time_tag";
    private boolean shouldRestoreRangeSeekBar = false;
    private Video untrimmedVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_trim);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        UserEventTracker userEventTracker = UserEventTracker.getInstance(MixpanelAPI.getInstance(this, BuildConfig.MIXPANEL_TOKEN));
        presenter = new TrimPreviewPresenter(this, userEventTracker);
        videonaPlayer.initVideoPreview(this);

        Intent intent = getIntent();
        videoIndexOnTrack = intent.getIntExtra(Constants.CURRENT_VIDEO_INDEX, 0);
        restoreState(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videonaPlayer.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videonaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(videoIndexOnTrack);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(VIDEO_POSITION, 0);
            startTimeMs = savedInstanceState.getInt(START_TIME_TAG);
            finishTimeMs = savedInstanceState.getInt(STOP_TIME_TAG);
            videoDuration = savedInstanceState.getInt(VIDEO_DURATION);
            shouldRestoreRangeSeekBar = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings_edit_options:
                navigateTo(SettingsActivity.class);
                return true;
            case R.id.action_settings_edit_gallery:
                navigateTo(GalleryActivity.class);
                return true;
            case R.id.action_settings_edit_tutorial:
                //navigateTo(TutorialActivity.class);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void navigateTo(Class cls) {
        startActivity(new Intent(getApplicationContext(), cls));
    }

    @Override
    public void onBackPressed() {
        navigateTo(EditActivity.class, videoIndexOnTrack);
        finish();
    }

    private void navigateTo(Class cls, int currentVideoIndex) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(Constants.CURRENT_VIDEO_INDEX, currentVideoIndex);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(VIDEO_POSITION, videonaPlayer.getCurrentPosition());
        outState.putInt(START_TIME_TAG, startTimeMs);
        outState.putInt(STOP_TIME_TAG, finishTimeMs);
        outState.putInt(VIDEO_DURATION, videoDuration);
        super.onSaveInstanceState(outState);
    }

    @OnClick(R.id.button_trim_accept)
    public void onClickTrimAccept() {
        presenter.setTrim(startTimeMs + videoStartFile, finishTimeMs + videoStartFile);
        navigateTo(EditActivity.class, videoIndexOnTrack);
        finish();
    }

    @OnClick(R.id.button_trim_cancel)
    public void onClickTrimCancel() {
        navigateTo(EditActivity.class, videoIndexOnTrack);
    }

    @Override
    public void showTrimBar(int videoStartTime, int videoStopTime, int videoDuration) {
        if (!shouldRestoreRangeSeekBar) {
            startTimeMs = videoStartTime;
            videoStartFile = videoStartTime;
            finishTimeMs = videoStopTime;
            this.videoDuration = videoDuration;

        }
        initTrimmingBars();

    }

    private void initTrimmingBars() {
        trimmingRangeSeekBar.setMinValue(0f);
        trimmingRangeSeekBar.setMaxValue(videoDuration + startTimeMs);
        trimmingRangeSeekBar.setMinStartValue(startTimeMs);
        trimmingRangeSeekBar.setMaxStartValue(finishTimeMs);
        trimmingRangeSeekBar.setOnRangeSeekbarChangeListener(this);
        updateTrimingTextTags();
        shouldRestoreRangeSeekBar=false;
    }

    @Override
    public void refreshDurationTag(int duration) {
        durationTag.setText(TimeUtils.toFormattedTime(duration));
    }

    @Override
    public void refreshStartTimeTag(int startTime) {
        startTimeTag.setText(TimeUtils.toFormattedTime(startTime));
    }

    @Override
    public void refreshStopTimeTag(int stopTime) {
        stopTimeTag.setText(TimeUtils.toFormattedTime(stopTime));
    }

    @Override
    public void playPreview() {
        videonaPlayer.playPreview();
    }

    @Override
    public void pausePreview() {
        videonaPlayer.pausePreview();
    }

    @Override
    public void seekTo(int timeInMsec) {
        videonaPlayer.seekTo(timeInMsec);
    }

    @Override
    public void showPreview(List<Video> movieList) {
        video = movieList.get(0);
        // TODO(jliarte): check this workarround.
        untrimmedVideo = new Video(video);
        untrimmedVideo.setStartTime(video.getStartTime());
        untrimmedVideo.setStopTime(video.getStopTime());

        List<Video> untrimedMovieList = new LinkedList<>();
        untrimedMovieList.add(untrimmedVideo);
        // end of workarround.

        videoDuration = video.getDuration();

        videonaPlayer.initPreviewLists(untrimedMovieList);
        videonaPlayer.initPreview(currentPosition);
    }

    @Override
    public void showError(String message) {
    }

    private void updateTrimingTextTags() {
        startTimeTag.setText(TimeUtils.toFormattedTime(startTimeMs));
        stopTimeTag.setText(TimeUtils.toFormattedTime(finishTimeMs));

        int duration = finishTimeMs - startTimeMs;
        durationTag.setText(TimeUtils.toFormattedTime(duration));
    }


    @Override
    public void valueChanged(Number minValue, Number maxValue) {
        Log.d(TAG, "ValueChanged minValue " + minValue + " maxValue " + maxValue);
        videonaPlayer.pausePreview();
        if (!shouldRestoreRangeSeekBar) {
            int min = minValue.intValue();
            int max = maxValue.intValue();
            startTimeMs = min;
            finishTimeMs = max;
            updateTrimingTextTags();
            updateTimeVideoPlaying();

        }
    }

    private void updateTimeVideoPlaying() {
        if (untrimmedVideo != null){
            untrimmedVideo.setStartTime(startTimeMs);
            untrimmedVideo.setStopTime(finishTimeMs);
            List<Video> untrimedMovieList = new LinkedList<>();
            untrimedMovieList.add(untrimmedVideo);
            videonaPlayer.initPreviewLists(untrimedMovieList);
            seekTo(startTimeMs);
            videonaPlayer.initPreview(currentPosition);
        }
    }


    @Override
    public void newClipPlayed(int currentClipIndex) {
    }

}
