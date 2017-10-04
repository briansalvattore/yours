package com.horses.yours.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.horses.yours.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Methods extends DrawableUtil {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static void init(Context context) {
        Methods.context = context;
        DrawableUtil.init(context);
    }

    public static boolean isInternetConnected() {
        boolean isConnected;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = (activeNetwork != null)
                && (activeNetwork.isConnectedOrConnecting());
        return isConnected;
    }

    private static Point getPoint() {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        return size;
    }

    public static int getWidthScreen() {

        return getPoint().x;
    }

    public static int getHeightScreen() {

        return getPoint().y;
    }

    public static int toPixels(int dpi) {

        float d = context.getResources().getDisplayMetrics().density;

        return (int) (dpi * d);
    }

    public static void receiverPriority(){

        Intent smsRecvIntent = new Intent("android.provider.Telephony.SMS_RECEIVED");
        List<ResolveInfo> infos =  context.getPackageManager().queryBroadcastReceivers(smsRecvIntent, 0);
        for (ResolveInfo info : infos) {
           Log.d("Receiver",  info.activityInfo.name + ": priority=" + info.priority);
        }
    }

    public static void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static int getStatusBarHeight() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return 0;
        }

        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static MaterialDialog getDialogLoading(Context context) {
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_connecting)
                .content(R.string.dialog_wait)
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    public static MaterialDialog getSimpleDialog(Context context, String message) {
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_ups)
                .content(message)
                .positiveText(R.string.dialog_ok)
                .show();
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }
}
