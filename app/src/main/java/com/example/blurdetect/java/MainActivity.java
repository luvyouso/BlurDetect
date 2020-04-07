package com.example.blurdetect.java;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.blurdetect.R;
import com.jain.ullas.imageblurdetection.MainActivityView;
import com.jain.ullas.imageblurdetection.MainPresenter;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgproc.Imgproc;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity implements MainActivityView {

    private final String TAG = "MainActivity";
    private final int PICK_IMAGE_REQUEST_CODE = 1001;
    private final int BLUR_THRESHOLD = 200;
    private final String BLURRED_IMAGE = "BLURRED IMAGE";
    private final String NOT_BLURRED_IMAGE = "NOT BLURRED IMAGE";

    private Mat sourceMatImage;
    private MainPresenter presenter;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    sourceMatImage = new Mat();
                }
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    TextView mRextCpuArchitecture;
    TextView mStatusFromRenderScript;
    TextView statusFromOpenCV;
    TextView statusFromRenderScript;
    ImageView scannedImage;
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRextCpuArchitecture = findViewById(R.id.textCpuArchitecture);
        mStatusFromRenderScript = findViewById(R.id.statusFromRenderScript);
        statusFromOpenCV = findViewById(R.id.statusFromOpenCV);
        scannedImage = findViewById(R.id.scannedImage);
        statusFromRenderScript = findViewById(R.id.statusFromRenderScript);
        progressBar = findViewById(R.id.progressBar);

        mRextCpuArchitecture.setText(getString(R.string.cpu_architecture, System.getProperty("os.arch")));
        mStatusFromRenderScript.setVisibility(View.VISIBLE);

        presenter = new MainPresenter(this);
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void onClickSelectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.gallery_pick_image)), PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pick_gallery_image, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_gallery: {
                presenter.onClickSelectImage();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResultForPickImageRequest(requestCode, resultCode, data);

    }

    @Override
    public void onActivityResultForPickImageRequest(@NotNull Intent data) {
        extractImageBitmapFromIntentData(data);
    }

    private void extractImageBitmapFromIntentData(Intent galleryIntentData) {
        showLoading();
        try {
            Uri imageUri = galleryIntentData.getData();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            scannedImage.setImageBitmap(bitmap);
            scannedImage.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //  showScoreFromRenderScript(BlurrinessDetectionRenderScript.runDetection(this, bitmap))
            }
            presenter.getDataFromImageBitmap(bitmap);
        } catch (Exception e) {
            hideLoading();
            Log.e(TAG, "Error", e);
        }
    }


    @Override
    public double getSharpnessScoreFromOpenCV(@NotNull Bitmap bitmap) {
        Mat destination = new Mat();
        Mat matGray = new Mat();
        Utils.bitmapToMat(bitmap, sourceMatImage);
        Imgproc.cvtColor(sourceMatImage, matGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Laplacian(matGray, destination, 3);
        MatOfDouble median = new MatOfDouble();
        MatOfDouble std = new MatOfDouble();
        Core.meanStdDev(destination, median, std);
        return Double.parseDouble(new DecimalFormat("0.00").format(Math.pow(std.get(0, 0)[0], 2.0)));
    }

    @Override
    public void onError() {

    }

    @Override
    public void showScoreFromOpenCV(double score) {
        if (score < BLUR_THRESHOLD) {
            statusFromOpenCV.setText(getString(R.string.result_from_opencv, BLURRED_IMAGE, String.valueOf(BLUR_THRESHOLD), String.valueOf(score)));
            statusFromOpenCV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blurred_image));
        } else {
            statusFromOpenCV.setText(getString(R.string.result_from_opencv, NOT_BLURRED_IMAGE, String.valueOf(BLUR_THRESHOLD), String.valueOf(score)));
            statusFromOpenCV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.not_blurred_image));
        }
    }

    @Override
    public void showScoreFromRenderScript(@NotNull Pair<Boolean, String> status) {
        if (status.first) {
            statusFromRenderScript.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blurred_image));
        } else {
            statusFromRenderScript.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.not_blurred_image));
        }
    }
}
