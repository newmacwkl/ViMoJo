package com.videonasocialmedia.vimojo.vimojoapiclient;

/**
 * Created by alvaro on 28/11/17.
 */

import com.videonasocialmedia.vimojo.vimojoapiclient.model.Video;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Class describing video services.
 */
public interface VideoService {
  @Multipart
  @POST("video")
  Call<Video> uploadVideo(
          // TODO(jliarte): 8/02/18 check if we can model the request body into a vimojoapiclient.model
          @Part("description") RequestBody description,
          @Part MultipartBody.Part file
  );
}
