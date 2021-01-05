package com.example.streamera;

import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowId;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

//import com.example.testing_cv.R;
//import com.felhr.usbserial.UsbSerialDevice;
//import com.felhr.usbserial.UsbSerialInterface;

import com.example.streamera.R;

import java.io.DataOutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    public static final String SERVER = "com.example.testing_cv.SERVER";
    public static final String PORT = "com.example.testing_cv.PORT";
    public static Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TEST", "IMHERE");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button vid_stream_button = (Button) findViewById(R.id.VideoStream);
        vid_stream_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoActivity();
            }
        });

        Button img_send_button = (Button) findViewById(R.id.ImageSend);
        img_send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openImageActivity();
            }
        });
    }

    public void openVideoActivity(){
        EditText server_ = (EditText) findViewById(R.id.IPAddr);
        String server = server_.getText().toString();

        EditText port_ = (EditText) findViewById(R.id.Port);
        Integer port = Integer.parseInt(port_.getText().toString());


        Intent intent  = new Intent(this, VideoStream.class);
        intent.putExtra(SERVER, server);
        intent.putExtra(PORT, port);
        startActivity(intent);
    }

    public void openImageActivity(){
        EditText server_ = (EditText) findViewById(R.id.IPAddr);
        String server = server_.getText().toString();

        EditText port_ = (EditText) findViewById(R.id.Port);
        Integer port = Integer.parseInt(port_.getText().toString());
        
        Intent intent  = new Intent(this, SendImage.class);
        intent.putExtra(SERVER, server);
        intent.putExtra(PORT, port);
        startActivity(intent);
    }
}