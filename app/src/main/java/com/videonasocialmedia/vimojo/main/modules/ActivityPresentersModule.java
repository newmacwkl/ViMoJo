package com.videonasocialmedia.vimojo.main.modules;

import android.content.SharedPreferences;

import com.videonasocialmedia.avrecorder.view.GLCameraView;
import com.videonasocialmedia.vimojo.domain.editor.AddVideoToProjectUseCase;
import com.videonasocialmedia.vimojo.domain.editor.RemoveVideoFromProjectUseCase;
import com.videonasocialmedia.vimojo.domain.editor.ReorderMediaItemUseCase;
import com.videonasocialmedia.vimojo.main.VimojoActivity;
import com.videonasocialmedia.vimojo.main.internals.di.PerActivity;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.DuplicatePreviewPresenter;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.EditPresenter;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.GalleryPagerPresenter;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.RecordPresenter;
import com.videonasocialmedia.vimojo.presentation.mvp.views.MusicDetailView;
import com.videonasocialmedia.vimojo.presentation.views.activity.EditActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.GalleryActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.RecordActivity;
import com.videonasocialmedia.vimojo.presentation.views.activity.VideoDuplicateActivity;
import com.videonasocialmedia.vimojo.repository.project.ProjectRepository;
import com.videonasocialmedia.vimojo.sound.domain.AddMusicToProjectUseCase;
import com.videonasocialmedia.vimojo.sound.domain.RemoveMusicFromProjectUseCase;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.presenters.MusicDetailPresenter;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.presenters.SoundVolumePresenter;
import com.videonasocialmedia.vimojo.sound.presentation.mvp.views.SoundVolumeView;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;

import dagger.Module;
import dagger.Provides;

/**
 * Created by jliarte on 1/12/16.
 */

@Module
public class ActivityPresentersModule {
  private final VimojoActivity activity;
  private GLCameraView cameraView = null;
  private boolean externalIntent;

  public ActivityPresentersModule(VimojoActivity vimojoActivity) {
    this.activity = vimojoActivity;
  }

  public ActivityPresentersModule(RecordActivity activity, boolean externalIntent,
                                  GLCameraView cameraView) {
    this.activity = activity;
    this.externalIntent = externalIntent;
    this.cameraView = cameraView;
  }

  @Provides @PerActivity
  SoundVolumePresenter getSoundVolumePresenter(RemoveMusicFromProjectUseCase useCase) {
    return new SoundVolumePresenter((SoundVolumeView) activity, useCase);
  }

  @Provides @PerActivity
  MusicDetailPresenter provideMusicDetailPresenter(UserEventTracker userEventTracker,
                              AddMusicToProjectUseCase addMusicToProjectUseCase,
                              RemoveMusicFromProjectUseCase removeMusicFromProjectUseCase) {
    return new MusicDetailPresenter((MusicDetailView) activity, userEventTracker,
            addMusicToProjectUseCase, removeMusicFromProjectUseCase);
  }

  @Provides @PerActivity
  EditPresenter provideEditPresenter(UserEventTracker userEventTracker,
                                     RemoveVideoFromProjectUseCase removeVideosFromProjectUseCase,
                                     ReorderMediaItemUseCase reorderMediaItemUseCase) {
    return new EditPresenter((EditActivity) activity,
            ((EditActivity) activity).getNavigatorCallback(), userEventTracker,
            removeVideosFromProjectUseCase, reorderMediaItemUseCase);
  }

  @Provides @PerActivity
  DuplicatePreviewPresenter provideDuplicatePresenter
          (UserEventTracker userEventTracker, AddVideoToProjectUseCase addVideoToProjectUseCase) {
    return new DuplicatePreviewPresenter((VideoDuplicateActivity) activity, userEventTracker,
            addVideoToProjectUseCase);
  }

  @Provides @PerActivity
  GalleryPagerPresenter provideGalleryPagerPresenter(
          AddVideoToProjectUseCase addVideoToProjectUseCase) {
    return new GalleryPagerPresenter((GalleryActivity) activity, addVideoToProjectUseCase);
  }

  @Provides @PerActivity
  RecordPresenter provideRecordPresenter(SharedPreferences sharedPreferences,
                                         AddVideoToProjectUseCase addVideoToProjectUseCase) {
    return new RecordPresenter(activity, (RecordActivity) activity, cameraView, sharedPreferences,
            externalIntent, addVideoToProjectUseCase);
  }

  @Provides
  RemoveMusicFromProjectUseCase provideMusicRemover(ProjectRepository projectRepository) {
    return new RemoveMusicFromProjectUseCase(projectRepository);
  }

  @Provides
  ReorderMediaItemUseCase provideMusicReorderer(ProjectRepository projectRepository) {
    return new ReorderMediaItemUseCase(projectRepository);
  }

  @Provides
  AddVideoToProjectUseCase provideVideoAdder(ProjectRepository projectRepository) {
    return new AddVideoToProjectUseCase(projectRepository);
  }
}
