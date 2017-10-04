package com.horses.yours.ui.holder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.horses.yours.R;
import com.horses.yours.ui.adapter.ThemeAdapter;
import com.horses.yours.util.DisplayListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

/**
 * @author Brian Salvattore
 */
@SuppressWarnings("WeakerAccess")
public class ThemeHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.image)
    protected ImageView image;

    @BindView(R.id.card)
    protected CardView card;

    private String path;
    private ThemeAdapter.OnItemClickListener listener;

    public static ThemeHolder init(ViewGroup parent) {
        return new ThemeHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_theme, parent, false));
    }

    public ThemeHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void invalidate() {

        ImageLoader
                .getInstance()
                .displayImage(
                        path,
                        image,
                        DisplayListener.fullImage,
                        new DisplayListener());

        card.setOnClickListener(view -> listener.done(path));
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setOnClickListener(ThemeAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
