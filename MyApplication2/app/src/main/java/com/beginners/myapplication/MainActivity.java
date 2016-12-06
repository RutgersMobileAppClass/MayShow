package com.beginners.myapplication;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.kickflip.sdk.Kickflip;
import io.kickflip.sdk.api.json.Stream;
import io.kickflip.sdk.av.BroadcastListener;
import io.kickflip.sdk.exception.KickflipException;
import io.kickflip.sdk.fragment.BroadcastFragment;

public class MainActivity extends AppCompatActivity {

    Button[] buttons;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttons = new Button[3];
        buttons[0] = (Button) findViewById(R.id.btn_square);
        buttons[1] = (Button) findViewById(R.id.btn_publish);
        buttons[2] = (Button) findViewById(R.id.btn_setting);

        buttons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
            }
        });

    }
    public void startRecording(){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}
