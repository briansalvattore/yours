package com.horses.yours.ui.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.horses.yours.R;
import com.horses.yours.business.model.UserEntity;
import com.horses.yours.ui.application.MainApp;
import com.horses.yours.util.Methods;
import com.horses.yours.util.NavigationDrawerUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

/**
 * @author Brian Salvattore
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    protected ActionBarDrawerToggle drawerToggle;
    protected MaterialDialog dialog;
    protected Fragment current;

    private Handler handler = new Handler();
    private Runnable runnable;

    private static int min = 0;

    private Snackbar snackbar;

    @Nullable
    @BindView(R.id.drawer)
    protected DrawerLayout drawerLayout;

    @Nullable
    @BindView(R.id.nav)
    protected NavigationView navigationView;

    @Nullable
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @Nullable
    @BindView(R.id.coordinator)
    protected CoordinatorLayout coordinator;

    protected DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    protected UserEntity me = Paper.book().read("user");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getView());

        MainApp.addActivity(this);

        ButterKnife.bind(this);

        runnable = new Runnable() {
            @Override
            public void run() {

                if (coordinator != null) {

                    if (Methods.isInternetConnected()) {

                        if (snackbar != null) {
                            snackbar.dismiss();
                        }
                    }
                    else {

                        if (snackbar == null) {

                            snackbar = Snackbar.make(coordinator, getString(R.string.alert_no_internet), Snackbar.LENGTH_INDEFINITE);
                            snackbar.show();
                        }
                        else {

                            if (!snackbar.isShown()) {
                                snackbar.show();
                            }
                        }
                    }
                }

                handler.postDelayed(this, 500);
            }
        };

        onCreate();
    }

    public void showSnackBar(String message) {
        if (coordinator == null) return;
        Snackbar.make(coordinator, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainApp.removeActivity(this);
    }

    //region Toolbar
    protected ActionBar setSupportActionBar() {
        return setSupportActionBar("");
    }

    protected ActionBar setSupportActionBar(@StringRes int title) {
        return setSupportActionBar(getString(title));
    }

    @SuppressWarnings("ConstantConditions")
    protected ActionBar setSupportActionBar(String title) {

        setSupportActionBar(toolbar);

        /*ViewCompat.setElevation(toolbar, Methods.toPixels(5));*/

        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        toolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.white));

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));

        return getSupportActionBar();
    }

    @SuppressWarnings("ConstantConditions")
    protected void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    protected void changeTitle(String title){

        getSupportActionBar().setTitle(title);
    }
    //endregion

    //region Components
    protected void addFragment(Fragment fragment){

        current = fragment;

        getSupportFragmentManager().beginTransaction().add(R.id.content, fragment).commit();
    }

    protected void replaceFragment(Fragment fragment){

        current = fragment;

        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }

    protected void startActivity(Class<?> cls){
        startActivity(new Intent(this, cls));
    }

    protected void startActivity(Class<?> cls, Serializable... extras) {

        Intent intent = new Intent(this, cls);

        for (int i = 0; i < extras.length; i++) {
            intent.putExtra("extra" + i, extras[i]);
        }

        startActivity(intent);
    }
    //endregion

    //region Drawer
    protected void setupDrawer(){
        drawerToggle = NavigationDrawerUtil.configNavigationDrawer(this, drawerLayout, null);
    }

    @SuppressWarnings("ConstantConditions")
    protected void setupDrawerContent(SimpleNavigationItemSelectedListener listener) {
        navigationView.setNavigationItemSelectedListener(item -> {
            listener.onNavigationItemSelected(item);
            drawerLayout.closeDrawers();
            return true;
        });
    }

    public interface SimpleNavigationItemSelectedListener {
        void onNavigationItemSelected(MenuItem item);
    }
    @Override
    protected void onResume() {
        super.onResume();

        handler.post(runnable);

        if (drawerToggle != null) drawerToggle.syncState();

        if (me != null) {
            ref.child("users").child(me.getKey()).child("detail")
                    .updateChildren(new HashMap<String, Object>(){{
                        put("online", true);
                    }});
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(runnable);

        if (me != null) {
            ref.child("users").child(me.getKey()).child("detail")
                    .updateChildren(new HashMap<String, Object>(){{
                        put("last", new Date().getTime());
                        put("online", false);
                    }});
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null)
            drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                try {
                    View view = this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                catch (Exception ignore) { }

                if(drawerToggle != null){

                    return drawerToggle.onOptionsItemSelected(item);
                }
                else{

                    onBackPressed();
                    return true;
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onBackPressed() {

        if (drawerLayout != null) {
            if(drawerLayout.isDrawerOpen(Gravity.LEFT)){

                drawerLayout.closeDrawers();
                return;
            }
        }

        super.onBackPressed();
    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        @SuppressLint("RestrictedApi")
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    protected abstract int getView();

    protected abstract void onCreate();

    //region Base View imp
    public void showLoading() {
        dialog = Methods.getDialogLoading(this);
    }

    public void hideLoading() {
        dialog.dismiss();
    }

    public void showErrorMessage(int message) {
        showErrorMessage(getString(message));
    }

    public void showErrorMessage(String message) {
        Methods.getSimpleDialog(this, message);
    }
    //endregion

    protected void onChangeKeyboard(CallbackKeyboard callback){

        //final View activityRootView = findViewById(android.R.id.content);
        final View activityRootView = findViewById(R.id.coordinator);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

            int diff = (getWindowManager().getDefaultDisplay().getHeight() - rect.top) -
                    activityRootView.getHeight();

            /*Log.e(TAG, "onChangeKeyboard: ==" + diff);*/
            /*Log.e(TAG, "onChangeKeyboard: ->" + min);*/

            if (min == 0) {
                min = diff;
                return;
            }

            /*if (min != diff) {
                callback.onShowKeyboard();

                if (min > diff) {
                    callback.onHideKeyboard();
                }
            }
            else {
                callback.onHideKeyboard();
            }*/

            if (min == diff) {
                callback.onShowKeyboard();
            }
            else {
                callback.onHideKeyboard();
            }

        });
    }

    public interface CallbackKeyboard {

        void onHideKeyboard();

        void onShowKeyboard();
    }

}
