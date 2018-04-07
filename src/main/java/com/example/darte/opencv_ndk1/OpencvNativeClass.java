package com.example.darte.opencv_ndk1;

/**
 * Created by darte on 04.12.2017.
 */

public class OpencvNativeClass {
    public native static boolean faceDetection(long matAddrRgba, long cascadeface, long cascadeOpenedEyes, long cascadeClosedEyes);
    public native static void rotate(long matAddrRgba);
}
