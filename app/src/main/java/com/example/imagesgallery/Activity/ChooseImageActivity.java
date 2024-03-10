package com.example.imagesgallery.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.imagesgallery.Adapter.ChooseImageAdapter;
import com.example.imagesgallery.Interface.ClickListener;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ChooseImageActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageButton btnAddImages;
    RecyclerView recyclerView;
    ArrayList<Image> imageArrayList, addedImagesArrayList;
    ChooseImageAdapter chooseImageAdapter;
    Album album;
    boolean isLoading = false, isAllItemsLoaded = false;
    private int CurrentMaxPosition = 0;
    private final int ItemsPerLoading = 21;
    int clickPosition = -1;
    ClickListener clickListener = new ClickListener() {
        @Override
        public void click(int index) {
            clickPosition = index;
            if (chooseImageAdapter.isInMultiSelectMode()) { // if this activity is in multi select mode
                // set chosen image checked
                chooseImageAdapter.toggleSelection(index);
                chooseImageAdapter.notifyDataSetChanged();
            } else { // if this activity is not in multi select mode
                int action = getIntent().getIntExtra("action", 0);
                ContentValues contentValues = new ContentValues();
                long rowID = 0;

                // change database
                if (action == AlbumInfoActivity.ACTION_ADD_IMAGE) { // if user choose adding an image to album
                    contentValues.put("id_album", album.getId());
                    contentValues.put("path", imageArrayList.get(index).getPath());
                    rowID = MainActivity.db.insert("Album_Contain_Images", null, contentValues);
                } else if (action == AlbumInfoActivity.ACTION_CHANGE_COVER) { // if user choose changing cover
                    contentValues.put("cover", imageArrayList.get(index).getPath());
                    String[] args2 = {String.valueOf(album.getId())};
                    rowID = MainActivity.db.update("Album", contentValues, "id_album = ?", args2);
                }

                if (rowID > 0) {
                    finishActivity();
                } else {
                    // show error
                    if (action == AlbumInfoActivity.ACTION_CHANGE_COVER) {
                        Toast.makeText(ChooseImageActivity.this, "Change cover failed", Toast.LENGTH_SHORT).show();
                    } else if (action == AlbumInfoActivity.ACTION_ADD_IMAGE) {
                        Toast.makeText(ChooseImageActivity.this, "Add image to album failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        @Override
        public void longClick(int index) {
            int action = getIntent().getIntExtra("action", 0);
            if (action == AlbumInfoActivity.ACTION_ADD_IMAGE) { // Enter multi-select mode if user choose adding images to album
                chooseImageAdapter.setMultiSelectMode(true);
                enterMultiselectMode(index);
            }
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);

        init();

        isAllItemsLoaded = false;
        CurrentMaxPosition = 0;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthInDp = displayMetrics.widthPixels / displayMetrics.density;
        int imageWidth = 110; // size of an image
        int desiredColumnCount = (int)screenWidthInDp / imageWidth; // the number of images in a row

        album = (Album) getIntent().getSerializableExtra("album");
        imageArrayList = new ArrayList<>();
        addedImagesArrayList = new ArrayList<>();
        chooseImageAdapter = new ChooseImageAdapter(ChooseImageActivity.this, imageArrayList, clickListener);
        recyclerView.setLayoutManager(new GridLayoutManager(ChooseImageActivity.this, desiredColumnCount));
        recyclerView.setAdapter(chooseImageAdapter);
        loadImages();
        chooseImageAdapter.notifyDataSetChanged();

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Choose Image");

        // load first images
        loadImages();

        // set return button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!chooseImageAdapter.isInMultiSelectMode()) {
                    finish(); // return to album tab
                } else {
                    // cancel multi select mode
                    exitMultiselectMode();
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && gridLayoutManager != null && gridLayoutManager.findLastCompletelyVisibleItemPosition() >= imageArrayList.size() - 1 && !isAllItemsLoaded) {
                    isLoading = true;
                    // Create an executor that executes tasks in the main thread and background thread
                    Executor mainExecutor = ContextCompat.getMainExecutor(ChooseImageActivity.this);
                    ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
                    // Load data in the background thread.
                    backgroundExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            loadImages();
                            // Update list images in a album on the main thread
                            mainExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    chooseImageAdapter.notifyDataSetChanged();
                                    isLoading = false;
                                }
                            });
                        }
                    });
                }
            }
        });

        btnAddImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addImagesToAlbum();
            }
        });
    }

    private void addImagesToAlbum() {
        if (chooseImageAdapter.getSelectedImages().size() == 0) {
            Toast.makeText(this, "You have not chosen any images", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Image> selectedImages = chooseImageAdapter.getSelectedImages();

        // change database
        MainActivity.db.beginTransaction();
        try {
            for (int i = 0; i < selectedImages.size(); i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("id_album", album.getId());
                contentValues.put("path", selectedImages.get(i).getPath());
                MainActivity.db.insert("Album_Contain_Images", null, contentValues);
            }
            MainActivity.db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Toast.makeText(this, "Insert failed", Toast.LENGTH_SHORT).show();
        } finally {
            MainActivity.db.endTransaction();
            finishActivity();
        }
    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.listImageToAdd);
        btnAddImages = (ImageButton) findViewById(R.id.btnAddImages_album);
    }

    private void finishActivity() {
        if (!chooseImageAdapter.isInMultiSelectMode()){ // if user only choose 1 image in normal mode
            Intent resultIntent = new Intent();
            resultIntent.putExtra("image", imageArrayList.get(clickPosition));
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else { // if user only choose images in multi select mode
            ArrayList<Image> selectedImages = chooseImageAdapter.getSelectedImages();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedImages", selectedImages);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }

    private void loadImages() {
        Log.d("aaaaa", "before: " + imageArrayList.size());

        String sql = "SELECT * FROM Image LIMIT ? OFFSET ?";
        String[] args = {String.valueOf(ItemsPerLoading), String.valueOf(CurrentMaxPosition)};
        Cursor cursor = null;
        try {
            cursor = MainActivity.db.rawQuery(sql, args);
        } catch (Exception exception) {
            Log.d("aaaa", exception.getMessage().toString());
            return;
        }

        if (!cursor.moveToFirst()) {
            isAllItemsLoaded = true;
        }
        cursor.moveToPosition(-1);

        while (cursor.moveToNext()) {
            int pathColumnIndex = cursor.getColumnIndex("path");
            int descriptionColumnIndex = cursor.getColumnIndex("description");
            int favorColumnIndex = cursor.getColumnIndex("isFavored");

            String path = cursor.getString(pathColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            int isFavored = cursor.getInt(favorColumnIndex);
            imageArrayList.add(new Image(path, description, isFavored));
        }

        int action = getIntent().getIntExtra("action", 0);
        if (action == AlbumInfoActivity.ACTION_ADD_IMAGE) {
            for (int i = 0; i < imageArrayList.size(); i++) {
                for (int j = 0; j < album.getListImage().size(); j++) {
                    if (imageArrayList.get(i).getPath().equals(album.getListImage().get(j).getPath())) {
                        imageArrayList.get(i).setCanAddToCurrentAlbum(false);
                    }
                }
            }
        }
        cursor.close();
        CurrentMaxPosition += ItemsPerLoading;
        Log.d("aaaaa", "after: " + imageArrayList.size());
    }

    private void exitMultiselectMode() {
        chooseImageAdapter.setMultiSelectMode(false);
        chooseImageAdapter.clearSelection();
        changeUI();
    }

    private void enterMultiselectMode(int index) {
        chooseImageAdapter.setMultiSelectMode(true);
        chooseImageAdapter.toggleSelection(index);
        changeUI();
    }

    private void changeUI() {
        if (chooseImageAdapter.isInMultiSelectMode()) {
            Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.close_icon);
            btnAddImages.setVisibility(View.VISIBLE);
        } else {
            Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            btnAddImages.setVisibility(View.GONE);
        }
    }
}