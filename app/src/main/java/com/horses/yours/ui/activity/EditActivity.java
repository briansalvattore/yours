package com.horses.yours.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.horses.yours.BuildConfig;
import com.horses.yours.R;
import com.horses.yours.ui.application.MainApp;
import com.horses.yours.ui.base.BaseActivity;
import com.horses.yours.util.DisplayListener;
import com.horses.yours.util.Methods;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;
import io.paperdb.Paper;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class EditActivity extends BaseActivity {

    private static final String TAG = EditActivity.class.getSimpleName();

    @BindViews({
            R.id.apper, R.id.themes, R.id.invitation, R.id.contact, R.id.policy,
            R.id.use, R.id.version, R.id.delete, R.id.exit})
    protected List<View> rippleLayouts;

    @BindView(R.id.name)
    protected TextView name;

    @BindView(R.id.phone)
    protected TextView phone;

    @BindView(R.id.photo)
    protected CircularImageView photo;

    private SwitchCompat apper;

    private static final int SELECT_PICTURE = 1823;
    private static final int REQUEST_IMAGE_CAPTURE = 3984;

    @Override
    protected int getView() {
        return R.layout.activity_edit;
    }

    @Override
    protected void onCreate() {
        setSupportActionBar(R.string.title_settings);

        Methods.addRipple(R.drawable.ripple_white, rippleLayouts);

        ((TextView) ((LinearLayout) rippleLayouts.get(6)).getChildAt(1)).setText(BuildConfig.VERSION_NAME);
        apper = (SwitchCompat) ((LinearLayout) rippleLayouts.get(0)).getChildAt(1);
        apper.setOnCheckedChangeListener((compoundButton, b) -> {
            me.setShowOnline(!b);
            Paper.book().write("user", me);
        });
        apper.setChecked(!me.isShowOnline());

        name.setText(me.getFullname());
        phone.setText(me.getNumber());
        phone.setEnabled(false);

        String filePath = "file://" + MainApp.getInstance().getCacheDirPath() + "/profiles/" + me.getKey() + ".jpg";
        ImageLoader.getInstance().loadImage(filePath, DisplayListener.outSaveImage, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (loadedImage != null) photo.setImageBitmap(loadedImage);
            }
        });
    }

    @OnClick(R.id.apper)
    protected void changeApper() {
        apper.setChecked(!apper.isChecked());
    }

    @OnClick(R.id.themes)
    protected void goToThemes() {
        startActivity(ThemeActivity.class);
    }

    @OnClick(R.id.policy)
    protected void goToPolicy() {
        startActivity(TermsActivity.class, getString(R.string.title_terms_policy));
    }

    @OnClick(R.id.use)
    protected void goToUse() {
        startActivity(TermsActivity.class, getString(R.string.title_terms_use));
    }

    @OnClick(R.id.exit)
    protected void exitToAccount() {
        Paper.book().destroy();
        finish();
        startActivity(SplashActivity.class);
    }

    @OnClick(R.id.edit)
    protected void changePhoto() {
        new MaterialDialog.Builder(this)
                .title(R.string.title_change)
                .items(R.array.array_change)
                .itemsCallback((dialog, itemView, which, text) -> {
                    switch (which) {
                        case 0:
                            EditActivityPermissionsDispatcher.takePictureWithCheck(this);
                            break;
                        case 1:
                            EditActivityPermissionsDispatcher.choosePhotoWithCheck(this);
                            break;
                    }
                })
                .show();
    }

    @OnClick(R.id.delete)
    protected void deleteAccount() {
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_ups)
                .content(R.string.dialog_delete_account)
                .positiveText(R.string.dialog_ok)
                .positiveColor(ContextCompat.getColor(this, R.color.secondary_text))
                .onPositive((dialog1, which) -> deleteAccount(true))
                .negativeText(R.string.dialog_cancel)
                .negativeColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .show();
    }

    @SuppressWarnings({"ConstantConditions", "StatementWithEmptyBody"})
    private void deleteAccount(boolean b) {
        Log.d(TAG, "deleteAccount() called");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // TODO: 9/06/2017 Re-authenticate a user
        }

        Task<Void> deleteTask = user.delete();
        deleteTask.addOnSuccessListener(aVoid -> {

            FirebaseDatabase.getInstance().getReference().child("users").child(me.getKey()).setValue(null);

            MainApp.destroyRooms();
            Paper.book().destroy();
            Paper.book("inbox").destroy();
            Paper.book("themes").destroy();
            Paper.book("firebase").destroy();
            Paper.book("numbers").destroy();

            MainApp.closeAll();
            startActivity(SplashActivity.class);
        });
        deleteTask.addOnFailureListener(e -> {
            Log.wtf(TAG, "onFailure: ", e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    protected void choosePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.title_select)), SELECT_PICTURE);
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    protected void takePicture() {
        Intent takePictureIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EditActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case SELECT_PICTURE:

                    Uri uri = data.getData();

                    ImageLoader.getInstance().loadImage(uri.toString(), DisplayListener.outSaveImage, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            updatePhoto(loadedImage);
                        }
                    });
                    break;
                case REQUEST_IMAGE_CAPTURE:

                    String path = data.getStringExtra("path");
                    Bitmap imageBitmap = BitmapFactory.decodeFile(path);
                    updatePhoto(imageBitmap);
                    break;
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void updatePhoto(Bitmap bitmap) {
        if (bitmap == null) return;

        Log.d(TAG, "updatePhoto() called with: bitmap = [" + bitmap + "]");
        photo.setImageBitmap(bitmap);

        final int quality = 85;
        final String fileName = me.getKey() + ".jpg";

        new Thread(() -> {
            /** save photo in SD Card */
            String filePath = MainApp.getInstance().getCacheDirPath() + "/profiles/" + fileName;

            File file = new File(filePath);
            File path = new File(file.getParent());

            if (file.getParent() != null && !path.isDirectory()) {
                path.mkdirs();
            }

            FileOutputStream outputStream = null;

            try {
                outputStream = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                Log.i(TAG, "updatePhoto: success with filePath =[" + filePath + "]");
            } catch (FileNotFoundException e) {
                Log.wtf(TAG, "updatePhoto: FileNotFoundException", e);
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                } catch (IOException e) {
                    Log.wtf(TAG, "updatePhoto: IOException", e);
                }
            }

            /** upload photo in Firebase */
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] data = baos.toByteArray();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profiles").child(fileName);
            UploadTask uploadTask = storageRef.putBytes(data);
            uploadTask.addOnCompleteListener(task -> {
                Log.d(TAG, "onComplete() called with: task = [" + task.isSuccessful() + "]");
                Log.d(TAG, "onComplete() called with: task = [" + task.getResult() + "]");
            });
        }).start();
    }
}
