package com.videonasocialmedia.vimojo.text.presentation.views.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.videonasocialmedia.videonamediaframework.model.media.effects.TextEffect;
import com.videonasocialmedia.videonamediaframework.playback.VideonaPlayer;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.vimojo.presentation.views.activity.EditActivity;
import com.videonasocialmedia.vimojo.main.VimojoActivity;
import com.videonasocialmedia.videonamediaframework.playback.VideonaPlayerExo;
import com.videonasocialmedia.vimojo.text.presentation.mvp.presenters.EditTextPreviewPresenter;
import com.videonasocialmedia.vimojo.text.presentation.mvp.views.EditTextView;
import com.videonasocialmedia.vimojo.text.presentation.views.customviews.MaxCharPerLineInputFilter;
import com.videonasocialmedia.videonamediaframework.utils.TextToDrawable;
import com.videonasocialmedia.vimojo.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.videonasocialmedia.vimojo.utils.UIUtils.tintButton;

/**
 * Created by ruth on 1/09/16.
 */
public class VideoEditTextActivity extends VimojoActivity implements EditTextView,
        VideonaPlayer.VideonaPlayerListener {
    private final int MAX_CHARS_PER_LINE = 20;
    private final int MAX_LINES = 2;
    private final String STATE_BUTTON_TOP = "state_button_top";
    private final String STATE_BUTTON_CENTER = "state_button_center";
    private final String STATE_BUTTON_BOTTOM ="state_button_bottom" ;
    private final String VIDEO_POSITION = "video_position";
    private final String CURRENT_TEXT = "current_text";
    boolean hasTypedMoreThanTwoLines = false;

    @Inject EditTextPreviewPresenter presenter;

    private boolean stateWasRestored = false;

    @Bind(R.id.text_activityText)
    EditText clipText;
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

    private Video video;
    int videoIndexOnTrack;
    private int currentPosition = 0;
    private String typedText;

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

        getActivityPresentersComponent().inject(this);
        videonaPlayer.setSeekBarLayoutEnabled(false);
        videonaPlayer.setListener(this);

        Intent intent = getIntent();

        setupActivityButtons();
        videoIndexOnTrack = intent.getIntExtra(Constants.CURRENT_VIDEO_INDEX, 0);
        stateWasRestored=false;
        button_editText_top.setSelected(false);
        button_editText_center.setSelected(true);
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
        videonaPlayer.onShown(this);
        presenter.init(videoIndexOnTrack);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(VIDEO_POSITION, 0);
            typedText=savedInstanceState.getString(CURRENT_TEXT,null);
            button_ediText_bottom.setSelected(savedInstanceState.getBoolean(STATE_BUTTON_BOTTOM));
            button_editText_center.setSelected(savedInstanceState.getBoolean(STATE_BUTTON_CENTER));
            button_editText_top.setSelected(savedInstanceState.getBoolean(STATE_BUTTON_TOP));
            stateWasRestored = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
               onBackPressed();
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
        outState.putString(CURRENT_TEXT, getTextFromEditText());
        outState.putBoolean(STATE_BUTTON_TOP, button_editText_top.isSelected());
        outState.putBoolean(STATE_BUTTON_CENTER, button_editText_center.isSelected());
        outState.putBoolean(STATE_BUTTON_BOTTOM, button_ediText_bottom.isSelected());
        super.onSaveInstanceState(outState);
    }

    @OnClick(R.id.button_editText_top)
    public void onClickAddTextTop(){
        paintPositionEditText(TextEffect.TextPosition.TOP);
        createDrawableFromText(typedText, TextEffect.TextPosition.TOP);
        hideKeyboard(clipText);
    }

    @OnClick(R.id.button_editText_center)
    public void onClickAddTextCenter() {
        paintPositionEditText(TextEffect.TextPosition.CENTER);
        createDrawableFromText(typedText, TextEffect.TextPosition.CENTER);
        hideKeyboard(clipText);
    }

    @OnClick(R.id.button_editText_bottom)
    public void onClickAddTextBottom() {
        paintPositionEditText(TextEffect.TextPosition.BOTTOM);
        createDrawableFromText(typedText, TextEffect.TextPosition.BOTTOM);
        hideKeyboard(clipText);
    }

    private void createDrawableFromText(String text, TextEffect.TextPosition textPosition) {
        text = getTextFromEditText();
        presenter.createDrawableWithText(text, textPosition.name(), Constants.DEFAULT_VIMOJO_WIDTH, Constants.DEFAULT_VIMOJO_HEIGHT);
    }

    @NonNull
    private String getTextFromEditText() {
        return clipText.getText().toString();
    }

    public void paintPositionEditText(TextEffect.TextPosition position) {
        if(position == TextEffect.TextPosition.BOTTOM) {
            button_editText_top.setSelected(false);
            button_editText_center.setSelected(false);
            button_ediText_bottom.setSelected(true);
        }
        if(position == TextEffect.TextPosition.CENTER) {
            button_editText_top.setSelected(false);
            button_editText_center.setSelected(true);
            button_ediText_bottom.setSelected(false);
        }
        if(position == TextEffect.TextPosition.TOP) {
            button_editText_top.setSelected(true);
            button_editText_center.setSelected(false);
            button_ediText_bottom.setSelected(false);
        }
    }

    @OnClick(R.id.button_editText_accept)
    public void onClickEditTextAccept() {
        presenter.setTextToVideo(getTextFromEditText(), getTextPositionSelected());
        navigateTo(EditActivity.class, videoIndexOnTrack);
        //finish();
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
        // (alvaro.martinez) 4/10/17 work on a copy to not modify original one until user accepts text
        video = new Video(movieList.get(0));
        ArrayList<Video> clipList = new ArrayList<>();
        clipList.add(video);
        videonaPlayer.initPreviewLists(clipList);
        videonaPlayer.initPreview(currentPosition);
        if(movieList.get(0).hasText())
            initTextToVideoAdded(video.getClipText(),video.getClipTextPosition());
        MaxCharPerLineInputFilter.applyAutoWrap(clipText,MAX_CHARS_PER_LINE);
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
    public void initTextToVideoAdded(String text, String position) {
        if(stateWasRestored) {
            position = getTextPositionSelected().name();
            typedText = getTextFromEditText();
        } else {
            clipText.setText(text);
        }
        TextEffect.TextPosition positionText = TextToDrawable.getTypePositionFromString(position);
        paintPositionEditText(positionText);
        createDrawableFromText(typedText, positionText);
    }

    @Override
    public void newClipPlayed(int currentClipIndex) {
    }

    @OnTextChanged(R.id.text_activityText)
    void onTextChanged() {
        typedText = getTextFromEditText();
        updateTextinPreview();

        if (null != clipText.getLayout() && clipText.getLayout().getLineCount() > MAX_LINES) {
            if(!hasTypedMoreThanTwoLines){
                showError(getString(R.string.error_videoEdit));
                hideKeyboard(clipText);
                hasTypedMoreThanTwoLines =true;
            }
        } else {
            hasTypedMoreThanTwoLines =false;
        }
    }

    private void updateTextinPreview() {
        if (button_editText_top.isSelected()) {
            createDrawableFromText(typedText, TextEffect.TextPosition.TOP);
            return;
        }
        if (button_editText_center.isSelected()) {
            createDrawableFromText(typedText, TextEffect.TextPosition.CENTER);
            return;
        }
        if (button_ediText_bottom.isSelected()) {
            createDrawableFromText(typedText, TextEffect.TextPosition.BOTTOM);
            return;
        }
    }

    public TextEffect.TextPosition getTextPositionSelected() {
        if (button_editText_top.isSelected()) {
            return TextEffect.TextPosition.TOP;
        }
        if (button_editText_center.isSelected()) {
            return TextEffect.TextPosition.CENTER;
        }
        if (button_ediText_bottom.isSelected()) {
            return TextEffect.TextPosition.BOTTOM;
        }
        // default
        return TextEffect.TextPosition.CENTER;
    }

    private void hideKeyboard(View v) {
        InputMethodManager keyboard =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}