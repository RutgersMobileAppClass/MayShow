package com.beginners.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.IOException;

import io.kickflip.sdk.Kickflip;
import io.kickflip.sdk.activity.BroadcastActivity;
import io.kickflip.sdk.api.KickflipApiClient;
import io.kickflip.sdk.api.KickflipCallback;
import io.kickflip.sdk.api.json.Response;
import io.kickflip.sdk.api.json.Stream;
import io.kickflip.sdk.api.json.User;
import io.kickflip.sdk.av.BroadcastListener;
import io.kickflip.sdk.av.SessionConfig;
import io.kickflip.sdk.exception.KickflipException;
import io.kickflip.sdk.fragment.BroadcastFragment;

import static io.kickflip.sdk.Kickflip.isKickflipUrl;

public class MainActivity extends Activity implements MainFragmentInteractionListener, StreamListFragment.StreamListFragmenListener {
    private static final String TAG = "MainActivity";


    private boolean mKickflipReady = false;


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

            // If you're manually injecting the BroadcastFragment,
            // you'll want to remove/replace BroadcastFragment
            // when the Broadcast is over.

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commit();
        }

        @Override
        public void onBroadcastError(KickflipException error) {
            Log.i(TAG, "onBroadcastError " + error.getMessage());
        }
    };
    // By default, Kickflip stores video in a "Kickflip" directory on external storage
    private String mRecordingOutputPath = new File(Environment.getExternalStorageDirectory(), "MySampleApp/index.m3u8").getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_main);




        // This must happen before any other Kickflip interactions
        Kickflip.setup(this, SECRETS.CLIENT_KEY, SECRETS.CLIENT_SECRET, new KickflipCallback() {
            @Override
            public void onSuccess(Response response) {
                mKickflipReady = true;
            }

            @Override
            public void onError(KickflipException error) {

            }
        });

        if(!handleLaunchingIntent()) {
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new StreamListFragment())
                        .commit();
            }
        }
       // tintStatusBar();


        findViewById(R.id.btn_square).setSelected(true);
        findViewById(R.id.btn_publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mKickflipReady) {
                    startBroadcastingActivity();
                }
//                else {
//                    new AlertDialog.Builder(this)
//                            .setTitle(getString(R.string.dialog_title_not_ready))
//                            .setMessage(getString(R.string.dialog_msg_not_ready))
//                            .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            }).show();
            }
        });

        final Intent i = new Intent(this, ProfileActivity.class);
        findViewById(R.id.btn_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(i);
            }
        });

    }

//    private void tintStatusBar() {
//        SystemBarTintManager tintManager = new SystemBarTintManager(this);
//        // enable status bar tint
//        tintManager.setStatusBarTintEnabled(true);
//        // enable navigation bar tint
//        tintManager.setNavigationBarTintEnabled(false);
////        tintManager.setTintColor(getResources().getColor(R.color.kickflip_green));
//        tintManager.setTintColor(R.drawable.login_bg);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_broadcast:
//                if (mKickflipReady) {
//                    startBroadcastingActivity();
//                } else {
//                    new AlertDialog.Builder(this)
//                            .setTitle(getString(R.string.dialog_title_not_ready))
//                            .setMessage(getString(R.string.dialog_msg_not_ready))
//                            .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            }).show();
//                }
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onFragmentEvent(MainFragment.EVENT event) {
        startBroadcastingActivity();
    }

    /**
     * Unused method demonstrating how to use
     * Kickflip's BroadcastFragment.
     * <p/>
     * Note that in this scenario your Activity is responsible for
     * removing the BroadcastFragment in your onBroadcastStop callback.
     * When the user stops recording, the BroadcastFragment begins releasing
     * resources and freezes the camera preview.
     */
    public void startBroadcastFragment() {
        // Before using the BroadcastFragment, be sure to
        // register your BroadcastListener with Kickflip
        configureNewBroadcast();
        Kickflip.setBroadcastListener(mBroadcastListener);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, BroadcastFragment.getInstance())
                .commit();
    }


    @Override
    public void onStreamPlaybackRequested(String streamUrl) {
        // Play with Kickflip's built-in Media Player
        Kickflip.startMediaPlayerActivity(this, streamUrl, false);

        // Play via Intent for 3rd party Media Player
        //Intent i = new Intent(Intent.ACTION_VIEW);
        //i.setDataAndType(Uri.parse(stream.getStreamUrl()), "application/vnd.apple.mpegurl");
        //startActivity(i);
    }

    private void startBroadcastingActivity() {
        configureNewBroadcast();

        Intent broadcastIntent = new Intent(this, CameraActivity.class);
        startActivity(broadcastIntent);

        //Kickflip.startBroadcastActivity(this, mBroadcastListener);
    }

    private void configureNewBroadcast() {
        // Should reset mRecordingOutputPath between recordings
        SessionConfig config = Util.create720pSessionConfig(mRecordingOutputPath);
        //SessionConfig config = Util.create420pSessionConfig(mRecordingOutputPath);
        Kickflip.setSessionConfig(config);
    }

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
