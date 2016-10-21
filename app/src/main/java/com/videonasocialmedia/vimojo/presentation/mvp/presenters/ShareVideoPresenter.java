package com.videonasocialmedia.vimojo.presentation.mvp.presenters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.videonasocialmedia.vimojo.R;
import com.videonasocialmedia.vimojo.VimojoApplication;
import com.videonasocialmedia.vimojo.domain.social.ObtainNetworksToShareUseCase;
import com.videonasocialmedia.vimojo.domain.social.GetFtpListUseCase;
import com.videonasocialmedia.vimojo.model.entities.editor.Project;
import com.videonasocialmedia.vimojo.model.entities.editor.utils.VideoResolution;
import com.videonasocialmedia.vimojo.model.entities.social.FtpNetwork;
import com.videonasocialmedia.vimojo.model.entities.social.SocialNetwork;
import com.videonasocialmedia.vimojo.presentation.mvp.views.ShareVideoView;
import com.videonasocialmedia.vimojo.utils.ConfigPreferences;
import com.videonasocialmedia.vimojo.utils.UserEventTracker;
import com.videonasocialmedia.vimojo.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jca on 11/12/15.
 */
public class ShareVideoPresenter {

    private ObtainNetworksToShareUseCase obtainNetworksToShareUseCase;
    private GetFtpListUseCase getFtpListUseCase;
    private ShareVideoView shareVideoView;
    protected Project currentProject;
    protected UserEventTracker userEventTracker;
    private SharedPreferences sharedPreferences;
    private List<FtpNetwork> ftpList;
    private List<SocialNetwork> socialNetworkList;
    private List optionToShareList;
    private SharedPreferences.Editor preferencesEditor;

    public ShareVideoPresenter(ShareVideoView shareVideoView, UserEventTracker userEventTracker,
                               SharedPreferences sharedPreferences) {
        this.shareVideoView = shareVideoView;
        this.userEventTracker = userEventTracker;
        this.sharedPreferences = sharedPreferences;
        currentProject = loadCurrentProject();
    }

    private Project loadCurrentProject() {
        return Project.getInstance(null, null, null);
    }

    public void onCreate() {
        obtainNetworksToShareUseCase = new ObtainNetworksToShareUseCase();
        getFtpListUseCase = new GetFtpListUseCase();
    }

    public void onResume() {
        obtainNetworksToShare();
        obtainListFtp();
        obtainListOptionsToShare(ftpList, socialNetworkList);
        shareVideoView.showOptionsShareList(optionToShareList);
    }

    private void obtainListFtp() {
        ftpList = getFtpListUseCase.getFtpList();
    }

    public void obtainNetworksToShare() {
       socialNetworkList = obtainNetworksToShareUseCase.obtainMainNetworks();
    }

    private void obtainListOptionsToShare(List<FtpNetwork> ftpList, List<SocialNetwork> socialNetworkList) {
        optionToShareList = new ArrayList();
        optionToShareList.addAll(ftpList);
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

    public void obtainExtraAppsToShare() {
        List networks = obtainNetworksToShareUseCase.obtainSecondaryNetworks();
        shareVideoView.hideShareNetworks();
        shareVideoView.showMoreNetworks(networks);
    }

    public void updateNumTotalVideosShared() {
        int totalVideosShared = sharedPreferences.getInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, 0);
        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, ++totalVideosShared);
        preferencesEditor.commit();
    }

    public int getNumTotalVideosShared() {
        return  sharedPreferences.getInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, 0);
    }

    public String getResolution(){
        VideoResolution videoResolution = currentProject.getProfile().getVideoResolution();
        return videoResolution.getWidth() + "x" + videoResolution.getHeight();
    }

    public void trackVideoShared(String socialNetwork) {

        userEventTracker.trackVideoSharedSuperProperties();
        userEventTracker.trackVideoShared(socialNetwork, currentProject, getNumTotalVideosShared());
        userEventTracker.trackVideoSharedUserTraits();
    }
}
