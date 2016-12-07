package com.beginners.myapplication;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.eventbus.Subscribe;

import java.io.IOException;
import io.kickflip.sdk.Kickflip;
import io.kickflip.sdk.Share;
import io.kickflip.sdk.api.KickflipApiClient;
import io.kickflip.sdk.api.KickflipCallback;
import io.kickflip.sdk.api.json.Response;
import io.kickflip.sdk.api.json.User;
import io.kickflip.sdk.av.Broadcaster;
import io.kickflip.sdk.av.FullFrameRect;
import io.kickflip.sdk.event.BroadcastIsBufferingEvent;
import io.kickflip.sdk.event.BroadcastIsLiveEvent;
import io.kickflip.sdk.exception.KickflipException;
import io.kickflip.sdk.view.GLCameraEncoderView;

/**
 * Created by shiwang on 12/1/16.
 */
public class CameraFragment extends Fragment  {
    private static final String TAG = "BroadcastFragment";
    private static final boolean VERBOSE = false;
    private static CameraFragment mFragment;
    private static Broadcaster mBroadcaster;        // Make static to survive Fragment re-creation
    private GLCameraEncoderView mCameraView;


    View.OnClickListener mRecordButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBroadcaster.isRecording()) {
                mBroadcaster.stopRecording();
                v.setBackgroundResource(io.kickflip.sdk.R.drawable.red_dot);

            } else {
                mBroadcaster.startRecording();
                v.setBackgroundResource(io.kickflip.sdk.R.drawable.red_dot_stop);
            }
        }
    };



    public CameraFragment() {
        // Required empty public constructor
        if (VERBOSE) Log.i(TAG, "construct");
    }

    public static CameraFragment getInstance() {
        if (mFragment == null) {
            // We haven't yet created a BroadcastFragment instance
            mFragment = recreateCameraFragment();
        } else if (mBroadcaster != null && !mBroadcaster.isRecording()) {
            // We have a leftover BroadcastFragment but it is not recording
            // Treat it as finished, and recreate
            mFragment = recreateCameraFragment();
        } else {
            Log.i(TAG, "Recycling BroadcastFragment");
        }
        return mFragment;
    }

    private static CameraFragment recreateCameraFragment() {
        Log.i(TAG, "Recreating BroadcastFragment");
        mBroadcaster = null;
        return new CameraFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (VERBOSE) Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (!Kickflip.readyToBroadcast()) {
            Log.e(TAG, "Kickflip not properly prepared by BroadcastFragment's onCreate. SessionConfig: " + Kickflip.getSessionConfig() + " key " + Kickflip.getApiKey() + " secret " + Kickflip.getApiSecret());
        } else {
            setupBroadcaster();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (VERBOSE) Log.i(TAG, "onAttach");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBroadcaster != null)
            mBroadcaster.onHostActivityResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBroadcaster != null)
            mBroadcaster.onHostActivityPaused();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBroadcaster != null && !mBroadcaster.isRecording())
            mBroadcaster.release();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (VERBOSE) Log.i(TAG, "onCreateView");

        View root;
        if (mBroadcaster != null && getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            root = inflater.inflate(R.layout.fragment_camera, container, false);
            mCameraView = (GLCameraEncoderView) root.findViewById(R.id.cameraPreview);
            mCameraView.setKeepScreenOn(true);
            mBroadcaster.setPreviewDisplay(mCameraView);
            Button recordButton = (Button) root.findViewById(R.id.recordButton);

            recordButton.setOnClickListener(mRecordButtonClickListener);



            if (mBroadcaster.isRecording()) {
                recordButton.setBackgroundResource(io.kickflip.sdk.R.drawable.red_dot_stop);
            }

            setupCameraFlipper(root);
        } else
            root = new View(container.getContext());
        return root;
    }

    protected void setupBroadcaster() {
        // By making the recorder static we can allow
        // recording to continue beyond this fragment's
        // lifecycle! That means the user can minimize the app
        // or even turn off the screen without interrupting the recording!
        // If you don't want this behavior, call stopRecording
        // on your Fragment/Activity's onStop()
            if (mBroadcaster == null) {
                if (VERBOSE)
                    Log.i(TAG, "Setting up Broadcaster for output " + Kickflip.getSessionConfig().getOutputPath() + " client key: " + Kickflip.getApiKey() + " secret: " + Kickflip.getApiSecret());
                // TODO: Don't start recording until stream start response, so we can determine stream type...
                Context context = getActivity().getApplicationContext();
                try {
                    mBroadcaster = new Broadcaster(context, Kickflip.getSessionConfig(), Kickflip.getApiKey(), Kickflip.getApiSecret());
                    ////////////////////////

                    mBroadcaster.getEventBus().register(this);
                    mBroadcaster.setBroadcastListener(Kickflip.getBroadcastListener());
                    Kickflip.clearSessionConfig();


                } catch (IOException e) {
                    Log.e(TAG, "Unable to create Broadcaster. Could be trouble creating MediaCodec encoder.");
                    e.printStackTrace();
                }
            }
        }




    private void setupCameraFlipper(View root) {
        View flipper = root.findViewById(R.id.cameraFlipper);
        if (Camera.getNumberOfCameras() == 1) {
            flipper.setVisibility(View.GONE);
        } else {
            flipper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBroadcaster.requestOtherCamera();
                }
            });
        }
    }


    public void stopBroadcasting() {
        if (mBroadcaster.isRecording()) {
            mBroadcaster.stopRecording();
            mBroadcaster.release();
        }
    }


}
