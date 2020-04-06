package com.example.blurdetect.convertjava;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.blurdetect.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;

public class MainConvert extends AppCompatActivity {
    private String TAG = "MainActivity";
       final int PICK_IMAGE_REQUEST_CODE = 1001;
    private int BLUR_THRESHOLD = 200;
        final String BLURRED_IMAGE = "BLURRED IMAGE";
        final String NOT_BLURRED_IMAGE = "NOT BLURRED IMAGE";
        Mat sourceMatImage=new Mat();
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("wwwww", "OpenCV loaded successfully");
                    //  mOpenCvCameraView.enableView();
                    sourceMatImage =new Mat();

                    // imageView.setVisibility(View.INVISIBLE);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_convert);
    }
}
