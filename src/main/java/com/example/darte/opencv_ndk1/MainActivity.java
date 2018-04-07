package com.example.darte.opencv_ndk1;

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
    JavaCameraView javaCameraView;
    Mat mRgba, mRgbaT;
    static CascadeClassifier faceCascade;
    static CascadeClassifier eyesClosedCascade;
    static CascadeClassifier eyesOpenedCascade;

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

        faceCascade = new CascadeClassifier( Environment.getExternalStorageDirectory().getAbsolutePath() + "/opencv/lbpcascade_frontalface_improved.xml" );
        eyesClosedCascade = new CascadeClassifier(  Environment.getExternalStorageDirectory().getAbsolutePath() + "/opencv/0_995_0_2.xml"  );
        eyesOpenedCascade = new CascadeClassifier(Environment.getExternalStorageDirectory().getAbsolutePath() + "/opencv/haarcascade_eye_tree_eyeglasses.xml");

        setContentView(R.layout.activity_main);
        final MediaPlayer alarmSound = MediaPlayer.create(this, R.raw.alarm2);

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
        OpencvNativeClass.faceDetection(mRgba.getNativeObjAddr(), faceCascade.getNativeObjAddr(), eyesOpenedCascade.getNativeObjAddr(), eyesClosedCascade.getNativeObjAddr());
        return mRgba;
    }
}
