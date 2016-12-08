package com.beginners.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import io.kickflip.sdk.api.KickflipCallback;
import io.kickflip.sdk.api.json.Response;
import io.kickflip.sdk.exception.KickflipException;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    // UI references.
    private EditText mUsername;
    private EditText mPassword;
    private String username;
    private String password;

    private View mProgressView;
    private View mLoginFormView;
    private boolean cancel = false;
    private View focusView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);

        // Reset errors.
        mUsername.setError(null);
        mPassword.setError(null);

        // Store values at the time of the login attempt.
        username = mUsername.getText().toString();
        password = mPassword.getText().toString();


        findViewById(R.id.btn_signin).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        findViewById(R.id.btn_signup).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterActivity();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    public void RegisterActivity(){
        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsername.setError("Username required!");
            focusView = mUsername;
            cancel = true;
        }
        if (username.length() > 20) {
            mUsername.setError("Username is too long");
            focusView = mUsername;
            cancel = true;
        }

        // Check for a valid password.
        if (!TextUtils.isEmpty(password)) {
            mPassword.setError("Password required");
            focusView = mPassword;
            cancel = true;
        }
        if (password.length() < 4) {
            mPassword.setError("Password is too short");
            focusView = mPassword;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            MainActivity.mKickflip.createNewUser(username, password, null,null,null,new KickflipCallback() {
                @Override
                public void onSuccess(Response response) {}
                @Override
                public void onError(KickflipException error) {}
            });

            startActivity(new Intent(this, MainActivity.class));

            //showProgress(true);
        }
    }

    private void attemptLogin() {
        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsername.setError("Username required!");
            focusView = mUsername;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsername.setError("Username not exists");
            focusView = mUsername;
            cancel = true;
        }

        // Check for a valid password.
        if (!TextUtils.isEmpty(password)) {
            mPassword.setError("Password required");
            focusView = mPassword;
            cancel = true;
        }
        if (!isPasswordValid(username, password)) {
            mPassword.setError("Password is not match");
            focusView = mPassword;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            MainActivity.mKickflip.loginUser(username, password, new KickflipCallback() {
                @Override
                public void onSuccess(Response response) {}
                @Override
                public void onError(KickflipException error) {}
            });

            startActivity(new Intent(this, MainActivity.class));

            //showProgress(true);
        }
    }


    private boolean isUsernameValid(String username) {
        return MainActivity.match_name.containsKey(username);
    }

    private boolean isPasswordValid(String username, String password) {
        return MainActivity.match_name.get(username) == password;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

