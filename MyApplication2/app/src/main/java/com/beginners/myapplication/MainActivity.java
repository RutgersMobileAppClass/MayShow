package com.beginners.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.kickflip.sdk.Kickflip;
import io.kickflip.sdk.api.json.Stream;
import io.kickflip.sdk.av.BroadcastListener;
import io.kickflip.sdk.exception.KickflipException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Kickflip.setup(this, CLIENT_ID, CLIENT_SECRET);
        Kickflip.startBroadcastActivity(this, new BroadcastListener() {
            @Override
            public void onBroadcastStart() {

            }

            @Override
            public void onBroadcastLive(Stream stream) {

            }

            @Override
            public void onBroadcastStop() {

            }

            @Override
            public void onBroadcastError(KickflipException error) {

            }
        });
    }
}
