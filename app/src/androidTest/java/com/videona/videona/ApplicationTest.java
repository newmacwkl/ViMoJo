package com.videona.videona;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<VideonaApplication> {
    public ApplicationTest() {
        super(VideonaApplication.class);
    }

    public void testApiClientIsNotNull () throws Exception{
        assertNotNull(this.getApplication().getApiClient());
    }
}