#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/highgui.hpp>

using namespace cv;
using namespace std;

extern "C" {

bool detect(Mat& frame, CascadeClassifier& faceCascade, CascadeClassifier& eyesOpenedCascade, CascadeClassifier& eyesClosedCascade);
void rotate(Mat& src, int angle);

JNIEXPORT jboolean JNICALL
Java_com_example_darte_opencv_1ndk1_OpencvNativeClass_faceDetection(JNIEnv *env, jclass type,
                                                                    jlong matAddrRgba, jlong cascadeFace, jlong cascadeOpenedEyes, jlong cascadeClosedEyes) {
    Mat& frame = *(Mat*)matAddrRgba;
    CascadeClassifier& cascadeFace1 = *(CascadeClassifier*) cascadeFace;
    CascadeClassifier& cascadeOpenedEyes1 = *(CascadeClassifier*) cascadeOpenedEyes;
    CascadeClassifier& cascadeClosedEyes1 = *(CascadeClassifier*) cascadeClosedEyes;

    bool sleep = detect(frame, cascadeFace1, cascadeOpenedEyes1, cascadeClosedEyes1);
    jboolean jsleep = (jboolean) sleep;
    return jsleep;
}

bool detect(Mat& frame, CascadeClassifier& faceCascade, CascadeClassifier& eyesOpenedCascade, CascadeClassifier& eyesClosedCascade ){

    bool sleep = false;
    bool eyesOpenedBln = false;

    std::vector<Rect> faces;
    Mat frameGray;
    Mat eyeROIBin;

    rotate(frame, 90);
    cvtColor( frame, frameGray, CV_BGR2GRAY );
    equalizeHist( frameGray, frameGray );

    //-- Detect faces
    faceCascade.detectMultiScale( frameGray, faces, 1.1, 1, 0|CV_HAAR_SCALE_IMAGE, Size(150, 150) );

    for( size_t i = 0; i < faces.size(); i++ ){
        rectangle(frame, Point(faces[i].x, faces[i].y), Point(faces[i].x + faces[i].width, faces[i].y + faces[i].height), Scalar(0, 0, 255), 1, LINE_AA);

        Mat faceROI = frameGray( faces[i] );
        vector<Rect> eyesOpened;
        vector<Rect> eyesClosed;

        //-- In each face, detect eyes
        eyesOpenedCascade.detectMultiScale( faceROI, eyesOpened, 1.1, 13, 0 |CV_HAAR_SCALE_IMAGE, Size(30, 30));
        eyesClosedCascade.detectMultiScale( faceROI, eyesClosed, 1.1, 13, 0 |CV_HAAR_SCALE_IMAGE, Size(30, 30));

        if(eyesOpened.size() == 2) eyesOpenedBln = true;
        if(eyesClosed.size() == 2 && !eyesOpenedBln) sleep = true;

        for( size_t j = 0; j < eyesOpened.size(); j++ ){
            rectangle(frame, Point(faces[i].x + eyesOpened[j].x, faces[i].y + eyesOpened[i].y),
                      Point(faces[i].x + eyesOpened[j].x + eyesOpened[j].width, faces[i].y + eyesOpened[j].y + eyesOpened[j].height),
                      Scalar(0, 255, 0), 1, LINE_AA);
        }

        for( size_t j = 0; j < eyesClosed.size(); j++ ){
            rectangle(frame, Point(faces[i].x + eyesClosed[j].x, faces[i].y + eyesClosed[i].y),
                      Point(faces[i].x + eyesClosed[j].x + eyesClosed[j].width, faces[i].y + eyesClosed[j].y + eyesClosed[j].height),
                      Scalar(255, 0, 0), 1, LINE_AA);
        }
    }

    rotate(frame, -90);
    return sleep;

}

JNIEXPORT void JNICALL
Java_com_example_darte_opencv_1ndk1_OpencvNativeClass_rotate(JNIEnv *env, jclass type,
                                                             jlong matAddrRgba) {
    Mat src = *(Mat*)matAddrRgba;
    rotate(src, 270 );

}

void rotate(Mat& src, int angle){
    Point2f src_center(src.cols/2.0F, src.rows/2.0F);
    Mat rot_mat = getRotationMatrix2D(src_center, angle, 1.0);
    warpAffine(src, src, rot_mat, src.size());
}


}