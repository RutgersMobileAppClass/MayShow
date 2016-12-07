package com.beginners.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import io.kickflip.sdk.activity.ImmersiveActivity;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by shiwang on 12/2/16.
 */
public class MediaActivity extends ImmersiveActivity {
    private static final String TAG = "MediaPlayerActivity";
    private VideoView mVideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseImmersiveMode(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        // This must be setup before
        //Uri intentData = getIntent().getData();
        //String mediaUrl = isKickflipUrl(intentData) ? intentData.toString() : getIntent().getStringExtra("mediaUrl");
        String mediaUrl = getIntent().getStringExtra("mediaUrl");
        checkNotNull(mediaUrl, new IllegalStateException("MediaPlayerActivity started without a mediaUrl"));
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        mVideoView =(VideoView) findViewById(R.id.vitamio_videoView);
        mVideoView.setVideoPath(mediaUrl);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });
    }
}
