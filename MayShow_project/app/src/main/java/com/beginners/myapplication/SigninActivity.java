package com.beginners.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import io.kickflip.sdk.Kickflip;
import io.kickflip.sdk.api.KickflipApiClient;
import io.kickflip.sdk.api.KickflipCallback;
import io.kickflip.sdk.api.json.Response;
import io.kickflip.sdk.exception.KickflipException;

import static android.content.ContentValues.TAG;

public class SigninActivity extends Activity {

    private EditText mUsername;
    private EditText mPassword;
    private String username;
    private String password;

    private boolean cancel = false;
    public static KickflipApiClient mKickflip;
    public static boolean mKickflipReady = false;

    private DatabaseReference upload;
    private DatabaseReference download;


    //public static HashMap<String,String> match_name;
    public static HashMap<String,String> pair_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);


        //match_name = new HashMap<>();
        pair_name = new HashMap<>();

        mUsername = (EditText) this.findViewById(R.id.username);
        mPassword = (EditText) this.findViewById(R.id.password);


        //firebase upload user info
        upload = FirebaseDatabase.getInstance().getReference("UserName");
        //firebase download all users info
        download =FirebaseDatabase.getInstance().getReference("UserName");


        // Reset errors.
        mUsername.setError(null);
        mPassword.setError(null);

        // Store values at the time of the login attempt.

        mKickflip= Kickflip.setup(this, SECRETS.CLIENT_KEY, SECRETS.CLIENT_SECRET, new KickflipCallback() {
            @Override
            public void onSuccess(Response response) {
                mKickflipReady = true;
                //Toast.makeText(getApplicationContext(),mKickflip.getActiveUser().getName(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(KickflipException error) {}
        });

        findViewById(R.id.btn_signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = mUsername.getText().toString();
                password = String.valueOf(mPassword.getText());
                System.out.println("==>" + username + ":" + password);
                attemptLogin(username,password);
            }
        });

        findViewById(R.id.btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = mUsername.getText().toString();
                password = String.valueOf(mPassword.getText());
                System.out.println("==>" + username + ":" + password);
                //user1 来自用户注册时输入的用户名。
                upload.child(username).child("username").setValue(username);
                //upload.child(username).child("backgroundname").setValue(mKickflip.getActiveUser().getName());
                upload.child(username).child("password").setValue(password);
                RegisterActivity(username,password);
            }
        });


        download.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot person : dataSnapshot.getChildren()){
                    //for(DataSnapshot info : person.getChildren()){
                        String username_ = (String) person.child("username").getValue();
                        System.out.println(username_+"----");
                        //String backgroundname = (String) info.child("backgroundname").getValue();
                        String password_ = (String) person.child("password").getValue();
                        System.out.println(password_ + "====");
                        pair_name.put(username_,password_);
                        //Toast.makeText(SigninActivity.this, "pair_name.size(): "+pair_name.size(), Toast.LENGTH_SHORT).show();
                        //match_name.put(backgroundname,username);
                    //}
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void RegisterActivity(String username, String password){
        cancel=false;
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
//        if (!TextUtils.isEmpty(password)) {
//            mPassword.setError("Password required");
//            cancel = true;
//        }
//        if (password.length() < 4) {
//            mPassword.setError("Password is too short");
//            cancel = true;
//        }
        if (!cancel){
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mKickflip.createNewUser(username, password, null,username,null,new KickflipCallback() {
                @Override
                public void onSuccess(Response response) {}
                @Override
                public void onError(KickflipException error) {}
            });

//            mKickflip.loginUser(username, password, new KickflipCallback() {
//                @Override
//                public void onSuccess(Response response) {}
//                @Override
//                public void onError(KickflipException error) {
//                    Log.w(TAG, "loginUser Error: ziji login " + error.getMessage());
//                }
//            });

            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void attemptLogin(String username, String password) {
        // Check for a valid email address.

        cancel = false;
        //Toast.makeText(this, "pair_name.get(user2)= "+pair_name.get("user2"), Toast.LENGTH_SHORT).show();
        if (TextUtils.isEmpty(username)) {
            mUsername.setError("Username required!");
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsername.setError("Username not exists");
            cancel = true;
        }

        // Check for a valid password.
//        if (!TextUtils.isEmpty(password)) {
//            mPassword.setError("Password required");
//            cancel = true;
//        }
//        if (!(pair_name.get(username).toString() == (String) password)) {
//            Toast.makeText(this, "pair_name.get(user2)= "+pair_name.get("user2")+"  password  "+password, Toast.LENGTH_SHORT).show();
//            mPassword.setError("Password is not match");
//            cancel = true;
//        }
        if(!cancel){
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mKickflip.loginUser(username, password, new KickflipCallback() {
                @Override
                public void onSuccess(Response response) {
                    Log.i("Hooray", "You logged in as aaaaaaa: " );
                }
                @Override
                public void onError(KickflipException error) {
                    Log.w(TAG, "loginUser Error: ziji login " + error.getMessage());
//                    cancel = true;
                }
            });

            Log.i("Junjiefeng","we start the main intent");
            startActivity(new Intent(this, MainActivity.class));
            //startActivity(new Intent(this,MainActivity.class));
        }

    }


    private boolean isUsernameValid(String username) {
        return pair_name.containsKey(username);
    }

    private boolean isPasswordValid(String username, String password) {
        //Toast.makeText(this, "password "+password, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "username "+username, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "pair_name.get(user2)= "+pair_name.get("user2"), Toast.LENGTH_SHORT).show();
        return pair_name.get(username) == password;
    }
}
