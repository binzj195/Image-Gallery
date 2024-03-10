package com.example.imagesgallery.Adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.example.imagesgallery.R;

import java.util.ArrayList;

public class SlideshowPagerAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<String> imagePaths;

    public SlideshowPagerAdapter(Context context, ArrayList<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.slideshow_item, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);

        // Load and display the image using Glide or another image-loading library
        Glide.with(context).load(imagePaths.get(position)).into(imageView);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}

