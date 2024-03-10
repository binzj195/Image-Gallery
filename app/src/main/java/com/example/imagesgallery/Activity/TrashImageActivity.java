package com.example.imagesgallery.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagesgallery.Adapter.ImageAdapter;
import com.example.imagesgallery.Interface.ClickListener;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;
import com.example.imagesgallery.Utility.FileUtility;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class TrashImageActivity extends AppCompatActivity {
    RecyclerView recycler;
    ArrayList<Image> images;
    ImageAdapter adapter;

    ImageButton imageButtonReturnHome;
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
                Intent intent = new Intent(TrashImageActivity.this, ImageInfoActivity.class);
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
        setContentView(R.layout.activity_trash_images);

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
                Intent intent = new Intent(TrashImageActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        //linearLayout = (LinearLayout) findViewById(R.id)
        recycler = findViewById(R.id.trash_gallery_recycler);
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


    }

    private void init(){
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        imageButtonReturnHome = (ImageButton) findViewById(R.id.btnReturnHomeFromTrash);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (images!=null) {
            images.clear();
            loadTrashImage();
        }
        if (adapter!=null)
        {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadTrashImage() {

        String trashFolderPath = Environment.getExternalStorageDirectory()+ File.separator+".trash_image_folder";
        ArrayList<File> resultFile = FileUtility.getAllImageInADirectory(trashFolderPath);
        if (resultFile!=null) {
            for (File file : resultFile) {
                if (file.getName() != ".nomedia") {
                    Image newImage = new Image(file.getAbsolutePath(), "", 0, false, true);
                    images.add(newImage);
                }

            }
            Log.d("hidden", "oke");
        }

    }
}
