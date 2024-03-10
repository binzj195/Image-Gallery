package com.example.imagesgallery.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imagesgallery.Interface.ClickListener;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.io.File;
import java.util.ArrayList;

public class ChooseImageAdapter extends RecyclerView.Adapter<ChooseImageAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Image> imageArrayList;
    ClickListener clickListener;

    public ChooseImageAdapter(Context context, ArrayList<Image> imageArrayList, ClickListener clickListener) {
        this.context = context;
        this.imageArrayList = imageArrayList;
        this.selectedImages = new ArrayList<>(); //For multi select
        this.clickListener = clickListener;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ArrayList<Image> getImageArrayList() {
        return imageArrayList;
    }

    public void setImageArrayList(ArrayList<Image> imageArrayList) {
        this.imageArrayList = imageArrayList;
    }

    private boolean isMultiSelectMode = false;
    private ArrayList<Integer> selectedPositions = new ArrayList<>();
    private ArrayList<Image> selectedImages; // New list to track selected images

    public interface SelectionChangeListener {
        void onSelectionChanged(boolean hasSelection);
    }

    private ImageAdapter.SelectionChangeListener selectionChangeListener;

    public void setSelectionChangeListener(ImageAdapter.SelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    public void setMultiSelectMode(boolean multiSelectMode) {
        this.isMultiSelectMode = multiSelectMode;
    }

    public boolean isInMultiSelectMode() {
        return isMultiSelectMode;
    }

    public ArrayList<Image> getSelectedImages() {
        return selectedImages;
    }

    public ArrayList<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public void setSelectedPositions(ArrayList<Integer> selectedPositions) {
        this.selectedPositions = selectedPositions;
    }

    public void setSelectedImages(ArrayList<Image> selectedImages) {
        this.selectedImages = selectedImages;
    }

    public void toggleSelection(int position) {
        Image image = imageArrayList.get(position);
        if (selectedImages.contains(image)) {
            selectedImages.remove(image);
        } else {
            selectedImages.add(image);
        }

        if (selectedPositions.contains(position)) {
            selectedPositions.remove(Integer.valueOf(position));
        } else {
            selectedPositions.add(position);
        }

        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(!selectedImages.isEmpty());
        }
        notifyDataSetChanged(); // Update the UI to reflect the selection
    }

    public void clearSelection() {
        selectedImages.clear();
        selectedPositions.clear();
        notifyDataSetChanged(); // Update the UI to clear selection
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int pos = position;
        File image_file = new File(imageArrayList.get(pos).getPath());
        if (image_file.exists()) {
            Glide.with(context).load(image_file).into(holder.imageView);
        }

        if (selectedPositions.contains(pos)) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);
        }

        // use these codes to avoid item in DefaultArray have same UI of item in SearchArray due to viewHolder
        holder.imageView.setEnabled(true);
        holder.imageView.setAlpha(1f);

        if (!imageArrayList.get(pos).isCanAddToCurrentAlbum()) {
            // disable image and change its appearance if it is in current album
            holder.imageView.setEnabled(false);
            holder.imageView.setAlpha(0.5f);
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(true);
        }
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                clickListener.click(position);
            }
        });

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
                clickListener.longClick(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.gallery_item);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
        }
    }

}
