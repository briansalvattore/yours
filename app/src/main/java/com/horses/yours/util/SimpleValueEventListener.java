package com.horses.yours.util;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * @author Brian Salvattore
 */
public class SimpleValueEventListener implements ValueEventListener {
    private static final String TAG = SimpleValueEventListener.class.getSimpleName();
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.wtf(TAG, "onCancelled: ", databaseError.toException());
    }
}
