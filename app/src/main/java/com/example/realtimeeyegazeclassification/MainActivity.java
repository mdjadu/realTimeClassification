package com.example.realtimeeyegazeclassification;

import static android.content.ContentValues.TAG;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
//import android.graphics.Camera;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class MainActivity extends AppCompatActivity {

    Camera PhoneCamera;
    CameraPreview CameraPreview;
    FrameLayout CameraView;
    LinearLayout Left,Right;
    Button Start, Stop;
    RelativeLayout Classification;
    WebSocket webSocket;

    int count = 0, flag = 0;
    final boolean[] toExit = {false};
    private boolean safeToTakePicture = false;


    SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
    Calendar c = Calendar.getInstance();
    private final String date1 = sdf.format(c.getTime());
    private final String gdaxUrl = "ws://10.129.158.228:6677/ws/classification/" + date1;
    private OkHttpClient mClient1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkCameraHardware(MainActivity.this)){
            Toast.makeText(this,"has camera",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this,"No camera",Toast.LENGTH_LONG).show();
        }

        if (!(getApplicationContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)){
            requestPermissions(new String[]{Manifest.permission.CAMERA},7);
        }
        if (!(getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},7);
        }

        CameraView = (FrameLayout) findViewById(R.id.cameraLayout);
        Start = (Button) findViewById(R.id.start);
        Stop = (Button) findViewById(R.id.stop);
        Left = (LinearLayout) findViewById(R.id.left);
        Right = (LinearLayout) findViewById(R.id.right);
        Classification = (RelativeLayout) findViewById(R.id.classification);

        setCameraViewSize();

        PhoneCamera = getCameraInstance();
        PhoneCamera.setDisplayOrientation(90);

        CameraPreview = new CameraPreview(this,PhoneCamera);
        CameraView.addView(CameraPreview);
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams();
//        CameraView.setLayoutParams(FrameLayout.LayoutParams.C);
        CameraView.setLayoutParams(CameraView.getLayoutParams());

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);



//        final boolean[] toExit = {false};
        final Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                while(!toExit[0]){
                    // Your code
                    PhoneCamera.startPreview();
                    safeToTakePicture = true;
                    if(safeToTakePicture) {
                        PhoneCamera.takePicture(null, null, mPicture);
                        safeToTakePicture = false;
                        PhoneCamera.stopPreview();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });



        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                setCameraViewSize();
                PhoneCamera = getCameraInstance();
                PhoneCamera.setDisplayOrientation(0);
                Start.setVisibility(View.GONE);
                setClassificationView();

//                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams((int) LinearLayout.LayoutParams.WRAP_CONTENT,
//                                                                            (int) LinearLayout.LayoutParams.WRAP_CONTENT);
//                p.addRule(RelativeLayout.ALIGN_BOTTOM,R.id.cameraLayout);
//                Linearlayout.setLayoutParams(p);//.setLayoutDirection(p);


                webSocket = start();
                toExit[0] = false;
                if(flag == 0) {t.start(); flag = flag+1;}
                else {t.run(); flag = flag+1;}
//                t.start();
//        Log.d("webSocket","ek bar dikh jaddddddd");

//        webSocket.send("hello");

            }
        });

        Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toExit[0] = true;
                t.interrupt();
                //                t.stop();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                setCameraViewSize();
                PhoneCamera.setDisplayOrientation(90);
                Start.setVisibility(View.VISIBLE);
                Classification.setVisibility(View.GONE);
                PhoneCamera.stopPreview();
                PhoneCamera.release();


            }
        });



//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        


    }

    private void setClassificationView() {
        Classification.setVisibility(View.VISIBLE);

        int width_of_screen = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height_of_screen = Resources.getSystem().getDisplayMetrics().heightPixels;

        int w = (int) ((width_of_screen/15)*getResources().getDisplayMetrics().density);
//        int hi = (int) ((height_of_screen/15)*getResources().getDisplayMetrics().density);
//        int h =  (w*5)/3;

        int wid = Stop.getWidth();
        int dis = 0;
        if(w>wid) dis = w;
        else dis = wid;

        int fac = (int) (10*getResources().getDisplayMetrics().density);
        RelativeLayout.LayoutParams classification = new RelativeLayout.LayoutParams(height_of_screen,width_of_screen);
        Classification.setLayoutParams(classification);

        classification = new RelativeLayout.LayoutParams((height_of_screen-dis-fac-fac)/2,width_of_screen);
        Left.setLayoutParams(classification);



        classification = new RelativeLayout.LayoutParams((height_of_screen-dis-fac-fac)/2,width_of_screen);
        classification.addRule(RelativeLayout.RIGHT_OF,R.id.left);
//        if wid
        classification.setMargins(dis+fac+fac+fac,0,0,0);
        Right.setLayoutParams(classification);

//        LinearLayout.LayoutParams c = new LinearLayout.LayoutParams((int) LinearLayout.LayoutParams.WRAP_CONTENT,
//                                                        (int) LinearLayout.LayoutParams.WRAP_CONTENT);
//        classification.
//        c.setMargins(0,0,0,0);
//        Stop.setLayoutParams(c);

    }

    private void setCameraViewSize() {
        int width_of_screen = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height_of_screen = Resources.getSystem().getDisplayMetrics().heightPixels;

        Log.d("PrintMessage",String.valueOf(width_of_screen) + " X " + String.valueOf(height_of_screen));
        int w = (int) ((width_of_screen/15)*getResources().getDisplayMetrics().density);
        int hi = (int) ((height_of_screen/15)*getResources().getDisplayMetrics().density);
        int h =  (w*5)/3;
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w,h);
        CameraView.setLayoutParams(p);




//        CameraView.setLayoutParams(new RelativeLayout.LayoutParams());
//        SurfaceView.getHolder().setFixedSize(w,h);
//        CameraLayout.setMinimumWidth(w);
//        CameraLayout.setMaxHeight(h);
//        CameraLayout.setMinimumHeight(h);

        Log.d("PrintMessage",String.valueOf(hi) + " --- " + String.valueOf(h));
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a Camera instance
            Camera.Parameters params = c.getParameters();
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
//            c.setDisplayOrientation(90);

            for (Camera.Size size : sizes) {
                Log.i(TAG, "Available resolution: "+size.width+" "+size.height);
//                mSize = size;
            }


            Log.i(TAG, "Chosen resolution: "+ sizes.get(0).width+" "+ sizes.get(0).height);
            params.setPictureSize(sizes.get(0).width, sizes.get(0).height);
//            params.setPictureSize(sizes.get(0).height, sizes.get(0).width);
            c.setParameters(params);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        int num = 10;
        int rotation;
        @SuppressLint("WrongConstant")
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
            Calendar c = Calendar.getInstance();
            String date = sdf.format(c.getTime());

            String encodedImage = Base64.encodeToString(data, Base64.DEFAULT);
            count = count + 1;
            Log.d(TAG, "count = " + count);
//            Toast.makeText(MainActivity.this, String.valueOf(count), 1000).show();
            OrientationEventListener orientationEventListener = new OrientationEventListener(MainActivity.this) {
                @Override
                public void onOrientationChanged(int orientation) {
                    if (orientation >= 45 && orientation < 135) {
                        rotation = 180;
                    } else if (orientation >= 135 && orientation < 225) {
                        rotation = 270;
                    } else if (orientation >= 225 && orientation < 315) {
                        rotation = 0;
                    } else {
                        rotation = 90;
                    }
                    //                    angle.setText(String.valueOf(orientation));
                }
            };
            orientationEventListener.enable();
//            rotation = 90;
            webSocket.send(("{\n" +
                    "\"file\": \"" + encodedImage + "\"," +
                    "\"phone_id\": \"" + date + "\"," +
                    "\"angle\": \"" + rotation + "\"" +
                    "}"));
            //            uploadImage(f.getPath(), rotation);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                }
            }, 3000);
            safeToTakePicture = true;
//
//                num = num - 1;
//            }
        }

    };

    private WebSocket start() {

        mClient1 = new OkHttpClient();
        Request request = new Request.Builder().url(gdaxUrl).build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket webSocket = mClient1.newWebSocket(request, listener);

//        Log.d("webSocket",webSocket.);


        return webSocket;
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


}