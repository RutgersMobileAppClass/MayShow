package com.beginners.myapplication;

import android.content.Context;

import io.kickflip.sdk.api.KickflipApiClient;

/**
 * Created by Avril on 12/7/16.
 */

public class MyClient extends KickflipApiClient {
    public MyClient(Context appContext, String key, String secret) {
        super(appContext, key, secret);
    }
}
