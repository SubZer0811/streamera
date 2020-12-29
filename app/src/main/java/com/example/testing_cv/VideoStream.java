package com.example.testing_cv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.DataOutputStream;
import java.net.Socket;

public class VideoStream extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {// OpenCV variables

    private boolean isFrontCam = false;
    private Mat mRgba;
    private CameraBridgeViewBase opencvCamView;
    private BaseLoaderCallback baseLoaderCallback;

    // GUI and other variables
    private static final String TAG = "VideoStream";
    private Button backButton;

    // socket variables
    private boolean lock = true;
    private boolean init_status = false;
    private byte[] arr;

    Socket socket;
    DataOutputStream out;
    String server;
    Integer port;

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
        setContentView(R.layout.activity_video_stream);

        Intent intent = getIntent();
        server = intent.getStringExtra(MainActivity.SERVER);
        port = intent.getIntExtra(MainActivity.PORT, 0);

        Thread init = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    socket = new Socket(server, port);
                    out = new DataOutputStream(socket.getOutputStream());
                }
                catch (Exception e){
                    e.printStackTrace();
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
                while (true) if (!lock) {              // if lock == 0, its turn of consumer
                    out.write(arr);
                    lock = true;
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
            consumer.start();
            init_status = true;
        }
        else{
            if(lock){
                Mat dst = new Mat();
                // Convert image to RGB
                Imgproc.cvtColor(mRgba,dst,Imgproc.COLOR_BGRA2RGB);
                MatOfByte byteMat = new MatOfByte();

                arr = null;
                Imgcodecs.imencode(".jpg", dst, byteMat);
                arr = byteMat.toArray();

                lock = false;

            }
        }
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

    private void swapCamera() {
        opencvCamView.disableView();
        opencvCamView.setCameraIndex(isFrontCam ? 0 : 1);
        opencvCamView.enableView();
    }
}