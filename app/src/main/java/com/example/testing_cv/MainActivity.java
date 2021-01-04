package com.example.testing_cv;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//import com.example.testing_cv.R;
//import com.felhr.usbserial.UsbSerialDevice;
//import com.felhr.usbserial.UsbSerialInterface;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

import com.example.testing_cv.R;

public class MainActivity extends AppCompatActivity{

    public static final String SERVER = "com.example.testing_cv.SERVER";
    public static final String PORT = "com.example.testing_cv.PORT";

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