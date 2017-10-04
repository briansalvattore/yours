package com.horses.yours.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
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

import com.horses.yours.R;
import com.horses.yours.business.retrofit.Api;
import com.horses.yours.ui.application.MainApp;
import com.horses.yours.util.DisplayListener;
import com.horses.yours.util.Methods;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import butterknife.ButterKnife;
import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Brian Salvattore
 */
public class ChatLeftView extends BaseChat {

    private static final String TAG = ChatLeftView.class.getSimpleName();

    public ChatLeftView(Context context) {
        super(context);
        init();
    }

    public ChatLeftView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatLeftView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, margin, 0);
        setLayoutParams(params);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.LEFT);

        inflate(getContext(), R.layout.view_chat_left_2, this);

        ButterKnife.bind(this, this);

        content.setOnClickListener(view -> setTimeVisibility());

        time.setVisibility(GONE);
    }

    public void invalidate2() {
        setTime();

        final String fileName = data.getUser() + ".jpg";
        final String filePath = MainApp.getInstance().getCacheDirPath() + "/profiles/" + fileName;

        displayImage(filePath);

        switch (data.getType()) {
            case "text":
                comment.setText(data.getMessage());
                break;
            case "image":
                initImage2();
                break;
            case "audio":
                initAudio2();
                break;
        }
    }

    private void initAudio2() {
        comment.setVisibility(GONE);

        if (data.getAudioDownload().isEmpty()) {
            comment.setVisibility(VISIBLE);
            comment.setText(R.string.error_not_message_sound);
            return;
        }

        RecordItem recordItem = initRecordItem();
        //recordItem.setColor(R.color.md_white_1000);

        ImageView play = recordItem.getPlay();
        ProgressBar loading = recordItem.getLoading();
        SeekBar seek = recordItem.getSeek();
        seek.setEnabled(false);

        extras.addView(recordItem.getView());

        if (data.getAudioPath().isEmpty()) {
            /** download audio*/
            play.setVisibility(GONE);
            loading.setVisibility(VISIBLE);

            Api.getService().downloadFile(data.getAudioDownload()).enqueue(new Callback<ResponseBody>() {
                @SuppressWarnings("ResultOfMethodCallIgnored")
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            final String fileName = new Date().getTime() + ".wav";

                            new Thread(() -> {
                                /** save audio in SD Card */
                                String filePath = MainApp.getInstance().getCacheDirPath() + "/chats/" + room + "/" + fileName;

                                File file = new File(filePath);
                                File path = new File(file.getParent());

                                if (file.getParent() != null && !path.isDirectory()) {
                                    path.mkdirs();
                                }

                                InputStream inputStream = null;
                                OutputStream outputStream = null;

                                try {
                                    byte[] fileReader = new byte[4096];

                                    long fileSize = response.body().contentLength();
                                    long fileSizeDownloaded = 0;

                                    inputStream = response.body().byteStream();
                                    outputStream = new FileOutputStream(file);

                                    while (true) {
                                        int read = inputStream.read(fileReader);

                                        if (read == -1) {
                                            break;
                                        }

                                        outputStream.write(fileReader, 0, read);

                                        fileSizeDownloaded += read;

                                        Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                                    }

                                    outputStream.flush();

                                    post(() -> {
                                        findViewById(R.id.record).findViewById(R.id.play).setVisibility(VISIBLE);
                                        findViewById(R.id.record).findViewById(R.id.loading).setVisibility(GONE);
                                    });
                                }
                                catch (IOException e) {
                                    Log.wtf(TAG, "updateAudio: FileNotFoundException", e);
                                }
                                finally {
                                    try {
                                        if (outputStream != null) {
                                            outputStream.flush();
                                            outputStream.close();
                                        }
                                        if (inputStream != null) {
                                            inputStream.close();
                                        }
                                    } catch (IOException e) {
                                        Log.wtf(TAG, "updateAudio: IOException", e);
                                    }
                                }

                                data.setAudioPath(filePath);

                                Paper.book(room).write(data.getKey(), data);
                            }).start();
                        }
                        else {
                            post(() -> {
                                extras.removeView(findViewById(R.id.record));
                                comment.setVisibility(VISIBLE);
                                comment.setText(R.string.error_not_message_sound);
                            });
                        }
                    }
                    else {
                        post(() -> {
                            extras.removeView(findViewById(R.id.record));
                            comment.setVisibility(VISIBLE);
                            comment.setText(R.string.error_not_message_sound);
                        });
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.wtf(TAG, "onFailure: ", t);
                    post(() -> {
                        extras.removeView(findViewById(R.id.record));
                        comment.setVisibility(VISIBLE);
                        comment.setText(R.string.error_not_message_sound);
                    });
                }
            });
        }
    }

    private void initImage2() {
        comment.setVisibility(GONE);
        ImageView image = new ImageView(getContext());
        image.setId(R.id.image);
        extras.addView(image);

        if (data.getImagePath().isEmpty()) {
            downloadImage();
            return;
        }

        ImageLoader.getInstance().loadImage("file://" + data.getImagePath(), new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                image.setImageBitmap(loadedImage);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                Log.e(TAG, "onLoadingFailed: ", failReason.getCause());
                /*comment.setVisibility(VISIBLE);
                comment.setText(R.string.error_not_message_image);*/

                downloadImage();
            }
        });
    }

    private void downloadImage() {
        if (data.getImageDownload().isEmpty()) {
            extras.removeView(findViewById(R.id.image));
            comment.setVisibility(VISIBLE);
            comment.setText(R.string.error_not_message_image);
            return;
        }

        ProgressBar progress = new ProgressBar(getContext());
        progress.setId(R.id.progress);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Methods.toPixels(50), Methods.toPixels(50));
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progress.setLayoutParams(params);
        extras.addView(progress);

        downloadImage2();
    }

    private void displayImage(String image) {
        ImageLoader.getInstance().loadImage("file:///" + image, DisplayListener.outSaveImage, new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                avatar.setImageBitmap(loadedImage);
            }
        });
    }
}