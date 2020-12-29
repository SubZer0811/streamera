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

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // OpenCV variables
    private boolean isFrontCam = false;
    private Mat mRgba;
    private CameraBridgeViewBase opencvCamView;
    private BaseLoaderCallback baseLoaderCallback;
    private Size size = new Size(128, 72);

    // GUI and other variables
    private static final String TAG = "MainActivity";
    private Handler mainHandler;
    private TextView logTv;
    private Button connect;
    private TextView portButton, serverButton;

    // socket variables
    private boolean lock = true;
    private boolean init_status = false;
    private byte[] arr;
    // Initializations
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // USE THIS TO FIND VARIOUS FRAME SIZES

//        Camera camera = Camera.open();
//        List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
//
//        //searches for good picture quality
//        for(Camera.Size dimens : sizes){
//            Log.d("SIZEOFFRAME", "Width: " + dimens.width + " Height: " + dimens.height);
//        }

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mainHandler = new Handler();

        connect = findViewById(R.id.connect);
        portButton = findViewById(R.id.port);
        serverButton = findViewById(R.id.server);

        opencvCamView = findViewById(R.id.camView);
        opencvCamView.setMaxFrameSize(640, 480);
        opencvCamView.setVisibility(SurfaceView.VISIBLE);
        opencvCamView.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        Log.wtf(TAG,"OpenCV loaded successfully");
                        opencvCamView.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    Thread consumer = new Thread(new Runnable() {

        @Override
        public void run() {
            String TAG = "SOCKET";
            Log.d(TAG, "Consumer Started");
            try {
                /*
                Integer PORT = Integer.parseInt(portButton.getText().toString());
                String SERVER = serverButton.getText().toString();
                Log.d("SOCKET", "SERVER: "+ SERVER + "   PORT: " + PORT);
                Socket socket = new Socket(SERVER, PORT);
                 */
                Socket socket = new Socket("192.168.100.6", 50001);
                Log.d(TAG, "Connected to Server");
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                while (true) if (!lock) {              // if lock == 0, its turn of consumer
//                    out.write((""+arr.length+"#").getBytes());
                    Log.d("TIMING", "CONS_START");
                    Log.d(TAG, "Consumed: ");
                    out.write(arr);
                    lock = true;
                    Log.d("TIMING", "CONS_END");
//                    break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    // Camera and image processing >>>>>

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if(!init_status){
            connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    consumer.start();
                    init_status = true;
                }
            });
        }
        else{
            if(lock){
                Log.d("TIMING", "PROD_START");
                Mat dst = new Mat();
                // Convert image to RGB
                Imgproc.cvtColor(mRgba,dst,Imgproc.COLOR_BGRA2BGR);
                MatOfByte byteMat = new MatOfByte();

                arr = null;
                Imgcodecs.imencode(".jpg", dst, byteMat);
                arr = byteMat.toArray();
                Log.d("SIZE__", "LENGTH: "+arr.length);
//                Log.d("SIZE__", ""+arr[0]+" "+arr[1]+" "+arr[2]+" "+arr[3]+" "+arr[4]+" ");


                // flatten Mat object and save it to arr (byte[])
//                dst.get(0, 0, arr);

                lock = false;
                Log.d("TIMING", "PROD_END");

            }
        }
        return mRgba;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("INITINIT", ""+width+", "+height);
        mRgba = new Mat(height,width, CvType.CV_8UC4);
        arr = new byte[height*width*3];
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        Log.wtf(TAG,"CameraView Stopped");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(opencvCamView !=null){
            opencvCamView.disableView();
            Log.wtf(TAG,"Camera Paused");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.wtf(TAG,"OpenCVLoader.initdebug() returned false");
            Toast.makeText(getApplicationContext(),"Prob 00",Toast.LENGTH_SHORT).show();
        }
        else{
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
            Log.wtf(TAG,"Camera Resumed");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(opencvCamView !=null){
            opencvCamView.disableView();
            Log.w(TAG,"Camera Turned OFF");
        }
    }

    private void swapCamera() {
        opencvCamView.disableView();
        opencvCamView.setCameraIndex(isFrontCam ? 0 : 1);
        opencvCamView.enableView();
    }


    private void Log(String text) {
        final String ftext = text+"\n";
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                logTv.append(ftext);
            }
        });
    }
}
