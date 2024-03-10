package com.example.imagesgallery.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imagesgallery.Activity.AlbumInfoActivity;
import com.example.imagesgallery.Interface.ClickListener;
import com.example.imagesgallery.Activity.ImageInfoActivity;
import com.example.imagesgallery.Activity.MainActivity;
import com.example.imagesgallery.Fragment.ImageFragment;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> implements Filterable {
    MainActivity mainActivity;
    ImageFragment imageFragment = new ImageFragment();
    private Context context;
    private ArrayList<Image> images_list;

    private ArrayList<Image> images_listOld;

    //AT: check if there is selected mode
    private boolean isMultiSelectMode = false;
    private ArrayList<Integer> selectedPositions = new ArrayList<>();
    private ArrayList<String> selectedImages; // New list to track selected images

    public interface SelectionChangeListener {
        void onSelectionChanged(boolean hasSelection);
    }

    private SelectionChangeListener selectionChangeListener;

    public void setSelectionChangeListener(SelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }
    //AT


    public ArrayList<Image> getImages_list() {
        return images_list;
    }

    public void setImages_list(ArrayList<Image> images_list) {
        this.images_list = images_list;
    }

    ClickListener listener;
    public static int DELETE_REQUEST_CODE = 15;
    public int pos = 0;

    int getPos() {
        return this.pos;
    }

    //AT: set multi select mode
    public void setMultiSelectMode(boolean multiSelectMode) {
        this.isMultiSelectMode = multiSelectMode;
    }

    public boolean isInMultiSelectMode(){
        return isMultiSelectMode;
    }

    public ArrayList<String> getSelectedImages() {
        return selectedImages;
    }

    public ArrayList<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public void setSelectedPositions(ArrayList<Integer> selectedPositions) {
        this.selectedPositions = selectedPositions;
    }

    public void setSelectedImages(ArrayList<String> selectedImages) {
        this.selectedImages = selectedImages;
    }

    //AT

    public ImageAdapter(Context context, ArrayList<Image> images_list, ClickListener listener) {
        this.context = context;
        this.images_list = images_list;
        this.listener = listener;
        this.selectedImages = new ArrayList<>(); //For multi select
        this.images_listOld = images_list;
    }

    public void addImage(Image image) {
        this.images_list.add(image);
    }
    public void removeImage(int pos) { this.images_list.remove(pos);}

    //  AT Toggle the selection state of an item at the given position
    public void toggleSelection(int position) {
        String imagePath = images_list.get(position).getPath();
        if (selectedImages.contains(imagePath)) {
            selectedImages.remove(imagePath);
        } else {
            selectedImages.add(imagePath);
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

    //AT Clear the selection
    public void clearSelection() {
        selectedImages.clear();
        selectedPositions.clear();
        notifyDataSetChanged(); // Update the UI to clear selection
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(false);
        }
    }
    //AT

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File image_file = new File(images_list.get(position).getPath());
        if (image_file.exists()) {
            Glide.with(context).load(image_file).into(holder.image);
//            Log.d("imgAdapter",  String.valueOf(position));
        }

        //AT
        // Check if the item is selected and update its appearance
        boolean isSelected = selectedImages.contains(images_list.get(position));
        holder.itemView.setSelected(isSelected);
        // Initially set the checkbox visibility to GONE
    /*      if (isMultiSelectMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(true);
        }*/
        if (selectedPositions.contains(position)) {
;            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);
        }
        //AT

//        imageFragment.updateDeleteButtonState(selectedImages.size() > 0);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the position of the image
                int position = holder.getAdapterPosition();
                listener.click(position);

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
                listener.longClick(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return images_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;

        private CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            image = itemView.findViewById(R.id.gallery_item);
        }
    }
    //Search Image Exif
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String txtSearch = constraint.toString();
                if(txtSearch.isEmpty()){
                    images_list = images_listOld;
                }
                else {
                    ArrayList<Image> list = new ArrayList<>();
                    for (Image image : images_listOld) {
                        //Lay ngay
                        Date currentDate = image.getDate();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
                        String formattedDay ="Day" + " " + dateFormat.format(currentDate);

                        //Lay thang
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(currentDate);
                        int month = calendar.get(Calendar.MONTH);
                        month = month + 1;
                        String formattedMonth = "Month" + " " + month;

                        //Lay nam
                        int year = calendar.get(Calendar.YEAR);
                        String formattedYear = "Year" + " " + year;

                        //Load size
                        long size = image.getSize();
                        String formattedSize="Size"+ " " + size;

                        //Load type
                        String type = image.getType();
                        String formattedType ="Type"+ " "+ type;

                        if (formattedDay.toLowerCase().contains(txtSearch.toLowerCase()) ||
                                formattedMonth.toLowerCase().contains(txtSearch.toLowerCase()) ||
                                formattedYear.toLowerCase().contains(txtSearch.toLowerCase()) ||
                                formattedSize.toLowerCase().contains(txtSearch.toLowerCase()) ||
                                formattedType.toLowerCase().contains(txtSearch.toLowerCase())) {
                            list.add(image);
                        }
                    }
                    images_list = list;

                }
                /*int count = images_list.size();
                Intent intent = new Intent(context,ImageInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("CountImage", count);
                intent.putExtras(bundle);
                context.startActivity(intent);*/

                FilterResults filterResults = new FilterResults();
                filterResults.values = images_list;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                images_list = (ArrayList<Image>) results.values;
                notifyDataSetChanged();


            }
        };
    }
}
