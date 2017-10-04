package com.horses.yours.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import java.util.List;

import codetail.graphics.drawables.DrawableHotspotTouch;
import codetail.graphics.drawables.LollipopDrawable;
import codetail.graphics.drawables.LollipopDrawablesCompat;

/**
 * @author Brian Salvattore
 */
@SuppressWarnings("WeakerAccess")
public class DrawableUtil {

    private static final String TAG = DrawableUtil.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static void init(Context context) {
        DrawableUtil.context = context;
    }

    public static void addRipple(int id, View... views){
        addRipple(new int[]{id}, views);
    }

    public static void addRipple(int id, List<View> views){
        addRipple(new int[]{id}, views);
    }

    public static void addRipple(int[] ids, List<View> views) {
        for(View view : views){
            setBackgroundDrawable(ids, view);
        }
    }

    public static void addRipple(int[] ids, View... views) {
        for(View view : views){
            setBackgroundDrawable(ids, view);
        }
    }

    @SuppressWarnings("deprecation")
    private static void setBackgroundDrawable(int[] ids, View view) {
        try {
            view.setBackgroundDrawable(getDrawable(ids[0]));
            view.setOnTouchListener(new DrawableHotspotTouch((LollipopDrawable) view.getBackground()));
        }
        catch (Exception e) {
            Log.wtf(TAG, "setBackgroundDrawable: ", e);

            if (ids.length == 1) return;

            view.setBackgroundDrawable(ContextCompat.getDrawable(context, ids[1]));
        }

        view.setClickable(true);
    }

    private static Drawable getDrawable(int id) throws Exception {
        return LollipopDrawablesCompat.getDrawable(context.getResources(), id, context.getTheme());
    }
}
