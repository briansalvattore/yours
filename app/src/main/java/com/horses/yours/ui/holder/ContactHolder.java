package com.horses.yours.ui.holder;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.horses.yours.R;
import com.horses.yours.business.model.ContactSimpleEntity;
import com.horses.yours.util.Methods;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Brian Salvattore
 */
public class ContactHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.linear)
    protected LinearLayout linear;

    @BindView(R.id.circle)
    protected ImageView circle;

    @BindView(R.id.name)
    protected TextView name;

    @BindView(R.id.description)
    protected TextView number;

    private int[] colors = new int[]{ R.color.md_red_500, R.color.md_purple_500, R.color.md_indigo_500,
            R.color.md_teal_500, R.color.md_green_500, R.color.md_orange_500};

    private ContactSimpleEntity data;

    public static ContactHolder init(ViewGroup parent) {
        return new ContactHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_invite, parent, false));
    }

    public ContactHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        Methods.addRipple(R.drawable.ripple_grey, linear);
    }

    public void setData(ContactSimpleEntity data) {
        this.data = data;
    }

    public void invalidate() {
        number.setText(data.getNumber());
        name.setText(data.getName());

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .fontSize(50)
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(data.getName().substring(0, 1), ContextCompat.getColor(circle.getContext(), colors[new Random().nextInt(6)]));

        circle.setImageDrawable(drawable);
    }
}
