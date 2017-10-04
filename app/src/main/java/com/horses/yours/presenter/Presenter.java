package com.horses.yours.presenter;

/**
 * @author Brian Salvattore
 */
@SuppressWarnings("WeakerAccess")
public interface Presenter<V>{

    void setView(V view);

    void detachView();
}

