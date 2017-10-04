package com.horses.yours.ui.fragment;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.horses.yours.R;
import com.horses.yours.business.model.LastEntity;
import com.horses.yours.business.model.UserEntity;
import com.horses.yours.ui.base.BaseFragment;
import com.horses.yours.ui.view.ContactView;
import com.horses.yours.util.Methods;
import com.horses.yours.util.SimpleValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.paperdb.Book;
import io.paperdb.Paper;

public class ChatsFragment extends BaseFragment {

    private static final String TAG = ChatsFragment.class.getSimpleName();

    @BindView(R.id.help)
    protected TextView help;

    @BindView(R.id.linear)
    protected LinearLayout linear;

    protected UserEntity me = Paper.book().read("user");
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private List<ContactView> chats = new ArrayList<>();

    private Map<DatabaseReference, ValueEventListener> eventListenerMap = new HashMap<>();

    @Override
    protected int getFragmentView() {
        return R.layout.fragment_chats;
    }

    @Override
    protected void onCreate() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) help.getLayoutParams();
        params.height = Methods.getHeightScreen() - Methods.toPixels(85);
        help.setLayoutParams(params);
    }

    @Override
    public void onResume() {
        super.onResume();
        fillChats();
    }

    @Override
    public void onPause() {
        super.onPause();
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : eventListenerMap.entrySet()) {
            entry.getKey().removeEventListener(entry.getValue());
        }
    }

    @SuppressWarnings({"unchecked", "RedundantCast"})
    private void fillChats() {

        Book inbox = Paper.book("inbox");

        List<String> keys = inbox.getAllKeys();

        if (!keys.isEmpty()) {
            help.setVisibility(View.GONE);
            linear.removeAllViews();
            chats = new ArrayList<>();
        }

        for (String key : keys) {

            HashMap<String, Object> map = (HashMap<String, Object>) inbox.read(key);

            ContactView view = new ContactView(getActivity());
            view.setData(map);
            view.setKey(key);
            view.invalidate(this::updateList);

            chats.add(view);
            linear.addView(view);
        }

        UserEntity me = Paper.book().read("user");

        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(me.getKey())
                .addListenerForSingleValueEvent(new SimpleValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        JsonObject data = (JsonObject) new JsonParser().parse(new Gson().toJson(dataSnapshot.getValue()));

                        //region Clear cache
                        if (data.get("inbox") == null) {
                            for (String key : Paper.book("inbox").getAllKeys()) {
                                Paper.book("inbox").delete(key);
                            }

                            help.setVisibility(View.VISIBLE);
                            linear.removeAllViews();
                            return;
                        }
                        //endregion

                        List<String> current = new ArrayList<>();

                        for (String key : getKeys((Map) dataSnapshot.getValue())) {
                            Log.d(TAG, "onDataChange() called with: key = [" + key + "]");

                            JsonObject item = data.get("inbox").getAsJsonObject().get(key).getAsJsonObject();

                            current.add(key);

                            if (!inbox.exist(key)) {

                                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                                Map<String, Object> first = new Gson().fromJson(item, type);
                                HashMap<String, Object> map = new HashMap<String, Object>(first);

                                Paper.book("inbox").write(key, map);

                                ContactView view = new ContactView(getActivity());
                                view.setData(map);
                                view.invalidate(lastEntity -> updateList(lastEntity));

                                linear.addView(view);

                                help.setVisibility(View.GONE);
                            }
                        }

                        //region Print new chats
                        /*for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            current.add(snapshot.getKey());

                            if (!inbox.exist(snapshot.getKey())) {

                                HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getValue();

                                Paper.book("inbox").write(snapshot.getKey(), map);

                                ContactView view = new ContactView(getActivity());
                                view.setData(map);
                                view.invalidate(lastEntity -> updateList(lastEntity));

                                linear.addView(view);

                                help.setVisibility(View.GONE);
                            }
                        }*/
                        //endregion

                        //region Verify is deleted
                        verifyIfDeleted:
                        for (String key : Paper.book("inbox").getAllKeys()) {
                            Log.d(TAG, "onDataChange() called with: paper = [" + key + "]");
                            Log.d(TAG, "onDataChange() called with: indexOf = [" + current.indexOf(key) + "]");

                            if (current.indexOf(key) == -1) {
                                Paper.book("inbox").delete(key);

                                for (ContactView view : chats) {
                                    if (view.getKey().equals(key)) {
                                        linear.removeView(view);
                                        continue verifyIfDeleted;
                                    }
                                }
                            }
                        }
                        //endregion
                    }
                });
    }

    private List<String> getKeys(Map snap) {

        List<String> newKeys = new ArrayList<>();

        try {
            Iterator<String> keys = ((JSONObject) new JSONObject(snap).get("inbox")).keys();

            while (keys.hasNext()) {
                newKeys.add(keys.next());
            }
        }
        catch (JSONException e) {
            Log.wtf(TAG, "getKeys: ", e);
        }
        finally {
            return newKeys;
        }
    }

    private void updateList(ContactView.LastEntity lastEntity) {

        DatabaseReference eventRef = ref.child("chats").child(lastEntity.getKey()).child("last");
        ValueEventListener eventListener = new SimpleValueEventListener(){
            @SuppressWarnings("unchecked")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null) return;
                if (dataSnapshot.getValue() == null) return;

                LastEntity last = dataSnapshot.getValue(LastEntity.class);

                if (me.getKey().equals(last.getKey())) {
                    Log.d(TAG, "onDataChange() called with: dataSnapshot = [Yo: " + last.getMessage() + "]");
                    lastEntity.getContactView().setLast(getString(R.string.title_me) + ": " + last.getMessage());
                }
                else {
                    Log.d(TAG, "onDataChange() called with: dataSnapshot = [" + last.getMessage() + "]");
                    lastEntity.getContactView().setLast(last.getName().split(" ")[0] + ": " + last.getMessage());
                }
            }
        };
        eventRef.addValueEventListener(eventListener);
        eventListenerMap.put(eventRef, eventListener);
    }
}
