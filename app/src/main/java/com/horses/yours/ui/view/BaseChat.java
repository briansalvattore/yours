package com.horses.yours.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.horses.yours.R;
import com.horses.yours.business.model.ChatItemEntity;
import com.horses.yours.business.retrofit.Api;
import com.horses.yours.ui.application.MainApp;
import com.horses.yours.util.Methods;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Brian Salvattore
 */
public abstract class BaseChat extends LinearLayout {

    private static final String TAG = BaseChat.class.getSimpleName();

    private MediaPlayer player;

    /*protected final int corner = Methods.toPixels(15);*/
    protected final int padding = Methods.toPixels(10);
    protected final int margin = Methods.toPixels(40);

    protected String room;

    @BindView(R.id.content)
    protected View content;

    @BindView(R.id.extras)
    protected RelativeLayout extras;

    @BindView(R.id.avatar)
    protected CircularImageView avatar;

    @BindView(R.id.comment)
    protected TextView comment;

    @BindView(R.id.time)
    protected TextView time;

    protected ChatItemEntity data;

    public BaseChat(Context context) {
        super(context);
    }

    public BaseChat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseChat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("SimpleDateFormat")
    protected void setTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date input = new Date(data.getTime());
        time.setText(sdf.format(input));
    }

    public ChatItemEntity getData() {
        return data;
    }

    public void setData(ChatItemEntity data) {
        this.data = data;
    }

    public void setOnLongClickCallback(OnLongClickCallback callback) {
        content.setOnLongClickListener(view -> {
            callback.done(this);
            return true;
        });
    }

    public void setTimeVisibility() {
        time.setVisibility(time.getVisibility() == GONE ? VISIBLE : GONE);
    }

    public void setTimeVisibility(int visibility) {
        time.setVisibility(visibility);
    }

    public interface OnLongClickCallback {
        void done(BaseChat data);
    }

    protected RecordItem initRecordItem() {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_record, extras, false);
        view.setId(R.id.record);

        ImageView play = (ImageView) view.findViewById(R.id.play);
        ProgressBar loading = (ProgressBar) view.findViewById(R.id.loading);
        SeekBar seek = (SeekBar) view.findViewById(R.id.seek);
        Chronometer seconds = (Chronometer) view.findViewById(R.id.seconds);

        play.setOnClickListener(view1 -> {
            play.setEnabled(false);

            player = new MediaPlayer();

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    /*play.setEnabled(true);*/

                    seek.setMax(mediaPlayer.getDuration());

                    seconds.setBase(SystemClock.elapsedRealtime());
                    seconds.start();

                    Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {

                            if (player != null && player.getCurrentPosition() < player.getDuration()) {
                                seek.setProgress(player.getCurrentPosition());
                                handler.postDelayed(this, 100);
                            }
                            else {
                                handler.removeCallbacks(this);
                            }
                        }
                    };
                    handler.post(runnable);
                }
            });
            player.setOnCompletionListener(mediaPlayer -> {
                seek.setProgress(0);
                seconds.stop();
                seconds.setBase(SystemClock.elapsedRealtime());

                play.setEnabled(true);

                if (player != null) {
                    player.stop();
                    player.release();
                    player = null;
                }
            });

            try {
                Log.d(TAG, "initRecordItem() called with: path=[" + data.getAudioPath() +"]");
                player.setDataSource(data.getAudioPath());
                player.prepare();
                player.start();
            } catch (IOException e) {
                Log.wtf(TAG, "initRecordItem: ", e);
            }
        });

        RecordItem recordItem = new RecordItem();
        recordItem.setPlay(play);
        recordItem.setLoading(loading);
        recordItem.setSeek(seek);
        recordItem.setSeconds(seconds);
        recordItem.setView(view);

        return recordItem;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public class RecordItem {
        private ImageView play;
        private ProgressBar loading;
        private SeekBar seek;
        private Chronometer seconds;

        private View view;

        public void setColor(@ColorRes int res) {

            int color = ContextCompat.getColor(getContext(), res);

            play.setColorFilter(color);
            loading.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            seek.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            seek.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            seconds.setTextColor(color);
        }

        //region Getter and Setter
        public ImageView getPlay() {
            return play;
        }

        public void setPlay(ImageView play) {
            this.play = play;
        }

        public ProgressBar getLoading() {
            return loading;
        }

        public void setLoading(ProgressBar loading) {
            this.loading = loading;
        }

        public SeekBar getSeek() {
            return seek;
        }

        public void setSeek(SeekBar seek) {
            this.seek = seek;
        }

        public Chronometer getSeconds() {
            return seconds;
        }

        public void setSeconds(Chronometer seconds) {
            this.seconds = seconds;
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }
        //endregion
    }

    public void setRoom(String room) {
        this.room = room;
    }

    protected void downloadImage2() {
        Api.getService().downloadFile(data.getImageDownload()).enqueue(new Callback<ResponseBody>() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        final String fileName = data.getKey() + ".jpg";

                        new Thread(() -> {
                            /** save photo in SD Card */
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

                                drawImage(filePath);
                            }
                            catch (IOException e) {
                                Log.wtf(TAG, "updatePhoto: FileNotFoundException", e);
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
                                    Log.wtf(TAG, "updatePhoto: IOException", e);
                                }
                            }

                            data.setImagePath(filePath);

                            Paper.book(room).write(data.getKey(), data);
                        }).start();
                    }
                    else {
                        post(() -> {
                            extras.removeView(findViewById(R.id.progress));
                            extras.removeView(findViewById(R.id.image));
                            comment.setVisibility(VISIBLE);
                            comment.setText(R.string.error_not_message_image);
                        });
                    }
                }
                else {
                    post(() -> {
                        extras.removeView(findViewById(R.id.progress));
                        extras.removeView(findViewById(R.id.image));
                        comment.setVisibility(VISIBLE);
                        comment.setText(R.string.error_not_message_image);
                    });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.wtf(TAG, "onFailure: ", t);
                post(() -> {
                    extras.removeView(findViewById(R.id.progress));
                    extras.removeView(findViewById(R.id.image));
                    comment.setVisibility(VISIBLE);
                    comment.setText(R.string.error_not_message_image);
                });
            }
        });
    }

    protected void drawImage(String filePath) {
        ImageLoader.getInstance().loadImage("file://" + filePath, new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                post(() -> {
                    ImageView image = (ImageView) findViewById(R.id.image);
                    image.setImageBitmap(loadedImage);

                    int widthBitmap = loadedImage.getWidth();
                    int heightBitmap = loadedImage.getHeight();

                    ViewTreeObserver vto = image.getViewTreeObserver();
                    vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            int widthImage = image.getMeasuredWidth();
                            int heightImage = image.getMeasuredHeight();

                            Log.d(TAG, "initImage() called with: widthImage=[" + widthImage + "], heightImage=[" + heightImage + "]");

                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(widthImage, (widthImage * heightBitmap) / widthBitmap);
                            params.setMargins(0, Methods.toPixels(5), 0, Methods.toPixels(5));
                            image.setLayoutParams(params);

                            extras.removeView(findViewById(R.id.progress));

                            image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                });
            }
        });
    }

    /** save photo in SD Card */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected String savePhotoSD(String fileName, Bitmap bitmap, int quality) {

        String filePath = MainApp.getInstance().getCacheDirPath() + "/chats/" + room + "/" + fileName;

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
        }
        catch (FileNotFoundException e) {
            Log.wtf(TAG, "updatePhoto: FileNotFoundException", e);
            filePath = null;
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

        return filePath;
    }
}
