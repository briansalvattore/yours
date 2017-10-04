package com.horses.yours.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getFragmentView(), container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        onCreate();
    }

    protected abstract int getFragmentView();

    protected abstract void onCreate();

    protected void startActivity(Class<?> cls){
        startActivity(new Intent(getActivity(), cls));
    }

    protected void startActivity(Class<?> cls, Serializable... extras) {

        Intent intent = new Intent(getActivity(), cls);

        for (int i = 0; i < extras.length; i++) {
            intent.putExtra("extra" + i, extras[i]);
        }

        startActivity(intent);
    }
}
