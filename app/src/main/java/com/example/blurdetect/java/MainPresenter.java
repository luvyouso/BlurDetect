package com.example.blurdetect.java;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainPresenter {
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    MainActivityView mView;

    public MainPresenter(MainActivityView view) {
        mView = view;
    }

    public void getDataFromImageBitmap(Bitmap galleryImageBitmap) {
        Subscription subscription =
                Observable.just(galleryImageBitmap)
                        .map(bitmap -> {
                            return resizeBitmap(bitmap, 500, 5000);
                        })
                        .map(bitmap -> {
                            return mView.getSharpnessScoreFromOpenCV(bitmap);
                        })
                        .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                        .subscribe(new Subscriber<Double>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                mView.hideLoading();
                                mView.onError();
                            }

                            @Override
                            public void onNext(Double score) {
                                mView.hideLoading();
                                mView.showScoreFromOpenCV(score);
                            }
                        });
        compositeSubscription.add(subscription);
    }

    private Bitmap resizeBitmap(Bitmap image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width > height) {
            float ratio = (width / (float) maxWidth);
            width = maxWidth;
            height = (int) (height / ratio);
        } else if (height > width) {
            float ratio = height / (float) maxHeight;
            height = maxHeight;
            width = (int) (width / ratio);
        } else {
            width = maxWidth;
            height = maxHeight;
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public void onDestroy() {
        compositeSubscription.clear();
    }

    public void onClickSelectImage() {
        mView.onClickSelectImage();
    }

    public void onActivityResultForPickImageRequest(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.PICK_IMAGE_REQUEST_CODE
                && resultCode == Activity.RESULT_OK && null != data) {
            mView.onActivityResultForPickImageRequest(data);
        }
    }

}
