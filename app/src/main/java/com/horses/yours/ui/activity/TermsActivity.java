package com.horses.yours.ui.activity;

import com.horses.yours.R;
import com.horses.yours.ui.base.BaseActivity;

public class TermsActivity extends BaseActivity {

    @Override
    protected int getView() {
        return R.layout.activity_terms;
    }

    @Override
    protected void onCreate() {
        setSupportActionBar(getIntent().getStringExtra("extra0"));
    }
}
