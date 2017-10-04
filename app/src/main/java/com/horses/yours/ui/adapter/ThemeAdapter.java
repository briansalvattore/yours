package com.horses.yours.ui.adapter;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.horses.yours.ui.holder.ThemeHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Brian Salvattore
 */
public class ThemeAdapter extends RecyclerView.Adapter<ThemeHolder> {

    private List<String> backs = new ArrayList<>();
    private OnItemClickListener listener;

    public ThemeAdapter() {
        backs = Collections.emptyList();
    }

    public void setList(List<String> backs) {
        this.backs = backs;
    }

    @Override
    public ThemeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ThemeHolder.init(parent);
    }

    @Override
    public void onBindViewHolder(ThemeHolder holder, int position) {
        holder.setPath(backs.get(position));
        holder.setOnClickListener(path -> listener.done(path));
        holder.invalidate();

    }

    @Override
    public int getItemCount() {
        return backs.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void done(String path);
    }
}
