package com.horses.yours.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.horses.yours.R;
import com.horses.yours.ui.activity.Chat2Activity;
import com.horses.yours.ui.application.MainApp;
import com.horses.yours.util.DisplayListener;
import com.horses.yours.util.Methods;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Brian Salvattore
 */
public class ContactView extends LinearLayout {

    private static final String TAG = ContactView.class.getSimpleName();

    private HashMap<String, Object> data;

    @BindView(R.id.name)
    protected TextView name;

    @BindView(R.id.last)
    protected TextView last;

    @BindView(R.id.photo)
    protected CircularImageView photo;
    private String key;

    public ContactView(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);

        inflate(getContext(), R.layout.view_contact, this);

        ButterKnife.bind(this, this);

        Methods.addRipple(R.drawable.ripple_grey, this);
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void invalidate(ThisCallback callback) {

        JsonObject object = (JsonObject) new JsonParser().parse(new Gson().toJson(data));

        Log.d(TAG, "invalidate() called with: object= [" + object + "]");

        if (object.get("type").getAsString().equals("single")) {
            name.setText(object.get("receiver").getAsJsonObject().get("name").getAsString());

            final String fileName = object.get("receiver").getAsJsonObject().get("key").getAsString() + ".jpg";
            final String filePath = MainApp.getInstance().getCacheDirPath() + "/profiles/" + fileName;

            displayImage(filePath);

            new Thread(() -> {

                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profiles").child(fileName);

                File file = new File(filePath);
                File path = new File(file.getParent());

                if (file.getParent() != null && !path.isDirectory()) {
                    path.mkdirs();
                }

                FileDownloadTask downloadTask = storageRef.getFile(file);
                downloadTask.addOnSuccessListener(taskSnapshot -> photo.post(() -> displayImage(filePath)));
                downloadTask.addOnFailureListener(e -> Log.wtf(TAG, "onFailure: ", e));
            }).start();
        }
        else {
            name.setText(data.get("room").toString());
            photo.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.background_circle_group));
        }

        setOnClickListener(v -> getContext().startActivity(new Intent(getContext(), Chat2Activity.class).putExtra("data", data)));
        callback.done(LastEntity.create(this, object.get("key").getAsString()));
    }

    private void displayImage(String image) {
        ImageLoader.getInstance().loadImage("file:///" + image, DisplayListener.outSaveImage, new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                photo.setImageBitmap(loadedImage);
            }
        });
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public interface ThisCallback {
        void done(LastEntity lastEntity);
    }

    @SuppressWarnings("WeakerAccess")
    public static class LastEntity {
        private ContactView contactView;
        private String key;

        public static LastEntity create(ContactView contactView, String key) {
            LastEntity lastEntity = new LastEntity();
            lastEntity.setContactView(contactView);
            lastEntity.setKey(key);
            return lastEntity;
        }

        public ContactView getContactView() {
            return contactView;
        }

        public void setContactView(ContactView contactView) {
            this.contactView = contactView;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public void setLast(String message) {
        last.setVisibility(VISIBLE);
        last.setText(message);
    }
}
