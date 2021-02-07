package com.example.streamera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.streamera.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.DataOutputStream;
import java.net.Socket;

public class CaptureImage extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private boolean isFrontCam = false;
    private Mat mRgba;
    private CameraBridgeViewBase opencvCamView;
    private BaseLoaderCallback baseLoaderCallback;

    // GUI and other variables
    private static final String TAG = "SendImage";
    private Button captureButton;

    // socket variables
    private boolean lock = true;
    private boolean init_status = false;
    private byte[] arr;

    Socket socket;
    DataOutputStream out;
    String server;
    Integer port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_send_image);

        Intent intent = getIntent();
        server = intent.getStringExtra(MainActivity.SERVER);
        port = intent.getIntExtra(MainActivity.PORT, 0);

        Thread init = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Log.d(TAG+"SOCKET", "trying to connect!");
                    socket = new Socket(server, port);
                    out = new DataOutputStream(socket.getOutputStream());
                    Log.d(TAG+"SOCKET", "Connected");
                    init_status = true;
                }
                catch (Exception e){
                    e.printStackTrace();
                    init_status = false;
                }
            }
        });

        init.start();
        try {
            init.join();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if(!init_status){
            Toast.makeText(this, "Could not connect to streamera server", Toast.LENGTH_SHORT).show();
            finish();
        }

        consumer.start();

        captureButton = (Button) findViewById(R.id.Capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lock){
                    lock = false;
                }
            }
        });

        opencvCamView = findViewById(R.id.camView);
        opencvCamView.setMaxFrameSize(640, 480);
        opencvCamView.setVisibility(SurfaceView.VISIBLE);
        opencvCamView.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status){
                    case BaseLoaderCallback.SUCCESS:
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
            try {

                while (true) { // if lock == 0, its turn of consumer
                    if(!lock) {
                        Log.d("SOCKET", "Attempting to send");
                        Mat dst = new Mat();
                        // Convert image to RGB
                        Imgproc.cvtColor(mRgba, dst, Imgproc.COLOR_BGRA2RGB);
                        MatOfByte byteMat = new MatOfByte();

                        arr = null;
                        Imgcodecs.imencode(".jpg", dst, byteMat);
                        arr = byteMat.toArray();
                        out.write(arr);
                        Log.d("SOCKET", "SENT");
                        lock = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        return mRgba;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(opencvCamView !=null){
            opencvCamView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){ }
        else{
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(opencvCamView !=null){
            try {
                socket.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            opencvCamView.disableView();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            try {
                socket.close();
                if(socket.isClosed()){
                    Log.d(TAG+"SOCKET", "Socket is closed");
                }
                else{
                    Log.d(TAG+"SOCKET", "Socket is NOT closed");
                }
            }
            catch (Exception e){
                Log.d(TAG+"SOCKET", "Could not close socket");
                e.printStackTrace();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void swapCamera() {
        opencvCamView.disableView();
        opencvCamView.setCameraIndex(isFrontCam ? 0 : 1);
        opencvCamView.enableView();
    }
}