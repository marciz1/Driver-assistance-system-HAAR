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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static String TAG ="MainActivity";

    private boolean alarmStart;
    private long startTime;
    private int counter;
    private MediaPlayer alarmSound;
    // values from seekBar
    private int sensibility, alarmLength;
    private float threshold;
    private String chosenAlarm;



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

        faceCascade = new CascadeClassifier( Environment.getExternalStorageDirectory().getAbsolutePath() + "/Cascades/lbpcascade_frontalface_improved.xml" );
        eyesClosedCascade = new CascadeClassifier(  Environment.getExternalStorageDirectory().getAbsolutePath() + "/Cascades/haar_closed_eye_improved.xml"  );
        eyesOpenedCascade = new CascadeClassifier(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Cascades/haarcascade_eye_tree_eyeglasses.xml");

        counter = 0;
        alarmStart = false;

        loadValuesFromSettings();
        initializeAlarm();

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

    public void loadValuesFromSettings() {
        sensibility = getIntent().getExtras().getInt("sensibility");
        int sensibilityMax = getIntent().getExtras().getInt("sensibilityMax");
        sensibility = (sensibilityMax - sensibility) + 1;

        alarmLength = getIntent().getExtras().getInt("alarmLength") + 1;
    }

    private void initializeAlarm() {
        chosenAlarm = getIntent().getExtras().getString("chosenAlarm");
        switch (chosenAlarm) {
            case "Soft alarm":
                alarmSound = MediaPlayer.create(this, R.raw.soft_alarm);
                break;
            case "Fire alarm":
                alarmSound = MediaPlayer.create(this, R.raw.fire_alarm);
                break;
            case "Clock buzzer":
                alarmSound = MediaPlayer.create(this, R.raw.clock_buzzer);
                break;
            case "School bell":
                alarmSound = MediaPlayer.create(this, R.raw.school_bell);
                break;
            case "Tornado siren":
                alarmSound = MediaPlayer.create(this, R.raw.tornado_siren);
                break;
            case "Woop woop":
                alarmSound = MediaPlayer.create(this, R.raw.woop_woop);
                break;
            case "Siren":
                alarmSound = MediaPlayer.create(this, R.raw.siren);
                break;
        }
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

        boolean sleep = OpencvNativeClass.faceDetection(mRgba.getNativeObjAddr(),
                faceCascade.getNativeObjAddr(),
                eyesOpenedCascade.getNativeObjAddr(),
                eyesClosedCascade.getNativeObjAddr());
        Core.flip(mRgba, mRgba, 1);
        runAlarm(sleep, sensibility, alarmLength);

        return mRgba;
    }

    public void runAlarm(boolean sleep, int sensibility, int alarmLength) {
        Log.w("alarmStart", "SensibilityMax " + sensibility);
        Log.w("alarmStart", "Sensibility " + counter);

        if (sleep) {
            if (counter < sensibility) counter++;
        } else {
            if (counter > 0) counter--;
        }

        if (counter == sensibility) {
            if (!alarmStart) {
                startTime = System.currentTimeMillis();
                alarmSound.start();
                alarmStart = true;
            }
        }

        if (alarmStart) {
            float estimatedTime = System.currentTimeMillis() - startTime;
            if ((estimatedTime / 1000 < alarmLength)) {
                alarmSound.start();
            } else {
                alarmStart = false;
                stopAlarm();
            }
        }
    }

    private void stopAlarm() {
        alarmSound.pause();
        alarmSound.seekTo(0);
    }
}
