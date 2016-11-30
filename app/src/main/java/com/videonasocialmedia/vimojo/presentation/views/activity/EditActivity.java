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
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.main.EditorActivity;
import com.videonasocialmedia.vimojo.main.VimojoApplication;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Music;
import com.videonasocialmedia.vimojo.model.entities.editor.media.Video;
import com.videonasocialmedia.vimojo.presentation.mvp.views.EditActivityView;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.EditPresenter;
import com.videonasocialmedia.vimojo.presentation.views.adapter.VideoTimeLineAdapter;
import com.videonasocialmedia.vimojo.presentation.views.adapter.helper.videoTimeLineTouchHelperCallback;
import com.videonasocialmedia.vimojo.presentation.views.customviews.ToolbarNavigator;
import com.videonasocialmedia.vimojo.presentation.views.customviews.VideonaPlayerExo;
import com.videonasocialmedia.vimojo.presentation.views.listener.VideoTimeLineRecyclerViewClickListener;
import com.videonasocialmedia.vimojo.presentation.views.listener.VideonaPlayerListener;
import com.videonasocialmedia.vimojo.presentation.views.services.ExportProjectService;
import com.videonasocialmedia.vimojo.sound.presentation.views.activity.SoundActivity;
import com.videonasocialmedia.vimojo.split.presentation.views.activity.VideoSplitActivity;
import com.videonasocialmedia.vimojo.text.presentation.views.activity.VideoEditTextActivity;
import com.videonasocialmedia.vimojo.trim.presentation.views.activity.VideoTrimActivity;
import com.videonasocialmedia.vimojo.utils.Constants;
import com.videonasocialmedia.vimojo.utils.FabUtils;

import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class EditActivity extends EditorActivity implements EditActivityView,
        VideonaPlayerListener, VideoTimeLineRecyclerViewClickListener {

    private static final String CURRENT_TIME_POSITION = "current_time_position";
    private final int NUM_COLUMNS_GRID_TIMELINE_HORIZONTAL = 3;
    private final int NUM_COLUMNS_GRID_TIMELINE_VERTICAL = 4;
  private final int ID_BUTTON_FAB_TOP=1;
  private final int ID_BUTTON_FAB_CENTER=2;
  private final int ID_BUTTON_FAB_BOTTOM=3;


   @Nullable @Bind(R.id.button_edit_duplicate)
    ImageButton editDuplicateButton;
   @Nullable @Bind(R.id.button_edit_trim)
    ImageButton editTrimButton;
   @Nullable @Bind(R.id.button_edit_split)
    ImageButton editSplitButton;
    @Nullable @Bind(R.id.recyclerview_editor_timeline)
    RecyclerView videoListRecyclerView;
    @Nullable @Bind(R.id.videona_player)
    VideonaPlayerExo videonaPlayer;
    @Nullable @Bind(R.id.fab_edit_room)
    FloatingActionsMenu fabMenu;
    @Nullable @Bind(R.id.bottomBar)
    BottomBar bottomBar;
    @Nullable @Bind(R.id.relative_layout_activity_edit)
    RelativeLayout relativeLayoutActivityEdit;

    private List<Video> videoList;
    private int currentVideoIndex = 0;
    private int currentProjectTimePosition = 0;
    private EditPresenter editPresenter;
    private VideoTimeLineAdapter timeLineAdapter;
    private AlertDialog progressDialog;
    private int selectedVideoRemovePosition;
    private FloatingActionButton newFab;
    private boolean isEnableFabText =false;


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
                  bottomBar.selectTabWithId(R.id.tab_editactivity);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLinearLayout(R.id.container_layout,R.layout.activity_edit);
        inflateLinearLayout(R.id.container_navigator,R.layout.edit_activity_layout_button_navigator);
        ButterKnife.bind(this);
        editPresenter = new EditPresenter(this, userEventTracker);
        videonaPlayer.setListener(this);
        createProgressDialog();
        if (savedInstanceState != null) {
            this.currentVideoIndex = savedInstanceState.getInt(Constants.CURRENT_VIDEO_INDEX);
            currentProjectTimePosition = savedInstanceState.getInt(CURRENT_TIME_POSITION, 0);
          }
        bottomBar.selectTabWithId(R.id.tab_editactivity);
        setupBottomBar(bottomBar);
        setupFabMenu();
      }

  private void setupBottomBar(BottomBar bottomBar) {
    bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
      @Override
      public void onTabSelected(@IdRes int tabId) {
        switch (tabId){
          case(R.id.tab_sound):
            navigateTo(SoundActivity.class);
            break;
          case (R.id.tab_share):
            Intent intent = new Intent(VimojoApplication.getAppContext(), ExportProjectService.class);
            Snackbar.make(relativeLayoutActivityEdit, "Starting export", Snackbar.LENGTH_INDEFINITE).show();
            VimojoApplication.getAppContext().startService(intent);
            break;
        }
      }
    });
  }

   private void setupFabMenu() {
     addAndConfigurateFabButton(ID_BUTTON_FAB_TOP, R.drawable.common_navigate_record, R.color.colorWhite);
     addAndConfigurateFabButton(ID_BUTTON_FAB_CENTER, R.drawable.common_navigate_gallery, R.color.colorWhite);
     addAndConfigurateFabButton(ID_BUTTON_FAB_BOTTOM, R.drawable.activity_edit_clip_text_normal, R.color.colorWhite );
  }

  private void addAndConfigurateFabButton(int id, int icon, int color) {
    newFab = FabUtils.createNewFab(id, icon, color);
    onClickFabButton(newFab);
    fabMenu.addButton(newFab);
  }

  private void onClickFabButton(final FloatingActionButton fab) {
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        switch (fab.getId()){
          case ID_BUTTON_FAB_TOP:
            fabMenu.collapse();
              navigateTo(RecordActivity.class);
              break;
          case ID_BUTTON_FAB_CENTER:
            fabMenu.collapse();
            navigateTo(GalleryActivity.class);
            break;
          case ID_BUTTON_FAB_BOTTOM:
            if(isEnableFabText) {
              fabMenu.collapse();
              navigateTo(VideoEditTextActivity.class, currentVideoIndex);
            }else
              showMessage(R.string.add_videos_to_project);
            break;
        }
      }
    });
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

    @Override
    public void updateViewResetProject() {
        initVideoListRecycler();
        super.updateViewResetProject();
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

    private void createProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_export_progress, null);
        progressDialog = builder.setCancelable(false)
                .setView(dialogView)
                .create();
    }

    public void navigateTo(Class cls) {
        Intent intent = new Intent(VimojoApplication.getAppContext(), cls);
        startActivity(intent);
    }

   @Nullable @OnClick(R.id.button_edit_fullscreen)
    public void onClickEditFullscreen() {
        // navigateTo(Activity.class)
    }

    @Nullable @OnClick(R.id.button_edit_duplicate)
    public void onClickEditDuplicate() {
        if (!editDuplicateButton.isEnabled())
            return;
        navigateTo(VideoDuplicateActivity.class, currentVideoIndex);
    }

    public void navigateTo(Class cls, int currentVideoIndex) {
        Intent intent = new Intent(VimojoApplication.getAppContext(), cls);
        intent.putExtra(Constants.CURRENT_VIDEO_INDEX, currentVideoIndex);
        startActivity(intent);
        finish();
    }

   @Nullable @OnClick(R.id.button_edit_trim)
    public void onClickEditTrim() {
        if (!editTrimButton.isEnabled())
            return;
        navigateTo(VideoTrimActivity.class, currentVideoIndex);
    }

    @Nullable @OnClick(R.id.button_edit_split)
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
        Snackbar snackbar = Snackbar.make(relativeLayoutActivityEdit, stringToast, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void showMessage(final int stringToast) {
        Snackbar snackbar = Snackbar.make(relativeLayoutActivityEdit, stringToast, Snackbar.LENGTH_LONG);
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
        videonaPlayer.setVolumen(1f);
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
    }

    @Override
    public void disableEditActions() {
        editTrimButton.setEnabled(false);
        editSplitButton.setEnabled(false);
        editDuplicateButton.setEnabled(false);
        videonaPlayer.releaseView();
  }

  @Override
  public void enableBottomBar() {
    bottomBar.getTabWithId(R.id.tab_sound).setEnabled(true);
    bottomBar.getTabWithId(R.id.tab_share).setEnabled(true);
  }

  @Override
  public void disableBottomBar() {
    bottomBar.getTabWithId(R.id.tab_sound).setEnabled(false);
    bottomBar.getTabWithId(R.id.tab_share).setEnabled(false);
  }

  @Override
  public void changeAlphaBottomBar(float alpha) {
    bottomBar.getTabWithId(R.id.tab_sound).setAlpha(alpha);
    bottomBar.getTabWithId(R.id.tab_share).setAlpha(alpha);
  }

  @Override
    public void resetPreview() {
        videonaPlayer.resetPreview();
    }


  @Override
  public void enableFabText(boolean enableFabText) {
    isEnableFabText = enableFabText;
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
