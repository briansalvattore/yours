package com.horses.yours.ui.activity;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.InputFilter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.horses.yours.R;
import com.horses.yours.business.model.UserEntity;
import com.horses.yours.presenter.LoginPresenter;
import com.horses.yours.ui.application.MainApp;
import com.horses.yours.ui.base.BaseActivity;
import com.horses.yours.util.Methods;
import com.horses.yours.util.SimpleActionListener;
import com.horses.yours.view.LoginView;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkTextView;
import com.mihaelisaev.metw.MaskedEditTextWatcher;

import butterknife.BindView;
import butterknife.OnClick;
import io.paperdb.Paper;

public class SignInActivity extends BaseActivity implements LoginView {

    private static final String TAG = SignInActivity.class.getSimpleName();

    @BindView(R.id.start)
    protected Button start;

    @BindView(R.id.go)
    protected Button go;

    @BindView(R.id.main)
    protected View main;

    @BindView(R.id.help)
    protected AutoLinkTextView help;

    @BindView(R.id.name)
    protected EditText name;

    @BindView(R.id.number)
    protected EditText number;

    @BindView(R.id.name_wrapper)
    protected TextInputLayout nameWrapper;

    @BindView(R.id.number_wrapper)
    protected TextInputLayout numberWrapper;

    private static final int MAX_LENGTH = 15;
    private boolean isSignUp = false;

    protected LoginPresenter presenter = new LoginPresenter();

    @Override
    protected int getView() {
        return R.layout.activity_sign_in;
    }

    @Override
    protected void onCreate() {
        setActionBar();
        initComponents();

        presenter.setView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        number.addTextChangedListener(new MaskedEditTextWatcher(number, text -> "+51 ### ### ###"));
        SimpleActionListener.send(number, this::verifyNumber);
    }

    @SuppressWarnings("ConstantConditions")
    private void setActionBar() {
        ActionBar actionBar = setSupportActionBar(R.string.title_sign_in);

        Drawable close = ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp);
        close.setColorFilter(ContextCompat.getColor(this, R.color.md_grey_500), PorterDuff.Mode.SRC_ATOP);

        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.primary_text));
        actionBar.setHomeAsUpIndicator(close);
    }

    @SuppressWarnings("ConstantConditions")
    private void initComponents() {
        Methods.addRipple(R.drawable.ripple_white, start);
        // TODO: 15/12/2016 change disable
        Methods.addRipple(R.drawable.ripple_primary, go);

        help.setCustomModeColor(ContextCompat.getColor(this, R.color.colorPrimary));
        help.setSelectedStateColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        help.setCustomRegex(getString(R.string.regex_terms));
        help.addAutoLinkMode(AutoLinkMode.MODE_CUSTOM);
        help.setAutoLinkText(getString(R.string.title_terms));
        help.setAutoLinkOnClickListener((autoLinkMode, matchedText) -> {
            Log.d(TAG, "onAutoLinkTextClick() called with: autoLinkMode = [" + autoLinkMode.toString() + "], matchedText = [" + matchedText + "]");
            startActivity(TermsActivity.class, matchedText);
        });

        nameWrapper.setErrorEnabled(true);
        numberWrapper.setErrorEnabled(true);
        nameWrapper.getChildAt(1).setVisibility(View.GONE);
        numberWrapper.getChildAt(1).setVisibility(View.GONE);

        number.setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_LENGTH) });

        main.animate().alpha(0).y(Methods.getHeightScreen()).setDuration(0).setListener(null).start();
        toolbar.animate().alpha(0).y(Methods.getHeightScreen()).setDuration(0).setListener(null).start();
    }

    @OnClick(R.id.go)
    protected void verifyNumber () {
        nameWrapper.getChildAt(1).setVisibility(View.GONE);
        numberWrapper.getChildAt(1).setVisibility(View.GONE);

        if (name.getText().toString().trim().isEmpty()) {
            nameWrapper.setError(getString(R.string.error_empty));
            nameWrapper.getChildAt(1).setVisibility(View.VISIBLE);
        }

        if (number.getText().toString().trim().isEmpty()) {
            numberWrapper.setError(getString(R.string.error_empty));
            numberWrapper.getChildAt(1).setVisibility(View.VISIBLE);
        }

        if (number.getText().toString().isEmpty() || name.getText().toString().isEmpty()) {
            return;
        }

        UserEntity user = new UserEntity();
        user.setFullname(name.getText().toString().trim());
        user.setNumber(number.getText().toString().trim());

        presenter.validateNumber(user);
    }

    @SuppressWarnings({"unused", "ConstantConditions"})
    @OnClick(R.id.start)
    protected void showSignUp() {
        isSignUp = true;

        new Handler().post(() -> {
            toolbar.animate().alpha(1).y(Methods.getStatusBarHeight()).setDuration(500).setListener(null).start();
            main.animate().alpha(1).y(Methods.getStatusBarHeight()).setDuration(400).setListener(null).start();
        });

        new Handler().postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(name, InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    @SuppressWarnings("ConstantConditions")
    private void hideSignUp() {
        isSignUp = false;

        Methods.hideKeyboard(name);

        toolbar.animate().alpha(0).y(Methods.getHeightScreen()).setDuration(400).setListener(null).start();
        main.animate().alpha(0).y(Methods.getHeightScreen()).setDuration(500).setListener(null).start();
    }

    @Override
    public void onBackPressed() {

        if (isSignUp) {
            hideSignUp();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void successfully() {
        Paper.book().write("session", true);
        MainApp.closeAll();
        MainApp.refreshNumbers();
        startActivity(MainActivity.class);
    }

    /*@Override
    public void successfully(UserRequestEntity.Body body) {
        startActivity(VerifyActivity.class, body);
    }*/
}
