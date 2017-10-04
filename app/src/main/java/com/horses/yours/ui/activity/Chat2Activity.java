package com.horses.yours.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.horses.yours.R;
import com.horses.yours.business.model.ChatItemEntity;
import com.horses.yours.business.model.DetailEntity;
import com.horses.yours.ui.application.MainApp;
import com.horses.yours.ui.base.BaseActivity;
import com.horses.yours.ui.messaging.MessageManager;
import com.horses.yours.ui.view.BaseChat;
import com.horses.yours.ui.view.ChatLeftView;
import com.horses.yours.ui.view.ChatRightView;
import com.horses.yours.util.DisplayListener;
import com.horses.yours.util.Methods;
import com.horses.yours.util.SimpleActionListener;
import com.horses.yours.util.SimpleChildEventListener;
import com.horses.yours.util.SimpleTextWatcher;
import com.horses.yours.util.SimpleValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;
import io.paperdb.Paper;
import omrecorder.AudioSource;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class Chat2Activity extends BaseActivity {

    private static final String TAG = Chat2Activity.class.getSimpleName();

    private static final int SELECT_PICTURE = 1823;
    private static final int REQUEST_IMAGE_CAPTURE = 3984;

    private HashMap<String, Object> data;
    private String room;

    //private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference roomRef;
    private DatabaseReference chatRef;
    private DatabaseReference typingRef;
    private DatabaseReference receiverRef;

    private ChildEventListener chatListener;
    private ValueEventListener typingListener;
    private ValueEventListener receiverListener;

    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;

    private int lastHeight = 0;

    private boolean isMic = true;

    private Recorder recorder;
    private String audioPath;

    @BindView(R.id.background)
    protected ImageView background;

    @BindView(R.id.scroll)
    protected NestedScrollView scroll;

    @BindView(R.id.linear)
    protected LinearLayout linear;

    @BindView(R.id.message)
    protected EditText message;

    @BindView(R.id.send)
    protected FloatingActionButton send;

    @BindView(R.id.typing)
    protected LinearLayout typing;

    @BindView(R.id.chronometer)
    protected Chronometer chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getView() {
        return R.layout.activity_chat;
    }

    @Override
    protected void onCreate() {
        initData();
        initControllers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCache();

        chatListener = getChatListener();
        typingListener = getTypingListener();
        receiverListener = getReceiverListener();

        layoutListener = getLayoutListener();

        linear.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        chatRef.addChildEventListener(chatListener);
        if (typingRef != null) typingRef.addValueEventListener(typingListener);
        if (receiverRef != null) receiverRef.addValueEventListener(receiverListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        linear.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        chatRef.removeEventListener(chatListener);
        if (typingRef != null) typingRef.removeEventListener(typingListener);
        if (receiverRef != null) receiverRef.removeEventListener(receiverListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        roomRef.child("participants").child(me.getKey()).updateChildren(new HashMap<String, Object>() {{
            put("typing", false);
        }});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.attach) attachFile();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Chat2ActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
                    /*Bitmap imageBitmap = BitmapFactory.decodeFile(path);
                    updatePhoto(imageBitmap);*/
                    uploadPhoto(path);
                    break;
            }
        }
    }

    private void uploadPhoto(String path) {
        DatabaseReference push = chatRef.push();

        ChatItemEntity item = new ChatItemEntity();
        item.setType("image_upload");
        item.setUser(me.getKey());
        item.setTime(new Date().getTime());
        item.setKey(push.getKey());

        item.setRoom(room);
        item.setImageUpload(path);

        Paper.book(room).write(item.getKey(), item);
        push.setValue(item);
    }

    private void updatePhoto(Bitmap bitmap) {
        DatabaseReference push = chatRef.push();

        ChatItemEntity item = new ChatItemEntity();
        item.setType("image");
        item.setUser(me.getKey());
        item.setTime(new Date().getTime());
        item.setKey(push.getKey());

        ChatRightView rightView = new ChatRightView(this);
        rightView.setData(item);
        rightView.setRoom(room);
        rightView.setTempBitmap(bitmap, push);
        rightView.setOnLongClickCallback(this::addPossible);
        rightView.invalidate2();

        linear.addView(rightView);
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    @OnClick(R.id.send)
    protected void sendMessage() {
        if (message.getText().toString().trim().isEmpty()) return;

        DatabaseReference push = chatRef.push();

        ChatItemEntity item = new ChatItemEntity();
        item.setType("text");
        item.setTime(new Date().getTime());
        item.setUser(me.getKey());
        item.setMessage(message.getText().toString().trim());
        item.setKey(push.getKey());

        Paper.book(room).write(item.getKey(), item);

        /** Save data in firebase*/
        push.setValue(item);
        MessageManager.sendPush();

        /** Save last message */
        roomRef.child("last").setValue(new HashMap<String, Object>() {{
            put("key", me.getKey());
            put("message", message.getText().toString().trim());
            put("time", new Date().getTime());
            put("name", me.getFullname().split(" ")[0]);
        }});

        ChatRightView rightView = new ChatRightView(Chat2Activity.this);
        rightView.setData(item);
        rightView.setRoom(room);
        rightView.setOnLongClickCallback(this::addPossible);
        rightView.invalidate2();

        linear.addView(rightView);
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));

        message.setText("");
    }

    private void addPossible(BaseChat item) {
        new MaterialDialog.Builder(this)
                .title(R.string.title_options)
                .items(R.array.array_option)
                .itemsCallback((dialog1, itemView, which, text) -> {
                    switch (which) {
                        case 0:
                            copyText(item.getData().getMessage());
                            break;
                        case 1:
                            linear.removeView(item);
                            Paper.book(room).delete(item.getData().getKey());
                            chatRef.child(item.getData().getKey()).child("deleted").child(me.getKey()).setValue(true);
                            break;
                        case 2:
                            item.setTimeVisibility(View.VISIBLE);
                            break;
                    }
                })
                .show();
    }

    private void copyText(String message) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("message", message);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, getString(R.string.toast_copy), Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("unchecked")
    private void initData() {
        data = (HashMap<String, Object>) getIntent().getSerializableExtra("data");
        JsonObject detail = (JsonObject) new JsonParser().parse(new Gson().toJson(data));

        room = detail.get("key").getAsString();

        roomRef = ref.child("chats").child(room);
        chatRef = roomRef.child("chats");

        MainApp.addRoom(room);

        if (detail.get("type").getAsString().equals("single")) {
            setSupportActionBar(detail.get("receiver").getAsJsonObject().get("name").getAsString());

            String key = detail.get("receiver").getAsJsonObject().get("key").getAsString();

            typingRef = roomRef.child("participants").child(key).child("typing");

            if (me.isShowOnline()) receiverRef = ref.child("users").child(key).child("detail");
        }
        else {
            setSupportActionBar(detail.get("room").getAsString());
        }

        setSupportActionBarClickSingle();

        Log.d(TAG, "initData() called with: map=[" + detail + "]");

        if (Paper.book().exist("background")) {
            ImageLoader.getInstance().displayImage(
                    Paper.book().read("background"),
                    background,
                    DisplayListener.fullImage,
                    new DisplayListener());
        }
    }

    private void setSupportActionBarClickSingle() {
        if (toolbar == null) return;
        toolbar.setOnClickListener(v -> startActivity(ContactActivity.class, data));
        Methods.addRipple(R.drawable.ripple_primary_square, toolbar);
    }

    private void initControllers() {
        roomRef.child("participants").child(me.getKey()).updateChildren(new HashMap<String, Object>() {{
            put("typing", false);
        }});

        message.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {

                boolean isTyping = editable.length() != 0;

                if (isTyping) {
                    isMic = false;
                    send.setImageDrawable(ContextCompat.getDrawable(Chat2Activity.this, R.drawable.ic_send_white_24dp));
                } else {
                    isMic = true;
                    send.setImageDrawable(ContextCompat.getDrawable(Chat2Activity.this, R.drawable.ic_mic_white_24dp));
                }

                roomRef.child("participants").child(me.getKey()).updateChildren(new HashMap<String, Object>() {{
                    put("typing", isTyping);
                }});
            }
        });

        SimpleActionListener.send(message, this::sendMessage);

        initSendAudio();
    }

    private void initCache() {
        linear.removeAllViews();
        for (String key : Paper.book(room).getAllKeys()) {

            ChatItemEntity item = Paper.book(room).read(key);

            if (item.getUser().equals(me.getKey())) {

                ChatRightView rightView = new ChatRightView(Chat2Activity.this);
                rightView.setData(item);
                rightView.setRoom(room);
                rightView.setOnLongClickCallback(this::addPossible);
                rightView.invalidate2();

                linear.addView(rightView);
            } else {

                ChatLeftView leftView = new ChatLeftView(Chat2Activity.this);
                leftView.setData(item);
                leftView.setRoom(room);
                leftView.setOnLongClickCallback(this::addPossible);
                leftView.invalidate2();

                linear.addView(leftView);
            }

            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void attachFile() {
        new MaterialDialog.Builder(this)
                .title(R.string.title_change)
                .items(R.array.array_change)
                .itemsCallback((dialog, itemView, which, text) -> {
                    switch (which) {
                        case 0:
                            Chat2ActivityPermissionsDispatcher.takePictureWithCheck(this);
                            break;
                        case 1:
                            Chat2ActivityPermissionsDispatcher.choosePhotoWithCheck(this);
                            break;
                    }
                })
                .show();
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

    private void initSendAudio() {
        send.setOnTouchListener((v, event) -> {
            if (!isMic) return false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    send.animate().setDuration(300).scaleY(1.8f).scaleX(1.8f).start();

                    message.setVisibility(View.GONE);
                    chronometer.setVisibility(View.VISIBLE);
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();

                    Chat2ActivityPermissionsDispatcher.recSoundWithCheck(this);
                    break;
                case MotionEvent.ACTION_UP:
                    send.animate().setDuration(300).scaleY(1f).scaleX(1f).start();

                    long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
                    int seconds = (int) elapsedMillis / 1000;

                    message.setVisibility(View.VISIBLE);
                    chronometer.setVisibility(View.GONE);
                    chronometer.stop();

                    Log.e(TAG, "onTouch: " + chronometer.getText());
                    Log.e(TAG, "onTouch: " + seconds);

                    if (seconds > 1) updateSound(seconds);
                    break;
            }

            return true;
        });
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE })
    protected void recSound() {
        audioPath = getAudioPath();

        recorder = OmRecorder.wav(new PullTransport.Default(mic()), new File(audioPath));
        recorder.startRecording();
    }

    private void updateSound(int seconds) {

        try {
            recorder.stopRecording();

            DatabaseReference push = chatRef.push();

            ChatItemEntity item = new ChatItemEntity();
            item.setType("audio_update");
            item.setUser(me.getKey());
            item.setTime(new Date().getTime());
            item.setKey(push.getKey());

            item.setDuration(seconds);
            item.setAudioPath(audioPath);

            ChatRightView rightView = new ChatRightView(this);
            rightView.setData(item);
            rightView.setRoom(room);
            rightView.setTempBitmap(null, push);
            rightView.setOnLongClickCallback(this::addPossible);
            rightView.invalidate2();

            linear.addView(rightView);
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        }
        catch (IOException e) {
            Log.wtf(TAG, "saveSound: IOException", e);
            Toast.makeText(this, getString(R.string.error_not_message_sound), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Log.wtf(TAG, "saveSound: Exception", e);
            Toast.makeText(this, getString(R.string.error_not_message_sound), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private String getAudioPath() {
        final String fileName = new Date().getTime() + ".wav";
        String filePath = MainApp.getInstance().getCacheDirPath() + "/chats/" + room + "/" + fileName;

        File file = new File(filePath);
        File path = new File(file.getParent());

        if (file.getParent() != null && !path.isDirectory()) {
            path.mkdirs();
        }

        return filePath;
    }

    private AudioSource mic() {
        return new AudioSource.Smart(MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.CHANNEL_IN_MONO, 44100);
    }

    private ValueEventListener getTypingListener() {
        return new SimpleValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean isTyping = (boolean) dataSnapshot.getValue();

                    typing.setVisibility(isTyping ? View.VISIBLE : View.GONE);

                    if (isTyping) scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
                }
            }
        };
    }

    private ValueEventListener getReceiverListener() {
        return new SimpleValueEventListener() {
            @SuppressWarnings({"unchecked", "ConstantConditions"})
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DetailEntity detail = dataSnapshot.getValue(DetailEntity.class);

                    if (detail.isOnline()) {
                        toolbar.setSubtitle(R.string.title_active_now);
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        Date input = new Date(detail.getLast());

                        toolbar.setSubtitle(getString(R.string.title_last_connection).replace("%time%", sdf.format(input)));
                    }
                }
            }
        };
    }

    private ChildEventListener getChatListener() {
        return new SimpleChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatItemEntity item = dataSnapshot.getValue(ChatItemEntity.class);
                item.setKey(dataSnapshot.getKey());

                if (item.getDeleted() != null) {
                    if (item.getDeleted().get(me.getKey()) != null) return;
                }

                if (Paper.book(room).exist(item.getKey())) return;

                if (item.getUser().equals(me.getKey())) {

                    ChatRightView rightView = new ChatRightView(Chat2Activity.this);
                    rightView.setData(item);
                    rightView.setRoom(room);
                    rightView.setOnLongClickCallback(base -> addPossible(base));
                    rightView.invalidate2();

                    linear.addView(rightView);
                }
                else {

                    ChatLeftView leftView = new ChatLeftView(Chat2Activity.this);
                    leftView.setData(item);
                    leftView.setRoom(room);
                    leftView.setOnLongClickCallback(base -> addPossible(base));
                    leftView.invalidate2();

                    linear.addView(leftView);
                }

                Paper.book(room).write(item.getKey(), item);
                scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            }
        };
    }

    private ViewTreeObserver.OnGlobalLayoutListener getLayoutListener() {
        return () -> {
            int width = linear.getMeasuredWidth();
            int height = linear.getMeasuredHeight();

            Log.d(TAG, "onGlobalLayout() called with: width=[" + width + "], height=[" + height + "]");

            if (lastHeight != height) {
                lastHeight = height;
                scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            }
        };
    }
}
