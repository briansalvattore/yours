package com.horses.yours.util;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.horses.yours.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Brian Salvattore
 */
public class DisplayListener extends SimpleImageLoadingListener {

    static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<>());

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        if (loadedImage != null) {
            ImageView imageView = (ImageView) view;
            boolean firstDisplay = !displayedImages.contains(imageUri);
            if (firstDisplay) {
                FadeInBitmapDisplayer.animate(imageView, 500);
                displayedImages.add(imageUri);
            }
        }
    }

    public static final DisplayImageOptions fullImage = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.background_shadow)
            .showImageForEmptyUri(R.drawable.background_shadow)
            .showImageOnFail(R.drawable.background_shadow)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();

    public static final DisplayImageOptions outSaveImage = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.background_shadow)
            .showImageForEmptyUri(R.drawable.background_shadow)
            .showImageOnFail(R.drawable.background_shadow)
            .cacheInMemory(false)
            .cacheOnDisk(false)
            .build();
}
