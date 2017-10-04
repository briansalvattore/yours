package com.horses.yours.presenter;

import android.util.Log;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.horses.yours.business.model.UserEntity;
import com.horses.yours.view.LoginView;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

/**
 * @author Brian Salvattore
 */
public class LoginPresenter extends BasePresenter<LoginView> {

    private static final String TAG = LoginPresenter.class.getSimpleName();

    public void validateNumber(UserEntity user) {
        view.showLoading();

        user.setNumber(user.getNumber().replace(" ", ""));
        user.setMail(user.getNumber() + "@yours.com");

        signUp(user);
    }

    private void signUp(UserEntity user) {
        Log.d(TAG, "signUp() called with: user = [" + user + "]");

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.getMail(), user.getNumber())
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "onSuccess() called with: authResult = [" + authResult + "]");
                    completeLogin(authResult, user);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        if (((FirebaseAuthUserCollisionException) e).getErrorCode().equals("ERROR_EMAIL_ALREADY_IN_USE")){
                            Log.i(TAG, "validateAccount: account already created");
                            signIn(user);
                            return;
                        }
                    }

                    Log.wtf(TAG, "onFailure: ", e);

                    if (view == null) return;
                    view.hideLoading();
                    view.showErrorMessage(e.getMessage());
                });
    }

    private void signIn(UserEntity user) {
        Log.d(TAG, "signIn() called with: user = [" + user + "]");

        FirebaseAuth.getInstance().signInWithEmailAndPassword(user.getMail(), user.getNumber())
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "onSuccess() called with: authResult = [" + authResult + "]");
                    completeLogin(authResult, user);
                })
                .addOnFailureListener(e -> {
                    Log.wtf(TAG, "onFailure: ", e);

                    if (view == null) return;
                    view.hideLoading();
                    view.showErrorMessage(e.getMessage());
                });
    }

    private void completeLogin(AuthResult authResult, UserEntity user) {
        Log.d(TAG, "completeLogin() called with: authResult = [" + authResult + "]");

        user.setKey(authResult.getUser().getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        /** update numbers list */
        ref.child("numbers")
                .updateChildren(new HashMap<String, Object>(){{
                    put(user.getNumber(), authResult.getUser().getUid());
                }});

        /** update last time online */
        ref.child("users").child(user.getKey()).child("detail")
                .updateChildren(new HashMap<String, Object>(){{
                    put("last", new Date().getTime());
                    put("online", true);
                    put("token", Paper.book("token").read("token", "nope"));
                }});

        Paper.book().write("user", user);

        if (view == null) return;
        view.hideLoading();
        view.successfully();
    }
}
