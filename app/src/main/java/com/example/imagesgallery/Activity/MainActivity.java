package com.example.imagesgallery.Activity;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;
import static android.os.Environment.MEDIA_MOUNTED;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.content.AsyncTaskLoader;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagesgallery.Fragment.AlbumFragment;
import com.example.imagesgallery.Fragment.ImageFragment;
import com.example.imagesgallery.Interface.DownloadService;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;
import com.example.imagesgallery.Utility.FileUtility;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    public static SQLiteDatabase db;
    public static final String pathNoImage = "no_image";
    AlbumFragment albumFragment = new AlbumFragment();
    ImageFragment imageFragment = new ImageFragment();
    boolean isStorageImagePermitted = false;
    boolean isStorageVideoPermitted = false;
    boolean isStorageAudioPermitted = false;//no use now
    boolean isCameraPermitted = false;
    boolean isSetWallpaperPermitted = false;
    boolean isManageExternalStoragePermitted = false;

    boolean isInternetAccessPermitted = false;
    CallbackManager callbackManager;
    ProgressDialog progressDialog;
    Dialog dialogNavBottom;
    Button btnFavoriteAlbums;
    Button btnFavoriteImages;

    //AT
    Switch switchMode;
    boolean nightMode = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    //

    // Method to start the slideshow activity with selected images
    public void startSlideshowActivity(ArrayList<String> selectedImages) {
        Intent intent = new Intent(this, SlideshowActivity.class);
        intent.putStringArrayListExtra("selectedImages", selectedImages);
        startActivity(intent);
    }

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
    AlertDialog alertDialog;
    String DatabaseName = "myDatabase";

    private void setDarkMode(boolean enabled) {
        int mode = enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);

        // Update the background color or theme based on the dark mode state
        int backgroundColor = enabled ? android.R.color.background_dark : android.R.color.white;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
//        mainLayout = findViewById(R.id.mainLayout);  // Replace with the ID of your main layout
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean isDarkModeEnabled = sharedPreferences.getBoolean("nightMode", false);
//
//        // Apply dark mode to the entire app
//        setDarkMode(isDarkModeEnabled);
//        setTheme(android.R.style.Theme_Material_Dialog);

        // Login Facebook
        //Tam comment
        /*FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this.getApplication());
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });*/

        /*callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));*/

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (!isStorageImagePermitted) {
            requestPermissionStorageImage();
        } else {
            Toast.makeText(MainActivity.this, "you accepted the permission to read image", Toast.LENGTH_SHORT).show();
        }
        if (!isCameraPermitted) {
            requestPermissionCamera();
        }
        if (!isInternetAccessPermitted) {
            requestInternetAccessPermission();
        }
        // create database
        File storagePath = getApplication().getFilesDir();
        String myDbPath = storagePath + "/" + DatabaseName;
        try {
            db = SQLiteDatabase.openDatabase(myDbPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            db.execSQL("CREATE TABLE IF NOT EXISTS Album(id_album INTEGER PRIMARY KEY AUTOINCREMENT, description TEXT, cover INTEGER, name TEXT, isFavored INTEGER)");
            db.execSQL("CREATE TABLE IF NOT EXISTS Image(path TEXT PRIMARY KEY,  description TEXT, isFavored INTEGER)");
            db.execSQL("CREATE TABLE IF NOT EXISTS Album_Contain_Images(id INTEGER PRIMARY KEY AUTOINCREMENT, id_album INTEGER, path TEXT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS Image_Tag(id INTEGER PRIMARY KEY AUTOINCREMENT, Image_Path TEXT, Tag Text)");
        } catch (SQLiteException ignored) {
            ignored.printStackTrace();
        }


        // Set default fragment when open app
        getSupportFragmentManager().beginTransaction().replace(R.id.container, imageFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.album) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, albumFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.image) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, imageFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.itemBottomNav) {
                    showDialogNavBottom();
                }
                return false;
            }
        });
    }
    //Tam comment
    /*public static void printHashKey(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(TAG, "printHashKey()", e);
        }
    }*/
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }*/

    public void hideBottomNavigationView() {
        bottomNavigationView.setVisibility(View.GONE);
    }

    public void showBottomNavigationView() {
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public void requestPermissionStorageImage() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permissionsStr[0]) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, permissionsStr[0] + " Granted");
            isStorageImagePermitted = true;

        } else {
            request_permission_launcher_storage_images.launch(permissionsStr[0]);

        }
    }

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
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Alert for permission")
                .setMessage("Go to settings for Permissions")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent rIntent = new Intent();
                        rIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        rIntent.setData(uri);
                        startActivity(rIntent);
                        alertDialog.dismiss();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
    }

    public void requestInternetAccessPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permissionsStr[3]) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, permissionsStr[3] + " Granted");
            isInternetAccessPermitted = true;

        } else {
            request_permission_launcher_internet_access.launch(permissionsStr[3]);

        }
    }

    private ActivityResultLauncher<String> request_permission_launcher_internet_access =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Log.d(TAG, permissionsStr[3] + " Granted");
                            isInternetAccessPermitted = true;
                        } else {
                            Log.d(TAG, permissionsStr[3] + " Not Granted");
                            isInternetAccessPermitted = false;
                            sendToSettingDialog();
                        }
                    });

    public void requestPermissionCamera() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permissionsStr[4]) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, permissionsStr[4] + " Granted");
            isCameraPermitted = true;

        } else {
            request_permission_launcher_camera.launch(permissionsStr[4]);

        }
    }

    private ActivityResultLauncher<String> request_permission_launcher_camera =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Log.d(TAG, permissionsStr[4] + " Granted");
                            isCameraPermitted = true;
                        } else {
                            Log.d(TAG, permissionsStr[4] + " Not Granted");
                            isCameraPermitted = false;
                            sendToSettingDialog();
                        }
                    });

    public boolean hasManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return true;
        }
    }

    Button btnDownloadImage;
    Button btnOpenSettings;
    Button btnOpenHiddenFolder;
    Button btnOpenTrashFolder;

    Button btnDeleteDuplicateImage;

    private void showDialogNavBottom() {
        dialogNavBottom = new Dialog(MainActivity.this);
        dialogNavBottom.setContentView(R.layout.dialog_nav_bottom);

        dialogNavBottom.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogNavBottom.getWindow().setGravity(Gravity.BOTTOM);

        btnDownloadImage = (Button) dialogNavBottom.findViewById(R.id.buttonDownload);
        btnFavoriteAlbums = (Button) dialogNavBottom.findViewById(R.id.buttonFavoriteAlbums);
        btnFavoriteImages = (Button) dialogNavBottom.findViewById(R.id.buttonFavoriteImages);
        btnOpenSettings = (Button) dialogNavBottom.findViewById(R.id.buttonSettings);
        btnOpenHiddenFolder = (Button) dialogNavBottom.findViewById(R.id.buttonHidden);
        btnOpenTrashFolder = (Button) dialogNavBottom.findViewById(R.id.buttonTrash);
        btnDeleteDuplicateImage = (Button) dialogNavBottom.findViewById(R.id.buttonDeleteDuplicateImage);
        btnDownloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Loading ...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();

                //https://en.wikipedia.org/wiki/Wikipedia:In_the_news/Candidates#/media/File:Ryan_blaney_(52866797550)_(cropped).jpg
                String imageUrl="https://en.wikipedia.org/";//https://en.wikipedia.org/wiki/Main_Page#/media/File:Australiformis_Distribution.png";
                //downloadImage(imageUrl);
                String imageUrl2 = "https://en.wikipedia.org/wiki/Main_Page#/media/File:Australiformis_Distribution.png";
                //(imageUrl2);
                downLoadImage3(imageUrl2);*/
                showDownloadButton();
                dialogNavBottom.dismiss();
            }
        });

        btnFavoriteAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FavoriteAlbumsActivity.class);
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                if (fragment instanceof AlbumFragment) {
                    Log.d("aaaaa", "album");
                    AlbumFragment AlbumFragment = (AlbumFragment) fragment;
                    AlbumFragment.startIntentSeeFavoriteAlbums.launch(intent);
                } else {
                    Log.d("aaaaa", "image");
                    startActivity(intent);
                }
            }
        });

        btnFavoriteImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FavoriteImagesActivity.class);
                startActivity((intent));
            }
        });

     /*   btnDownloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FavoriteImagesActivity.class);
                startActivity(intent);
            }
        });*/

        btnOpenSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        btnOpenHiddenFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HiddenImageActivity.class);
                startActivity(intent);
            }
        });

        btnOpenTrashFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TrashImageActivity.class);
                startActivity(intent);
            }
        });
        btnDeleteDuplicateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: continue implement duplicate
                DeleteDuplicateAsyncTask deleteDuplicateAsyncTask = new DeleteDuplicateAsyncTask();
                deleteDuplicateAsyncTask.execute();
            }
        });

        dialogNavBottom.show();

    }

    public class DeleteDuplicateAsyncTask extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Void... voids) {
            deleteDuplicateImage();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Loading, please wait...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Intent intent_duplicate = new Intent(MainActivity.this, TrashImageActivity.class);
            /*intent_duplicate.putStringArrayListExtra("data", (ArrayList<String>) list);
            intent_duplicate.putExtra("name", "Duplicate Image");
            intent_duplicate.putExtra("duplicateImg", 2);
            intent_duplicate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/
            startActivity(intent_duplicate);
            progressDialog.cancel();
        }
    }

    public ArrayList<String> getAllImagePath() {
        ArrayList<String> result = new ArrayList<String>();

        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);

        if (SDCard) {

            final String[] projection = {MediaStore.Images.Media.DATA};

            final String order = MediaStore.Images.Media.DATE_ADDED + " DESC";
            ContentResolver contentResolver = this.getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    order);
            int count = cursor.getCount();

            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                int columnindex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                String imageLink = cursor.getString(columnindex);
                result.add(imageLink);
            }


        }
        return result;

    }

    public int deleteDuplicateImage() {
        ArrayList<String> imageList = getAllImagePath();// = getAll data of image
        long hash = 0;
        ArrayList<Long> hashImage = new ArrayList<Long>();
        for (String imagePath : imageList) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            hash = hashBitmap(bitmap);
            hashImage.add(hash);
        }
       /* for (int i=hashImage.size();i>=0;i--){

        }*/

        int count = 0;
        String trashFolder = Environment.getExternalStorageDirectory() + File.separator + ".trash_image_folder";
        for (int i = 0; i < hashImage.size(); i++) {
            for (int j = i + 1; j < hashImage.size(); j++) {
                if (hashImage.get(i).equals(hashImage.get(j))) {
                    File sourceImage = new File(imageList.get(j));
                    String oldImagePath = sourceImage.getAbsolutePath();
                    deleteImageFromDatabaseUsingImagePath(oldImagePath);


                    File destinationFile = FileUtility.moveImageToSecretFolder(sourceImage, trashFolder);

                    if (destinationFile!=null) {
                        boolean deletedFile = sourceImage.delete();
                        Log.d("hidden","res: "+deletedFile);
                    }

                    String newImagePath = destinationFile.getAbsolutePath();
                    updateTagOfImage(oldImagePath, newImagePath);
                    deleteImageOnExternalContentURIUsingImagePath(oldImagePath);
                    // change database
                    String[] args = {imageList.get(j)};
                    long rowID = MainActivity.db.delete("Image", "path = ?", args);
                    long rowID2 = MainActivity.db.delete("Album_Contain_Images", "path = ?", args);

                    count++;
                }
            }
        }

        Log.d("dup", "number: " + count);
        return count;
        //loadImage2(); this function to load image without query database

    }
    public void updateTagOfImage(String oldImagePath, String newImagePath) {
        ContentValues values = new ContentValues();
        values.put("Image_Path",newImagePath);
        String condition = "Image_Path = ?";
        String[] args = { oldImagePath };
        db.update("Image_Tag",values,condition,args);
    }

    public void deleteImageFromDatabaseUsingImagePath(String imagePath) {
        String tableName = "Image";
        String condition = "path = ?";
        String[] args = {imagePath};
        int deletedRows = db.delete(tableName,condition,args);

        int deletedRowInAlbum = db.delete("Album_Contain_Images",condition,args);
        Log.d("deletedRows","Count: "+deletedRows+" - albums: "+deletedRowInAlbum);
    }
    public void deleteImageOnExternalContentURIUsingImagePath(String imagePath) {
        ContentResolver contentResolver = getContentResolver();
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{ imagePath };

        int deletedRows = contentResolver.delete(imageUri, selection, selectionArgs);
        Log.d("deletedRows","External: "+deletedRows);
    }
    public long hashBitmap(Bitmap bmp) {
        long hash = 31;
        for (int x = 1; x < bmp.getWidth(); x = x * 2) {
            for (int y = 1; y < bmp.getHeight(); y = y * 2) {
                hash *= (bmp.getPixel(x, y) + 31);
                hash = hash % 1111122233;
            }
        }
        return hash;
    }

    Dialog downloadDialog;
    private EditText edtImageUrl;
    private Button btnStartDownload;
    private Button btnCancelDownload;

    private void showDownloadButton() {
        downloadDialog = new Dialog(MainActivity.this);
        downloadDialog.setContentView(R.layout.download_image_dialog);
        downloadDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        downloadDialog.getWindow().setGravity(Gravity.CENTER);


        edtImageUrl = (EditText) downloadDialog.findViewById(R.id.edtImageUrl);
        btnStartDownload = (Button) downloadDialog.findViewById(R.id.buttonDownloadImage);
        btnCancelDownload = (Button) downloadDialog.findViewById(R.id.buttonCancelDownload);


        progressDialog = new ProgressDialog(this);
        btnStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = edtImageUrl.getText().toString();

                progressDialog.show();
                downloadBitmap(url);
                progressDialog.dismiss();
                //new FetchImage(url).start();
            }
        });
        btnCancelDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadDialog.dismiss();
            }
        });


        downloadDialog.show();

    }

    boolean writeFileToStorage(ResponseBody body) {
        String nameOfFile = "Downloaded_image" + System.currentTimeMillis() + ".jpg";

        File location = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                location = new File(String.valueOf(MediaStore.Downloads.EXTERNAL_CONTENT_URI), nameOfFile);

                if (location.exists()) {
                    location.delete();
                }
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, nameOfFile);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, nameOfFile);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

                Uri uri = null;
                uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(uri, "w");
                FileDescriptor fileDescriptor = descriptor.getFileDescriptor();

                InputStream inputStream = body.byteStream();

                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;


                //OutputStream outputstream = new FileOutputStream(fileDescriptor);
                OutputStream outputstream = new ParcelFileDescriptor.AutoCloseOutputStream(descriptor);
                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputstream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                    Log.d(TAG, "File download size: " + fileSizeDownloaded + " from " + fileSize);

                }

                outputstream.flush();

                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputstream != null) {
                    outputstream.close();
                }


                File readLocation = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS) + "/" + nameOfFile);
                Log.d(TAG, "Read location: " + readLocation);
                //setImageDrawable(drawable.createFromPath(readLocation.sotString()));


            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    public void downloadImage(String fileUrl) {
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(fileUrl);
        Retrofit retrofit = builder.build();

        DownloadService downloadService = retrofit.create(DownloadService.class);
        Call<ResponseBody> call = downloadService.downloadFileFromUrl("wiki/Wikipedia:In_the_news/Candidates#/media/File:Ryan_blaney_(52866797550)_(cropped).jpg");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        boolean writeToDisk = writeFileToStorage(response.body());
                        Toast.makeText(MainActivity.this, "file downloaded or not status -> " + writeToDisk, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "file downloaded or not status -> " + writeToDisk);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d(TAG, "server connection error");
                    Toast.makeText(MainActivity.this, "server connection error", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Something went wrong");
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void downloadImage2(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            // Get the file name from the URL
            //tring fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String fileName = "Downloaded_image_group_9_" + System.currentTimeMillis() + imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            // Create a file in the device's external storage directory
            File directory = Environment.getExternalStorageDirectory();
            File file = new File(directory, fileName);

            // Create a FileOutputStream to write the image data to the file
            FileOutputStream fileOutput = new FileOutputStream(file);

            // Read the image data from the input stream and write it to the file
            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutput.write(buffer, 0, bytesRead);
            }

            // Close the streams
            fileOutput.close();
            inputStream.close();

            // Optionally, you can scan the downloaded image file with the MediaScanner
            // to make it available in the device's gallery or media library
            MediaScannerConnection.scanFile(
                    this,
                    new String[]{file.getAbsolutePath()},
                    null,
                    null
            );

            // Optionally, you can show a toast or log a message to indicate the successful download
            // Toast.makeText(this, "Image downloaded successfully", Toast.LENGTH_SHORT).show();
            // Log.d("Download", "Image downloaded successfully");

        } catch (IOException e) {
            e.printStackTrace();
            // Handle any errors that occurred during the download process
        }
    }

    public void downLoadImage3(String imageUrl) {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://example.com/") // Set the base URL of your API
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DownloadService imageApiService = retrofit.create(DownloadService.class);
        Call<ResponseBody> call = imageApiService.downloadFileFromUrl(imageUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Save the image to a file
                    saveImageToFile(response.body());
                } else {
                    // Handle unsuccessful response
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Handle failure
            }
        });
    }

    private void saveImageToFile(ResponseBody responseBody) {
        try {
            // Prepare the file to save the image
            File directory = Environment.getExternalStorageDirectory();
            File file = new File(directory, "image.jpg");

            // Create an input stream to read the response body
            InputStream inputStream = responseBody.byteStream();

            // Create an output stream to write the image data to the file
            OutputStream outputStream = new FileOutputStream(file);

            // Read the data from the input stream and write it to the output stream
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close the streams
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Optionally, you can scan the downloaded image file with the MediaScanner
            // to make it available in the device's gallery or media library
            MediaScannerConnection.scanFile(
                    this,
                    new String[]{file.getAbsolutePath()},
                    null,
                    null
            );

            // Optionally, you can show a toast or log a message to indicate the successful download
            // Toast.makeText(this, "Image downloaded successfully", Toast.LENGTH_SHORT).show();
            // Log.d("Download", "Image downloaded successfully");

        } catch (IOException e) {
            e.printStackTrace();
            // Handle any errors that occurred during the file-saving process
        }
    }

    private Handler mainHandler = new Handler();

    public class FetchImage extends Thread {
        String url;
        Bitmap bitmap;

        public FetchImage(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Loading ... ");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                }
            });

            InputStream inputStream = null;
            try {
                inputStream = new URL(this.url).openStream();
                this.bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        //save
                        String fileName = "download_image_group_9_" + url.substring(url.lastIndexOf("/") + 1);
                        saveBitmapToExternalStorage(bitmap, fileName);
                    }
                }
            });

        }
    }

    private void saveBitmapToExternalStorage(Bitmap bitmap, String fileName) {
        // Get the external storage directory
        String externalStorageDirectory = Environment.getExternalStorageDirectory().toString();

        // Create a file object with the desired directory and file name
        File file = new File(externalStorageDirectory, fileName);

        try {
            // Create an output stream to write the bitmap data to the file
            FileOutputStream outputStream = new FileOutputStream(file);

            // Compress the bitmap data into the output stream as JPEG with 100% quality (you can adjust the quality as needed)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            // Flush and close the output stream
            outputStream.flush();
            outputStream.close();

            // Optionally, you can trigger a media scan to make the saved image visible in the device's gallery or media library
            MediaScannerConnection.scanFile(
                    this,
                    new String[]{file.getAbsolutePath()},
                    null,
                    null
            );

            // Optionally, you can show a toast or log a message to indicate the successful save
            // Toast.makeText(this, "Bitmap saved successfully", Toast.LENGTH_SHORT).show();
            // Log.d("SaveBitmap", "Bitmap saved successfully");

        } catch (IOException e) {
            e.printStackTrace();
            // Handle any errors that occurred during the saving process
        }
    }

    protected Bitmap downloadBitmap(String imageUrl) {
        InputStream inputStream = null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK)
                inputStream = connection.getInputStream();
            else
                return null;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}


