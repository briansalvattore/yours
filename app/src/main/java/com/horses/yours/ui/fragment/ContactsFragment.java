package com.horses.yours.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.horses.yours.R;
import com.horses.yours.business.model.ContactSimpleEntity;
import com.horses.yours.business.model.NumberEntity;
import com.horses.yours.ui.activity.GroupActivity;
import com.horses.yours.ui.base.BaseFragment;
import com.horses.yours.ui.view.InviteView;
import com.horses.yours.util.Methods;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.paperdb.Paper;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ContactsFragment extends BaseFragment {

    private static final String TAG = ContactsFragment.class.getSimpleName();

    @BindView(R.id.contacts)
    protected LinearLayout cache;

    @BindView(R.id.group)
    protected LinearLayout group;

    @BindView(R.id.linear)
    protected LinearLayout linear;

    @BindView(R.id.permissions)
    protected LinearLayout permission;

    @BindView(R.id.title)
    protected AutoLinkTextView title;

    @BindView(R.id.request)
    protected Button request;

    private int point = 0;
    private RunnableContacts runnableContacts;
    private Handler handler = new Handler();

    @Override
    protected int getFragmentView() {
        return R.layout.fragment_contacts;
    }

    @Override
    protected void onCreate() {
        Methods.addRipple(R.drawable.ripple_primary, request);
        Methods.addRipple(R.drawable.ripple_grey, group);

        title.setCustomModeColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        title.setSelectedStateColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        title.setCustomRegex(getString(R.string.regex_permissions));
        title.addAutoLinkMode(AutoLinkMode.MODE_CUSTOM);
        title.setAutoLinkText(getString(R.string.title_need_permissions));

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) permission.getLayoutParams();
        params.height = Methods.getHeightScreen() - Methods.toPixels(85);
        permission.setLayoutParams(params);

        linear.setVisibility(View.GONE);
        group.setVisibility(View.GONE);

        initFromCache();
    }

    private void initFromCache() {

        for (String key : Paper.book("numbers").getAllKeys()) {

            cache.setVisibility(View.VISIBLE);
            group.setVisibility(View.VISIBLE);

            ContactSimpleEntity contact = Paper.book("numbers").read(key);

            InviteView view = new InviteView(getActivity());
            view.setData(contact);
            view.setEntity(contact.getNumberEntity());
            view.noInvite();
            view.invalidate();

            cache.addView(view);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Paper.book().exist("contacts")) {

            List<ContactSimpleEntity> contact = Paper.book().read("contacts");

            if (!contact.isEmpty()) {
                fillContacts(contact);
            }
        }

        /*if (linear.getChildCount() > 1) {
            for (int i = 1; i < linear.getChildCount(); i++) {
                linear.removeView(linear.getChildAt(i));
            }
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnableContacts);
    }

    @OnClick(R.id.request)
    protected void allowContacts() {
        ContactsFragmentPermissionsDispatcher.requestContactsWithCheck(this);
    }

    @OnClick(R.id.group)
    protected void newGroup() {
        startActivity(GroupActivity.class);
    }

    @SuppressLint("StaticFieldLeak")
    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    protected void requestContacts() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .content(R.string.dialog_connecting)
                .progress(true, 0)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();

        ContentResolver contentResolver = getActivity().getContentResolver();

        new AsyncTask<ContentResolver, Void, List<ContactSimpleEntity>>(){

            @SuppressWarnings({"ConstantConditions", "LoopStatementThatDoesntLoop"})
            @SuppressLint("Recycle")
            @Override
            protected List<ContactSimpleEntity> doInBackground(ContentResolver... contentResolvers) {

                ContentResolver contentResolver =  contentResolvers[0];Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                List<ContactSimpleEntity> contacts = new ArrayList<>();

                do {
                    try {
                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                        if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                            Cursor pCur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{ id }, null);

                            while (pCur.moveToNext()) {

                                String contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                String contactAccount = pCur.getString(pCur.getColumnIndex("account_name"));

                                contactNumber = contactNumber.replace("-", " ").replace("_", " ").replace(" ", " ");

                                if(contactNumber.length() > 7) {

                                    ContactSimpleEntity contact = new ContactSimpleEntity();
                                    contact.setNumber(contactNumber);
                                    contact.setName(contactName);
                                    contact.setAccount(contactAccount);

                                    contacts.add(contact);
                                }

                                break;
                            }
                            pCur.close();
                        }
                    } catch (Exception ignore) { }

                }
                while (cursor.moveToNext()) ;

                Collections.sort(contacts, (t0, t1) -> t0.getName().toLowerCase().compareTo(t1.getName().toLowerCase()));

                return contacts;
            }

            @Override
            protected void onPostExecute(List<ContactSimpleEntity> contacts) {
                Paper.book().write("contacts", contacts);
                dialog.dismiss();
                fillContacts(contacts);
            }
        }.execute(contentResolver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ContactsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressWarnings("unchecked")
    private void fillContacts(List<ContactSimpleEntity> contacts) {
        runnableContacts = new RunnableContacts(contacts);
        handler.postDelayed(runnableContacts, 500);

        linear.setVisibility(View.VISIBLE);
        group.setVisibility(View.VISIBLE);
        permission.setVisibility(View.GONE);
    }

    private class RunnableContacts implements Runnable {

        private final int max = 10;
        private List<ContactSimpleEntity> contacts;

        @SuppressWarnings("WeakerAccess")
        public RunnableContacts(List<ContactSimpleEntity> contacts) {
            this.contacts = contacts;
        }

        @Override
        public void run() {

            if (point > contacts.size()) {
                handler.removeCallbacks(this);
                return;
            }

            for (int i = point; i < point + max; i++) {

                if (i >= contacts.size()) continue;

                ContactSimpleEntity contact = contacts.get(i);

                if (contact.getNumber().replace("+", "").replace(" ", "").isEmpty()) continue;

                InviteView view = new InviteView(getActivity());
                view.setData(contact);
                view.setOnClickSendSms(number ->
                        ContactsFragmentPermissionsDispatcher.sendInviteSmsWithCheck(ContactsFragment.this, number));
                //view.setToken(user.getAccessToken());
                view.invalidate();

                List<NumberEntity> list = Paper.book("firebase").read("numbers", new ArrayList<NumberEntity>());

                boolean isCache = false;

                Log.d(TAG, "run() called point=[" + point + "], name=[" + contact.getName() + "], number=[" + contact.getNumber() + "]");

                for (NumberEntity entity : list) {

                    String a = contact.getNumber().replace("+", "").replace(" ", "");
                    a = a.substring(a.length() - 8, a.length());

                    String b = entity.getNumber().replace("+", "").replace(" ", "");
                    b = b.substring(b.length() - 8, b.length());

                    if (a.equals(b)) {

                        contact.setNumberEntity(entity);

                        if (!Paper.book("numbers").exist(a)) {
                            Paper.book("numbers").write(a, contact);
                            view.noInvite();
                            view.setEntity(entity);
                            cache.addView(view);
                            cache.setVisibility(View.VISIBLE);
                        }

                        isCache = true;
                    }

                }

                if (!isCache) linear.addView(view);
            }

            handler.postDelayed(this, 1000);
            point = point + max;
        }
    }

    @NeedsPermission(Manifest.permission.SEND_SMS)
    protected void sendInviteSms(String number) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number));
        intent.putExtra("sms_body", "Try Your's in your smartphone. Download here www...");
        startActivity(intent);
    }
}
