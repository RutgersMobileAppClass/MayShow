package com.beginners.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import io.kickflip.sdk.Kickflip;
import io.kickflip.sdk.Share;
import io.kickflip.sdk.activity.ImmersiveActivity;
import io.kickflip.sdk.api.json.Stream;
import io.kickflip.sdk.av.AVRecorder;
import io.kickflip.sdk.av.BroadcastListener;
import io.kickflip.sdk.av.Broadcaster;
import io.kickflip.sdk.av.FullFrameRect;
import io.kickflip.sdk.av.SessionConfig;
import io.kickflip.sdk.exception.KickflipException;
import io.kickflip.sdk.fragment.BroadcastFragment;
import io.kickflip.sdk.view.GLCameraEncoderView;

/**
 * Demonstrates using the Kickflip SDK components to
 * create a more traditional Camera app that allows creating multiple
 * recordings per Activity lifecycle.
 *
 * In this example recording will stop if the Activity proceeds through {@link #onStop()}
 *
 * <b>Note:</b> This Activity is marked in AndroidManifest.xml with the property android:configChanges="orientation|screenSize"
 *              This is a shortcut to prevent the onDestroy ... onCreate ... onDestroy cycle when the screen powers off. Without this
 *              shortcut, more careful management of application state is required to make sure you don't create a recorder and set the preview
 *              display as a result of that ephemeral onCreate. If you do, you'll get a crash as the Camera won't be able to be acquired.
 *              See: http://stackoverflow.com/questions/10498636/prevent-android-activity-from-being-recreated-on-turning-screen-off
 */
public class CameraActivity extends ImmersiveActivity implements BroadcastListener {
    private static final String TAG = "CameraActivity";

    private CameraFragment mFragment;
    private BroadcastListener mMainBroadcastListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Intent i = getIntent();
        System.out.println("==========="+i.getStringExtra("name"));

        mMainBroadcastListener = Kickflip.getBroadcastListener();
        Kickflip.setBroadcastListener(mMainBroadcastListener);

        if (savedInstanceState == null) {
            mFragment = CameraFragment.getInstance();
            getFragmentManager().beginTransaction()
                    .replace(io.kickflip.sdk.R.id.container, mFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragment != null) {
            mFragment.stopBroadcasting();
        }
        super.onBackPressed();
    }

    @Override
    public void onBroadcastStart() {
        mMainBroadcastListener.onBroadcastStart();
    }

    @Override
    public void onBroadcastLive(Stream stream) {
        mMainBroadcastListener.onBroadcastLive(stream);
    }

    @Override
    public void onBroadcastStop() {
        finish();
        mMainBroadcastListener.onBroadcastStop();
    }

    @Override
    public void onBroadcastError(KickflipException error) {
        mMainBroadcastListener.onBroadcastError(error);
    }

}
