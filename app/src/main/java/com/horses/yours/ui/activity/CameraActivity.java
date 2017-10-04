package com.horses.yours.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.cjt2325.cameralibrary.JCameraView;
import com.cjt2325.cameralibrary.lisenter.JCameraLisenter;
import com.cjt2325.cameralibrary.util.FileUtil;
import com.horses.yours.R;
import com.horses.yours.ui.application.MainApp;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = CameraActivity.class.getSimpleName();

    @BindView(R.id.camera)
    protected JCameraView camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        final String fileName = new Date().getTime() + ".jpg";
        String filePath = MainApp.getInstance().getCacheDirPath() + "/tmp/" + fileName;

        camera.setJCameraLisenter(bitmap -> {
            String path = FileUtil.saveBitmap2(filePath, bitmap);
            Intent intent = new Intent();
            intent.putExtra("path", path);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        camera.onPause();
    }
}
