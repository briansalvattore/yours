package com.horses.yours.ui.messaging;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import io.paperdb.Paper;

/**
 * @author Brian Salvattore
 */
public class FirebaseIdService extends FirebaseInstanceIdService {

    private static final String TAG = FirebaseIdService.class.getSimpleName();

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefresh() called with: token=[" + token + "]");
        Paper.book("token").write("token", token);
    }
}
