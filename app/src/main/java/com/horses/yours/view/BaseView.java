package com.horses.yours.view;

/**
 * @author Brian Salvattore
 */
@SuppressWarnings("WeakerAccess")
public interface BaseView {

    void showLoading();

    void hideLoading();

    void showErrorMessage(int message);

    void showErrorMessage(String message);
}
