package com.videonasocialmedia.videonamediaframework.playback;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Range;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.CodecCounters;
import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.Util;
import com.videonasocialmedia.videonaframework.R;
import com.videonasocialmedia.videonamediaframework.model.Constants;
import com.videonasocialmedia.videonamediaframework.model.media.Music;
import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.videonamediaframework.playback.customviews.AspectRatioVideoView;
import com.videonasocialmedia.videonamediaframework.utils.TextToDrawable;
import com.videonasocialmedia.videonamediaframework.utils.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO(jliarte): 21/11/16 move layout to SDK lib, thus R should be used from there

/**
 * Created by jliarte on 25/08/16.
 */
public class VideonaPlayerExo extends RelativeLayout implements VideonaPlayer,
    SeekBar.OnSeekBarChangeListener, ExoPlayer.Listener, ExtractorSampleSource.EventListener,
    MediaCodecAudioTrackRenderer.EventListener, MediaCodecVideoTrackRenderer.EventListener {
  private static final String TAG = "VideonaPlayerExo";
  private static final int RENDERER_COUNT = 3;
  private static final int BUFFER_LENGTH_MIN = 50;
  private static final int REBUFFER_LENGTH_MIN = 100;
  public static final int TYPE_VIDEO = 0;
  public static final int TYPE_AUDIO = 1;
  public static final int TYPE_TEXT = 2;
  public static final int TYPE_METADATA = 3;
  private static final int DISABLED_TRACK = -1;

  private static final int RENDERER_BUILDING_STATE_IDLE = 1;
  private static final int RENDERER_BUILDING_STATE_BUILDING = 2;
  private static final int RENDERER_BUILDING_STATE_BUILT = 3;
  private final TextToDrawable drawableGenerator;

  AspectRatioVideoView videoPreview;
  SeekBar seekBar;
  ImageButton playButton;
  ImageView imageTextPreview;
  TextView textTimeCurrentSeekbar;
  TextView textTimeProjectSeekbar;
  LinearLayout seekBarLayout;

  private final View videonaPlayerView;
  private VideonaPlayerListener videonaPlayerListener;
  private ExoPlayer player;
  private RendererBuilder rendererBuilder;
  private TrackRenderer videoRenderer;
  private CodecCounters codecCounters;
  private BandwidthMeter bandwidthMeter;
  private Format videoFormat;
  private Surface surface;

  private Music music;
  private MediaPlayer musicPlayer;
  private List<Video> videoList;
  private int currentClipIndex = 0;
  private int currentTimePositionInList = 0;
  private int totalVideoDuration = 0;
  private int rendererBuildingState;
  // TODO:(alvaro.martinez) 23/11/16 Get this functionality compatible with API 18 to downgrade API module from 21 to 18
  private List<Range> clipTimesRanges;
  private Handler mainHandler;
  private Handler seekBarUpdaterHandler = new Handler();

  private final Runnable updateTimeTask = new Runnable() {
    @Override
    public void run() {
      try {
        updateSeekBarProgress();
      } catch (Exception exception) {
        Log.d(TAG, "Exception in update seekbar progress thread");
        Log.d(TAG, String.valueOf(exception));
      }
    }
  };
  private float volumeMusic = 0.5f;
  private static final float DEFAULT_VOLUME = 0.5f;

  /**
   * Default constructor.
   *
   * @param context view context
   */
  public VideonaPlayerExo(Context context) {
    super(context);
    drawableGenerator = new TextToDrawable(context);
    this.videonaPlayerView = ((Activity) getContext()).getLayoutInflater()
        .inflate(R.layout.video_preview, this, true);
    initLayoutsComponents();
    initVideonaPlayerComponents(context);
  }

  /**
   * Constructor with attributes.
   *
   * @param context view context
   * @param attrs   view attributes
   */
  public VideonaPlayerExo(Context context, AttributeSet attrs) {
    super(context, attrs);
    drawableGenerator = new TextToDrawable(context);
    this.videonaPlayerView = ((Activity) getContext()).getLayoutInflater()
        .inflate(R.layout.video_preview, this, true);
    initLayoutsComponents();
    initVideonaPlayerComponents(context);
  }

  /**
   * Contructor with attributes and style.
   *
   * @param context  view context
   * @param attrs    view attributes
   * @param defStyle view style
   */
  public VideonaPlayerExo(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    drawableGenerator = new TextToDrawable(context);
    this.videonaPlayerView = ((Activity) getContext()).getLayoutInflater()
        .inflate(R.layout.video_preview, this, true);
    initLayoutsComponents();
    initVideonaPlayerComponents(context);
  }

  private void initLayoutsComponents() {

    videoPreview = (AspectRatioVideoView) findViewById(R.id.video_editor_preview);
    videoPreview.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        onTouchPreview(motionEvent);
        return false;
      }
    });
    seekBar = (SeekBar) findViewById(R.id.seekbar_editor_preview);
    playButton = (ImageButton) findViewById(R.id.button_editor_play_pause);
    playButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        onClickPlayPauseButton();
      }
    });
    imageTextPreview = (ImageView) findViewById(R.id.image_text_preview);
    textTimeCurrentSeekbar = (TextView) findViewById(R.id.video_view_time_current);
    textTimeProjectSeekbar = (TextView) findViewById(R.id.video_view_time_project);
    seekBarLayout = (LinearLayout) findViewById(R.id.video_view_seekbar_layout);

  }

  /**
   * Common VideonaPlayerExo initialization code called in constructors.
   * Creates mainHandler, initializes exoplayer without videos and audio manager
   */
  private void initVideonaPlayerComponents(Context context) {
    mainHandler = new Handler();
    player = ExoPlayer.Factory.newInstance(RENDERER_COUNT, BUFFER_LENGTH_MIN, REBUFFER_LENGTH_MIN);
    player.addListener(this);
    player.setSelectedTrack(TYPE_TEXT, DISABLED_TRACK);
    surface = videoPreview.getHolder().getSurface();
    rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    String userAgent = Util.getUserAgent(context, "ExoPlayerDemo");
    rendererBuilder = new RendererBuilder(context, userAgent);

    // TODO(jliarte): 1/09/16 instantiate field when need to adjust volume
    //        AudioManager audioManager = (AudioManager) context.getApplicationContext()
    //                .getSystemService(Context.AUDIO_SERVICE);
    initMusicPlayer();
    initClipList();
    initSeekBar();
    clipTimesRanges = new ArrayList<>();
  }

  private void initMusicPlayer() {
    if (musicPlayer == null && videoHasMusic()) {
      musicPlayer = MediaPlayer.create(getContext(), Uri.parse(music.getMediaPath()));
      if (musicPlayer != null) {
        musicPlayer.setVolume(volumeMusic, volumeMusic);
      }
    }

  }

  /**
   * Sets video and music/voice over volumes.
   *
   * @param volume music/voice over track volume. Video track volume will be the 1 - volume
   */
  public void setVolume(float volume) {
    player.sendMessage(rendererBuilder.getAudioRenderer(),
        MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 1f - volume);
    if (musicPlayer != null) {
      musicPlayer.setVolume(volume, volume);
    }
    volumeMusic = volume;
  }

  private void updateSeekBarProgress() {
    if (player != null && isPlaying()) {
      try {
        if (currentClipIndex() < videoList.size()) {
          int progress = ((int) player.getCurrentPosition())
              + (int) clipTimesRanges.get(currentClipIndex()).getLower()
              - videoList.get(currentClipIndex()).getStartTime();
          setSeekBarProgress(progress);
          currentTimePositionInList = progress;
          // detect end of trimming and play next clip or stop
          if (isEndOfCurrentClip()) {
            playNextClip();
          }
        }
      } catch (Exception exception) {
        Log.d(TAG, "updateSeekBarProgress: exception updating videonaplayer seekbar");
        Log.d(TAG, String.valueOf(exception));
      }
      if (isPlaying()) {
        seekBarUpdaterHandler.postDelayed(updateTimeTask, 20);
      }
    }
  }

  private boolean isEndOfCurrentClip() {
    return seekBar.getProgress() >= (int) clipTimesRanges.get(currentClipIndex()).getUpper();
  }

  /***
   * VideonaPlayer methods.
   ***/

  @Override
  public void onShown(Context context) {
    if (player == null) {
      initVideonaPlayerComponents(context);
    }
  }

  @Override
  public void onDestroy() {
    seekBarUpdaterHandler.removeCallbacksAndMessages(null);
    mainHandler.removeCallbacksAndMessages(null);
  }

  @Override
  public void onPause() {
    pausePreview();
    releaseVideoView();
    releaseMusicPlayer();
  }

  private void releaseVideoView() {
    if (player != null) {
      currentTimePositionInList = getCurrentPosition();
      player.release();
      player = null;
    }
  }

  private void releaseMusicPlayer() {
    if (musicPlayer != null) {
      musicPlayer.stop();
      musicPlayer.release();
      musicPlayer = null;
    }
  }

  /**
   * Sets listener activity to send back player events.
   *
   * @param videonaPlayerListener the activity or class that implements VideonaPlayerListener
   *                              interface
   */
  @Override
  public void setListener(VideonaPlayerListener videonaPlayerListener) {
    this.videonaPlayerListener = videonaPlayerListener;
  }

  private void initClipList() {
    this.videoList = new ArrayList<>();
  }

  private void initSeekBar() {
    seekBar.setProgress(0);
    seekBar.setOnSeekBarChangeListener(this);
  }

  @Override
  public void initPreview(int instantTime) {
    if (playerHasVideos()) {
      this.currentTimePositionInList = instantTime;
      setSeekBarProgress(currentTimePositionInList);
      Video clipToPlay = videoList.get(getClipIndexByProgress(this.currentTimePositionInList));
      initClipPreview(clipToPlay);
    }
  }

  private void initClipPreview(Video clipToPlay) {
    if (rendererBuildingState == RENDERER_BUILDING_STATE_BUILT) {
      player.stop();
    }
    rendererBuilder.cancel();
    videoFormat = null;
    videoRenderer = null;
    rendererBuildingState = RENDERER_BUILDING_STATE_BUILDING;
    maybeReportPlayerState();
    rendererBuilder.buildRenderers(this, Uri.fromFile(new File(clipToPlay.getMediaPath())));
  }

  public void clearImageText() {
    imageTextPreview.setImageDrawable(null);
  }

  private boolean playerHasVideos() {
    return this.videoList.size() > 0;
  }

  private int getClipIndexByProgress(int progress) {
    for (int i = 0; i < clipTimesRanges.size(); i++) {
      if (clipTimesRanges.get(i).contains(progress)) {
        return i;
      }
    }
    return 0;
  }

  private void maybeReportPlayerState() {
    // TODO(jliarte): 31/08/16 is this necessary?
    //        boolean playWhenReady = player.getPlayWhenReady();
    //        int playbackState = getPlaybackState();
    //        if (lastReportedPlayWhenReady != playWhenReady
    //                || lastReportedPlaybackState != playbackState) {
    //            for (Listener listener : listeners) {
    //                listener.onStateChanged(playWhenReady, playbackState);
    //            }
    //            lastReportedPlayWhenReady = playWhenReady;
    //            lastReportedPlaybackState = playbackState;
    //        }
  }

  @Override
  public void initPreviewLists(List<Video> videoList) {
    this.videoList = videoList;
    this.updatePreviewTimeLists();
    this.setSeekBarTotalVideoDuration();
  }

  private void setSeekBarTotalVideoDuration() {
    seekBar.setMax(totalVideoDuration);
    updateTextProjectDuration(totalVideoDuration);
  }

  private void updateTextProjectDuration(int totalProjectDuration) {
    textTimeProjectSeekbar.setText(
        TimeUtils.toFormattedTimeHoursMinutesSecond(totalProjectDuration));
  }

  @Override
  public void bindVideoList(List<Video> videoList) {
    this.initPreviewLists(videoList);
    this.initPreview(currentTimePositionInList);
  }

  @Override
  public void updatePreviewTimeLists() {
    totalVideoDuration = 0;
    clipTimesRanges.clear();
    for (Video clip : videoList) {
      if (videoList.indexOf(clip) != 0) {
        totalVideoDuration++;
      }
      int clipStartTime = this.totalVideoDuration;
      int clipStopTime = this.totalVideoDuration + clip.getDuration();
      clipTimesRanges.add(new Range(clipStartTime, clipStopTime));
      this.totalVideoDuration = clipStopTime;
    }
  }

  @Override
  public void playPreview() {
    if (player != null) {
      player.setPlayWhenReady(true);
    }
    // TODO(jliarte): 31/08/16 else??? - player should not ever be null!!
    hidePlayButton();
    seekBarUpdaterHandler.postDelayed(updateTimeTask, 20);
  }

  @Override
  public void pausePreview() {
    pauseMusic();
    pauseVideo();
    seekBarUpdaterHandler.removeCallbacksAndMessages(null);
    showPlayButton();
  }

  private void pauseVideo() {
    if (player != null) {
      player.setPlayWhenReady(false);
    }
  }

  private void pauseMusic() {
    if (musicPlayer != null && musicPlayer.isPlaying()) {
      musicPlayer.pause();
    }
  }

  @Override
  public void seekTo(int seekTimeInMsec) {
    if (player != null && seekTimeInMsec < totalVideoDuration) {
      currentTimePositionInList = seekTimeInMsec;
      setSeekBarProgress(currentTimePositionInList);
      int clipIndex = getClipIndexByProgress(currentTimePositionInList);
      if (currentClipIndex() != clipIndex) {
        seekToClip(clipIndex);
      } else {
        player.seekTo(getClipPositionFromTimeLineTime());
      }
    }
  }

  @Override
  public void seekClipTo(int seekTimeInMsec) {
    if (player != null) {
      currentTimePositionInList = seekTimeInMsec
          - videoList.get(currentClipIndex()).getStartTime()
          + (int) clipTimesRanges.get(currentClipIndex()).getLower();
      setSeekBarProgress(currentTimePositionInList);
      player.seekTo(seekTimeInMsec);
    }
  }

  @Override
  public void seekToClip(int position) {
    pausePreview();
    currentClipIndex = position;
    // TODO(jliarte): 6/09/16 (hot)fix for Pablo's Index out of bonds
    if (position >= videoList.size() && position >= clipTimesRanges.size()) {
      position = 0;
    }
    currentTimePositionInList = (int) clipTimesRanges.get(currentClipIndex()).getLower();
    initClipPreview(videoList.get(position));
    setSeekBarProgress(currentTimePositionInList);
  }

  @Override
  public void setMusic(Music music) {
    this.music = music;
    initMusicPlayer();
    musicPlayer.seekTo(currentTimePositionInList);
  }

  @Override
  public void setVolumen(float volume) {
    setVolume(volume);
  }

  @Override
  public int getCurrentPosition() {
    return currentTimePositionInList;
  }


  @Override
  public void setSeekBarProgress(int progress) {
    seekBar.setProgress(progress);
    textTimeCurrentSeekbar.setText(TimeUtils.toFormattedTimeHoursMinutesSecond(progress));
  }

  @Override
  public void setSeekBarLayoutEnabled(boolean seekBarEnabled) {
    if (seekBarEnabled) {
      seekBarLayout.setVisibility(VISIBLE);
    } else {
      seekBarLayout.setVisibility(GONE);
    }
  }

  @Override
  public void resetPreview() {
    setBlackBackgroundColor();
    showPlayButton();
    initPreview(0);
    setSeekBarProgress(0);
    updateTextProjectDuration(0);
    clearImageText();
  }

  private void setBlackBackgroundColor() {
    videoPreview.setBackgroundColor(Color.BLACK);
  }

  /**
   * Sets text and text position for video preview.
   *
   * @param text         the text to render
   * @param textPosition the text position
   */
  public void setImageText(String text, String textPosition) {
    Drawable textDrawable = drawableGenerator.createDrawableWithTextAndPosition(
        text, textPosition, Constants.DEFAULT_CANVAS_WIDTH, Constants.DEFAULT_CANVAS_HEIGHT);
    imageTextPreview.setImageDrawable(textDrawable);
  }

  /***
   * End of VideonaPlayer methods.
   ***/

  public void onClickPlayPauseButton() {
    if (playerHasVideos()) {
      if (isPlaying()) {
        pausePreview();
      } else {
        playPreview();
      }
    }
  }

  /**
   * Touch event listener for video preview.
   *
   * @param event received @{@link MotionEvent}
   * @return true if event has beeen processed
   */

  public boolean onTouchPreview(MotionEvent event) {
    boolean eventProcessed = false;
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      onClickPlayPauseButton();
      eventProcessed = true;
    }
    return eventProcessed;
  }

  /***
   * Seekbar listener methods.
   ***/

  @Override
  public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
    if (fromUser) {
      if (isPlaying()) {
        pausePreview();
      }
      if (playerHasVideos()) {
        hidePlayButton();
        seekTo(progress);
        notifyNewClipPlayed();
      } else {
        setSeekBarProgress(0);
      }
    }
  }

  private boolean isPlaying() {
    if (player == null) {
      return false;
    } else {
      return player.getPlayWhenReady();
    }
  }

  private void showPlayButton() {
    playButton.setVisibility(View.VISIBLE);
  }

  public void hidePlayButton() {
    playButton.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {

  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    showPlayButton();

  }

  /***
   * end of Seekbar listener methods.
   ***/

  private void playNextClip() {
    currentClipIndex++;
    if (hasNextClipToPlay()) {
      Video video = videoList.get(currentClipIndex());
      currentTimePositionInList = (int) clipTimesRanges.get(currentClipIndex()).getLower();
      initClipPreview(video);
    } else {
      pausePreview();
      seekToClip(0);
    }
    notifyNewClipPlayed();
  }

  private boolean hasNextClipToPlay() {
    return currentClipIndex < videoList.size();
  }

  private void notifyNewClipPlayed() {
    if (videonaPlayerListener != null) {
      videonaPlayerListener.newClipPlayed(currentClipIndex());
    }
  }

  /***
   * exo player listener.
   ***/

  @Override
  public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    Log.d(TAG, "Playwhenready: " + playWhenReady + " state: " + playbackState);
    clearImageText();
    switch (playbackState) {
      case ExoPlayer.STATE_BUFFERING:
        break;
      case ExoPlayer.STATE_ENDED:
        if (playWhenReady) {
          // (jliarte): 14/10/16 as playNextClip() was called from both here and
          // updateSeekbarProgress thread, sometimes it's called twice in a clip end, causing
          // a double currentClipIndex++ and thus skipping a clip
          //          playNextClip();
        }
        break;
      case ExoPlayer.STATE_IDLE:
        break;
      case ExoPlayer.STATE_PREPARING:
        break;
      case ExoPlayer.STATE_READY:
        updateClipTextPreview();
        if (playWhenReady) {
          startMusicTrackPlayback();
          // player.seekTo(getClipPositionFromTimeLineTime());
        }
        break;
      default:
        break;
    }
  }

  /**
   * Renders text if has been set for current clip.
   */
  public void updateClipTextPreview() {
    if (videoList.size() > 0 && getCurrentClip().hasText()) {
      setImageText(getCurrentClip().getClipText(), getCurrentClip().getClipTextPosition());
    } else {
      clearImageText();
    }
  }

  private int currentClipIndex() {
    if (currentClipIndex >= videoList.size()) {
      return 0;
    }
    return currentClipIndex;
  }

  private Video getCurrentClip() {
    if (videoList.size() > 0) {
      return videoList.get(currentClipIndex());
    }
    return null;
  }

  private void startMusicTrackPlayback() {
    if (videoHasMusic()) {
      playMusicSyncWithVideo();
    }
  }

  private void playMusicSyncWithVideo() {
    // releaseMusicPlayer();
    initMusicPlayer();
    musicPlayer.seekTo(currentTimePositionInList);
    if (!musicPlayer.isPlaying()) {
      musicPlayer.start();
    }
  }

  /**
   * Sets video preview mute status.
   *
   * @param shouldMute true if preview should mute
   */
  public void muteVideo(boolean shouldMute) {
       /* // TODO(jliarte): 1/09/16 test mute
        if (player != null)
            if (shouldMute) {
                volumeMusic = 0f;
                player.sendMessage(rendererBuilder.getAudioRenderer(),
                                   MediaCodecAudioTrackRenderer.MSG_SET_VOLUME,volumeMusic);
            } else {
                volumeMusic = DEFAULT_VOLUME;
                player.sendMessage(rendererBuilder.getAudioRenderer(),
                                   MediaCodecAudioTrackRenderer.MSG_SET_VOLUME,DEFAULT_VOLUME);
            }
            */
  }

  private boolean videoHasMusic() {
    return (music != null);
  }

  @Override
  public void onPlayWhenReadyCommitted() {

  }

  @Override
  public void onPlayerError(ExoPlaybackException error) {

  }


  public Handler getMainHandler() {
    return mainHandler;
  }

  private void onRenderers(TrackRenderer[] renderers, DefaultBandwidthMeter bandwidthMeter) {
    for (int i = 0; i < RENDERER_COUNT; i++) {
      if (renderers[i] == null) {
        // Convert a null renderer to a dummy renderer.
        renderers[i] = new DummyTrackRenderer();
      }
    }
    // Complete preparation.
    this.videoRenderer = renderers[TYPE_VIDEO];
    this.codecCounters = videoRenderer instanceof MediaCodecTrackRenderer
        ? ((MediaCodecTrackRenderer) videoRenderer).codecCounters
        : renderers[TYPE_AUDIO] instanceof MediaCodecTrackRenderer
        ? ((MediaCodecTrackRenderer) renderers[TYPE_AUDIO]).codecCounters : null;
    this.bandwidthMeter = bandwidthMeter;
    pushSurface(false);
    player.prepare(renderers);
    player.seekTo(getClipPositionFromTimeLineTime());
    setVolume(volumeMusic);
    rendererBuildingState = RENDERER_BUILDING_STATE_BUILT;
  }

  protected int getClipPositionFromTimeLineTime() {
    int clipIndex = getClipIndexByProgress(currentTimePositionInList);
    if (clipIndex >= videoList.size()) {
      return 0;
    }
    return currentTimePositionInList
        - (int) clipTimesRanges.get(clipIndex).getLower()
        + videoList.get(clipIndex).getStartTime();
  }

  private void pushSurface(boolean blockForSurfacePush) {
    if (videoRenderer == null) {
      return;
    }

    if (blockForSurfacePush) {
      player.blockingSendMessage(
          videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
    } else {
      player.sendMessage(
          videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
    }
  }

  /***
   * Renderer Event Listener.
   ***/

  @Override
  public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                 float pixelWidthHeightRatio) {
  }

  /** end of Renderer Event Listener. **/

  /***
   * MediaCodecVideoTrackRenderer EventListener.
   ***/

  @Override
  public void onDrawnToSurface(Surface surface) {

  }

  @Override
  public void onLoadError(int sourceId, IOException exception) {

  }

  @Override
  public void onDroppedFrames(int count, long elapsed) {

  }
  /** End of MediaCodecVideoTrackRenderer EventListener. **/

  /***
   * MediaCodecAudioTrackRenderer.EventListener.
   ***/
  @Override
  public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

  }

  @Override
  public void onAudioTrackInitializationError(AudioTrack.InitializationException exception) {

  }

  @Override
  public void onAudioTrackWriteError(AudioTrack.WriteException exception) {

  }

  @Override
  public void onDecoderInitializationError(
      MediaCodecTrackRenderer.DecoderInitializationException exception) {

  }

  @Override
  public void onCryptoError(MediaCodec.CryptoException exception) {

  }

  @Override
  public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs,
                                   long initializationDurationMs) {

  }

  // TODO(jliarte): 31/08/16 move to interface?
  public void releaseView() {

  }

  /**
   * End of MediaCodecAudioTrackRenderer.EventListener
   **/

  public class RendererBuilder {
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;

    private final Context context;
    private final String userAgent;
    private MediaCodecAudioTrackRenderer audioRenderer;

    public MediaCodecAudioTrackRenderer getAudioRenderer() {
      return audioRenderer;
    }


    public RendererBuilder(Context context, String userAgent) {
      this.context = context;
      this.userAgent = userAgent;
    }

    protected void buildRenderers(VideonaPlayerExo player, Uri videoUri) {
      final long startTime = System.currentTimeMillis();

      Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
      Handler mainHandler = player.getMainHandler();

      // Build the video and audio renderers.
      DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(mainHandler, null);
      DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
      ExtractorSampleSource sampleSource = new ExtractorSampleSource(videoUri, dataSource,
          allocator, BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE, mainHandler, player, 0);
      MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context,
          sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT,
          5000, mainHandler, player, 50);
      audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
          MediaCodecSelector.DEFAULT, null, true, mainHandler, player,
          AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);
      // (jliarte): 1/09/16 maybe it's easier with the traditional method using audio manager,
      //      as here we would to advance the audio track to the current position in timeline
      //      on each clip played
      //            if (musicTrack != null) {
      //                MediaCodecAudioTrackRenderer musicTrackRenderer =
      //                    new MediaCodecAudioTrackRenderer(musicSource,
      //                        MediaCodecSelector.DEFAULT, null)
      //            }

      // Invoke the callback.
      TrackRenderer[] renderers = new TrackRenderer[VideonaPlayerExo.RENDERER_COUNT];
      renderers[VideonaPlayerExo.TYPE_VIDEO] = videoRenderer;
      renderers[VideonaPlayerExo.TYPE_AUDIO] = audioRenderer;
      player.onRenderers(renderers, bandwidthMeter);
      long stopTime = System.currentTimeMillis();
      long elapsedTime = stopTime - startTime;
      Log.d(TAG, "----------- time spent building renderers: " + elapsedTime);
    }

    public void cancel() {
      // Do nothing.
    }
  }

}