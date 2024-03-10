package com.example.imagesgallery.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.imagesgallery.Adapter.ImageAdapter;
import com.example.imagesgallery.Interface.ClickListener;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class FavoriteImagesActivity extends AppCompatActivity {

    Toolbar toolbar;
    MainActivity mainActivity;

    RecyclerView recycler;
    ArrayList<Image> images;
    ImageAdapter adapter;
    ImageButton imageButtonReturnHome;

    LinearLayout linearLayout;

    int clickPosition = 0;
    private ActivityResultLauncher<Intent> startIntentSeeImageInfo;

    ClickListener clickListener = new ClickListener() {
        @Override
        public void click(int index) {
            clickPosition = index;
            if (adapter.isInMultiSelectMode()) {
                adapter.toggleSelection(index);
                // Notify the adapter that the data set has changed
                adapter.notifyDataSetChanged();
                // Show the checkbox only for the clicked image and set it to true
            }

            if (!adapter.isInMultiSelectMode()) {
                // Pass the position to the listener
                //listener.click(position);
                // if (!(context instanceof AlbumInfoActivity)) {
                //context = view.getContext();
                // Create an intent to start the new activity
                Intent intent = new Intent(FavoriteImagesActivity.this, ImageInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("position", index);
                intent.putExtras(bundle);
                intent.putExtra("image_path", adapter.getImages_list().get(index).getPath());
                intent.putExtra("image", (Serializable) adapter.getImages_list().get(index));
                //intent.putExtra("next_image_path", images_list.get(position + 1));

                // Pass the path to the image to the new activity
                // Start the new activity
                //context.startActivity(intent);
                Log.d("aaaaa", "1");
                startIntentSeeImageInfo.launch(intent);
                //}
            }
        }

        @Override
        public void longClick(int index) {

        }
    };
    GridLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_images);

        startIntentSeeImageInfo = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        /*Intent data = result.getData();
                        if (data != null) {
                            String imageDeleted = data.getStringExtra("ImageDeleted");
                            String imageMoveToTrash = data.getStringExtra("ImageToTrash");
                            Boolean didEditImage = data.getBooleanExtra("EditedImage",false);
                            Log.d("aaaaa", "2");
                            if (imageDeleted != null) {
                                Log.d("aaaaa", "3");
                                images.remove(clickPosition);
                                adapter.notifyDataSetChanged();
                                updateNumberOfImage();
                            }
                            if (imageMoveToTrash != null) {
                                images.remove(clickPosition);
                                adapter.notifyDataSetChanged();
                                updateNumberOfImage();
                            }
                            if (didEditImage ==true) {
                                loadNewestImageOnResume();
                                adapter.notifyDataSetChanged();
                                updateNumberOfImage();
                            }
                        }*/
                    }
                }
        );
        //getSupportFragmentManager().beginTransaction().replace(R.id.container, albumFragment).commit();
        init();
        imageButtonReturnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FavoriteImagesActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        //linearLayout = (LinearLayout) findViewById(R.id)
        recycler = findViewById(R.id.favorite_gallery_recycler);
        images = new ArrayList<>();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthInDp = displayMetrics.widthPixels / displayMetrics.density;
        int imageWidth = 110; // size of an image
        int desiredColumnCount = (int)screenWidthInDp / imageWidth; // the number of images in a row

        adapter = new ImageAdapter(this, images, clickListener);
        manager = new GridLayoutManager(this, desiredColumnCount);

        recycler.setLayoutManager(manager);
        recycler.setAdapter(adapter);




        recycler.getAdapter().notifyDataSetChanged();

        // using toolbar as ActionBar
        /*setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Favorite images");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);*/
    }

    private void init(){
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        imageButtonReturnHome = (ImageButton) findViewById(R.id.btnReturnHomeFromFavorites);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (images!=null) {
            images.clear();
            loadFavoritesImage();
        }
        if (adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }


    private void loadFavoritesImage() {

        images.clear();
        String args[] = {"1"};
        Cursor cursor = mainActivity.db.rawQuery("SELECT * FROM Image WHERE isFavored = ?",args);
        cursor.moveToPosition(-1);

        int isFavored=1;
        String description = "";
        String path="";
        while(cursor.moveToNext()) {
            int favorColumn = cursor.getColumnIndex("isFavored");
            int descriptionColumn = cursor.getColumnIndex("description");
            int pathColumn = cursor.getColumnIndex("path");
            isFavored = cursor.getInt(favorColumn);
            description = cursor.getString(descriptionColumn);
            path = cursor.getString(pathColumn);

            Image newImage = new Image(path, description, isFavored);


            images.add(newImage);
        }

        Log.d("favor","oke");

    }
}