package com.videonasocialmedia.vimojo.sound.presentation.views.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Video;
import com.videonasocialmedia.vimojo.presentation.views.activity.EditActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.GalleryActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.SettingsActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.VimojoActivity;
import com.videonasocialmedia.vimojo.presentation.views.customviews.VideonaPlayerExo;
import com.videonasocialmedia.vimojo.presentation.views.listener.VideonaPlayerListener;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.presenters.VoiceOverPresenter;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.views.VoiceOverView;
import com.videonasocialmedia.vimojo.utils.Constants;
import com.videonasocialmedia.vimojo.utils.TimeUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ruth on 14/09/16.
 */
public class VoiceOverActivity extends VimojoActivity implements VoiceOverView, VideonaPlayerListener,
        View.OnTouchListener {

    private static final String VOICE_OVER_POSITION = "voice_over_position";
    private static final String VOICE_OVER_PROJECT_POSITION = "voice_over_project_position";
    private static final String TAG = "VoiceOverActivity";

    @Bind(R.id.videona_player)
    VideonaPlayerExo videonaPlayer;
    @Bind(R.id.text_time_video_voice_over)
    TextView timeTag;
    @Bind(R.id.text_time_start_voice_over)
    TextView timeStart;
    @Bind(R.id.text_time_final_voice_over)
    TextView timeFinal;
    @Bind(R.id.progressBar_voice_over)
    ProgressBar progressBarVoiceOver;
    @Bind(R.id.button_voice_over_accept)
    ImageButton buttonVoiceOverAccept;
    @Bind(R.id.button_voice_over_cancel)
    ImageButton buttonVoiceOverCancel;
    @Bind (R.id.button_record_voice_over)
    ImageButton buttonRecordVoiceOver;

    int videoIndexOnTrack;
    private VoiceOverPresenter presenter;
    private int currentVoiceOverPosition = 0;
    private int startTime = 0;

    private CountDownTimer timer;
    private int millisecondsLeft;
    private int maxDuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_voice_over);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        restoreState(savedInstanceState);

        presenter = new VoiceOverPresenter(this);
        videonaPlayer.setSeekBarEnabled(false);
        videonaPlayer.setListener(this);
        presenter.onCreate();

        buttonRecordVoiceOver.setOnTouchListener(this);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentVoiceOverPosition = savedInstanceState.getInt(VOICE_OVER_POSITION, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videonaPlayer.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
        videonaPlayer.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
        videonaPlayer.onShown(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

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
        Intent intent = new Intent(getApplicationContext(), cls);
        if (cls == GalleryActivity.class) {
            intent.putExtra("SHARE", false);
        }
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        navigateTo(EditActivity.class, videoIndexOnTrack);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(VOICE_OVER_POSITION, videonaPlayer.getCurrentPosition());
        super.onSaveInstanceState(outState);
    }

    private void navigateTo(Class cls, int currentVideoIndex) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(Constants.CURRENT_VIDEO_INDEX, currentVideoIndex);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.button_voice_over_accept)
    public void onClickVoiceOverAccept() {
        presenter.addVoiceOver();
    }

    @OnClick(R.id.button_voice_over_cancel)
    public void onClickVoiceOverCancel(){

        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        resetVoiceRecorder();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        // TODO:(alvaro.martinez) 16/09/16 Define these strings, es and eng
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.VideonaDialog);
        builder.setMessage("¿Desea descartar la locución y volver a grabarla de nuevo?").setPositiveButton("Aceptar", dialogClickListener)
                .setNegativeButton("Declinar", dialogClickListener).show();
    }

    private void resetVoiceRecorder() {
        presenter.cleanDirectory();
        progressBarVoiceOver.setProgress(0);
        refreshTimeTag(0);
        buttonRecordVoiceOver.setImageResource(R.drawable.activity_edit_sound_voice_record_normal);
        millisecondsLeft = maxDuration;
        videonaPlayer.seekTo(0);
    }


    private void refreshTimeTag(int currentPosition) {

        timeTag.setText(TimeUtils.toFormattedTime(currentPosition + startTime));
    }

    @Override
    public void initVoiceOverView(int startTime, int maxSeekBar) {
        progressBarVoiceOver.setIndeterminate(false);
        progressBarVoiceOver.setMax(maxSeekBar);
        progressBarVoiceOver.setScaleY(5f);
        maxDuration = maxSeekBar;
        millisecondsLeft = maxSeekBar - currentVoiceOverPosition;
        this.startTime = startTime;
        timeStart.setText(TimeUtils.toFormattedTime(startTime));
        timeFinal.setText(TimeUtils.toFormattedTime(maxSeekBar));
        refreshTimeTag(currentVoiceOverPosition);
    }


    @Override
    public void bindVideoList(List<Video> movieList) {

        videonaPlayer.bindVideoList(movieList);
        videonaPlayer.seekToClip(0);
        videonaPlayer.seekTo(currentVoiceOverPosition);
        videonaPlayer.muteVideo(true);
        videonaPlayer.hidePlayButton();
        // Disable ontouch view playerExo. Now you can't play/pause video.
        enableDisableView(videonaPlayer,false);
    }

    @Override
    public void resetPreview() {
        videonaPlayer.resetPreview();
    }

    @Override
    public void playVideo() {
        videonaPlayer.playPreview();
        videonaPlayer.hidePlayButton();
        videonaPlayer.muteVideo(true);
    }

    @Override
    public void pauseVideo() {
        videonaPlayer.pausePreview();
        videonaPlayer.hidePlayButton();
    }

    @Override
    public void updateSeekBar(int progress) {
        progressBarVoiceOver.setProgress(progress);
    }

    @Override
    public void navigateToEditActivity() {
        Intent intent = new Intent(this, EditActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void newClipPlayed(int currentClipIndex) {
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            // start recording.
            presenter.requestRecord();
            buttonRecordVoiceOver.setImageResource(R.drawable.activity_edit_sound_voice_record_pressed);
            timerStart(millisecondsLeft);
            return true;
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            // Stop recording and save file
            presenter.stopRecording();
            buttonRecordVoiceOver.setImageResource(R.drawable.activity_edit_sound_voice_record_add);
            timer.cancel();
            return true;
        }
        return false;
    }

    public void timerStart(int timeLengthMilli) {
        timer = new CountDownTimer(timeLengthMilli, 100) {

            @Override
            public void onTick(long milliTillFinish) {
                millisecondsLeft=(int)milliTillFinish;
                int timeProgress = maxDuration - (int) milliTillFinish;
                progressBarVoiceOver.setProgress(timeProgress);
                currentVoiceOverPosition = timeProgress;
                refreshTimeTag(timeProgress);
            }

            @Override
            public void onFinish() {
                presenter.stopRecording();
                buttonRecordVoiceOver.setImageResource(R.drawable.activity_edit_sound_voice_record_add);
                refreshTimeTag(maxDuration);
            }
        }.start();
    }

    public static void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);
        if ( view instanceof ViewGroup ) {
            ViewGroup group = (ViewGroup)view;

            for ( int idx = 0 ; idx < group.getChildCount() ; idx++ ) {
                enableDisableView(group.getChildAt(idx), enabled);
            }
        }
    }
}

