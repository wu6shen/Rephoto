#include <jni.h>
#include <android/log.h>
#include <string>
#include <opencv2/opencv.hpp>
#include "track.h"
#include "sift.h"
#include <GLES2/gl2.h>


reloc::Track my_track;


extern "C"
JNIEXPORT void JNICALL
Java_com_example_wu6shen_rephoto_MainActivity_clearTracker(JNIEnv *env, jobject instance) {

    // TODO
    my_track.clear();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_wu6shen_rephoto_MainActivity_alphaBlend(JNIEnv *env, jobject instance, jlong src1,
                                                         jlong src2, jlong result) {

    // TODO
    cv::Mat *ptr1 = (cv::Mat *) src1;
    cv::Mat *ptr2 = (cv::Mat *) src2;
    cv::Mat *ptr3 = (cv::Mat *) result;
    (*ptr3) = 0.7 * (*ptr2) + 0.3 * (*ptr1);

    cv::resize((*ptr3), (*ptr3), cv::Size(1080, 1920));

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_wu6shen_rephoto_MainActivity_setNew(JNIEnv *env, jobject instance) {

    // TODO
    my_track.method_ = 0;

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_wu6shen_rephoto_MainActivity_setOld(JNIEnv *env, jobject instance) {

    // TODO

    my_track.method_ = 1;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_example_wu6shen_rephoto_MainActivity_MatchPhotoRANSAC(JNIEnv *env, jobject instance,
                                                               jlong src1, jlong src2,
                                                               jlong result) {

    // TODO
    cv::Mat *ptr_mat = (cv::Mat *) src2;
    cv::Mat *ptr_result = (cv::Mat *) result;
    uchar* data = (*ptr_mat).ptr<uchar>(0);
    (*ptr_result) = my_track.tracking(*ptr_mat);
    return my_track.getScore();

}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_example_wu6shen_rephoto_MainActivity_MatchPhotoRANSACD(JNIEnv *env, jobject instance,
                                                                jlong src1, jlong src2, jlong src3,
                                                                jlong result) {

    // TODO

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_wu6shen_rephoto_MainActivity_testTime(JNIEnv *env, jobject instance) {

    // TODO

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_wu6shen_rephoto_MainActivity_MatchPhotoLMEDS(JNIEnv *env, jobject instance,
                                                              jlong src1, jlong src2,
                                                              jlong result) {

    // TODO

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_wu6shen_rephoto_MainActivity_matMul(JNIEnv *env, jobject instance, jlong mat1,
                                                     jlong mat2, jlong result) {

    // TODO

    cv::Mat *ptrMat1 = (cv::Mat*) mat1;
    cv::Mat *ptrMat2 = (cv::Mat*) mat2;
    cv::Mat *ptrResult = (cv::Mat*) result;

    (*ptrResult) = (*ptrMat1) * (*ptrMat2);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_wu6shen_rephoto_MainActivity_initTrack(JNIEnv *env, jobject instance, jlong src) {

    // TODO
    double start = clock(), end;
    GLuint test;
    glGenTextures(1, &test);

    cv::Mat *ptrMat = (cv::Mat*) src;

    my_track = reloc::Track(*ptrMat, 0, 0);
    cv::Ptr<cv::Feature2D> ptr = cv::SIFT::create(500, 5);
    std::vector<cv::KeyPoint> kp;
    cv::Mat des, mask;
    ptr->detectAndCompute(*ptrMat, mask, kp, des);
    end = clock();
    __android_log_print(ANDROID_LOG_INFO, "info", "sift time descriptor:%lf", (end - start) / CLOCKS_PER_SEC);

}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_wu6shen_rephoto_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    cv::Mat asd;
    return env->NewStringUTF(hello.c_str());
}
