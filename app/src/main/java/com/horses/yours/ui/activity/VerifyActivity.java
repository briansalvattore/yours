package com.horses.yours.ui.activity;

import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.horses.yours.R;
import com.horses.yours.presenter.VerifyPresenter;
import com.horses.yours.ui.base.BaseActivity;
import com.horses.yours.util.Methods;
import com.horses.yours.util.SimpleActionListener;
import com.horses.yours.view.VerifyView;

import butterknife.BindView;
import butterknife.OnClick;

public class VerifyActivity extends BaseActivity implements VerifyView {

    @BindView(R.id.help)
    protected TextView help;

    @BindView(R.id.go)
    protected Button go;

    @BindView(R.id.number)
    protected EditText number;

    @BindView(R.id.number_wrapper)
    protected TextInputLayout numberWrapper;

    protected VerifyPresenter presenter = new VerifyPresenter();

    @Override
    protected int getView() {
        return R.layout.activity_verify;
    }

    @Override
    protected void onCreate() {
        setSupportActionBar(getString(R.string.title_verify));

        Methods.addRipple(R.drawable.ripple_primary, go);

        numberWrapper.setErrorEnabled(true);
        numberWrapper.getChildAt(1).setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SimpleActionListener.send(number, this::verifyCode);
    }

    @OnClick(R.id.go)
    protected void verifyCode () {

    }
}
