package com.example.streamera;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class SendImage extends Activity {

    TextView textTargetUri;
    ImageView targetImage;

    // socket variables
    private boolean lock = true;
    private boolean init_status = false;
    private byte[] arr;
    Socket socket;
    DataOutputStream out;
    String server;
    Integer port;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);
        Button buttonLoadImage = (Button)findViewById(R.id.loadimage);
        textTargetUri = (TextView)findViewById(R.id.targeturi);
        targetImage = (ImageView)findViewById(R.id.targetimage);

        Intent intent = getIntent();
        server = intent.getStringExtra(MainActivity.SERVER);
        port = intent.getIntExtra(MainActivity.PORT, 0);

        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
            OpenCVLoader.initDebug();
        }

        Thread init = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
//                    Log.d("SOCKET", "trying to connect!");
                    socket = new Socket(server, port);
                    out = new DataOutputStream(socket.getOutputStream());
//                    Log.d("SOCKET", "Connected");
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

        buttonLoadImage.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }});
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Uri targetUri = data.getData();
//            Log.d("PATH-TO-IMAGE", ""+targetUri);
//            Log.d("PATH-TO-IMAGE", ""+targetUri.toString());
            textTargetUri.setText(targetUri.toString());

            Bitmap bitmap;
            try {
                while (!lock);
                Mat dst = new Mat();
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                Utils.bitmapToMat(bitmap, dst);

                Imgproc.cvtColor(dst,dst,Imgproc.COLOR_BGRA2RGB);
                MatOfByte byteMat = new MatOfByte();

                arr = null;
                Imgcodecs.imencode(".jpg", dst, byteMat);
                arr = byteMat.toArray();
                targetImage.setImageBitmap(bitmap);
                lock = false;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}