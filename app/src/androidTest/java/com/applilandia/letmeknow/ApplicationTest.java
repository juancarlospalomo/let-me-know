package com.applilandia.letmeknow;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    private static final String LOG_TAG = ApplicationTest.class.getSimpleName();

    public ApplicationTest() {
        super(Application.class);
    }

    public void setUp() {
        Log.v(LOG_TAG, "setup test");
    }
}