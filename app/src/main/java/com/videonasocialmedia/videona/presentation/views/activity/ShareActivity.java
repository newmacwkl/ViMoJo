package com.videonasocialmedia.videona.presentation.views.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.videonasocialmedia.videona.R;
import com.videonasocialmedia.videona.VideonaApplication;
import com.videonasocialmedia.videona.utils.UserPreferences;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/*
 * Copyright (C) 2015 Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 *
 * Authors:
 * Juan Javier Cabanas
 * Álvaro Martínez Marco
 *
 */
public class ShareActivity extends Activity {

    /*VIEWS*/
    @InjectView(R.id.imageButtonShare)
    ImageButton btnShare;
    @InjectView(R.id.imageButtonPlayShare)
    ImageButton btnPlay;
    @InjectView(R.id.buttonRateApp)
    ImageButton btnRateApp;


    /*CONFIG*/
    private final String LOG_TAG = this.getClass().getSimpleName();

    private static final int CHOOSE_SHARE_REQUEST_CODE = 600;

    private static VideoView videoView;
    private static MediaPlayer mediaPlayer;

    private int durationVideoRecorded;

    private String videoEdited;

    private SeekBar seekBar;

    private boolean isRunning = false;

    private ProgressDialog progressDialog;

    private UserPreferences appPrefs;

    private Typeface tf;

    private boolean music_selected = false;

    private final Runnable updateTimeTask = new Runnable() {
        @Override
        public void run() {
            updateSeekProgress();
        }
    };
    /**
     * Tracker google analytics
     */
    private VideonaApplication app;
    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share);

        ButterKnife.inject(this);

        app = (VideonaApplication) getApplication();
        tracker = app.getTracker();

        tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");

        videoView = (VideoView) findViewById(R.id.videoViewShare);

        mediaPlayer = new MediaPlayer();

        btnShare.setOnClickListener(shareClickListener());

        progressDialog = new ProgressDialog(ShareActivity.this);

        appPrefs = new UserPreferences(getApplicationContext());

        // getting intent data
        Intent in = getIntent();
        videoEdited = in.getStringExtra("MEDIA_OUTPUT");

        Log.d(LOG_TAG, "VideoEdited " + videoEdited);

        setVideoInfo();

        previewVideo();


        seekBar = (SeekBar) findViewById(R.id.seekBarShare);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (mediaPlayer.isPlaying()) {

                    mediaPlayer.pause();

                    btnPlay.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                if (fromUser) {

                    mediaPlayer.seekTo(progress);

                }

            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnPlay.setVisibility(View.INVISIBLE);

                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }

                updateSeekProgress();

            }
        });

    }

    @OnClick (R.id.buttonRateApp)
    public void rateApp(){

        //Uri uri = Uri.parse("market://details?id=" + getPackageName());
        // Redirect to videozone
        Uri uri = Uri.parse("market://details?id=" + "com.visiona.videozone");
        Intent rateApp = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(rateApp);


    }

    private void setProgressDialog() {

        /// TODO define strings progressDialog

        progressDialog.setMessage("Adding audio");
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setIndeterminate(true);
        progressDialog.show();


        // Custom progress dialog
        progressDialog.setIcon(R.drawable.activity_edit_icon_cut_normal);

        ((TextView) progressDialog.findViewById(Resources.getSystem()
                .getIdentifier("message", "id", "android")))
                .setTypeface(tf);
        ((TextView) progressDialog.findViewById(Resources.getSystem()
                .getIdentifier("message", "id", "android")))
                .setTextColor(Color.WHITE);
        ((TextView) progressDialog.findViewById(Resources.getSystem()
                .getIdentifier("alertTitle", "id", "android")))
                .setTypeface(tf);
        ((TextView) progressDialog.findViewById(Resources.getSystem()
                .getIdentifier("alertTitle", "id", "android")))
                .setTextColor(Color.WHITE);

        progressDialog.findViewById(
                Resources.getSystem().getIdentifier("topPanel", "id",
                        "android")).setBackgroundColor(getResources().getColor(R.color.videona_blue_1));
        progressDialog.findViewById(
                Resources.getSystem().getIdentifier("customPanel", "id",
                        "android"))
                .setBackgroundColor(getResources().getColor(R.color.videona_blue_1));

    }

    private void updateSeekProgress() {

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {

            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            handler.postDelayed(updateTimeTask, 50);

        }
    }

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mediaPlayer.isPlaying() && isRunning) {
                // stop playback and set playback position on the end of trim
                // border
                mediaPlayer.pause();

                //mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
            }
        }

    };



    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    /**
     * Use screen touches to toggle the video between playing and paused.
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mediaPlayer.isPlaying()) {

                mediaPlayer.pause();
                btnPlay.setVisibility(View.VISIBLE);

            } else {

                btnPlay.setVisibility(View.VISIBLE);

            }
            return true;
        } else {
            return false;
        }
    }

    private View.OnClickListener shareClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendButtonTracked(arg0.getId());
                Log.d(LOG_TAG, "shareClickListener");

          /*      videoView.pause();

                videoView.stopPlayback();

                videoView.suspend();
            */

                if (mediaPlayer.isPlaying()) {

                    mediaPlayer.pause();
                    btnPlay.setVisibility(View.VISIBLE);

                }

                ContentValues content = new ContentValues(4);
                content.put(Video.VideoColumns.TITLE, videoEdited);
                content.put(Video.VideoColumns.DATE_ADDED,
                        System.currentTimeMillis() / 1000);
                content.put(Video.Media.MIME_TYPE, "video/mp4");
                content.put(MediaStore.Video.Media.DATA, videoEdited);
                ContentResolver resolver = getContentResolver();
                Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        content);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.share_using)), CHOOSE_SHARE_REQUEST_CODE);

            }

        };
    }

    private void setVideoInfo() {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoEdited);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInmillisec = Long.parseLong(time);
        long duration = timeInmillisec / 1000;
        long hours = duration / 3600;
        long minutes = (duration - hours * 3600) / 60;
        long seconds = duration - (hours * 3600 + minutes * 60);

        durationVideoRecorded = (int) duration;

    }

    public void previewVideo() {

        try {

            videoView.setVideoPath(videoEdited);
            videoView.setMediaController(null);
            videoView.requestFocus();
            videoView.canSeekBackward();
            videoView.canSeekForward();
            videoView.setOnPreparedListener(new OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {

                    mediaPlayer = mp;

                    seekBar.setMax(durationVideoRecorded * 1000);

                    // avoid first black screen
                    // videoView.seekTo(500);

                    mediaPlayer.start();

                    mediaPlayer.seekTo(500);

                    mediaPlayer.pause();

                }
            });

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    Log.d(LOG_TAG, "EditVideoActivity setOnCompletionListener");

                    btnPlay.setVisibility(View.VISIBLE);

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

        if (music_selected) {

          /*  videoView.pause();

            videoView.stopPlayback();

            videoView.suspend();

          */
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }

            // Kill process. Needed to load again ffmpeg libraries
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);

        } else {

            setResult(Activity.RESULT_OK);

            finish();

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        isRunning = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {

        } else {
            Log.d(LOG_TAG, "requestCode " + requestCode + "resultCode " + resultCode + "intent data " + data.getDataString());

            if (requestCode == CHOOSE_SHARE_REQUEST_CODE) {

                //setResult(Activity.RESULT_OK);
                //finish();

                // Kill process. Needed to load again ffmpeg libraries
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);

            }

        }
    }

    /**
     * Sends button clicks to Google Analytics
     *
     * @param id the identifier of the clicked button
     */
    private void sendButtonTracked(int id) {
        String label;
        switch (id) {
            case R.id.imageButtonShare:
                label = "Share video";
                break;
            default:
                label = "Other";
        }
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("ShareActivity")
                .setAction("button clicked")
                .setLabel(label)
                .build());
        GoogleAnalytics.getInstance(this.getApplication().getBaseContext()).dispatchLocalHits();
    }

}
