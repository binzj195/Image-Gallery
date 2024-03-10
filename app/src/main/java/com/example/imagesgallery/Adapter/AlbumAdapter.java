package com.example.imagesgallery.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.imagesgallery.Activity.AddFavoriteAlbumActivity;
import com.example.imagesgallery.Activity.AlbumInfoActivity;
import com.example.imagesgallery.Activity.FavoriteAlbumsActivity;
import com.example.imagesgallery.Activity.MainActivity;
import com.example.imagesgallery.Interface.ClickListener;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.io.Serializable;
import java.util.ArrayList;

public class AlbumAdapter extends BaseAdapter {
    Context context;
    ArrayList<Album> albumArrayList;
    private boolean isMultiSelectMode = false;
    private ArrayList<Integer> selectedPositions = new ArrayList<>();
    private ArrayList<Album> selectedAlbums;
    ClickListener clickListener;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ArrayList<Album> getAlbumArrayList() {
        return albumArrayList;
    }

    public void setAlbumArrayList(ArrayList<Album> albumArrayList) {
        this.albumArrayList = albumArrayList;
    }

    public AlbumAdapter(Context context, ArrayList<Album> albumArrayList, ClickListener clickListener) {
        this.context = context;
        this.albumArrayList = albumArrayList;
        this.clickListener = clickListener;
        selectedAlbums = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return albumArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public interface SelectionChangeListener {
        void onSelectionChanged(boolean hasSelection);
    }
    private AlbumAdapter.SelectionChangeListener selectionChangeListener;

    public void setSelectionChangeListener(AlbumAdapter.SelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    public void setMultiSelectMode(boolean multiSelectMode) {
        this.isMultiSelectMode = multiSelectMode;
    }

    public boolean isInMultiSelectMode() {
        return isMultiSelectMode;
    }

    public ArrayList<Album> getSelectedAlbums() {
        return selectedAlbums;
    }

    public ArrayList<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public void setSelectedPositions(ArrayList<Integer> selectedPositions) {
        this.selectedPositions = selectedPositions;
    }

    public void setSelectedImages(ArrayList<Album> selectedAlbums) {
        this.selectedAlbums = selectedAlbums;
    }

    public void toggleSelection(int position) {
        Album album = albumArrayList.get(position);
        if (selectedAlbums.contains(album)) {
            selectedAlbums.remove(album);
        } else {
            selectedAlbums.add(album);
        }

        if (selectedPositions.contains(position)) {
            selectedPositions.remove(Integer.valueOf(position));
        } else {
            selectedPositions.add(position);
        }

        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(!selectedAlbums.isEmpty());
        }
        notifyDataSetChanged(); // Update the UI to reflect the selection
    }

    public void clearSelection() {
        selectedAlbums.clear();
        selectedPositions.clear();
        notifyDataSetChanged(); // Update the UI to clear selection
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(false);
        }
    }

    private class ViewHolder {
        TextView AlbumName;
        ImageView AlbumCover;
        CheckBox checkBox;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.album_item, null);
            viewHolder.AlbumName = (TextView) view.findViewById(R.id.nameAlbum);
            viewHolder.AlbumCover = (ImageView) view.findViewById(R.id.imageCoverAlbum);
            viewHolder.checkBox = (CheckBox) view.findViewById(R.id.checkboxAlbum);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Album album = albumArrayList.get(i);
        String coverPath = album.getCover().getPath();
        if (coverPath.equals(MainActivity.pathNoImage)) {
            viewHolder.AlbumCover.setImageResource(R.drawable.no_image);
        } else {
            Glide.with(context).load(coverPath).into(viewHolder.AlbumCover);
        }
        viewHolder.AlbumName.setText(album.getName());

        // use these codes to avoid item in DefaultArray have same UI of item in SearchArray due to viewHolder
        viewHolder.AlbumCover.setEnabled(true);
        viewHolder.AlbumCover.setAlpha(1f);

        // check album the user choose in multi selection mode
        if (selectedPositions.contains(i)) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setChecked(true);
        } else {
            viewHolder.checkBox.setVisibility(View.GONE);
            viewHolder.checkBox.setChecked(false);
        }

        // mark albums that have already been in favorites
        if (context instanceof AddFavoriteAlbumActivity && album.getIsFavored() == 1) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setChecked(true);
            viewHolder.AlbumCover.setEnabled(false);
            viewHolder.AlbumCover.setAlpha(0.5f);
        }

        viewHolder.AlbumCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(i);
            }
        });

        viewHolder.AlbumCover.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clickListener.longClick(i);
                return true;
            }
        });

        return view;
    }
}
