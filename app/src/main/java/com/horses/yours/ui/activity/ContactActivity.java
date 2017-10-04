package com.horses.yours.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.horses.yours.R;
import com.horses.yours.business.model.ContactSimpleEntity;
import com.horses.yours.ui.application.MainApp;
import com.horses.yours.ui.base.BaseActivity;
import com.horses.yours.util.DisplayListener;
import com.horses.yours.util.Methods;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;
import io.paperdb.Paper;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ContactActivity extends BaseActivity {

    private static final String TAG = ContactActivity.class.getSimpleName();

    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private String number;
    private String room;
    private String key;

    @BindView(R.id.image)
    protected ImageView image;

    @BindView(R.id.call)
    protected TextView call;

    @BindView(R.id.delete)
    protected TextView delete;

    @BindView(R.id.block)
    protected Button block;

    @Override
    protected int getView() {
        return R.layout.activity_contact;
    }

    @Override
    protected void onCreate() {
        initData();
    }

    @SuppressWarnings("unchecked")
    private void initData() {
        HashMap<String, Object> data = (HashMap<String, Object>) getIntent().getSerializableExtra("extra0");
        JsonObject detail = (JsonObject) new JsonParser().parse(new Gson().toJson(data));

        room = detail.get("key").getAsString();

        if (detail.get("type").getAsString().equals("single")) {
            setSupportActionBar(detail.get("receiver").getAsJsonObject().get("name").getAsString());

            number = detail.get("receiver").getAsJsonObject().get("number").getAsString();
            key = detail.get("receiver").getAsJsonObject().get("key").getAsString();

            call.setText(number);
            displayImage();
        }
        else {
            setSupportActionBar(detail.get("room").getAsString());
            call.setVisibility(View.GONE);
        }

        Methods.addRipple(R.drawable.ripple_white, call, delete);
        Methods.addRipple(R.drawable.ripple_red, block);
    }

    @OnClick(R.id.call)
    protected void callUser() {
        ContactActivityPermissionsDispatcher.callWithCheck(this, number);
    }

    @OnClick(R.id.delete)
    protected void deleteAllChats() {

        dialog = Methods.getDialogLoading(this);

        ref.child("chats").child(room).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deleteInBackground(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
                Log.wtf(TAG, "onCancelled: ", databaseError.toException());
                Methods.getSimpleDialog(ContactActivity.this, getString(R.string.error_try_again));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ContactActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressLint("StaticFieldLeak")
    private void deleteInBackground(DataSnapshot dataSnapshot) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "doInBackground() called with: snapshot = [" + snapshot + "]");

                    Paper.book(room).delete(snapshot.getKey());
                    ref.child("chats").child(room).child("chats").child(snapshot.getKey()).child("deleted").child(me.getKey()).setValue(true);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dialog.dismiss();
            }
        }.execute();
    }

    private void displayImage() {
        final String fileName = key + ".jpg";
        final String filePath = MainApp.getInstance().getCacheDirPath() + "/profiles/" + fileName;

        ImageLoader.getInstance().loadImage("file:///" + filePath, DisplayListener.outSaveImage, new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setImageBitmap(loadedImage);
            }
        });
    }

    @SuppressLint("MissingPermission")
    @NeedsPermission({Manifest.permission.CALL_PHONE})
    protected void call(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        startActivity(callIntent);
    }
}
