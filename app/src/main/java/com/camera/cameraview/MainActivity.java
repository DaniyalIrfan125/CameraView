package com.camera.cameraview;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.ml.common.FirebaseMLException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.FileCallback;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements ImageClassifier.MyListener {
    CameraView camera;
    ImageClassifier imageClassifier;
    Button btn_capture;
    TextView tvDetectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionsForCamera();
        camera = findViewById(R.id.cameraView);
        btn_capture = findViewById(R.id.btn_capture);
        tvDetectedItem = findViewById(R.id.tvDetectedItem);
        camera.setLifecycleOwner(this);

        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture();
            }
        });

        try {
            imageClassifier = new ImageClassifier(MainActivity.this);
            imageClassifier.setMyListener(this);
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(PictureResult result) {

                // Picture was taken!
                // If planning to show a Bitmap, we will take care of
                // EXIF rotation and background threading for you...
                result.toBitmap(2000, 2000, new BitmapCallback() {
                    @Override
                    public void onBitmapReady(@Nullable Bitmap bitmap) {
                       // imageClassifier.classifyFrame(bitmap);
                        SaveImage(bitmap);
                    }
                });


//                // If planning to save a file on a background thread,
//                // just use toFile. Ensure you have permissions.

//               File filepath = Environment.getExternalStorageDirectory();
//               File filewNewDir = new File(filepath.getAbsolutePath()+ "/Demo/");
//               filewNewDir.mkdir();
//
//               File file = new File(filewNewDir,System.currentTimeMillis()+".jpg");
//                try {
//                    new FileOutputStream(file);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//
//                result.toFile(file, new FileCallback() {
//                    @Override
//                    public void onFileReady(@Nullable File file) {
//
//                    }
//                });

                // Access the raw data if needed.
                byte[] data = result.getData();
            }

            @Override
            public void onVideoTaken(VideoResult result) {
                // A Video was taken!
            }

            // And much more
        });

        camera.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                // frame.getData()

            imageClassifier.extractDataFromFrame(frame,frame.getData());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
    }

    @Override
    public void callback(@org.jetbrains.annotations.Nullable String result) {
        tvDetectedItem.setText(result);
    }

    private void permissionsForCamera() {
        Dexter.withActivity(MainActivity.this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {


                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {

                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).onSameThread()
                .check();

    }

    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ())
            file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
