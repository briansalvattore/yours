package com.horses.yours.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.horses.yours.R;
import com.horses.yours.business.model.ContactSimpleEntity;
import com.horses.yours.business.model.NumberEntity;
import com.horses.yours.business.model.UserEntity;
import com.horses.yours.ui.activity.Chat2Activity;
import com.horses.yours.util.Methods;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

/**
 * @author Brian Salvattore
 */
public class InviteView extends LinearLayout {

    private static final String TAG = InviteView.class.getSimpleName();

    final int marginHorizontal = Methods.toPixels(20);
    final int marginVertical = Methods.toPixels(8);

    @BindView(R.id.linear)
    protected LinearLayout linear;

    @BindView(R.id.circle)
    protected ImageView circle;

    @BindView(R.id.name)
    protected TextView name;

    @BindView(R.id.description)
    protected TextView number;

    @BindView(R.id.invite)
    protected TextView invite;

    private CallbackSms callbackSms;

    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    private int[] colors = new int[]{ R.color.md_red_500, R.color.md_purple_500, R.color.md_indigo_500,
            R.color.md_teal_500, R.color.md_green_500, R.color.md_orange_500};

    private ContactSimpleEntity data;
    private NumberEntity entity;

    public InviteView(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical);
        setLayoutParams(params);

        inflate(getContext(), R.layout.view_invite, this);

        ButterKnife.bind(this, this);

        Methods.addRipple(R.drawable.ripple_grey, linear);
    }

    public void setOnClickSendSms(CallbackSms callbackSms) {
        this.callbackSms = callbackSms;
    }

    public void setData(ContactSimpleEntity data) {
        this.data = data;
    }

    @Override
    public void invalidate() {
        name.setText(data.getName());

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .fontSize(50)
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(data.getName().substring(0, 1), ContextCompat.getColor(getContext(), colors[new Random().nextInt(6)]));

        circle.setImageDrawable(drawable);

        number.setText(data.getNumber());

        if (callbackSms != null) {
            invite.setOnClickListener(view -> callbackSms.done(data.getNumber()));
        }

        linear.setOnClickListener(view -> {

            if (invite.getVisibility() == View.VISIBLE) return;

            MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                    .title(R.string.dialog_connecting)
                    .content(R.string.dialog_wait)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();

            UserEntity me = Paper.book().read("user");

            ref.child("chats").child("ref").child(me.getKey() + "|" + entity.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    dialog.dismiss();

                    String key = (String) dataSnapshot.getValue();

                    if (key == null) {

                        key = new Date().getTime() + "|" + me.getKey() + "|" + entity.getKey();

                        ref.child("chats").child("ref").child(me.getKey() + "|" + entity.getKey()).setValue(key);
                        ref.child("chats").child("ref").child(entity.getKey() + "|" + me.getKey()).setValue(key);
                    }

                    Log.d(TAG, "onDataChange() called with: key = [" + key + "]");

                    final String finalKey = key;

                    /** update receiver inbox */
                    ref.child("users").child(entity.getKey()).child("inbox").child(finalKey)
                            .updateChildren(new HashMap<String, Object>(){{
                                put("type", "single");
                                put("key", finalKey);
                                put("receiver", new HashMap<String, Object>(){{
                                    put("number", me.getNumber());
                                    put("key", me.getKey());
                                    put("name", me.getFullname());
                                }});
                    }});

                    /** update me(sender) inbox */
                    HashMap<String, Object> map = new HashMap<String, Object>(){{
                        put("type", "single");
                        put("key", finalKey);
                        put("receiver", new HashMap<String, Object>(){{
                            put("number", entity.getNumber());
                            put("key", entity.getKey());
                            put("name", data.getName());
                        }});
                    }};
                    ref.child("users").child(me.getKey()).child("inbox").child(finalKey).updateChildren(map);

                    /** init typing */
                    ref.child("chats").child(finalKey).child("participants").child(me.getKey()).child("typing").setValue(false);
                    ref.child("chats").child(finalKey).child("participants").child(entity.getKey()).child("typing").setValue(false);

                    /** init chat with receiver */
                    getContext().startActivity(new Intent(getContext(), Chat2Activity.class).putExtra("data", map));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dialog.dismiss();
                    Toast.makeText(InviteView.this.getContext(), R.string.alert_try_again, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    public void noInvite() {
        invite.setVisibility(GONE);
    }

    public void invalidateForGroup() {
        setTag(0);
        name.setText(data.getName());

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .fontSize(50)
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(data.getName().substring(0, 1), ContextCompat.getColor(getContext(), colors[new Random().nextInt(6)]));

        circle.setImageDrawable(drawable);

        number.setText(data.getNumber());

        linear.setOnClickListener(view -> {
            if ((int) getTag() == 0) {
                setTag(1);
                circle.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.background_circle_check));
            } else {
                setTag(0);
                circle.setImageDrawable(drawable);
            }
        });
    }

    public ContactSimpleEntity getData() {
        return data;
    }

    public void setEntity(NumberEntity entity) {
        this.entity = entity;
    }

    public interface CallbackSms {
        void done(String number);
    }
}
