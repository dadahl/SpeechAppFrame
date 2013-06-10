package com.ct.SpeechAppFrame;

import org.apache.cordova.*;

import android.os.Bundle;
import android.webkit.WebSettings;

public class SpeechAppFrameActivity extends DroidGap {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/index.html");
        WebSettings settings = this.appView.getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
    }
}