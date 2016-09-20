package com.videonasocialmedia.vimojo.sound.presentation.views.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.VimojoApplication;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Music;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Video;
import com.videonasocialmedia.vimojo.presentation.views.activity.EditActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.GalleryActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.SettingsActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.VimojoActivity;
import com.videonasocialmedia.vimojo.presentation.views.customviews.VideonaPlayerExo;
import com.videonasocialmedia.vimojo.presentation.views.listener.VideonaPlayerListener;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.presenters.SoundVolumePresenter;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.views.SoundVolumeView;
import com.videonasocialmedia.vimojo.utils.Constants;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.videonasocialmedia.vimojo.presentation.views.customviews.VideonaPlayerExo.TYPE_AUDIO;

/**
 * Created by ruth on 19/09/16.
 */
public class SoundVolumeActivity extends VimojoActivity implements SeekBar.OnSeekBarChangeListener, VideonaPlayerListener, SoundVolumeView {

    private static final String SOUND_VOLUME_POSITION_VOLUME = "sound_volume_position";
    private static final String SOUND_VOLUME_PROJECT_POSITION = "sound_volume_project_position";
    private static final String TAG = "SoundVolumeActivity";

    @Bind(R.id.videona_player)
    VideonaPlayerExo videonaPlayer;
    SoundVolumePresenter presenter;
    @Bind(R.id.textView_seekBar_volume_sound)
    TextView textSeekBarVolume;
    @Bind (R.id.seekBar_volume_sound)
    SeekBar seekBarVolume;
    @Bind (R.id.button_volume_sound_accept)
    ImageButton buttonVolumeSoundAccept;
    @Bind (R.id.button_volume_sound_cancel)
    ImageButton buttonVolumeSoundCancel;
    int videoIndexOnTrack;
    private int currentSoundVolumePosition = 0;
    private int currentProjectPosition = 0;
    private TrackRenderer audioRenderer;
    Music voiceOver;
    ExoPlayer exoPlayer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_volume);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        String voiceOverPath = Constants.PATH_APP_EDITED + File.separator + "AUD_Prueba.mp4";
        voiceOver = new Music(R.drawable.gatito_rules_pressed, "Voice over recorded", R.raw.audio_hiphop,
                voiceOverPath, R.color.folk, "Author", "04:35");

        presenter = new SoundVolumePresenter(this);
        videonaPlayer.setListener(this);
        seekBarVolume.setOnSeekBarChangeListener(this);
        presenter.onCreate();
        seekBarVolume.setProgress(50);
        textSeekBarVolume.setText("50 %");



        restoreState(savedInstanceState);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentSoundVolumePosition = savedInstanceState.getInt(SOUND_VOLUME_POSITION_VOLUME, 0);
            currentProjectPosition = savedInstanceState.getInt(SOUND_VOLUME_PROJECT_POSITION, 0);
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
        videonaPlayer.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
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
        outState.putInt(SOUND_VOLUME_PROJECT_POSITION, videonaPlayer.getCurrentPosition()  );
        outState.putInt(SOUND_VOLUME_POSITION_VOLUME, seekBarVolume.getProgress());
        super.onSaveInstanceState(outState);
    }

    private void navigateTo(Class cls, int currentVideoIndex) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(Constants.CURRENT_VIDEO_INDEX, currentVideoIndex);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.button_volume_sound_accept)
    public void onClickVolumeSoundAccept(){
        navigateTo(EditActivity.class);
    }

    @OnClick(R.id.button_volume_sound_cancel)
    public void onClickVolumeSoundCancel(){

        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        navigateTo(VoiceOverActivity.class);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        // TODO:(ruth.delToro) 19/09/16 Define these strings, es and eng
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.VideonaDialog);
        builder.setMessage("¿Desea descartar la locución y volver a grabarla de nuevo?").setPositiveButton("Aceptar", dialogClickListener)
                .setNegativeButton("Cancelar", dialogClickListener).show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        textSeekBarVolume.setText(progress+" % ");
        float finalVolume=(float)((progress*0.01));
        videonaPlayer.changeVolume(finalVolume);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void bindVideoList(List<Video> movieList) {
        videonaPlayer.bindVideoList(movieList);
        videonaPlayer.seekToClip(0);
        videonaPlayer.setMusic(voiceOver);
    }


    @Override
    public void resetPreview() {
        videonaPlayer.resetPreview();
    }

    @Override
    public void newClipPlayed(int currentClipIndex) {

    }
}
