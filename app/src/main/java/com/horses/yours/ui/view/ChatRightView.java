package com.horses.yours.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.horses.yours.R;
import com.horses.yours.business.model.ChatItemEntity;
import com.horses.yours.business.model.UserEntity;
import com.horses.yours.business.retrofit.Api;
import com.horses.yours.ui.application.MainApp;
import com.horses.yours.ui.messaging.MessageManager;
import com.horses.yours.util.Methods;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;

import butterknife.ButterKnife;
import io.paperdb.Paper;
import me.zhanghai.android.materialprogressbar.internal.ThemeUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Brian Salvattore
 */
public class ChatRightView extends BaseChat {

    private static final String TAG = ChatRightView.class.getSimpleName();
    private Bitmap tempBitmap;
    private DatabaseReference push;
    protected UserEntity me = Paper.book().read("user");

    public ChatRightView(Context context) {
        super(context);
        init();
    }

    public ChatRightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatRightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, 0, 0, 0);
        setLayoutParams(params);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.RIGHT);

        inflate(getContext(), R.layout.view_chat_right_2, this);

        ButterKnife.bind(this, this);

        content.setOnClickListener(view -> setTimeVisibility());

        time.setVisibility(GONE);
        avatar.setVisibility(GONE);
    }

    public void invalidate2() {
        setTime();

        switch (data.getType()) {
            case "text":
                comment.setText(data.getMessage());
                break;
            case "image":
                initImage();
                break;
            case "audio":
                initAudio();
                break;
            case "audio_update":
                updateAudio();
                break;
            case "image_upload":
                Log.d(TAG, "invalidate2() image_upload");
                uploadImage();
                break;
        }
    }

    private void uploadImage() {
        tempBitmap = BitmapFactory.decodeFile(data.getImageUpload());
        room = data.getRoom();
        push = FirebaseDatabase.getInstance().getReference().child("chats").child(room).child("chats").child(data.getKey());

        comment.setVisibility(GONE);
        ImageView image = new ImageView(getContext());
        image.setId(R.id.image);
        image.setImageBitmap(tempBitmap);
        extras.addView(image);

        final int widthBitmap = tempBitmap.getWidth();
        final int heightBitmap = tempBitmap.getHeight();

        Log.d(TAG, "initImage() called with: widthBitmap=[" + widthBitmap + "], heightBitmap=[" + heightBitmap + "]");

        ViewTreeObserver vto = image.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int widthImage = image.getMeasuredWidth();
                final int heightImage = image.getMeasuredHeight();

                Log.d(TAG, "updateFromBitmap() called with: widthImage=[" + widthImage + "], heightImage=[" + heightImage + "]");

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(widthImage, (widthImage * heightBitmap) / widthBitmap);
                params.setMargins(0, Methods.toPixels(5), 0, Methods.toPixels(5));
                image.setLayoutParams(params);

                View over = new View(getContext());
                over.setId(R.id.over);
                over.setLayoutParams(params); over.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white_trans));
                extras.addView(over);

                image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        ProgressBar progress = new ProgressBar(getContext());
        progress.setId(R.id.progress);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Methods.toPixels(50), Methods.toPixels(50));
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progress.setLayoutParams(params);
        extras.addView(progress);

        ChatItemEntity item = data;

        final int quality = 85;
        final String fileName = item.getKey() + ".jpg";

        new Thread(() -> {
            /** save photo in SD Card */
            String filePath = savePhotoSD(fileName, tempBitmap, quality);

            /** upload photo in Firebase */
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            tempBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] data = baos.toByteArray();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("chats").child(room).child(fileName);
            UploadTask uploadTask = storageRef.putBytes(data);
            uploadTask.addOnCompleteListener(task -> {
                Log.d(TAG, "onComplete() called with: task = [" + task.isSuccessful() + "]");
                Log.d(TAG, "onComplete() called with: task = [" + task.getResult() + "]");
            });
            uploadTask.addOnSuccessListener(command -> {
                Log.d(TAG, "updateFromBitmap() called with: command=[" + command.getDownloadUrl() + "]");

                post(() -> {
                    extras.removeView(findViewById(R.id.over));
                    extras.removeView(findViewById(R.id.progress));

                    item.setImageDownload(String.valueOf(command.getDownloadUrl()));
                    item.setImagePath(filePath);

                    item.setRoom("");
                    item.setImageUpload("");
                    item.setType("image");

                    Paper.book(room).write(item.getKey(), item);

                    /** clean memory from firebase */
                    item.setImagePath("");

                    /** Save data in firebase*/
                    push.setValue(item);
                    MessageManager.sendPush();

                    /** Save last message */
                    FirebaseDatabase.getInstance().getReference().child("chats")
                            .child(room).child("last").setValue(new HashMap<String, Object>(){{
                        put("key", me.getKey());
                        put("message", getContext().getString(R.string.message_new_image));
                        put("time", new Date().getTime());
                        put("name", me.getFullname().split(" ")[0]);
                    }});
                });
            });
        }).start();
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void initImage() {
        if (tempBitmap != null) {
            updateFromBitmap();
            return;
        }
        else if (!data.getImagePath().isEmpty()) {
            loadFromSd();
            return;
        }
        else if (!data.getImageDownload().isEmpty()){
            downloadImage();
            return;
        }
        else {
            // TODO: 8/06/2017 error for image is empty
        }
    }

    private void initAudio() {
        comment.setVisibility(GONE);

        if (data.getAudioPath().isEmpty()) {
            downloadAudio();
            return;
        }

        File file = new File(data.getAudioPath());

        if (!file.exists()) {
            downloadAudio();
            return;
        }

        RecordItem recordItem = initRecordItem();
        recordItem.setColor(R.color.md_white_1000);

        SeekBar seek = recordItem.getSeek();
        seek.setEnabled(false);
        extras.addView(recordItem.getView());
    }

    private void downloadImage() {
        comment.setVisibility(GONE);
        ImageView image = new ImageView(getContext());
        image.setId(R.id.image);
        extras.addView(image);

        if (data.getImageDownload().isEmpty()) {
            extras.removeView(findViewById(R.id.image));
            comment.setVisibility(VISIBLE);
            comment.setText(R.string.error_not_message_image);
            return;
        }

        ProgressBar progress = new ProgressBar(getContext());
        progress.setId(R.id.progress);
        Drawable drawable = progress.getIndeterminateDrawable().getCurrent();
        drawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.md_white_1000), PorterDuff.Mode.MULTIPLY );
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Methods.toPixels(50), Methods.toPixels(50));
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progress.setLayoutParams(params);
        extras.addView(progress);

        downloadImage2();
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void downloadAudio() {
        if (data.getAudioDownload().isEmpty()) {
            comment.setVisibility(VISIBLE);
            comment.setText(R.string.error_not_message_sound);
            return;
        }

        // TODO: 9/06/2017 download audio
    }

    @SuppressLint("VisibleForTests")
     private void updateAudio() {
        comment.setVisibility(GONE);

        if (data.getAudioPath().isEmpty()) {
            comment.setVisibility(VISIBLE);
            comment.setText(R.string.error_not_message_sound);
            return;
        }

        RecordItem recordItem = initRecordItem();
        recordItem.setColor(R.color.md_white_1000);

        ImageView play = recordItem.getPlay();
        ProgressBar loading = recordItem.getLoading();
        SeekBar seek = recordItem.getSeek();
        seek.setEnabled(false);
        extras.addView(recordItem.getView());

        loading.setVisibility(VISIBLE);
        play.setVisibility(GONE);

        ChatItemEntity item = data;

        final String fileName = item.getKey() + ".wav";
        final String path = data.getAudioPath();

        new Thread(() -> {
            /** upload audio in Firebase */
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("chats").child(room).child(fileName);
            UploadTask uploadTask = storageRef.putFile(Uri.fromFile(new File(data.getAudioPath())));
            uploadTask.addOnCompleteListener(task -> {
                Log.d(TAG, "onComplete() called with: task = [" + task.isSuccessful() + "]");
                Log.d(TAG, "onComplete() called with: task = [" + task.getResult() + "]");
            });
            uploadTask.addOnSuccessListener(command -> {
                Log.d(TAG, "updateAudio() called with: command=[" + command.getDownloadUrl() + "]");

                post(() -> {
                    loading.setVisibility(GONE);
                    play.setVisibility(VISIBLE);

                    item.setAudioDownload(String.valueOf(command.getDownloadUrl()));
                    item.setAudioPath(data.getAudioPath());
                    item.setType("audio");

                    Paper.book(room).write(item.getKey(), item);

                    /** clean memory from firebase */
                    item.setAudioPath("");

                    /** Save data in firebase*/
                    push.setValue(item);
                    MessageManager.sendPush();

                    /** Save last message */
                    FirebaseDatabase.getInstance().getReference().child("chats")
                            .child(room).child("last").setValue(new HashMap<String, Object>(){{
                        put("key", me.getKey());
                        put("message", getContext().getString(R.string.message_new_audio));
                        put("time", new Date().getTime());
                        put("name", me.getFullname().split(" ")[0]);
                    }});

                    /** reload audio path*/
                    data.setAudioPath(path);
                });
            });
        }).start();
    }

    public void setTempBitmap(Bitmap tempBitmap, DatabaseReference push) {
        this.tempBitmap = tempBitmap;
        this.push = push;
    }

    private void loadFromSd() {
        comment.setVisibility(GONE);
        ImageView image = new ImageView(getContext());
        image.setId(R.id.image);
        extras.addView(image);

        ImageLoader.getInstance().loadImage("file://" + data.getImagePath(), new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                image.setImageBitmap(loadedImage);

                final int widthBitmap = loadedImage.getWidth();
                final int heightBitmap = loadedImage.getHeight();

                ViewTreeObserver vto = image.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        final int widthImage = image.getMeasuredWidth();
                        final int heightImage = image.getMeasuredHeight();

                        Log.d(TAG, "loadFromSd() called with: widthImage=[" + widthImage + "], heightImage=[" + heightImage + "]");

                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(widthImage, (widthImage * heightBitmap) / widthBitmap);
                        params.setMargins(0, Methods.toPixels(5), 0, Methods.toPixels(5));
                        image.setLayoutParams(params);

                        image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                Log.e(TAG, "onLoadingFailed: ", failReason.getCause());
                extras.removeView(findViewById(R.id.image));
                comment.setVisibility(VISIBLE);
                comment.setText(R.string.error_not_message_image);
            }
        });
    }

    @SuppressLint("VisibleForTests")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void updateFromBitmap() {
        comment.setVisibility(GONE);
        ImageView image = new ImageView(getContext());
        image.setId(R.id.image);
        image.setImageBitmap(tempBitmap);
        extras.addView(image);

        final int widthBitmap = tempBitmap.getWidth();
        final int heightBitmap = tempBitmap.getHeight();

        Log.d(TAG, "initImage() called with: widthBitmap=[" + widthBitmap + "], heightBitmap=[" + heightBitmap + "]");

        ViewTreeObserver vto = image.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int widthImage = image.getMeasuredWidth();
                final int heightImage = image.getMeasuredHeight();

                Log.d(TAG, "updateFromBitmap() called with: widthImage=[" + widthImage + "], heightImage=[" + heightImage + "]");

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(widthImage, (widthImage * heightBitmap) / widthBitmap);
                params.setMargins(0, Methods.toPixels(5), 0, Methods.toPixels(5));
                image.setLayoutParams(params);

                View over = new View(getContext());
                over.setId(R.id.over);
                over.setLayoutParams(params); over.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white_trans));
                extras.addView(over);

                image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        ProgressBar progress = new ProgressBar(getContext());
        progress.setId(R.id.progress);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Methods.toPixels(50), Methods.toPixels(50));
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progress.setLayoutParams(params);
        extras.addView(progress);

        ChatItemEntity item = data;

        final int quality = 85;
        final String fileName = item.getKey() + ".jpg";

        new Thread(() -> {
            /** save photo in SD Card */
            String filePath = MainApp.getInstance().getCacheDirPath() + "/chats/" + room + "/" + fileName;

            File file = new File(filePath);
            File path = new File(file.getParent());

            if (file.getParent() != null && !path.isDirectory()) {
                path.mkdirs();
            }

            FileOutputStream outputStream = null;

            try {
                outputStream = new FileOutputStream(file);

                tempBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                Log.i(TAG, "updatePhoto: success with filePath =[" + filePath + "]");
            }
            catch (FileNotFoundException e) {
                Log.wtf(TAG, "updatePhoto: FileNotFoundException", e);
            }
            finally {
                try {
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                }
                catch (IOException e) {
                    Log.wtf(TAG, "updatePhoto: IOException", e);
                }
            }

            /** upload photo in Firebase */
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            tempBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] data = baos.toByteArray();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("chats").child(room).child(fileName);
            UploadTask uploadTask = storageRef.putBytes(data);
            uploadTask.addOnCompleteListener(task -> {
                Log.d(TAG, "onComplete() called with: task = [" + task.isSuccessful() + "]");
                Log.d(TAG, "onComplete() called with: task = [" + task.getResult() + "]");
            });
            uploadTask.addOnSuccessListener(command -> {
                Log.d(TAG, "updateFromBitmap() called with: command=[" + command.getDownloadUrl() + "]");

                post(() -> {
                    extras.removeView(findViewById(R.id.over));
                    extras.removeView(findViewById(R.id.progress));

                    item.setImageDownload(String.valueOf(command.getDownloadUrl()));
                    item.setImagePath(filePath);

                    Paper.book(room).write(item.getKey(), item);

                    /** clean memory from firebase */
                    item.setImagePath("");

                    /** Save data in firebase*/
                    push.setValue(item);
                    MessageManager.sendPush();

                    /** Save last message */
                    FirebaseDatabase.getInstance().getReference().child("chats")
                            .child(room).child("last").setValue(new HashMap<String, Object>(){{
                        put("key", me.getKey());
                        put("message", getContext().getString(R.string.message_new_image));
                        put("time", new Date().getTime());
                        put("name", me.getFullname().split(" ")[0]);
                    }});
                });
            });
        }).start();
    }
}