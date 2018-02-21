package com.videonasocialmedia.vimojo.vimojoapiclient;

/**
 * Created by jliarte on 8/02/18.
 */

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.videonasocialmedia.vimojo.BuildConfig;
import com.videonasocialmedia.vimojo.vimojoapiclient.model.VimojoApiError;
import com.videonasocialmedia.vimojo.vimojoapiclient.rest.ServiceGenerator;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * Parent Vimojo Api client with common methods for all vimojo api clients.
 */
class VimojoApiClient {
  private static final int INVALID_AUTH_CODE = 401;

  // TODO(jliarte): 12/01/18 tune this parameter
  private static final int N_THREADS = 5;
  private final ListeningExecutorService executorPool;

  public VimojoApiClient() {
    executorPool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(N_THREADS));
  }

  <T> T getService(Class<T> serviceClass) {
    return new ServiceGenerator(BuildConfig.API_BASE_URL).generateService(serviceClass);
  }

  <T> T getService(Class<T> serviceClass, String authToken) {
    return new ServiceGenerator(BuildConfig.API_BASE_URL).generateService(serviceClass, authToken);
  }

  <T> void parseError(Response<T> response) throws VimojoApiException {
    String apiErrorCode = "unknown error";
    int httpCode = response.code();
    if (response.errorBody() != null) {
      Gson gson = new Gson();
      try {
        String errorBody = response.errorBody().string();
        VimojoApiError apiError = gson.fromJson(errorBody, VimojoApiError.class);
        if (apiError.getError() != null && !apiError.getError().equals("")) {
          apiErrorCode = apiError.getError();
        }
      } catch (IOException ioException) {
        if (BuildConfig.DEBUG) {
          // TODO(jliarte): 12/01/18 check for occurrences
          ioException.printStackTrace();
        }
      }
      if (httpCode == INVALID_AUTH_CODE) {
        //        throw new VimojoAuthApiException(execute.code(), apiErrorCode);
        throw new VimojoApiException(httpCode, VimojoApiException.UNAUTHORIZED);
      } else {
        throw new VimojoApiException(httpCode, apiErrorCode);
      }
    }
  }

  protected final <T> ListenableFuture<T> executeUseCaseCall(Callable<T> callable) {
    return executorPool.submit(callable);
  }
}
