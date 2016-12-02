package com.beginners.myapplication;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;

import io.kickflip.sdk.FileUtils;
import io.kickflip.sdk.Kickflip;
import io.kickflip.sdk.api.KickflipApiClient;
import io.kickflip.sdk.api.KickflipCallback;
import io.kickflip.sdk.api.json.HlsStream;
import io.kickflip.sdk.api.json.Response;
import io.kickflip.sdk.api.json.Stream;
import io.kickflip.sdk.api.json.User;
import io.kickflip.sdk.api.s3.S3BroadcastManager;
import io.kickflip.sdk.av.BroadcastListener;
import io.kickflip.sdk.av.Broadcaster;
import io.kickflip.sdk.av.HlsFileObserver;
import io.kickflip.sdk.av.SessionConfig;
import io.kickflip.sdk.exception.KickflipException;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by shiwang on 12/1/16.
 */
public class NewBroadcaster extends Broadcaster {
    /**
     * Construct a Broadcaster with Session settings and Kickflip credentials
     *
     * @param context       the host application {@link Context}.
     * @param config        the Session configuration. Specifies bitrates, resolution etc.
     * @param CLIENT_ID     the Client ID available from your Kickflip.io dashboard.
     * @param CLIENT_SECRET the Client Secret available from your Kickflip.io dashboard.
     */

    private static final String TAG = "Broadcaster";
    private static final boolean VERBOSE = false;
    private static final int MIN_BITRATE = 3 * 100 * 1000;              // 300 kbps
    private final String VOD_FILENAME = "vod.m3u8";
    private Context mContext;
    private KickflipApiClient mKickflip;
    private User mUser;
    private HlsStream mStream;
    private HlsFileObserver mFileObserver;
    private S3BroadcastManager mS3Manager;
    private ArrayDeque<Pair<String, File>> mUploadQueue;
    private SessionConfig mConfig;
    private BroadcastListener mBroadcastListener;
    private EventBus mEventBus;
    private boolean mReadyToBroadcast;                                  // Kickflip user registered and endpoint ready
    private boolean mSentBroadcastLiveEvent;
    private int mVideoBitrate;
    private File mManifestSnapshotDir;                                  // Directory where manifest snapshots are stored
    private File mVodManifest;                                          // VOD HLS Manifest containing complete history
    private int mNumSegmentsWritten;
    private int mLastRealizedBandwidthBytesPerSec;                      // Bandwidth snapshot for adapting bitrate
    private boolean mDeleteAfterUploading;                              // Should recording files be deleted as they're uploaded?
    private ObjectMetadata mS3ManifestMeta;



    public NewBroadcaster(Context context, SessionConfig config, String CLIENT_ID, String CLIENT_SECRET) throws IOException {
        super(context, config, CLIENT_ID, CLIENT_SECRET);
        checkArgument(CLIENT_ID != null && CLIENT_SECRET != null);
        init();
        mContext = context;
        mConfig = config;
        mConfig.getMuxer().setEventBus(mEventBus);
        mVideoBitrate = mConfig.getVideoBitrate();
        if (VERBOSE) Log.i(TAG, "Initial video bitrate : " + mVideoBitrate);
        mManifestSnapshotDir = new File(mConfig.getOutputPath().substring(0, mConfig.getOutputPath().lastIndexOf("/") + 1), "m3u8");
        mManifestSnapshotDir.mkdir();
        mVodManifest = new File(mManifestSnapshotDir, VOD_FILENAME);
        writeEventManifestHeader(mConfig.getHlsSegmentDuration());

        String watchDir = config.getOutputDirectory().getAbsolutePath();
        mFileObserver = new HlsFileObserver(watchDir, mEventBus);
        mFileObserver.startWatching();
        if (VERBOSE) Log.i(TAG, "Watching " + watchDir);

        mReadyToBroadcast = false;
        mKickflip = Kickflip.setup(context, CLIENT_ID, CLIENT_SECRET, new KickflipCallback() {
            @Override
            public void onSuccess(Response response) {
                User user = (User) response;
                mUser = user;
                if (VERBOSE) Log.i(TAG, "Got storage credentials " + response);
            }

            @Override
            public void onError(KickflipException error) {
                Log.e(TAG, "Failed to get storage credentials" + error.toString());
                if (mBroadcastListener != null)
                    mBroadcastListener.onBroadcastError(error);
            }
        });
    }
    private void writeEventManifestHeader(int targetDuration) {
        FileUtils.writeStringToFile(
                String.format("#EXTM3U\n" +
                        "#EXT-X-PLAYLIST-TYPE:VOD\n" +
                        "#EXT-X-VERSION:3\n" +
                        "#EXT-X-MEDIA-SEQUENCE:0\n" +
                        "#EXT-X-TARGETDURATION:%d\n", targetDuration + 1),
                mVodManifest, false
        );
    }
    private void init() {
        mDeleteAfterUploading = true;
        mLastRealizedBandwidthBytesPerSec = 0;
        mNumSegmentsWritten = 0;
        mSentBroadcastLiveEvent = false;
        mEventBus = new EventBus("Broadcaster");
        mEventBus.register(this);
    }


    public void setName(){
        mKickflip.createNewUser("user","password","email","shiwang",null, new KickflipCallback() {
            @Override
            public void onSuccess(Response response) {
                User newUser = (User) response;
                Log.i("Hooray", "You've got a new user: " + newUser.getName());
            }

            @Override
            public void onError(KickflipException error) {
                Log.w(TAG, "createNewUser Error: " + error.getMessage());
            }
        });
    }







}
