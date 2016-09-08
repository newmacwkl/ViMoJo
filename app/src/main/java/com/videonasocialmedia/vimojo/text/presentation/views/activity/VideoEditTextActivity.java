package com.videonasocialmedia.vimojo.text.presentation.views.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.videonasocialmedia.vimojo.BuildConfig;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Video;
import com.videonasocialmedia.vimojo.presentation.views.activity.EditActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.GalleryActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.SettingsActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.VimojoActivity;
import com.videonasocialmedia.vimojo.presentation.views.customviews.VideonaPlayerExo;
import com.videonasocialmedia.vimojo.text.presentation.mvp.presenters.EditTextPreviewPresenter;
import com.videonasocialmedia.vimojo.text.presentation.mvp.views.EditTextView;
import com.videonasocialmedia.vimojo.presentation.views.customviews.VideonaPlayer;
import com.videonasocialmedia.vimojo.presentation.views.listener.VideonaPlayerListener;
import com.videonasocialmedia.vimojo.text.presentation.views.customviews.EditTextMaxCharPerLine;
import com.videonasocialmedia.vimojo.utils.Constants;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.videonasocialmedia.vimojo.utils.UIUtils.tintButton;

/**
 * Created by ruth on 1/09/16.
 */
public class VideoEditTextActivity extends VimojoActivity implements EditTextView,VideonaPlayerListener{

    private final String STATE_BUTTON_TOP = "state_button_top";
    private final String STATE_BUTTON_CENTER = "state_button_center";
    private final String STATE_BUTTON_BOTTOM ="state_button_bottom" ;
    private final String VIDEO_POSITION = "video_position";
    private final String CURRENT_TEXT = "current_text";
    private final String TEXT_TO_ADD = "image_of_text";
    boolean numLineEditTextTwo =false;
    public enum TextPosition{TOP, CENTER, BOTTOM}

    @Bind(R.id.text_activityText)
    EditText textFile;
    @Bind(R.id.videona_player)
    VideonaPlayerExo videonaPlayer;
    @Bind(R.id.button_editText_center)
    ImageButton button_editText_center;
    @Bind(R.id.button_editText_top)
    ImageButton button_editText_top;
    @Bind(R.id.button_editText_bottom)
    ImageButton button_ediText_bottom;
    @Bind(R.id.imageVideoText)
    ImageView image_view_text;


    int videoIndexOnTrack;
    private EditTextPreviewPresenter presenter;
    private Video video;
    private int currentPosition = 0;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_text);
        ButterKnife.bind(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        UserEventTracker userEventTracker = UserEventTracker.getInstance(MixpanelAPI.getInstance(this, BuildConfig.MIXPANEL_TOKEN));
        presenter = new EditTextPreviewPresenter(this, userEventTracker);
        videonaPlayer.setSeekBarEnabled(false);
        videonaPlayer.setListener(this);

        Intent intent = getIntent();

        setupActivityButtons();
        videoIndexOnTrack = intent.getIntExtra(Constants.CURRENT_VIDEO_INDEX, 0);
        button_editText_top.setSelected(true);
        button_editText_center.setSelected(false);
        button_ediText_bottom.setSelected(false);

        restoreState(savedInstanceState);

    }

    private void setupActivityButtons() {
        tintEditButtons(R.color.button_color);
    }

    private void tintEditButtons(int tintList) {
        tintButton(button_editText_top, tintList);
        tintButton(button_editText_center, tintList);
        tintButton(button_ediText_bottom, tintList);
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
        presenter.init(videoIndexOnTrack);
        image_view_text.setMaxHeight(videonaPlayer.getHeight());
        image_view_text.setMaxWidth(videonaPlayer.getWidth());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(VIDEO_POSITION, 0);
            text=savedInstanceState.getString(CURRENT_TEXT,null);
            button_ediText_bottom.setSelected(savedInstanceState.getBoolean(STATE_BUTTON_BOTTOM));
            button_editText_center.setSelected(savedInstanceState.getBoolean(STATE_BUTTON_CENTER));
            button_editText_top.setSelected(savedInstanceState.getBoolean(STATE_BUTTON_TOP));

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        outState.putString(CURRENT_TEXT,text);
        outState.putBoolean(STATE_BUTTON_TOP, button_editText_top.isSelected());
        outState.putBoolean(STATE_BUTTON_CENTER, button_editText_center.isSelected());
        outState.putBoolean(STATE_BUTTON_BOTTOM, button_ediText_bottom.isSelected());
        super.onSaveInstanceState(outState);
    }



    @OnClick(R.id.button_editText_top)
    public void onClickAddTextTop(){

        button_editText_top.setSelected(true);
        button_editText_center.setSelected(false);
        button_ediText_bottom.setSelected(false);
        setText(TextPosition.TOP);
        hideKeyboard(textFile);
    }

    private void setText(TextPosition textPosition) {
        text = getTextKeyboard();
        presenter.createDrawableWithText(text, textPosition);
    }

    @NonNull
    private String getTextKeyboard() {
        return textFile.getText().toString();
    }

    @OnClick(R.id.button_editText_center)
    public void onClickAddTextCenter(){

        button_editText_top.setSelected(false);
        button_editText_center.setSelected(true);
        button_ediText_bottom.setSelected(false);
        setText(TextPosition.CENTER);
        hideKeyboard(textFile);
    }

    @OnClick(R.id.button_editText_bottom)
    public void onClickAddTextBottom(){

        button_editText_top.setSelected(false);
        button_editText_center.setSelected(false);
        button_ediText_bottom.setSelected(true);
        setText(TextPosition.BOTTOM);
        hideKeyboard(textFile);
    }

    @OnClick(R.id.button_editText_accept)
    public void onClickEditTextAccept() {
        presenter.setTextToVideo(getTextKeyboard(),getTextPositionSelected());
        navigateTo(EditActivity.class, videoIndexOnTrack);
        finish();
    }

    @OnClick(R.id.button_editText_cancel)
    public void onClickEditTextCancel() {
        navigateTo(EditActivity.class, videoIndexOnTrack);
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
        videonaPlayer.initPreviewLists(movieList);
        videonaPlayer.initPreview(currentPosition);
        EditTextMaxCharPerLine.applyAutoWrap(textFile,30);
        onTextChanged();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showText(Drawable drawable) {
       image_view_text.setImageDrawable(drawable);
    }

    @Override
    public void newClipPlayed(int currentClipIndex) {
    }

    @OnTextChanged(R.id.text_activityText)
    void onTextChanged() {

        if (null != textFile.getLayout() && textFile.getLayout().getLineCount() > 2) {
            updateTextinPreview();

            if(!numLineEditTextTwo){

                showError(getString(R.string.error_videoEdit));
                hideKeyboard(textFile);
                numLineEditTextTwo=true;
            }

        } else {

            numLineEditTextTwo=false;
            updateTextinPreview();
        }
    }

    private void updateTextinPreview() {
        if (button_editText_top.isSelected()) {
            setText(TextPosition.TOP);
            return;
        }
        if (button_editText_center.isSelected()) {
            setText(TextPosition.CENTER);
            return;
        }
        if (button_ediText_bottom.isSelected()) {
            setText(TextPosition.BOTTOM);
            return;
        }
    }

    public TextPosition getTextPositionSelected() {

        if (button_editText_top.isSelected()) {
            return TextPosition.TOP;
        }
        if (button_editText_center.isSelected()) {
            return TextPosition.CENTER;
        }
        if (button_ediText_bottom.isSelected()) {
            return TextPosition.BOTTOM;
        }
        // default
        return TextPosition.CENTER;
    }

    private void hideKeyboard(View v) {
        InputMethodManager keyboard =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


}