package com.beginners.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import io.kickflip.sdk.api.KickflipCallback;
import io.kickflip.sdk.api.json.Response;
import io.kickflip.sdk.exception.KickflipException;

public class SigninActivity extends AppCompatActivity {

    private EditText mUsername;
    private EditText mPassword;
    private String username;
    private String password;

    private boolean cancel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);

        // Reset errors.
        mUsername.setError(null);
        mPassword.setError(null);

        // Store values at the time of the login attempt.
        username = mUsername.getText().toString();
        password = mPassword.getText().toString();


        findViewById(R.id.btn_signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        findViewById(R.id.btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterActivity();
            }
        });
    }

    public void RegisterActivity(){
        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsername.setError("Username required!");
            cancel = true;
        }
        if (username.length() > 20) {
            mUsername.setError("Username is too long");
            cancel = true;
        }

        // Check for a valid password.
        if (!TextUtils.isEmpty(password)) {
            mPassword.setError("Password required");
            cancel = true;
        }
        if (password.length() < 4) {
            mPassword.setError("Password is too short");
            cancel = true;
        }
        if (!cancel){
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            MainActivity.mKickflip.createNewUser(username, password, null,null,null,new KickflipCallback() {
                @Override
                public void onSuccess(Response response) {}
                @Override
                public void onError(KickflipException error) {}
            });

            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void attemptLogin() {
        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsername.setError("Username required!");
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsername.setError("Username not exists");
            cancel = true;
        }

        // Check for a valid password.
        if (!TextUtils.isEmpty(password)) {
            mPassword.setError("Password required");
            cancel = true;
        }
        if (!isPasswordValid(username, password)) {
            mPassword.setError("Password is not match");
            cancel = true;
        }
        if (!cancel){
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            MainActivity.mKickflip.loginUser(username, password, new KickflipCallback() {
                @Override
                public void onSuccess(Response response) {}
                @Override
                public void onError(KickflipException error) {}
            });

            startActivity(new Intent(this, MainActivity.class));
        }
    }


    private boolean isUsernameValid(String username) {
        return MainActivity.match_name.containsKey(username);
    }

    private boolean isPasswordValid(String username, String password) {
        return MainActivity.match_name.get(username) == password;
    }
}
