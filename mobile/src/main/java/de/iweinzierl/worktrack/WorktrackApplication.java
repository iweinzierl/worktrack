package de.iweinzierl.worktrack;

import android.app.Application;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EApplication;

@EApplication
public class WorktrackApplication extends Application {

    public static final String LOG_TAG = "[WT]";

    @AfterInject
    public void setup() {
        AndroidLoggerFactory.getInstance(LOG_TAG);
    }
}
