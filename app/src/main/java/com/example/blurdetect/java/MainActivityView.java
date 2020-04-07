package com.example.blurdetect.java;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Pair;

public interface MainActivityView {
    void showLoading();

    void hideLoading();

    void onClickSelectImage();

    void onActivityResultForPickImageRequest(Intent data);

    double getSharpnessScoreFromOpenCV(Bitmap bitmap);

    void onError();

    void showScoreFromOpenCV(double score);

    void showScoreFromRenderScript(Pair<Boolean, String> status);
}
