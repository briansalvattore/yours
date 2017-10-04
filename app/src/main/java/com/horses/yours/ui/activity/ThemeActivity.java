package com.horses.yours.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.horses.yours.R;
import com.horses.yours.ui.adapter.ThemeAdapter;
import com.horses.yours.ui.application.MainApp;
import com.horses.yours.ui.base.BaseActivity;
import com.horses.yours.util.Methods;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.paperdb.Paper;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ThemeActivity extends BaseActivity {

    private static final String TAG = ThemeActivity.class.getSimpleName();

    private static final int SELECT_PICTURE = 1823;

    private List<String> backgrounds = new ArrayList<>();

    @BindView(R.id.recycler)
    protected RecyclerView recycler;

    @Override
    protected int getView() {
        return R.layout.activity_theme;
    }

    @Override
    protected void onCreate() {
        setSupportActionBar(R.string.title_themes);

        for (String key : Paper.book("themes").getAllKeys()) {
            String path = Paper.book("themes").read(key);
            backgrounds.add(path);
        }

        ThemeAdapter adapter = new ThemeAdapter();
        adapter.setList(backgrounds);
        adapter.setOnItemClickListener(path -> {
            showSnackBar(getString(R.string.title_themes_change));
            Paper.book().write("background", path);
        });

        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        recycler.setAdapter(adapter);
        recycler.setItemViewCacheSize(20);
    }

    @OnClick(R.id.fab)
    protected void addImage() {
        ThemeActivityPermissionsDispatcher.choosePhotoWithCheck(this);
    }

    @NeedsPermission({ Manifest.permission.WRITE_EXTERNAL_STORAGE })
    protected void choosePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.title_select)), SELECT_PICTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ThemeActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {

            long time = new Date().getTime();
            String path = MainApp.getInstance().getCacheDirPath() + "/theme/" + time + ".jpg";

            Uri uri = data.getData();
            Log.d(TAG, "onActivityResult() called with: uri = [" + uri.toString() + "]");
            Log.d(TAG, "onActivityResult() called with: uri = [" + path + "]");

            File file = new File(path);

            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                Methods.copyStream(inputStream, fileOutputStream);
                fileOutputStream.close();

                if (inputStream != null) {
                    inputStream.close();
                }
            }
            catch (Exception e) {
                Log.wtf(TAG, "saveImage: ", e);
                // TODO: 16/02/2017 error handler
            }


            Paper.book("themes").write(String.valueOf(new Date().getTime()), "file://" + path);
            backgrounds.add("file://" + path);

            Paper.book().write("background", "file://" + path);

            recycler.getAdapter().notifyDataSetChanged();
        }
    }
}
