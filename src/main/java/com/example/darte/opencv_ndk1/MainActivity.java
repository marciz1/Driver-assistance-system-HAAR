package com.example.darte.opencv_ndk1;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static String TAG ="MainActivity";

    private boolean sleep;
    private boolean alarmStart;
    private long startTime;
    private long estimatedTime;
    private int counter;
    private MediaPlayer alarmSound;

    private int sensity, sensityMax, alarmLength;

    JavaCameraView javaCameraView;
    Mat mRgba;

    private static CascadeClassifier faceCascade;
    private static CascadeClassifier eyesClosedCascade;
    private static CascadeClassifier eyesOpenedCascade;

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
            super.onManagerConnected(status);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        faceCascade = new CascadeClassifier( Environment.getExternalStorageDirectory().getAbsolutePath() + "/opencv/lbpcascade_frontalface_improved.xml" );
        eyesClosedCascade = new CascadeClassifier(  Environment.getExternalStorageDirectory().getAbsolutePath() + "/opencv/0_995_0_2.xml"  );
        eyesOpenedCascade = new CascadeClassifier(Environment.getExternalStorageDirectory().getAbsolutePath() + "/opencv/haarcascade_eye_tree_eyeglasses.xml");

        counter = 0;
        alarmStart = false;
        alarmSound = MediaPlayer.create(this, R.raw.alarm2);

        loadValuesFromSettings();

        javaCameraView = findViewById(R.id.java_camera_view);
        javaCameraView.setCameraIndex(1);
        javaCameraView.setVisibility(View.VISIBLE);
        javaCameraView.setMaxFrameSize(960, 720);
        javaCameraView.setCvCameraViewListener(this);
    }

    protected void onPause(){
        super.onPause();
        if(javaCameraView!=null)
            javaCameraView.disableView();
    }

    protected void onDestroy(){
        super.onDestroy();
        if(javaCameraView!=null)
            javaCameraView.disableView();
    }

    protected void onResume(){
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.i(TAG, "Opencv succesfully loaded");
            Log.i(TAG,Environment.getExternalStorageDirectory().getAbsolutePath());
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            Log.i(TAG, "Opencv not succesfully loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0,  this, mLoaderCallback);

        }
    }

    public void loadValuesFromSettings(){
        sensity = getIntent().getExtras().getInt("sensity");
        sensityMax = getIntent().getExtras().getInt("sensityMax");
        sensity = (sensityMax - sensity) + 1;
        alarmLength = getIntent().getExtras().getInt("alarmLength") + 1;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();

        sleep = OpencvNativeClass.faceDetection(mRgba.getNativeObjAddr(),
                faceCascade.getNativeObjAddr(),
                eyesOpenedCascade.getNativeObjAddr(),
                eyesClosedCascade.getNativeObjAddr());

        runAlarm(sensity, alarmLength);

        return mRgba;
    }

    public void runAlarm(int sensity, int alarmLength) {

        Log.w(TAG, "counter: " + counter);

        if (sleep) {
            if(counter < sensity) counter++;
        } else {
            if(counter > 0) counter--;
        }

        if (counter == sensity) {
            if(!alarmStart) {
                startTime = System.currentTimeMillis();
                alarmStart = true;
            }
        }

        if(alarmStart){
            estimatedTime = System.currentTimeMillis() - startTime;
            if((estimatedTime/1000 < alarmLength)) alarmSound.start();
            else alarmStart = false;
        }

        Log.w(TAG, "runAlarm: " + alarmStart );
    }
}
