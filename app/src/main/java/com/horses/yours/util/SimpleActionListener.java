package com.horses.yours.util;

import android.annotation.SuppressLint;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

@SuppressWarnings("WeakerAccess")
public class SimpleActionListener {

    @SuppressLint("StaticFieldLeak")
    private static EditText editText;
    private static Callback callback;

    public static SimpleActionListener send(EditText editText, Callback callback){

        SimpleActionListener.editText = editText;
        SimpleActionListener.callback = callback;

        return new SimpleActionListener();
    }

    public SimpleActionListener(){

        editText.setOnEditorActionListener((v, actionId, event) -> {

            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                callback.done();
                handled = true;
            }
            return handled;
        });
    }

    public interface Callback{

        void done();
    }
}