package com.horses.yours.presenter;

/**
 * @author Brian Salvattore
 */
public class BasePresenter<T> implements Presenter<T> {

    protected T view;

    @Override
    public void setView(T view) {
        if (view == null)
            throw new IllegalArgumentException("You can't set a null view");

        this.view = view;
    }

    @Override
    public void detachView() {
        view = null;
    }
}
