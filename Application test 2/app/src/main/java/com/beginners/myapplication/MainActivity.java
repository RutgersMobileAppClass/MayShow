package com.beginners.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

import io.kickflip.sdk.Kickflip;
import io.kickflip.sdk.api.json.Stream;
import io.kickflip.sdk.av.BroadcastListener;
import io.kickflip.sdk.av.SessionConfig;
import io.kickflip.sdk.exception.KickflipException;
import io.kickflip.sdk.fragment.BroadcastFragment;

import static io.kickflip.sdk.Kickflip.isKickflipUrl;

public class MainActivity extends Activity implements MainFragmentInteractionListener, StreamListFragment.StreamListFragmenListener {
    private static final String TAG = "MainActivity";

//    private boolean mKickflipReady = false;

//  public static KickflipApiClient mKickflip;

    private EditText searchtext;
    private ImageButton searchhere;

// create a broadcastListener
    private BroadcastListener mBroadcastListener = new BroadcastListener() {
        @Override
        public void onBroadcastStart() {
            Log.i(TAG, "onBroadcastStart");
        }

        @Override
        public void onBroadcastLive(Stream stream) {
            Log.i(TAG, "onBroadcastLive @ " + stream.getKickflipUrl());
        }

        @Override
        public void onBroadcastStop() {
            Log.i(TAG, "onBroadcastStop");
            //if you wannt to manually stop the broadcast
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commit();
        }

        @Override
        public void onBroadcastError(KickflipException error) {
            Log.i(TAG, "onBroadcastError " + error.getMessage());
        }
    };

    // stores video in a "Kickflip" directory on external storage
    private String mRecordingOutputPath = new File(Environment.getExternalStorageDirectory(), "MySampleApp/index.m3u8").getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_main);

        //Toast.makeText(this, "Welcome to Mayshow!", Toast.LENGTH_SHORT).show();

        searchtext = (EditText) findViewById(R.id.search_name);
        searchhere = (ImageButton) findViewById(R.id.btn_search);

        if(!handleLaunchingIntent()) {
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new StreamListFragment())
                        .commit();
            }
        }
    }


    //show the Sqaure fragment
    public void onTabClicked(View v){
        searchtext.setVisibility(View.VISIBLE);
        searchhere.setVisibility(View.VISIBLE);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new StreamListFragment())
                .commit();

    }

    //start a live video stream
    public void onTabClicked1(View v){
        if (SigninActivity.mKickflipReady) {
            //
            startBroadcastingActivity();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_title_not_ready))
                    .setMessage(getString(R.string.dialog_msg_not_ready))
                    .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }

    //show my profile fragment
    public void onTabClicked2(View v){
        searchtext.setVisibility(View.GONE);
        searchhere.setVisibility(View.GONE);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new MyprofileFragment())
                .commit();

    }
    @Override
    public void onFragmentEvent(MainFragment.EVENT event) {
        startBroadcastingActivity();
    }


    //start a media player, get the media URL from the Stream structure
    @Override
    public void onStreamPlaybackRequested(String streamUrl) {
        //start a mediaActivity to play the video
        Intent playbackIntent = new Intent(this, MediaActivity.class);
        playbackIntent.putExtra("mediaUrl", streamUrl);
        startActivity(playbackIntent);

    }

    // start a camera activity
    private void startBroadcastingActivity() {
        configureNewBroadcast();

        Intent broadcastIntent = new Intent(this, CameraActivity.class);
        startActivity(broadcastIntent);

    }

    //set the configuration of the live stream, here is 720p
    private void configureNewBroadcast() {
        // Should reset mRecordingOutputPath between recordings
        SessionConfig config = Util.create720pSessionConfig(mRecordingOutputPath);
        //SessionConfig config = Util.create420pSessionConfig(mRecordingOutputPath);
        Kickflip.setSessionConfig(config);
    }

    //
    private boolean handleLaunchingIntent() {
        Uri intentData = getIntent().getData();
        if (isKickflipUrl(intentData)) {
            Kickflip.startMediaPlayerActivity(this, intentData.toString(), true);
            finish();
            return true;
        }
        return false;
    }
}
