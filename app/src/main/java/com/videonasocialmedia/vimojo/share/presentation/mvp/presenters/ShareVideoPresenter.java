package com.videonasocialmedia.vimojo.share.presentation.mvp.presenters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.videonasocialmedia.videonamediaframework.model.media.Video;
import com.videonasocialmedia.vimojo.BuildConfig;
import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.auth.domain.usecase.GetAuthToken;
import com.videonasocialmedia.vimojo.domain.editor.AddLastVideoExportedToProjectUseCase;
import com.videonasocialmedia.vimojo.export.domain.ExportProjectUseCase;
import com.videonasocialmedia.vimojo.main.VimojoApplication;
import com.videonasocialmedia.vimojo.domain.project.CreateDefaultProjectUseCase;
import com.videonasocialmedia.vimojo.share.domain.ObtainNetworksToShareUseCase;
import com.videonasocialmedia.vimojo.share.domain.GetFtpListUseCase;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.videonamediaframework.model.media.utils.VideoResolution;
import com.videonasocialmedia.vimojo.share.model.entities.FtpNetwork;
import com.videonasocialmedia.vimojo.share.model.entities.SocialNetwork;
import com.videonasocialmedia.vimojo.share.model.entities.VimojoNetwork;
import com.videonasocialmedia.vimojo.presentation.mvp.presenters.OnExportFinishedListener;
import com.videonasocialmedia.vimojo.share.presentation.mvp.views.ShareVideoView;
import com.videonasocialmedia.vimojo.share.presentation.views.utils.LoggedValidator;
import com.videonasocialmedia.vimojo.sync.UploadToPlatformQueue;
import com.videonasocialmedia.vimojo.utils.ConfigPreferences;
import com.videonasocialmedia.vimojo.utils.Constants;
import com.videonasocialmedia.vimojo.utils.DateUtils;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;
import com.videonasocialmedia.vimojo.utils.Utils;
import com.videonasocialmedia.vimojo.view.VimojoPresenter;
import com.videonasocialmedia.vimojo.sync.model.VideoUpload;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import static android.content.Context.*;

/**
 * Created by jca on 11/12/15.
 */
public class ShareVideoPresenter extends VimojoPresenter {

    private String LOG_TAG = ShareVideoPresenter.class.getCanonicalName();

    private Context context;
    private ObtainNetworksToShareUseCase obtainNetworksToShareUseCase;
    private GetFtpListUseCase getFtpListUseCase;
    private CreateDefaultProjectUseCase createDefaultProjectUseCase;
    private WeakReference<ShareVideoView> shareVideoViewReference;
    protected Project currentProject;
    protected UserEventTracker userEventTracker;
    private SharedPreferences sharedPreferences;
    private List<FtpNetwork> ftpList;
    private List<SocialNetwork> socialNetworkList;
    private VimojoNetwork vimojoNetwork;
    private List optionToShareList;
    private SharedPreferences.Editor preferencesEditor;

    private AddLastVideoExportedToProjectUseCase addLastVideoExportedProjectUseCase;
    private ExportProjectUseCase exportUseCase;
    private final GetAuthToken getAuthToken;
    private UploadToPlatformQueue uploadToPlatformQueue;
    private final LoggedValidator loggedValidator;
    private String authToken;
    private String description;

    @Inject
    public ShareVideoPresenter(Context context, ShareVideoView shareVideoView,
                               UserEventTracker userEventTracker,
                               SharedPreferences sharedPreferences,
                               CreateDefaultProjectUseCase createDefaultProjectUseCase,
                               AddLastVideoExportedToProjectUseCase
                                       addLastVideoExportedProjectUseCase,
                               ExportProjectUseCase exportProjectUseCase,
                               ObtainNetworksToShareUseCase obtainNetworksToShareUseCase,
                               GetFtpListUseCase getFtpListUseCase,
                               GetAuthToken getAuthToken,
                               UploadToPlatformQueue uploadToPlatformQueue,
                               LoggedValidator loggedValidator) {
        this.context = context;
        this.shareVideoViewReference = new WeakReference<>(shareVideoView);
        this.userEventTracker = userEventTracker;
        this.sharedPreferences = sharedPreferences;
        this.createDefaultProjectUseCase = createDefaultProjectUseCase;
        this.addLastVideoExportedProjectUseCase = addLastVideoExportedProjectUseCase;
        this.exportUseCase = exportProjectUseCase;
        this.obtainNetworksToShareUseCase = obtainNetworksToShareUseCase;
        this.getFtpListUseCase = getFtpListUseCase;
        this.getAuthToken = getAuthToken;
        this.uploadToPlatformQueue = uploadToPlatformQueue;
        this.loggedValidator = loggedValidator;
        currentProject = loadCurrentProject();
    }

    private Project loadCurrentProject() {
        return Project.getInstance(null, null, null, null);
    }

    public void onResume() {
        obtainNetworksToShare();
        obtainListFtp();
        setupVimojoNetwork();
        obtainListOptionsToShare(vimojoNetwork, ftpList, socialNetworkList);
        if (shareVideoViewReference != null) {
            shareVideoViewReference.get().showOptionsShareList(optionToShareList);
            shareVideoViewReference.get().startVideoExport();
        }
    }

    private void setupVimojoNetwork() {
        vimojoNetwork = new VimojoNetwork(ConfigPreferences.VIMOJO_NETWORK,
                context.getString(R.string.upload_to_server),
                R.drawable.activity_share_icon_vimojo_network);
    }

    private void obtainListFtp() {
        ftpList = getFtpListUseCase.getFtpList();
    }

    public void obtainNetworksToShare() {
        socialNetworkList = obtainNetworksToShareUseCase.obtainMainNetworks();
    }

    private void obtainListOptionsToShare(VimojoNetwork vimojoNetwork, List<FtpNetwork> ftpList,
                                          List<SocialNetwork> socialNetworkList) {
        optionToShareList = new ArrayList();
        if (BuildConfig.FEATURE_UPLOAD_VIDEOS) {
            optionToShareList.add(vimojoNetwork);
        }
        if (BuildConfig.FEATURE_FTP) {
            optionToShareList.addAll(ftpList);
        }
        optionToShareList.addAll(socialNetworkList);
    }

    public void shareVideo(String videoPath, SocialNetwork appToShareWith, Context ctx) {
        final ComponentName name = new ComponentName(appToShareWith.getAndroidPackageName(),
                appToShareWith.getAndroidActivityName());

        Uri uri = Utils.obtainUriToShare(ctx, videoPath);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                VimojoApplication.getAppContext().getResources().getString(R.string.sharedWithVideona));
        intent.putExtra(Intent.EXTRA_TEXT,
                VimojoApplication.getAppContext().getResources().getString(R.string.videonaTags));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        intent.setComponent(name);

        ctx.startActivity(intent);
    }

    // TODO(jliarte): 15/12/16 safe delete this method - old way to show networks?
    public void obtainExtraAppsToShare() {
        List networks = obtainNetworksToShareUseCase.obtainSecondaryNetworks();
        if (shareVideoViewReference.get() != null) {
            shareVideoViewReference.get().hideShareNetworks();
            shareVideoViewReference.get().showMoreNetworks(networks);
        }
    }

    public void updateNumTotalVideosShared() {
        int totalVideosShared = sharedPreferences.getInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, 0);
        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, ++totalVideosShared);
        preferencesEditor.commit();
    }

    public int getNumTotalVideosShared() {
        return sharedPreferences.getInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, 0);
    }

    public String getResolution() {
        VideoResolution videoResolution = currentProject.getProfile().getVideoResolution();
        return videoResolution.getWidth() + "x" + videoResolution.getHeight();
    }

    public void trackVideoShared(String socialNetwork) {
        userEventTracker.trackVideoSharedSuperProperties();
        userEventTracker.trackVideoShared(socialNetwork, currentProject, getNumTotalVideosShared());
        userEventTracker.trackVideoSharedUserTraits();
    }

    public void newDefaultProject(String rootPath, String privatePath) {
        clearProjectDataFromSharedPreferences();
        createDefaultProjectUseCase.createProject(rootPath, privatePath, isWatermarkActivated());
    }

    private boolean isWatermarkActivated() {
        if (BuildConfig.FEATURE_FORCE_WATERMARK) {
            return true;
        }
        return sharedPreferences.getBoolean(ConfigPreferences.WATERMARK, false);
    }

    // TODO(jliarte): 23/10/16 should this be moved to activity or other outer layer? maybe a repo?
    // TODO:(alvaro.martinez) 4/01/17 these data will no be saved in SharedPreferences,
    // rewrite mixpanel tracking and delete.
    private void clearProjectDataFromSharedPreferences() {
        sharedPreferences = VimojoApplication.getAppContext().getSharedPreferences(
                ConfigPreferences.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                MODE_PRIVATE);
        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putLong(ConfigPreferences.VIDEO_DURATION, 0);
        preferencesEditor.putInt(ConfigPreferences.NUMBER_OF_CLIPS, 0);
    }

    public void addVideoExportedToProject(String videoPath) {
        addLastVideoExportedProjectUseCase.addLastVideoExportedToProject(videoPath,
                DateUtils.getDateRightNow());
    }

    public void startExport() {
        exportUseCase.export(Constants.PATH_WATERMARK, new OnExportFinishedListener() {
            @Override
            public void onExportError(String error) {
                Crashlytics.log("Error exporting: " + error);
                // TODO(jliarte): 28/04/17 pass the string?
                // known strings
                switch (error) {
                    case "No space left on device":
                        if (shareVideoViewReference.get() != null) {
                            shareVideoViewReference.get()
                                    .showVideoExportError(Constants.EXPORT_ERROR_NO_SPACE_LEFT);
                        }
                        break;
                    default:
                        if (shareVideoViewReference.get() != null) {
                            shareVideoViewReference.get()
                                    .showVideoExportError(Constants.EXPORT_ERROR_UNKNOWN);
                        }
                }
            }

            @Override
            public void onExportSuccess(final Video video) {
                if (shareVideoViewReference.get() != null) {
                    shareVideoViewReference.get().loadExportedVideoPreview(video.getMediaPath());
                }
            }

            @Override
            public void onExportProgress(String progressMsg, int exportStage) {
                if (shareVideoViewReference.get() != null) {
                    shareVideoViewReference.get().showExportProgress(progressMsg);
                }
            }
        });
    }

    public void clickUploadToPlatform(boolean isWifiConnected,
                                      boolean acceptUploadVideoMobileNetwork,
                                      boolean isMobileNetworkConnected,
                                      String videoPath) {
        if(!isWifiOrMobileNetworkConnected(isWifiConnected, isMobileNetworkConnected)) {
            // TODO: 8/2/18 Should I saved this upload until user would be connected to network
            shareVideoViewReference.get().showError(context.getString(R.string.connect_to_network));
            return;
        }
        if(isNeededAskPermissionForMobileUpload(isWifiConnected, isMobileNetworkConnected,
            acceptUploadVideoMobileNetwork)) {
            shareVideoViewReference.get().showDialogUploadVideoWithMobileNetwork();
            return;
        }
        if(!isUserLogged(authToken)) {
            // TODO: 8/2/18 Should I ask confirmation from user that he is going to navigate to User Authentication screen.
            shareVideoViewReference.get().navigateToUserAuth();
            return;
        }
        if(!isThereFreeStorageOnPlatform(videoPath)) {
            // TODO:(alvaro.martinez) 26/01/18 Get user free storage from platform
            //shareVideoViewReference.get().showError("Don´t have enough storage to upload video");
            return;
        }
        /*
        if(!areThereProjectFieldsCompleted()){
            // TODO:(alvaro.martinez) 26/01/18 Check project fields, title, description, product types. Next story to merged.
            //shareVideoViewReference.get().showMessage("You need to complete project fields information.");
            return;
        }*/
        // TODO: 2/2/18 Define description Send flavor name for testing field
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String description = BuildConfig.FLAVOR + "_" + timeStamp;
        Log.d(LOG_TAG, "description " + description);
        uploadVideo(authToken, videoPath, description);
    }

    public void checkUserLoggedWithPlatform() {
        // TODO: 8/2/18 If user want to upload videos, should wait to this Future and Storage service
        ListenableFuture<String> authTokenFuture = executeUseCaseCall(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getAuthToken.getAuthToken(context).getToken();
            }
        });
        Futures.addCallback(authTokenFuture, new FutureCallback<String>() {
            @Override
            public void onSuccess(String authorizationToken) {
               authToken = authorizationToken;
            }
            @Override
            public void onFailure(Throwable errorGettingToken) {
            }
        });
    }

    private boolean isWifiOrMobileNetworkConnected(boolean isWifiConnected,
                                                   boolean isMobileNetworConnected) {
        if(isWifiConnected || isMobileNetworConnected) {
            return true;
        }
        return false;
    }

    private boolean isNeededAskPermissionForMobileUpload(boolean isWifiConnected,
                                                         boolean isMobileNetworConnected,
                                                         boolean acceptUploadVideoMobileNetwork) {
        return !isWifiConnected && isMobileNetworConnected && !acceptUploadVideoMobileNetwork;
    }

    protected boolean isUserLogged(String authToken) {
        return loggedValidator.loggedValidate(authToken);
    }

    private void uploadVideo(String authToken, String mediaPath, String description) {
        VideoUpload videoUpload = new VideoUpload(authToken, mediaPath, description);
        try {
            uploadToPlatformQueue.addVideoToUpload(videoUpload);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            Crashlytics.log("Error adding video to upload");
            Crashlytics.logException(ioException);
        }
        try {
            uploadToPlatformQueue.launchQueueVideoUploads();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            Crashlytics.log("Error launching queue video to upload");
            Crashlytics.logException(ioException);
        }
    }

    private boolean isThereFreeStorageOnPlatform(String mediaPath) {
        long videoToUploadLength = new File(mediaPath).length();
        // return (freeStorage > videoToUploadLenght)
        return true;
    }
}