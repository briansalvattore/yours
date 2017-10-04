package com.horses.yours.presenter;

import com.horses.yours.view.SignInView;

/**
 * @author Brian Salvattore
 */
public class SignInPresenter extends BasePresenter<SignInView> {

    private final String EMAIL_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE";

    private static final String TAG = SignInPresenter.class.getSimpleName();

    /*public void verifyNumber(UserRequestEntity.Body request) {
        if (!Methods.isInternetConnected()) return;

        view.showLoading();

        Api.getService().login(request).enqueue(new Callback<UserRequestEntity.Body>() {
            @Override
            public void onResponse(Call<UserRequestEntity.Body> call, Response<UserRequestEntity.Body> response) {
                if (view == null) return;
                view.hideLoading();

                if (response.isSuccessful()) {

                    UserRequestEntity.Body body = response.body();
                    body.getUser().setTemporalName(request.getUser().getFirstName());
                    body.getUser().setCountryCode(request.getUser().getCountryCode());
                    body.getUser().setMobileNumber(request.getUser().getMobileNumber());

                    view.successfully(body);
                } else {
                    // TODO: 9/01/2017 add error handler
                    view.showErrorMessage(R.string.dialog_denied);
                }
            }

            @Override
            public void onFailure(Call<UserRequestEntity.Body> call, Throwable t) {
                if (view == null) return;
                view.hideLoading();

                if (t instanceof SocketTimeoutException) {
                    view.showErrorMessage(R.string.error_try_again);
                    return;
                } else if (t instanceof UnknownHostException) {
                    view.showErrorMessage(R.string.error_internet);
                    return;
                }

                view.showErrorMessage(t.toString());
            }
        });
    }*/

    /*public void createAccount(UserEntity userEntity) {
        view.showLoading();

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(userEntity.getMailYours(), userEntity.getNumberYours())
                .addOnSuccessListener(authResult -> {
                    if (view == null) return;
                    view.hideLoading();

                    FirebaseUser user = authResult.getUser();

                    if (user == null) {
                        view.showErrorMessage(R.string.dialog_denied);
                        return;
                    }

                    // TODO: 30/04/2017 add callbacks
                    user.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(userEntity.getFullname())
                            .build());

                    userEntity.setKey(user.getUid());

                    view.successfully(userEntity);
                })
                .addOnFailureListener(e -> {
                    if (view == null) return;
                    view.hideLoading();

                    if (e instanceof FirebaseAuthUserCollisionException) {

                        if (((FirebaseAuthUserCollisionException) e).getErrorCode().equals(EMAIL_IN_USE)) {
                            authenticateAccount(userEntity);
                            return;
                        }
                    }

                    view.showErrorMessage(e.toString());
                });
    }*/

    /*public void authenticateAccount(UserEntity userEntity) {
        view.showLoading();

        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(userEntity.getMailYours(), userEntity.getNumberYours())
                .addOnSuccessListener(authResult -> {
                    if (view == null) return;
                    view.hideLoading();
                })
                .addOnFailureListener(e -> {
                    if (view == null) return;
                    view.hideLoading();

                    view.showErrorMessage(e.toString());
                });
    }*/


}
