package com.horses.yours.ui.activity;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DatabaseReference;
import com.horses.yours.R;
import com.horses.yours.business.model.ContactSimpleEntity;
import com.horses.yours.business.model.NumberEntity;
import com.horses.yours.ui.base.BaseActivity;
import com.horses.yours.ui.view.InviteView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import io.paperdb.Paper;

public class GroupActivity extends BaseActivity {

    private static final String TAG = GroupActivity.class.getSimpleName();

    @BindView(R.id.name)
    protected EditText name;

    @BindView(R.id.contacts)
    protected LinearLayout cache;


    @Override
    protected int getView() {
        return R.layout.activity_group;
    }

    @Override
    protected void onCreate() {
        setSupportActionBar(R.string.title_new_group);
        initFromCache();
    }

    private void initFromCache() {

        for (String key : Paper.book("numbers").getAllKeys()) {

            cache.setVisibility(View.VISIBLE);

            ContactSimpleEntity contact = Paper.book("numbers").read(key);

            int n1 = contact.getNumber().replace(" ", "").length();
            int n2 = me.getNumber().replace(" ", "").length();

            if (contact.getNumber().replace(" ", "").substring(n1 - 6 , n1)
                    .equals(me.getNumber().replace(" ", "").substring(n2 - 6, n2))) continue;

            InviteView view = new InviteView(this);
            view.setData(contact);
            view.noInvite();
            view.invalidateForGroup();

            cache.addView(view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.check_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.check:
                createNewGroup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createNewGroup() {

        List<ContactSimpleEntity> contacts = new ArrayList<>();
        List<String> names = new ArrayList<>();

        /** update me */
        names.add(me.getFullname());

        ContactSimpleEntity meSimple = new ContactSimpleEntity();
        meSimple.setName(me.getFullname());
        meSimple.setNumberEntity(NumberEntity.add(me.getNumber(), me.getKey()));
        contacts.add(meSimple);

        for (int i = 1; i < cache.getChildCount(); i++) {
            InviteView view = (InviteView) cache.getChildAt(i);

            if ((int) view.getTag() == 1) {
                contacts.add(view.getData());
                names.add(view.getData().getName());
            }
        }

        if (contacts.size() == 1) {
            new MaterialDialog.Builder(this)
                    .title(R.string.dialog_ups)
                    .content(R.string.dialog_group_no)
                    .positiveText(R.string.dialog_ok)
                    .show();
            return;
        }

        Log.d(TAG, "createNewGroup() called");

        final String key = new Date().getTime() + "|" + me.getKey() + "|group";

        /** update map for group inbox */
        HashMap<String, Object> map = new HashMap<String, Object>(){{
            put("type", "group");
            put("room", name.getText().toString().isEmpty() ? StringUtils.join(names, ", ") : name.getText().toString());
            put("key", key);
        }};

        for (ContactSimpleEntity contact : contacts) {

            DatabaseReference refContact = ref.child("users").child(contact.getNumberEntity().getKey()).child("inbox").child(key);

            refContact.updateChildren(map);

            for (ContactSimpleEntity inner : contacts) {

                refContact.child("group").push().setValue(new HashMap<String, Object>(){{
                    put("number", inner.getNumberEntity().getNumber());
                    put("key", inner.getNumberEntity().getKey());
                    put("name", inner.getName());
                }});
            }

            /** init typing */
            ref.child("chats").child(key).child("participants").child(contact.getNumberEntity().getKey()).child("typing").setValue(false);
        }

        finish();
        startActivity(new Intent(this, Chat2Activity.class).putExtra("data", map));
    }
}
