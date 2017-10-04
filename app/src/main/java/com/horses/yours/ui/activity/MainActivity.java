package com.horses.yours.ui.activity;

import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.horses.yours.R;
import com.horses.yours.business.model.UserEntity;
import com.horses.yours.ui.application.MainApp;
import com.horses.yours.ui.base.BaseActivity;
import com.horses.yours.ui.fragment.ChatsFragment;
import com.horses.yours.ui.fragment.ContactsFragment;

import io.paperdb.Paper;

public class MainActivity extends BaseActivity implements BaseActivity.SimpleNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected int getView() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate() {
        setSupportActionBar(getString(R.string.app_name));
        setupDrawer();
        setupDrawerContent(this);

        setTitle(R.string.title_chats);
        addFragment(new ChatsFragment());

        UserEntity user = Paper.book().read("user");
        Log.i(TAG, "user: " + user.toString());
    }

    @Override
    public void onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chats:
                if (current instanceof ChatsFragment)
                    break;
                setTitle(R.string.title_chats);
                replaceFragment(new ChatsFragment());
                break;
            case R.id.contacts:
                if (current instanceof ContactsFragment)
                    break;
                setTitle(R.string.title_contacts);
                replaceFragment(new ContactsFragment());
                break;
            case R.id.settings:
                /*if (current instanceof SettingsFragment)
                    break;
                setTitle(R.string.title_settings);
                replaceFragment(new SettingsFragment());*/
                startActivity(EditActivity.class);
                break;
            case R.id.close: {
                MainApp.destroyRooms();
                Paper.book().destroy();
                Paper.book("inbox").destroy();
                Paper.book("themes").destroy();
                Paper.book("firebase").destroy();
                Paper.book("numbers").destroy();
                /*Paper.book().write("session", false);*/

                FirebaseAuth.getInstance().signOut();

                MainApp.closeAll();
                startActivity(SplashActivity.class);
            }
        }
    }
}
