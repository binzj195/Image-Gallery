package com.example.imagesgallery.Fragment;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.isExternalStorageManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentResolver;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagesgallery.Activity.AlbumInfoActivity;
import com.example.imagesgallery.Activity.ImageInfoActivity;
import com.example.imagesgallery.Adapter.SortAdapter;
import com.example.imagesgallery.Interface.ClickListener;
import com.example.imagesgallery.Activity.MainActivity;
import com.example.imagesgallery.Adapter.ImageAdapter;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;
import com.example.imagesgallery.Utility.FileUtility;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class ImageFragment extends Fragment implements ImageAdapter.SelectionChangeListener {
    RecyclerView recycler;
    public ArrayList<Image> images;
    public ImageAdapter adapter;
    GridLayoutManager manager;
    TextView totalimages;
    MainActivity mainActivity;
    Context context;

    private ActivityResultLauncher<Intent> launcher_for_camera;
    LinearLayout linearLayout;
    ArrayList<String> permissionsList;
    private ActivityResultLauncher<Intent> startIntentSeeImageInfo;
    String[] permissionsStr = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.SET_WALLPAPER,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            MANAGE_EXTERNAL_STORAGE};
    int permissionsCount = 0;
    int clickPosition = 0;
    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onActivityResult(Map<String, Boolean> result) {
                            ArrayList<Boolean> list = new ArrayList<>(result.values());
                            permissionsList = new ArrayList<>();
                            permissionsCount = 0;
                            for (int i = 0; i < list.size(); i++) {
                                if (shouldShowRequestPermissionRationale(permissionsStr[i])) {
                                    permissionsList.add(permissionsStr[i]);
                                } else if (!hasPermission(mainActivity, permissionsStr[i])) {
                                    ActivityCompat.requestPermissions(mainActivity, permissionsStr, permissionsCode);
                                    permissionsCount++;
                                }
                            }
                            if (permissionsList.size() > 0) {
                                //Some permissions are denied and can be asked again.
                                askForPermissions(permissionsList);
                            } else if (permissionsCount > 0) {
                                //Show alert dialog
                                showPermissionDialog();
                            } else {
                                //All permissions granted. Do your stuff 🤞
                            }
                        }
                    });
    int REQUEST_IMAGE_CAPTURE = 100;
    int permissionsCode = 42;
    boolean isStorageImagePermitted = false;
    boolean isStorageVideoPermitted = false;
    boolean isStorageAudioPermitted = false;

    //AT: Add button multiSelectButton
    Button multiSelectButton;
    Button deleteButton;
    Button slideshowButton;
    boolean multiSelectMode = false;
    private Uri imageUri;
    private String imageUrl;
    private Bitmap thumbnail;
    //AT
    ImageButton imageBtnCamera;
    String TAG = "Permission";

    //AT
    Switch switchMode;
    boolean nightMode = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    AppCompatActivity activity;
    private long dateTaken = 0;
    private String imageLink = "...";

    Toolbar toolbar;
    SearchView searchView;
    //private ActivityResultLauncher<String> requestPermissionLauncher;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    ArrayList<String> items = new ArrayList<>(Arrays.asList("Decrease by day", "Increase by day", "Increase by name", "Decrease by name"));
    Spinner spinner;
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        context = getContext();

        setHasOptionsMenu(true);
        launcher_for_camera =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Log.d("cam", "result oke");

                                Intent data = result.getData();
                                if (data != null) {
                                    Log.d("cam", "data oke");
                                    Bundle extras = data.getExtras();
                                    Bitmap imageBitmap = (Bitmap) extras.get("data");

                                    try {
                                        // Create a file to save the captured image
                                        File imageFile = createImageFile();

                                        // Save the image bitmap to the file
                                        FileOutputStream fos = new FileOutputStream(imageFile);
                                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                        fos.close();

                                        // Insert the image file into the MediaStore
                                        ContentValues values = new ContentValues();
                                        values.put(MediaStore.Images.Media.TITLE, "Custom album group 9");
                                        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.getName());
                                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                                        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
                                        values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());

                                        Uri newImageUri = mainActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                                        loadNewestImageOnResume();
                                        adapter.notifyDataSetChanged();
                                        updateNumberOfImage();

                                        // Perform any further operations with the newImageUri as needed
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    /*ContentValues values = new ContentValues();
                                    values.put(MediaStore.Images.Media.TITLE, "Custom album group 9");
                                    newImageUri = mainActivity.getContentResolver().insert(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);*/

                                    //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, newImageUri);;

                                }
                            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                                Log.d("cam", "result cancel");
                            }
                        }

                );


/*new ActivityResultCallback<ActivityResult>() {
                            @Override
                            public void onActivityResult(ActivityResult result) {
                                if (result.getResultCode() == RESULT_OK) {
                                    loadNewestImageOnResume();
                                    adapter.notifyDataSetChanged();
                                    updateNumberOfImage();
                                    if (result.getData()!=null) {
                                        Log.d("cam","result data");
                                        if (newImageUri!=null) {
                                            Log.d("cam","uri: "+newImageUri );
                                            File tempImage = new File(newImageUri.getPath());
                                            Log.d("cam","file path: "+tempImage.getAbsolutePath());
                                            long size = tempImage.length() /1024;
                                            String extension = FileUtility.getFileExtension(tempImage);
                                            Image image = new Image(newImageUri.getPath(),"",0,new Date(),size,extension);
                                            images.add(image);

        newImageUri=null;
    }

}*/
                                /*try {
                                    thumbnail = MediaStore.Images.Media.getBitmap(
                                            mainActivity.getContentResolver(),imageUri);
                                    imageUrl = getPathFromUri(mainActivity,imageUri);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }*/
                                    /*Log.d("onResume","before count: "+adapter.getItemCount()+" - size: "+images.size());
                                    Image newImage = new Image(newImageUri.getPath(), "captured image", 0);
                                    images.add(newImage);
                                    //adapter.addImage(newImage);
                                    adapter.notifyDataSetChanged();
                                    adapter.notifyItemInserted(0);
                                    Log.d("onResume","after count: "+adapter.getItemCount()+" - size: "+images.size());*/

//onResume();
//triggerMediaScan(imageUri);

        // when click button back in toolbar or in smartphone to finish ImageInfoActivity
        startIntentSeeImageInfo = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String imageDeleted = data.getStringExtra("ImageDeleted");
                            String imageMoveToTrash = data.getStringExtra("ImageToTrash");
                            String hiddenImage = data.getStringExtra("HiddenImage");
                            Boolean didEditImage = data.getBooleanExtra("EditedImage", false);
                            String description = data.getStringExtra("description");
                            int isFavored = data.getIntExtra("isFavored", -1);

                            if (imageDeleted != null) {
                                deleteImageFromDatabase(images.get(clickPosition));
                                Log.d("deletedRows","Index: "+clickPosition);
                                //images.remove(clickPosition);
                                adapter.removeImage(clickPosition);
                                adapter.notifyDataSetChanged();
                                updateNumberOfImage();
                            }
                            if (imageMoveToTrash != null) {
                                deleteImageFromDatabase(images.get(clickPosition));
                                Log.d("deletedRows","Index: "+clickPosition);
                                //images.remove(clickPosition);
                                deleteImageOnExternalContentURI(images.get(clickPosition));
                                adapter.removeImage(clickPosition);
                                adapter.notifyDataSetChanged();
                                updateNumberOfImage();
                            }
                            if (hiddenImage!=null) {
                                deleteImageFromDatabase(images.get(clickPosition));
                                Log.d("deletedRows","Index: "+clickPosition);
                                //images.remove(clickPosition);
                                deleteImageOnExternalContentURI(images.get(clickPosition));
                                adapter.removeImage(clickPosition);
                                adapter.notifyDataSetChanged();
                                updateNumberOfImage();
                            }
                            if (description != null) {
                                images.get(clickPosition).setDescription(description);
                                adapter.notifyDataSetChanged();
                            }
                            if (isFavored != -1 && images.get(clickPosition).getIsFavored() != isFavored) {
                                images.get(clickPosition).setIsFavored(isFavored);
                                adapter.notifyDataSetChanged();
                            }
                            if (didEditImage == true) {
                                loadNewestImageOnResume();
                                adapter.notifyDataSetChanged();
                                updateNumberOfImage();
                            }
                        }
                    }
                }
        );
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures");

        // Create the directory if it doesn't exist
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        // Create the file
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Return the file
        return imageFile;
    }

    private void deleteImageOnExternalContentURI(Image image) {
        ContentResolver contentResolver = mainActivity.getContentResolver();
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{ image.getPath() };

        int deletedRows = contentResolver.delete(imageUri, selection, selectionArgs);
        Log.d("deletedRows","External: "+deletedRows);
    }
    public void deleteImageOnExternalContentURIUsingImagePath(String imagePath) {
        ContentResolver contentResolver = mainActivity.getContentResolver();
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{ imagePath };

        int deletedRows = contentResolver.delete(imageUri, selection, selectionArgs);
        Log.d("deletedRows","External: "+deletedRows);
    }
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
                if (!(context instanceof AlbumInfoActivity)) {
                    //context = view.getContext();
                    // Create an intent to start the new activity
                    Intent intent = new Intent(context, ImageInfoActivity.class);
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
                }
            }
        }

        @Override
        public void longClick(int index) {

        }
    };
    SortAdapter sortAdapter;

    public void loadImagesAscendingByDate() {
        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);

        if (SDCard) {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String[] projection = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.ORIENTATION};
            //final String order = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            final String order = MediaStore.Images.Media.DATE_ADDED + " ASC";
            ContentResolver contentResolver = requireActivity().getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    order);
            int count = cursor.getCount();
            totalimages.setText("Total items: " + count);

            //images.clear();
            Thread insertThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    images.clear();
                    ContentValues rowValues = new ContentValues();
                    for (int i = 0; i < count; i++) {
                        cursor.moveToPosition(i);
                        int columnindex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                        imageLink = cursor.getString(columnindex);
                        //load date
                        try {
                            int columnIndexDateTaken = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
                            dateTaken = cursor.getLong(columnIndexDateTaken);
                        } catch (Exception e) {
                            Log.e("testt", "run: " + e.getMessage(), e);
                        }
                        Date date = new Date(dateTaken);

                        //Load size image,type
                        ExifInterface exifInterface = null;
                        File imageFile = null;
                        String extensionName = "";
                        long imageSizeInBytes = 0;
                        long imageSizeInKB = 0;
                        try {
                            exifInterface = new ExifInterface(imageLink);
                            imageFile = new File(imageLink);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (imageFile != null) {
                            imageSizeInBytes = imageFile.length();
                            imageSizeInKB = imageSizeInBytes / 1024;
                            int dotIndex = imageFile.getAbsolutePath().lastIndexOf('.');
                            if (dotIndex >= 0 && dotIndex < imageLink.length() - 1) {
                                extensionName = imageFile.getAbsolutePath().substring(dotIndex + 1);
                            }
                        }

                        String path = cursor.getString(columnindex);
                        int isFavored = 0;
                        String description = "";
                        String[] args = {path};
                        Cursor cursor1 = MainActivity.db.rawQuery("SELECT * FROM Image WHERE path = ?", args);

                        if (!cursor1.moveToFirst()) {
                            rowValues.clear();
                            rowValues.put("path", path);
                            rowValues.put("description", "");
                            rowValues.put("isFavored", isFavored);
                            long rowID = MainActivity.db.insert("Image", null, rowValues);
                        } else {
                            cursor1.moveToPosition(-1);
                            while (cursor1.moveToNext()) {
                                int favorColumn = cursor1.getColumnIndex("isFavored");
                                int descriptionColumn = cursor1.getColumnIndex("description");
                                isFavored = cursor1.getInt(favorColumn);
                                description = cursor1.getString(descriptionColumn);
                            }
                        }

                        Image newImage = new Image(path, description, isFavored, date, imageSizeInKB, extensionName);
                        images.add(newImage);
                    }

                }
            });
            insertThread.start();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_image, container, false);
        spinner = (Spinner) linearLayout.findViewById(R.id.spinner);
        //sortAdapter = new SortAdapter(mainActivity,R.layout.custom_sort_spinner_view,items);
        //spinner.setAdapter(sortAdapter);
        spinner.setAdapter(new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(mainActivity,items.get(i),Toast.LENGTH_SHORT).show();
                /*if (i==1) {
                    loadImagesAscendingByDate();
                    adapter.notifyDataSetChanged();
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        recycler = linearLayout.findViewById(R.id.gallery_recycler);
        toolbar = (Toolbar) linearLayout.findViewById(R.id.toolbar);

//        switchMode = linearLayout.findViewById(R.id.switchMode);
//        sharedPreferences = mainActivity.getSharedPreferences("MODE", Context.MODE_PRIVATE);
//        nightMode = sharedPreferences.getBoolean("nightMode", false);
//        if (nightMode) {
//            switchMode.setChecked(true);
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        }
//        switchMode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (nightMode) {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                    editor = sharedPreferences.edit();
//                    editor.putBoolean("nightMode", false);
//                } else {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                    editor = sharedPreferences.edit();
//                    editor.putBoolean("nightMode", true);
//                }
//                editor.apply();
//            }
//        });

        images = new ArrayList<>();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthInDp = displayMetrics.widthPixels / displayMetrics.density;
        int imageWidth = 110; // size of an image
        int desiredColumnCount = (int) screenWidthInDp / imageWidth; // the number of images in a row

        adapter = new ImageAdapter(mainActivity, images, clickListener);
        manager = new GridLayoutManager(mainActivity, desiredColumnCount);
        totalimages = linearLayout.findViewById(R.id.gallery_total_images);
        recycler.setLayoutManager(manager);
        recycler.setAdapter(adapter);
        //getDateTaken();
        //getEditorImage();

        //deleteContentUri();
        //images.clear();
        loadImages();
        //loadImagesOnResume();
        adapter.notifyDataSetChanged();
        //recycler.getAdapter().notifyDataSetChanged();


        //set tool bar
        activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        //AT
        // Initialize the button
        multiSelectButton = linearLayout.findViewById(R.id.multiSelectButton);
        deleteButton = linearLayout.findViewById(R.id.deleteButton);
        slideshowButton = linearLayout.findViewById(R.id.slideshowButton);
        imageBtnCamera = (ImageButton) linearLayout.findViewById(R.id.imageBtnCamera);
        multiSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (multiSelectMode) {
                    multiSelectMode = false;
                    adapter.setMultiSelectMode(multiSelectMode);
                    adapter.clearSelection();
                    multiSelectButton.setText("Select");

                    // Handle actions in multi-select mode
                } else {
                    // Enter multi-select mode
                    multiSelectMode = true;
                    adapter.setMultiSelectMode(multiSelectMode);
                    // Update UI, e.g., change button text
                    multiSelectButton.setText("Cancel"); // Optionally, you can change the button label
                }
                toggleButtonsOfMultiSelectMode(multiSelectMode);
            }

        });
        deleteButton.setEnabled(false);
        slideshowButton.setEnabled(false);
        // Set state for buttons when in multi-select mode
        adapter.setSelectionChangeListener(this);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialogDeleteImage();
            }
        });
        slideshowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> selectedImages = adapter.getSelectedImages();
                if (!selectedImages.isEmpty()) {
                    // Call the method in MainActivity to start the SlideshowActivity
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).startSlideshowActivity(selectedImages);
                    }
                }
            }
        });
        //AT
        imageBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(),"Open camera",Toast.LENGTH_SHORT).show();
                openCamera();
            }
        });

        return linearLayout;
    }

    @Override
    public void onResume() {

        super.onResume();
        //Toast.makeText(context,"onResume",Toast.LENGTH_SHORT).show();
//        // Log.d("onResume","Count: "+adapter.getItemCount()+" - list: "+images.size());
//        if (images != null) {
//            images.clear();
//            loadImagesOnResume();
//            if (newImageUri != null) {
//                // loadNewestImageOnResume();
//                newImageUri = null;
//            }
//
//            /*if (newImageUri!=null) {
//                Log.d("onResume","newImage");
//                File newImageFile = null;
//                String extensionName = "";
//                long imageSizeInBytes = 0;
//                long imageSizeInKB = 0;
//                try {
//
//                    newImageFile = new File(newImageUri.getPath());
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                if (newImageFile != null) {
//                    imageSizeInBytes = newImageFile.length();
//                    imageSizeInKB = imageSizeInBytes / 1024;
//                    extensionName = FileUtility.getFileExtension(newImageFile);
//                }
//
//                Image newImage = new Image(newImageUri.getPath(), "captured image", 0,new Date(),imageSizeInKB,extensionName);
//                images.add(newImage);
//                newImageUri = null;
//            }
//*/
//        }
//
//        if (adapter != null) {
//            Log.d("onResume", "data changed");
//            adapter.notifyDataSetChanged();
//        }
//        /*if (recycler!=null) {
//            Log.d("onResume","recycler data changed");
//            recycler.getAdapter().notifyDataSetChanged();
//        }*/
    }


    private void getDateTaken() {
        ContentResolver contentResolver = mainActivity.getContentResolver();
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN,
                // Add other columns you need to retrieve
        };

        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC"; // Sorting by date taken in descending order

        Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Retrieve values from the cursor
                long imageId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                long dateTaken = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));

                // Process the retrieved values as needed

            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private void getEditorImage() {

        // The edit image activity completed successfully
        // Perform any necessary actions
        String internalFolder = Environment.getExternalStorageDirectory() + "/Pictures";
        ;
        String editorPhotoFolder = internalFolder + "/DS_Photo_Editor";
        //Toast.makeText(mainActivity,"internal: "+internalFolder,Toast.LENGTH_SHORT).show();
        //Toast.makeText(mainActivity,"editor: "+editorPhotoFolder,Toast.LENGTH_SHORT).show();

               /* String internalFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/Pictures";;
                String editorPhotoFolder = internalFolder+"/DS_Photo_Editor";*/

        ArrayList<File> resultFiles = FileUtility.moveAllImagesInAFolderToAnotherFolder(editorPhotoFolder, internalFolder);
        if (resultFiles != null) {
            for (File file : resultFiles) {
                Uri imageUri = FileProvider.getUriForFile(mainActivity, "com.example.imagesgallery.Utility.fileprovider", file);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, imageUri.getPath());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + FileUtility.getFileExtension(file));


                try {
                    ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
                    String dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                    Date dateTaken = dateFormat.parse(dateTime);

                    long dateTakenMillis = System.currentTimeMillis(); //dateTaken.getTime();
                    Toast.makeText(mainActivity, "date taken: " + dateTaken, Toast.LENGTH_LONG).show();
                    values.put(MediaStore.Images.Media.DATE_TAKEN, dateTakenMillis);
                    Toast.makeText(mainActivity, "success: " + dateTakenMillis, Toast.LENGTH_LONG).show();
                } catch (IOException | ParseException | java.text.ParseException e) {
                    e.printStackTrace();
                    // Handle the exception as needed
                }


                ContentResolver contentResolver = getContext().getContentResolver();
                Uri imageUriInserted = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (imageUriInserted != null) {
                    Toast.makeText(mainActivity, "editor success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mainActivity, "editor fail", Toast.LENGTH_SHORT).show();

                }

            }

        }
        recycler.getAdapter().notifyDataSetChanged();
        adapter.notifyDataSetChanged();
    }

    //Warning: this function is used for test and setup AVD only
    public void deleteContentUri() {
        // Create a content resolver instance
        ContentResolver contentResolver = mainActivity.getContentResolver();

        // Define the content URI for images
        Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Delete all images using the content resolver
        int rowsDeleted = contentResolver.delete(imagesUri, null, null);

        // Check the number of rows deleted to determine if the deletion was successful
        if (rowsDeleted > 0) {
            // Images deletion was successful
            // Add your desired logic here
        } else {
            // Images deletion failed or there are no images
            // Add your desired logic here
        }
    }

    //AT
    private void deleteImage(String imagePath) {
        File deleteImage = new File(imagePath);
        if (deleteImage.exists()) {
            if (deleteImage.delete()) {
                String[] args = {imagePath};
                long rowID = MainActivity.db.delete("Image", "path = ?", args);
                long rowID2 = MainActivity.db.delete("Album_Contain_Images", "path = ?", args);

                if (rowID > 0 && rowID2 > 0) {
                    Toast.makeText(getContext(), "Delete success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                }


                // Sau khi xóa tệp tin, thông báo cho MediaScanner cập nhật thư viện ảnh
                MediaScannerConnection.scanFile(getActivity().getApplicationContext(), new String[]{imagePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        //imageView = findViewById(R.id.imageFullScreen);
                        //Glide.with(context).load(nextImageTemp).into(imageView);
                    }
                });

                String PreviousActivity = null;

                if (getActivity() != null) {
                    Intent activityIntent = getActivity().getIntent();
                    if (activityIntent != null) {
                        PreviousActivity = activityIntent.getStringExtra("PreviousActivity");
                    }
                }
                if (Objects.equals(PreviousActivity, "AlbumInfoActivity")) {
                    // return to previous activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("ImageDeleted", imagePath);
                    getActivity().setResult(Activity.RESULT_OK, resultIntent);
                    getActivity().finish();
                } else {
                    Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Xóa Activity Stack
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Tạo mới Task
                        startActivity(intent);
                    }
                    /*
                    Intent intent = new Intent(getContext(),MainActivity.class);
                    startActivity(intent);*/

                }
            }
        }
    }

    public void addImageToTrashFolder(String imagePath) {
        File sourceImage = new File(imagePath);
        String oldImagePath = sourceImage.getAbsolutePath();

        String trashFolderPath = Environment.getExternalStorageDirectory() + File.separator + ".trash_image_folder";
        //File destinationFile = FileUtility.moveImageToFolder(sourceImage, trashFolderPath);

        File destinationFile = FileUtility.moveImageToSecretFolder(sourceImage, trashFolderPath);

        if (destinationFile!=null) {
            boolean deletedFile = sourceImage.delete();
            Log.d("hidden","res: "+deletedFile);
        }
        String newImagePath = destinationFile.getAbsolutePath();
        updateTagOfImage(oldImagePath, newImagePath);
        Log.d("trash","old: "+sourceImage.getAbsolutePath()+" - new: "+newImagePath);
        /*Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);*/
    }
    public void updateTagOfImage(String oldImagePath, String newImagePath) {
        ContentValues values = new ContentValues();
        values.put("Image_Path",newImagePath);
        String condition = "Image_Path = ?";
        String[] args = { oldImagePath };
        mainActivity.db.update("Image_Tag",values,condition,args);
    }

    public void createDialogDeleteImage() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to delete these images ?");

        // click yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "onClick: delete image");

                ArrayList<String> selectedImages = adapter.getSelectedImages();
                Log.d(TAG, "selectedImages size " + adapter.getSelectedImages().size());
                // Define variables to track the number of successfully deleted images
                for (String imagePath : selectedImages) {
                    //deleteImage(imagePath);
                    addImageToTrashFolder(imagePath);
                    deleteImageFromDatabaseUsingImagePath(imagePath);

                    //images.remove(clickPosition);
                    deleteImageOnExternalContentURIUsingImagePath(imagePath);



                }
                ArrayList<Integer> selectedPosition = adapter.getSelectedPositions();
                for (int i = selectedPosition.size()-1;i>=0; i--) {
                    adapter.removeImage(i);
                }
                adapter.notifyDataSetChanged();
                updateNumberOfImage();

                toggleButtonsOfMultiSelectMode(multiSelectMode);
                multiSelectMode = false;
                adapter.setMultiSelectMode(multiSelectMode);
                adapter.clearSelection();
                Log.d("selected images: ", adapter.getSelectedImages().toString());
                multiSelectButton.setText("Select");
            }
        });
        // click no
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button noButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);
                Button yesButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
                yesButton.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.lavender));
                noButton.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.lavender)); // Change to your desired color resource
            }
        });
        dialog.show();
    }

    //AT When click button Multi Select, it shows Delete Button
    private void toggleButtonsOfMultiSelectMode(Boolean isMultiSelectMode) {
        if (isMultiSelectMode) {
            deleteButton.setVisibility(View.VISIBLE);
            slideshowButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
            slideshowButton.setVisibility(View.INVISIBLE);
        }
    }

    // Method to update the delete button's state
    // Implement the onSelectionChanged method from the SelectionChangeListener interface
    @Override
    public void onSelectionChanged(boolean hasSelection) {
        if (hasSelection) {
            // At least one image is selected, enable the deleteButton
            slideshowButton.setEnabled(true);
            deleteButton.setEnabled(true);
        } else {
            // No images are selected, disable the deleteButton
            slideshowButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }
    //AT

    public void loadImages() {
        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);

        if (SDCard) {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String[] projection = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.ORIENTATION};
            //final String order = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            final String order = MediaStore.Images.Media.DATE_ADDED + " DESC";
            ContentResolver contentResolver = requireActivity().getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    order);
            int count = cursor.getCount();
            totalimages.setText("Total items: " + count);

            //images.clear();
            Thread insertThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    images.clear();
                    ContentValues rowValues = new ContentValues();
                    for (int i = 0; i < count; i++) {
                        cursor.moveToPosition(i);
                        int columnindex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                        imageLink = cursor.getString(columnindex);
                        //load date
                        try {
                            int columnIndexDateTaken = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
                            dateTaken = cursor.getLong(columnIndexDateTaken);
                        } catch (Exception e) {
                            Log.e("testt", "run: " + e.getMessage(), e);
                        }
                        Date date = new Date(dateTaken);

                        //Load size image,type
                        ExifInterface exifInterface = null;
                        File imageFile = null;
                        String extensionName = "";
                        long imageSizeInBytes = 0;
                        long imageSizeInKB = 0;
                        try {
                            exifInterface = new ExifInterface(imageLink);
                            imageFile = new File(imageLink);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (imageFile != null) {
                            imageSizeInBytes = imageFile.length();
                            imageSizeInKB = imageSizeInBytes / 1024;
                            int dotIndex = imageFile.getAbsolutePath().lastIndexOf('.');
                            if (dotIndex >= 0 && dotIndex < imageLink.length() - 1) {
                                extensionName = imageFile.getAbsolutePath().substring(dotIndex + 1);
                            }
                        }

                        String path = cursor.getString(columnindex);
                        int isFavored = 0;
                        String description = "";
                        String[] args = {path};
                        Cursor cursor1 = MainActivity.db.rawQuery("SELECT * FROM Image WHERE path = ?", args);

                        if (!cursor1.moveToFirst()) {
                            rowValues.clear();
                            rowValues.put("path", path);
                            rowValues.put("description", "");
                            rowValues.put("isFavored", isFavored);
                            long rowID = MainActivity.db.insert("Image", null, rowValues);
                        } else {
                            cursor1.moveToPosition(-1);
                            while (cursor1.moveToNext()) {
                                int favorColumn = cursor1.getColumnIndex("isFavored");
                                int descriptionColumn = cursor1.getColumnIndex("description");
                                isFavored = cursor1.getInt(favorColumn);
                                description = cursor1.getString(descriptionColumn);
                            }
                        }

                        Image newImage = new Image(path, description, isFavored, date, imageSizeInKB, extensionName);
                        images.add(newImage);
                    }
                }
            });
            insertThread.start();
        }
    }

    public void deleteImageFromDatabase(Image image) {
        String tableName = "Image";
        String condition = "path = ?";
        String[] args = {image.getPath()};
        int deletedRows = mainActivity.db.delete(tableName,condition,args);

        int deletedRowInAlbum = mainActivity.db.delete("Album_Contain_Images",condition,args);
        Log.d("deletedRows","Count: "+deletedRows+" - albums: "+deletedRowInAlbum);
    }
    public void deleteImageFromDatabaseUsingImagePath(String imagePath) {
        String tableName = "Image";
        String condition = "path = ?";
        String[] args = {imagePath};
        int deletedRows = mainActivity.db.delete(tableName,condition,args);

        int deletedRowInAlbum = mainActivity.db.delete("Album_Contain_Images",condition,args);
        Log.d("deletedRows","Count: "+deletedRows+" - albums: "+deletedRowInAlbum);
    }

    public void updateNumberOfImage() {
        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);

        if (SDCard) {
            final String[] projection = {MediaStore.Images.Media._ID
            };
            //final String order = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            final String order = MediaStore.Images.Media.DATE_ADDED + " DESC";
            ContentResolver contentResolver = requireActivity().getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    order);
            int count = cursor.getCount();
            totalimages.setText("Total items: " + count);
        }
    }

    public void loadImagesOnResume() {
        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);

        if (SDCard) {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.ImageColumns.ORIENTATION};
            //final String order = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            final String order = MediaStore.Images.Media.DATE_ADDED + " DESC";
            ContentResolver contentResolver = requireActivity().getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, order);
            int count = cursor.getCount();
            totalimages.setText("Total items: " + count);

            images.clear();

            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                int columnindex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                imageLink = cursor.getString(columnindex);
                //load date
                try {
                    int columnIndexDateTaken = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
                    dateTaken = cursor.getLong(columnIndexDateTaken);
                } catch (Exception e) {
                    Log.e("testt", "run: " + e.getMessage(), e);
                }
                Date date = new Date(dateTaken);

                //Load size image,type

                File imageFile = null;
                String extensionName = "";
                long imageSizeInBytes = 0;
                long imageSizeInKB = 0;
                try {

                    imageFile = new File(imageLink);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (imageFile != null) {
                    imageSizeInBytes = imageFile.length();
                    imageSizeInKB = imageSizeInBytes / 1024;
                    int dotIndex = imageFile.getAbsolutePath().lastIndexOf('.');
                    if (dotIndex >= 0 && dotIndex < imageLink.length() - 1) {
                        extensionName = imageFile.getAbsolutePath().substring(dotIndex + 1);
                    }
                }
                String path = cursor.getString(columnindex);
                int isFavored = 0;
                String description = "";
                String[] args = {path};
                Cursor cursor1 = MainActivity.db.rawQuery("SELECT * FROM Image WHERE path = ?", args);

                if (!cursor1.moveToFirst()) {

                } else {
                    cursor1.moveToPosition(-1);
                    while (cursor1.moveToNext()) {
                        int favorColumn = cursor1.getColumnIndex("isFavored");
                        int descriptionColumn = cursor1.getColumnIndex("description");
                        isFavored = cursor1.getInt(favorColumn);
                        description = cursor1.getString(descriptionColumn);
                    }
                }


                Image newImage = new Image(path, description, isFavored, date, imageSizeInKB, extensionName);
                images.add(newImage);
            }
        }
    }

    public void loadNewestImageOnResume() {
        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);

        if (SDCard) {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.ImageColumns.ORIENTATION};
            //final String order = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            final String order = MediaStore.Images.Media.DATE_ADDED + " DESC";
            ContentResolver contentResolver = requireActivity().getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, order);
            int count = cursor.getCount();
            totalimages.setText("Total items: " + count);

            //String firstImage = images.get(0).getPath();
            ContentValues rowValues = new ContentValues();
            for (int i = 0; i < 1; i++) {
                cursor.moveToPosition(i);
                int columnindex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                imageLink = cursor.getString(columnindex);
                //load date
                try {
                    int columnIndexDateTaken = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
                    dateTaken = cursor.getLong(columnIndexDateTaken);
                } catch (Exception e) {
                    Log.e("testt", "run: " + e.getMessage(), e);
                }
                Date date = new Date(dateTaken);

                //Load size image,type

                File imageFile = null;
                String extensionName = "";
                long imageSizeInBytes = 0;
                long imageSizeInKB = 0;
                try {

                    imageFile = new File(imageLink);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (imageFile != null) {
                    imageSizeInBytes = imageFile.length();
                    imageSizeInKB = imageSizeInBytes / 1024;
                    int dotIndex = imageFile.getAbsolutePath().lastIndexOf('.');
                    if (dotIndex >= 0 && dotIndex < imageLink.length() - 1) {
                        extensionName = imageFile.getAbsolutePath().substring(dotIndex + 1);
                    }
                }
                String path = cursor.getString(columnindex);
                int isFavored = 0;

                String description = "";
                String[] args = {path};
                Cursor cursor1 = MainActivity.db.rawQuery("SELECT * FROM Image WHERE path = ?", args);

                if (!cursor1.moveToFirst()) {
                    rowValues.clear();
                    rowValues.put("path", path);
                    rowValues.put("description", "");
                    rowValues.put("isFavored", isFavored);
                    long rowID = MainActivity.db.insert("Image", null, rowValues);
                } else {
                    cursor1.moveToPosition(-1);
                    while (cursor1.moveToNext()) {
                        int favorColumn = cursor1.getColumnIndex("isFavored");
                        int descriptionColumn = cursor1.getColumnIndex("description");
                        isFavored = cursor1.getInt(favorColumn);
                        description = cursor1.getString(descriptionColumn);
                    }
                }



                Image newImage = new Image(path, description, isFavored, date, imageSizeInKB, extensionName);
                images.add(0, newImage);
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        requireActivity().getMenuInflater().inflate(R.menu.menu_image_home_page, menu);
        MenuItem menuItemSearch = menu.findItem(R.id.search_image);
        searchView = (SearchView) menuItemSearch.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(mainActivity,
                Manifest.permission.SEND_SMS);
        int locationPermission = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(mainActivity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            permissionsLauncher.launch(newPermissionStr);

        } else {

            showPermissionDialog();
        }
    }

    AlertDialog alertDialog;

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("Permission required")
                .setMessage("Some permissions are need to be allowed to use this app without any problems.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    dialog.dismiss();
                });
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    private boolean hasPermission(Context context, String permissionStr) {
        return ContextCompat.checkSelfPermission(context, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (accepted) {
                loadImages();
            } else {
                Toast.makeText(mainActivity, "You have denied the permissions", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(mainActivity, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(mainActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private boolean allPermissionResultCheck() {
        return isStorageAudioPermitted && isStorageImagePermitted && isStorageVideoPermitted;
    }

    public void requestPermissionStorageImage() {
        if (ContextCompat.checkSelfPermission(mainActivity, permissionsStr[0]) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, permissionsStr[0] + " Granted");
            isStorageImagePermitted = true;

        } else {
            request_permission_launcher_storage_images.launch(permissionsStr[0]);

        }
    }

    //Just a comment to change message when commit
    private ActivityResultLauncher<String> request_permission_launcher_storage_images =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Log.d(TAG, permissionsStr[0] + " Granted");
                            isStorageImagePermitted = true;
                        } else {
                            Log.d(TAG, permissionsStr[0] + " Not Granted");
                            isStorageImagePermitted = false;
                            sendToSettingDialog();
                        }


                    });

    public void sendToSettingDialog() {
        new AlertDialog.Builder(mainActivity)
                .setTitle("Alert for permission")
                .setMessage("Go to settings for Permissions")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent rIntent = new Intent();
                        rIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", mainActivity.getPackageName(), null);
                        rIntent.setData(uri);
                        startActivity(rIntent);
                        alertDialog.dismiss();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mainActivity.finish();
                    }
                });
    }

    /* private void openCamera() {
         Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         if (takePictureIntent.resolveActivity(mainActivity.getPackageManager()) != null) {
             startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
         } else {
             Toast.makeText(getContext(), "Camera not available", Toast.LENGTH_SHORT).show();
         }
     }

     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
             Bundle extras = data.getExtras();
             Bitmap imageBitmap = (Bitmap) extras.get("data");

             // Save the captured image to external storage
             String imageFilePath = saveImageToExternalStorage(imageBitmap);
             if (imageFilePath != null) {
                 Toast.makeText(getContext(), "Save image successfully", Toast.LENGTH_SHORT).show();
             } else {
                 Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
             }
         }
     }*/
    /*private void openCamera() {
        mainActivity.requestPermissionManageExternalStorage();
        mainActivity.requestPermissionCamera();
        mainActivity.requestPermissionWriteExternalStorage();
        &&
                ContextCompat.checkSelfPermission(mainActivity, MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ) {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(mainActivity.getPackageManager()) != null)  {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(getContext(), "Camera not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Camera permission not approve", Toast.LENGTH_SHORT).show();
            requestCameraPermission();
        }

    }*/
    private void triggerMediaScan(Uri imageUri) {
        File tempFile = new File(imageUri.toString());
//       String[] filePaths = {imageUri.getPath()};
        String[] filePaths = {tempFile.getAbsolutePath()};
        MediaScannerConnection.scanFile(
                mainActivity,
                filePaths,
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {


                    }
                }
        );

        //adapter.notifyItemRangeInserted(0, 10);
    }

    private Uri newImageUri = null;
    private Uri newCapturedImageUri = null;

    protected void openCamera() {

        /*imageUri = mainActivity.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);*/

        /*ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Custom album group 9");
        newImageUri = mainActivity.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, newImageUri);*/
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        launcher_for_camera.launch(cameraIntent);
        //startActivityForResult(cameraIntent,REQUEST_IMAGE_CAPTURE);
        /*Image newImage = new Image(imageUri.getPath(),"captured image",0);
        images.add(0,newImage);
        //adapter.addImage(newImage);
        adapter.notifyDataSetChanged();
        adapter.notifyItemInserted(0);*/
    }


    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");

            //Uri imageUri = data.getData();
            String imagePath = getImagePathFromUri(imageUri);
            Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
            // Save the captured image to external storage
            // String imageFilePath = saveImageToExternalStorage(imageBitmap);
            //String imageFilePath  = saveImageToMediaStore(imageBitmap);
            //String imageFilePath  = saveImageToMediaStore2(imageBitmap);
            String imageFilePath = saveImageToMediaStore3(imageBitmap);
            if (imageFilePath != null) {
                // Image saved successfully, do something with the file path
                // ...
                Toast.makeText(getContext(), "Image path: " + imageFilePath, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getImagePathFromUri(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = mainActivity.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(columnIndex);
            cursor.close();
        }
        return path;
    }


    private String saveImageToExternalStorage(Bitmap imageBitmap) {
        String imageFileName = "JAVA_ANDROID_ALBUM_GROUP_9_IMG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            Toast.makeText(getContext(), "Save image successfully", Toast.LENGTH_SHORT).show();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String saveImageToMediaStore(Bitmap imageBitmap) {
        String imageFileName = "ALBUM_GROUP_9_IMG_" + System.currentTimeMillis() + ".jpg";
        /*ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        final ContentResolver contentResolver = getContext().getContentResolver();

        Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);*/

        // Get the directory for saving images in MediaStore
        File imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        //File imagesDir = Environment.getExternalStorageDirectory();
        Toast.makeText(mainActivity, imagesDir.toString(), Toast.LENGTH_SHORT).show();
        File imageFile = new File(imagesDir, imageFileName);

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);

            if (outputStream != null) {
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                //Toast.makeText(mainActivity, "Image saved to MediaStore successfully", Toast.LENGTH_SHORT).show();
                final ContentResolver contentResolver = mainActivity.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                MediaScannerConnection.scanFile(mainActivity, new String[]{imageFile.getAbsolutePath()}, null, null);

            }
        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                return imageFileName;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public String saveImageToMediaStore2(Bitmap imageBitmap) {

        String imageFileName = "ALBUM_G9_IMG_" + System.currentTimeMillis() + ".jpg";

        File storageDir = Environment.getExternalStorageDirectory();

        // Create the directory for the custom album
        File albumDir = new File(storageDir, "captured image");
        albumDir.mkdirs();

        // Create the image file within the album directory
        File imageFile = new File(albumDir, imageFileName);

        // Create the content values for the image file
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        // Get the content resolver
        ContentResolver resolver = mainActivity.getContentResolver();

        // Insert the image file into MediaStore
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Open an output stream for the image file
        OutputStream outputStream = null;
        try {
            outputStream = resolver.openOutputStream(imageUri);
            if (outputStream != null) {
                // Compress the bitmap and write it to the output stream
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the output stream
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Trigger media scanner to scan the newly added image file
        MediaScannerConnection.scanFile(mainActivity, new String[]{imageUri.getPath()}, null, null);
        return imageFileName;
    }

    public String saveImageToMediaStore3(Bitmap imageBitmap) {

        String imageFileName = "ALBUM_G9_IMG_" + System.currentTimeMillis() + ".jpg";

        // Create the content values for the image file
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        // Get the content resolver
        ContentResolver resolver = mainActivity.getContentResolver();

        // Insert the image file into MediaStore
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Open an output stream for the image file
        OutputStream outputStream = null;
        try {
            outputStream = resolver.openOutputStream(imageUri);
            if (outputStream != null) {
                // Compress the bitmap and write it to the output stream
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                // Get the file path from the imageUri
                String imagePath = getPathFromUri(mainActivity, imageUri);

                // Move the image file to the SD card/Camera folder
                File imageFile = new File(imagePath);

                //File storageDir = new File(Environment.getExternalStorageDirectory(),"DCIM/Camera");
                File storageDir = mainActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File albumDir = new File(storageDir, "Album Group 09/Captured Image");
                /*if (!albumDir.exists()) {
                    albumDir.mkdirs();
                }*/
                //albumDir.mkdirs();
                File destination = new File(storageDir, imageFileName);
                //Toast.makeText(mainActivity, destination.toString(), Toast.LENGTH_SHORT).show();
                if (mainActivity.hasManageExternalStoragePermission()) {
                    Toast.makeText(mainActivity, "Can access external memory", Toast.LENGTH_SHORT).show();
                    if (imageFile.renameTo(destination)) {
                        // Update the imageUri with the new file path
                        values.put(MediaStore.Images.Media.DATA, destination.getAbsolutePath());
                        resolver.update(imageUri, values, null, null);
                        // Trigger media scanner to scan the newly added image file
                    }
                    MediaScannerConnection.scanFile(mainActivity, new String[]{destination.getAbsolutePath()}, null, null);

                    //notify change
                    long imageSizeInBytes = 0;
                    long imageSizeInKB = 0;
                    if (imageFile != null) {
                        imageSizeInBytes = imageFile.length();
                        imageSizeInKB = imageSizeInBytes / 1024;

                    }
                    String extensionName = FileUtility.getFileExtension(destination);
                    Image newImage = new Image(destination.getAbsolutePath(), "", 0, new Date(), imageSizeInKB, extensionName);
                    images.add(newImage);
                    adapter.notifyDataSetChanged();


                } else {
                    Toast.makeText(mainActivity, "Can't access external memory", Toast.LENGTH_SHORT).show();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the output stream
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return imageFileName;
    }


    // Helper method to get the file path from Uri
    private String getPathFromUri(Context context, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

}