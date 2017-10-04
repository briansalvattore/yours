package com.horses.yours.ui.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.horses.yours.business.model.NumberEntity;
import com.horses.yours.util.Methods;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import io.paperdb.Paper;

/**
 * @author Brian Salvattore
 */
public class MainApp extends Application {

    private static final String TAG = MainApp.class.getSimpleName();

    private static MainApp instance;

    private static Stack<Activity> activities = new Stack<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Methods.init(this);
        Paper.init(this);

        refreshNumbers();

        File audioPath = new File(getCacheDirPath() + "/audio");
        audioPath.mkdirs();

        File imagePath = new File(getCacheDirPath() + "/image");
        imagePath.mkdirs();

        File themePath = new File(getCacheDirPath() + "/theme");
        themePath.mkdirs();

        initImageLoader(this);
    }

    private static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs()
                .build();

        ImageLoader.getInstance().init(config);
    }

    private static ValueEventListener numbersListener = new ValueEventListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            List<NumberEntity> list = new ArrayList<>();
            Map<String, String> numbers = (Map<String, String>) dataSnapshot.getValue();

            for (Map.Entry<String, String> item : numbers.entrySet()) {
                NumberEntity numberEntity = NumberEntity.add(item.getKey(), item.getValue());

                Log.d(TAG, "onDataChange() called with: dataSnapshot = [" + numberEntity.toString() + "]");

                list.add(numberEntity);
            }

            Paper.book("firebase").write("numbers", list);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.wtf(TAG, "onCancelled: ", databaseError.toException());
        }
    };

    public static void refreshNumbers() {
        FirebaseDatabase.getInstance().getReference()
                .child("numbers").addListenerForSingleValueEvent(numbersListener);
    }

    public static void addRoom(String room) {
        List<String> rooms = Paper.book().read("rooms", new ArrayList<String>());
        rooms.add(room);
        Paper.book().write("rooms", rooms);
    }

    public static void destroyRooms() {
        List<String> rooms = Paper.book().read("rooms", new ArrayList<String>());

        for (String room : rooms) {
            Paper.book(room).destroy();
        }
    }

    public static MainApp getInstance() {
        return instance;
    }

    public String getCacheDirPath() {
        return getCacheDir().getAbsolutePath();
    }

    public static void closeAll() {

        for (Activity act : activities) {

            try {

                act.finish();
            } catch (Exception ignore) {
            }
        }
        activities.clear();
    }

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }
}
