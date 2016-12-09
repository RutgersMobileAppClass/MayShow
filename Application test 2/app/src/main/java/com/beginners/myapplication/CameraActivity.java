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


public class CameraActivity extends ImmersiveActivity implements BroadcastListener {
    //private static final String TAG = "CameraActivity";

    private CameraFragment mFragment;
    private BroadcastListener mMainBroadcastListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //initial a broadcastListener
        mMainBroadcastListener = Kickflip.getBroadcastListener();
        Kickflip.setBroadcastListener(mMainBroadcastListener);

        //start the camera fragment
        if (savedInstanceState == null) {
            mFragment = CameraFragment.getInstance();
            getFragmentManager().beginTransaction()
                    .replace(io.kickflip.sdk.R.id.container, mFragment)
                    .commit();
        }
    }

    // override the following functions which are previously in the broadcast class
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
