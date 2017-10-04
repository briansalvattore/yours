package com.horses.yours.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.horses.yours.business.model.ContactSimpleEntity;
import com.horses.yours.ui.holder.ContactHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Salvattore
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {

    private static final String TAG = ContactAdapter.class.getSimpleName();

    private List<ContactSimpleEntity> list = new ArrayList<>();

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ContactHolder.init(parent);
    }

    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        holder.setData(list.get(position));
        holder.invalidate();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<ContactSimpleEntity> list) {
        this.list = list;
    }
}
