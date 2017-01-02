package com.videonasocialmedia.vimojo.presentation.views.activity;
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.videonasocialmedia.videonamediaframework.playback.VideonaPlayer;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.main.VimojoActivity;
import com.videonasocialmedia.vimojo.main.VimojoApplication;
import com.videonasocialmedia.videonamediaframework.model.media.Music;
import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.vimojo.presentation.mvp.views.EditorView;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.EditPresenter;

import com.videonasocialmedia.vimojo.presentation.views.adapter.VideoTimeLineAdapter;
import com.videonasocialmedia.vimojo.presentation.views.adapter.helper.videoTimeLineTouchHelperCallback;
import com.videonasocialmedia.vimojo.presentation.views.customviews.ToolbarNavigator;
import com.videonasocialmedia.videonamediaframework.playback.VideonaPlayerExo;
import com.videonasocialmedia.vimojo.presentation.views.listener.VideoTimeLineRecyclerViewClickListener;
import com.videonasocialmedia.vimojo.presentation.views.services.ExportProjectService;
import com.videonasocialmedia.vimojo.split.presentation.views.activity.VideoSplitActivity;
import com.videonasocialmedia.vimojo.text.presentation.views.activity.VideoEditTextActivity;
import com.videonasocialmedia.vimojo.trim.presentation.views.activity.VideoTrimActivity;
import com.videonasocialmedia.vimojo.utils.Constants;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.videonasocialmedia.vimojo.utils.UIUtils.tintButton;

public class EditActivity extends VimojoActivity implements EditorView,
        VideonaPlayer.VideonaPlayerListener, VideoTimeLineRecyclerViewClickListener {

    private static final String CURRENT_TIME_POSITION = "current_time_position";
    private final int NUM_COLUMNS_GRID_TIMELINE_HORIZONTAL = 3;
    private final int NUM_COLUMNS_GRID_TIMELINE_VERTICAL = 4;

    @Inject EditPresenter editPresenter;

    @Bind(R.id.button_edit_duplicate)
    ImageButton editDuplicateButton;
    @Bind(R.id.button_edit_trim)
    ImageButton editTrimButton;
    @Bind(R.id.button_edit_editText)
    ImageButton editTextButton;
    @Bind(R.id.button_edit_split)
    ImageButton editSplitButton;
    @Bind(R.id.recyclerview_editor_timeline)
    RecyclerView videoListRecyclerView;
    @Bind(R.id.navigator)
    ToolbarNavigator navigator;
    @Bind(R.id.videona_player)
    VideonaPlayerExo videonaPlayer;
    @Bind(R.id.fab_edit_room)
    FloatingActionsMenu fabEditRoom;
    private List<Video> videoList;
    private int currentVideoIndex = 0;
    private int currentProjectTimePosition = 0;
    private VideoTimeLineAdapter timeLineAdapter;
    private AlertDialog progressDialog;
    private int selectedVideoRemovePosition;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String videoToSharePath = bundle.getString(ExportProjectService.FILEPATH);
                int resultCode = bundle.getInt(ExportProjectService.RESULT);
                if (resultCode == RESULT_OK) {
                    // hideProgressDialog();
                    goToShare(videoToSharePath);
                } else {
                    //showProgressDialog();
                    // hideProgressDialog();
                    showError(R.string.addMediaItemToTrackError);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);
        setupActivityButtons();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        getActivityPresentersComponent().inject(this);
//        UserEventTracker userEventTracker = UserEventTracker.getInstance(MixpanelAPI.getInstance(this, BuildConfig.MIXPANEL_TOKEN));
//        editPresenter = new EditPresenter(this, getNavigatorCallback(), userEventTracker);

        videonaPlayer.setListener(this);

        createProgressDialog();
        if (savedInstanceState != null) {
            this.currentVideoIndex = savedInstanceState.getInt(Constants.CURRENT_VIDEO_INDEX);
            currentProjectTimePosition = savedInstanceState.getInt(CURRENT_TIME_POSITION, 0);
        }

    }

    public ToolbarNavigator.ProjectModifiedCallBack getNavigatorCallback() {
        return navigator.getCallback();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initVideoListRecycler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(ExportProjectService.NOTIFICATION));
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(Constants.CURRENT_VIDEO_INDEX)) {
                this.currentVideoIndex = getIntent().getIntExtra(Constants.CURRENT_VIDEO_INDEX, 0);
            }
        }
        videonaPlayer.onShown(this);
        editPresenter.loadProject();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videonaPlayer.onPause();
        unregisterReceiver(receiver);
        hideProgressDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initVideoListRecycler() {
        int orientation = LinearLayoutManager.VERTICAL;
        int num_grid_columns = NUM_COLUMNS_GRID_TIMELINE_VERTICAL;
        if (isLandscapeOriented()) {
            num_grid_columns = NUM_COLUMNS_GRID_TIMELINE_HORIZONTAL;
        }
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, num_grid_columns,
                orientation, false);
        videoListRecyclerView.setLayoutManager(layoutManager);

        timeLineAdapter = new VideoTimeLineAdapter(this);
        videoListRecyclerView.setAdapter(timeLineAdapter);

        videoTimeLineTouchHelperCallback callback = new videoTimeLineTouchHelperCallback(timeLineAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(videoListRecyclerView);
    }

    private void setupActivityButtons() {
        tintEditButtons(R.color.button_color);
    }

    private void createProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_export_progress, null);
        progressDialog = builder.setCancelable(false)
                .setView(dialogView)
                .create();
    }

    private void tintEditButtons(int tintList) {
        tintButton(editDuplicateButton, tintList);
        tintButton(editSplitButton, tintList);
        tintButton(editTrimButton, tintList);
        tintButton(editTextButton,tintList);
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
            case android.R.id.home:
                onBackPressed();
                return true;
            default:

        }
        return super.onOptionsItemSelected(item);
    }

    public void navigateTo(Class cls) {
        Intent intent = new Intent(VimojoApplication.getAppContext(), cls);
        startActivity(intent);
    }

    @OnClick(R.id.fab_go_to_record)
    public void onClickFabRecord() {
        fabEditRoom.collapse();
        navigateTo(RecordActivity.class);
    }

    @OnClick(R.id.fab_go_to_gallery)
    public void onClickFabGallery() {
        fabEditRoom.collapse();
        navigateTo(GalleryActivity.class);
    }

    @OnClick(R.id.button_edit_fullscreen)
    public void onClickEditFullscreen() {
        // navigateTo(Activity.class)
    }

    @OnClick(R.id.button_edit_duplicate)
    public void onClickEditDuplicate() {
        if (!editDuplicateButton.isEnabled())
            return;
        navigateTo(VideoDuplicateActivity.class, currentVideoIndex);
    }

    @OnClick(R.id.button_edit_editText)
    public void onClickEditEditText() {
        if (!editTextButton.isEnabled())
            return;
        navigateTo(VideoEditTextActivity.class, currentVideoIndex);
    }

    public void navigateTo(Class cls, int currentVideoIndex) {
        Intent intent = new Intent(VimojoApplication.getAppContext(), cls);
        intent.putExtra(Constants.CURRENT_VIDEO_INDEX, currentVideoIndex);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.button_edit_trim)
    public void onClickEditTrim() {
        if (!editTrimButton.isEnabled())
            return;
        navigateTo(VideoTrimActivity.class, currentVideoIndex);
    }

    @OnClick(R.id.button_edit_split)
    public void onClickEditSplit() {
        if (!editSplitButton.isEnabled())
            return;
        navigateTo(VideoSplitActivity.class, currentVideoIndex);
    }

    public void navigateTo(Class cls, String videoToSharePath) {
        Intent intent = new Intent(VimojoApplication.getAppContext(), cls);
        intent.putExtra(Constants.VIDEO_TO_SHARE_PATH, videoToSharePath);
        startActivity(intent);
        finish();
    }

    ////// RECYCLER VIDEO TIME LINE
    @Override
    public void onClipClicked(int position) {
        setSelectedClip(position);
    }

    public void setSelectedClip(int position) {
        currentVideoIndex = position;
        videonaPlayer.seekToClip(position);
        timeLineAdapter.updateSelection(position);
    }

    @Override
    public void onClipLongClicked(int adapterPosition) {
        videonaPlayer.pausePreview();
    }

    @Override
    public void onClipRemoveClicked(int position) {
        selectedVideoRemovePosition = position;
        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        timeLineAdapter.remove(selectedVideoRemovePosition);
                        setSelectedClipIndex(Math.max(selectedVideoRemovePosition-1, 0));
                        editPresenter.removeVideoFromProject(selectedVideoRemovePosition);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.VideonaDialog);
        builder.setMessage(R.string.dialog_edit_remove_message).setPositiveButton(R.string.dialog_edit_remove_accept, dialogClickListener)
                .setNegativeButton(R.string.dialog_edit_remove_cancel, dialogClickListener).show();
    }

    private void setSelectedClipIndex(int selectedIndex) {
        this.currentVideoIndex = selectedIndex;
        timeLineAdapter.updateSelection(selectedIndex);
    }

    @Override
    public void onClipMoved(int fromPosition, int toPosition) {
        currentVideoIndex = toPosition;
        editPresenter.moveItem(fromPosition, toPosition);
        videonaPlayer.seekToClip(currentVideoIndex);
    }

    @Override
    public void onClipReordered(int newPosition) {
        currentVideoIndex = newPosition;
        videonaPlayer.updatePreviewTimeLists();
        videonaPlayer.seekToClip(currentVideoIndex);
    }

    @Override
    public void goToShare(String videoToSharePath) {
        Intent intent = new Intent(VimojoApplication.getAppContext(), ShareActivity.class);
        intent.putExtra(Constants.VIDEO_TO_SHARE_PATH, videoToSharePath);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.CURRENT_VIDEO_INDEX, currentVideoIndex);
        outState.putInt(CURRENT_TIME_POSITION, videonaPlayer.getCurrentPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showProgressDialog() {
        progressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void showError(final int stringToast) {
        Snackbar snackbar = Snackbar.make(fabEditRoom, stringToast, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void showMessage(final int stringToast) {
        Snackbar snackbar = Snackbar.make(fabEditRoom, stringToast, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void bindVideoList(List<Video> videoList) {
        this.videoList = videoList;
        timeLineAdapter.setVideoList(videoList);
        timeLineAdapter.updateSelection(currentVideoIndex); // TODO: check this flow and previous updateSelection(0); in setVideoList
        videoListRecyclerView.scrollToPosition(currentVideoIndex);
        timeLineAdapter.notifyDataSetChanged();
        videonaPlayer.bindVideoList(videoList);
        videonaPlayer.seekTo(currentProjectTimePosition);
    }

    @Override
    public void setMusic(Music music) {
        videonaPlayer.setMusic(music);
    }

    @Override
    public void updateProject() {
        editPresenter.loadProject();
    }

    @Override
    public void enableEditActions() {
        editTrimButton.setEnabled(true);
        editSplitButton.setEnabled(true);
        editDuplicateButton.setEnabled(true);
        editTextButton.setEnabled(true);
    }

    @Override
    public void disableEditActions() {
        editTrimButton.setEnabled(false);
        editSplitButton.setEnabled(false);
        editDuplicateButton.setEnabled(false);
        editTextButton.setEnabled(false);
        videonaPlayer.releaseView();
    }

    @Override
    public void expandFabMenu() {
        fabEditRoom.expand();
    }

    @Override
    public void resetPreview() {
        videonaPlayer.resetPreview();
    }


    @Override
    public void newClipPlayed(int currentClipIndex) {
        currentVideoIndex = currentClipIndex;
        timeLineAdapter.updateSelection(currentClipIndex);
        videoListRecyclerView.scrollToPosition(currentClipIndex);
    }


    @Override
    public void onBackPressed() {
        navigateTo(RecordActivity.class);
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                this.onBackPressed();
                return true;
            default:
                return false;
        }
    }
}
