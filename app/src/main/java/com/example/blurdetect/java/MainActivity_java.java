package com.example.blurdetect.java;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.blurdetect.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.CAMERA;

public class MainActivity_java extends AppCompatActivity {

    Bitmap myBitmap;
    Uri picUri;
    Mat matImage;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    ImageView imageView;
    TextView textView;
    List<Intent> allIntents = new ArrayList<>();;
    private final static int ALL_PERMISSIONS_RESULT = 107;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("wwwww", "OpenCV loaded successfully");
                  //  mOpenCvCameraView.enableView();
                    matImage =new Mat();

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
        setContentView(R.layout.activity_mainn);
         imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.solution);
        allIntents.clear();



        Button button = (Button) findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(getPickImageChooserIntent(), 200);
            }
        });


        permissions.add(CAMERA);
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/


    /**
     * Create a chooser intent to select the source to get image from.<br/>
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the intent chooser.
     */
    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();


        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }


        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size()-1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }


    /**
     * Get URI to image received from capture by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        if (resultCode == Activity.RESULT_OK) {


            if (getPickImageResultUri(data) != null) {
                picUri = getPickImageResultUri(data);
                try {
                    myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);
                    Log.e("myBitmap",":"+myBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                ImageView croppedImageView = (ImageView) findViewById(R.id.imageView);
                croppedImageView.setImageBitmap(myBitmap);
                imageView.setImageBitmap(myBitmap);
             // Bitmap map=getResizedBitmap(myBitmap,700,1200);
               /* Glide.with(this)
                        .asBitmap()
                        .load(picUri)
                        .centerCrop()
                        .into(imageView);*/
                Log.e("mypicUri",":"+picUri);
                isBlurredImage(myBitmap);
//as sugestion

            } else {


                bitmap = (Bitmap) data.getExtras().get("data");

                myBitmap = bitmap;
                ImageView croppedImageView = (ImageView) findViewById(R.id.imageView);
                if (croppedImageView != null) {
                    croppedImageView.setImageBitmap(myBitmap);
                }
                imageView.setImageBitmap(myBitmap);

                //  imageView.setImageBitmap(myBitmap);
               /* Glide.with(this)
                        .asBitmap()
                        .load(picUri)
                        .centerCrop()
                        .into(imageView);*/
              //  Bitmap map=getResizedBitmap(myBitmap,700,1200);
                isBlurredImage(myBitmap);

            }

           // allIntents.clear();
        }

    }
    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public Bitmap getResizedBitmap(Bitmap image, int mWidth, int mHieght) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = mWidth;
            height = (int) (width / bitmapRatio);
        } else {
            height = mHieght;
            width = (int) (height * bitmapRatio);
        }


        return Bitmap.createScaledBitmap(image, width, height, true);
    }

   /* public Bitmap getResizedBitmap(Bitmap image, int mWidth, int mHieght) {
        int width = image.getWidth();
        int height = image.getHeight();

        //float bitmapRatio = (float) width / (float) height;
        if(width>height){
            // if (bitmapRatio > 0) {
            final float ratio = (float) width / (float) width;
            width = mWidth;
            height = (int) (height / ratio);

            // height = (int) (width / bitmapRatio);
        } else if(height>width) {
            final float ratio=(float) height / (float) height;
            height = mHieght;
            width = (int) (width / ratio);
        }else {
            width = mWidth;
            height = mHieght;
        }


        return Bitmap.createScaledBitmap(image, width, height, true);
    }*/


    /**
     * Get the URI of the selected image from {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera and gallery image.
     *
     * @param data the returned data of the activity result
     */
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }


        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("pic_uri", picUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        picUri = savedInstanceState.getParcelable("pic_uri");
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new
                AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (hasPermission(perms)) {

                    } else {

                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                //Log.d("API123", "permisionrejected " + permissionsRejected.size());

                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }
    private boolean isBlurredImage(Bitmap image) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inDither = true;
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;

        int l = CvType.CV_8UC1;
         matImage = new Mat();
        Utils.bitmapToMat(image, matImage);
        Mat matImageGrey = new Mat();
        Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);

        Mat dst2 = new Mat();
        Utils.bitmapToMat(image, dst2);

        Mat laplacianImage = new Mat();
        dst2.convertTo(laplacianImage, l);
        Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
        Mat laplacianImage8bit = new Mat();
        laplacianImage.convertTo(laplacianImage8bit, l);
        System.gc();

        Bitmap bmp = Bitmap.createBitmap(laplacianImage8bit.cols(),
                laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(laplacianImage8bit, bmp);

        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
        Log.e("GetHeight","blur image----------->"+bmp.getHeight()+"-------"+bmp.getWidth()+"---"+pixels);

        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
                bmp.getHeight());
        if (bmp != null)
            if (!bmp.isRecycled()) {
                bmp.recycle();

            }
        int maxLap = -16777216;

        for (int i = 0; i < pixels.length; i++) {

            if (pixels[i] > maxLap) {
                maxLap = pixels[i];
            }
        }
        int soglia = -6118750;

        if (maxLap < soglia || maxLap == soglia) {
            Log.e("blure image", "--------->blur image<------------"+maxLap);
            textView.setText("--------->blur image<------------");
            return true;
        } else {
               Log.e("non blure image", "----------->Not blur image<------------"+maxLap);
               textView.setText("----------->Not blur image<------------");
            return false;
        }
    }
private void fff(Bitmap destImage){
    BitmapFactory.Options opt = new BitmapFactory.Options();
    opt.inDither = true;
    opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
   // myBitmap = BitmapFactory.decodeByteArray(im, 0, im.length);
    int l = CvType.CV_8UC1; //8-bit grey scale image
    Mat matImage = new Mat();
    Utils.bitmapToMat(myBitmap, matImage);
    Mat matImageGrey = new Mat();
    Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);

   // destImage;
    destImage = Bitmap.createBitmap(myBitmap);
    Mat dst2 = new Mat();
    Utils.bitmapToMat(destImage, dst2);
    Mat laplacianImage = new Mat();
    dst2.convertTo(laplacianImage, l);
    Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
    Mat laplacianImage8bit = new Mat();
    laplacianImage.convertTo(laplacianImage8bit, l);

    Bitmap bmp = Bitmap.createBitmap(laplacianImage8bit.cols(),
            laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);
    Utils.matToBitmap(laplacianImage8bit, bmp);
    int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
    bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
            bmp.getHeight());

    int maxLap = -16777216;

    for (int i = 0; i < pixels.length; i++) {
        if (pixels[i] > maxLap)
            maxLap = pixels[i];
    }

    int soglia = -6118750;

    if (maxLap < soglia || maxLap == soglia) {
        Log.e("blure image", "--------->blur image<------------"+maxLap);
        textView.setText("--------->blur image<------------");
    } else {
        Log.e("non blure image", "----------->Not blur image<------------"+maxLap);
        textView.setText("----------->Not blur image<------------");
    }

}
/*
    @Override
    protected void onPause() {
        super.onPause();
        if (imageView != null)
            imageView.setVisibility(View.INVISIBLE);
    }*/


    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("eeee", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("eeeeeeeee", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    public void onDestroy() {
        super.onDestroy();
    }
}
