package com.example.imagesgallery.Fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import com.example.imagesgallery.Activity.AddFavoriteAlbumActivity;
import com.example.imagesgallery.Activity.AlbumInfoActivity;
import com.example.imagesgallery.Activity.FavoriteAlbumsActivity;
import com.example.imagesgallery.Activity.MainActivity;
import com.example.imagesgallery.Adapter.AlbumAdapter;
import com.example.imagesgallery.Interface.ClickListener;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class AlbumFragment extends Fragment {

    GridView gridView;
    ArrayList<Album> DefaultAlbumArrayList, SearchAlbumArrayList, CurrentAlbumArrayList;
    AlbumAdapter albumAdapter;
    ImageButton btnAddAlbum;
    Button btnAdd, btnCancel;
    EditText edtNameAlbum;
    TextView txtTitleDialog;
    Dialog dialog;
    MainActivity mainActivity;
    Context context;
    ConstraintLayout constraintLayoutAlbum;
    ContentValues rowValues;

    // Default: default album (not found through search)
    // Search: albums are found through search
    // Current: current album (default, search)
    int CurrentClickPosition = -1, DefaultAlbumClickPosition = -1;
    ImageView imgCheckAlbum;
    Toolbar toolbar;
    private final int ItemsPerLoading = 10;
    boolean isLoading = false;
    private final int[] DefaultCurrentMaxPosition = {0}, SearchCurrentMaxPosition = {0};
    private final boolean[] isAllItemsDefaultLoaded = {false}, isAllItemsSearchLoaded = {false};
    private final int[] IdMaxWhenStartingLoadDataDefault = {0}, IdMaxWhenStartingLoadDataSearch = {0};
    private final String DefaultSearchName = "";
    private String SearchName = DefaultSearchName;
    SearchView searchView;
    AppCompatActivity activity;
    private ActivityResultLauncher<Intent> startIntentAlbumInfo, startIntentAddAlbumToFavorites;
    public ActivityResultLauncher<Intent> startIntentSeeFavoriteAlbums;

    ClickListener clickListener = new ClickListener() {
        @Override
        public void click(int index) {
            if (albumAdapter.isInMultiSelectMode()) {
                // set chosen album checked
                albumAdapter.toggleSelection(index);
                albumAdapter.notifyDataSetChanged();
            } else {
                CurrentClickPosition = index;
                if (context instanceof MainActivity || context instanceof FavoriteAlbumsActivity) {
                    // see information of album
                    Intent intent = new Intent(context, AlbumInfoActivity.class);

                    if (CurrentAlbumArrayList == SearchAlbumArrayList) {
                        for (int i = 0; i < DefaultAlbumArrayList.size(); i++) {
                            if (DefaultAlbumArrayList.get(i).getId() == CurrentAlbumArrayList.get(CurrentClickPosition).getId()) {
                                DefaultAlbumClickPosition = i;
                            }
                        }
                    }

                    intent.putExtra("album", (Serializable) CurrentAlbumArrayList.get(CurrentClickPosition));
                    startIntentAlbumInfo.launch(intent);
                } else if (context instanceof AddFavoriteAlbumActivity) {
                    // add clicked album to favorites
                    addAlbumToFavorites(CurrentClickPosition);
                }
            }
        }

        @Override
        public void longClick(int index) {
            if (CurrentAlbumArrayList == DefaultAlbumArrayList) {
                albumAdapter.setMultiSelectMode(true);
                enterMultiselectMode(index);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        }

        setHasOptionsMenu(true);
        initActivityResultLauncher();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        constraintLayoutAlbum = (ConstraintLayout) inflater.inflate(R.layout.fragment_album, container, false);
        init();

        // set minimum items per row of gridview = 2
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthInDp = displayMetrics.widthPixels / displayMetrics.density;
        int itemWidth = 200; // size of an image
        int ColumnCount = (int) screenWidthInDp / itemWidth; // the number of images in a row
        if (ColumnCount < 2) {
            gridView.setNumColumns(2);
        }

        DefaultAlbumArrayList = new ArrayList<>();
        SearchAlbumArrayList = new ArrayList<>();
        CurrentAlbumArrayList = DefaultAlbumArrayList;
        albumAdapter = new AlbumAdapter(context, DefaultAlbumArrayList, clickListener);
        rowValues = new ContentValues();
        gridView.setAdapter(albumAdapter);

        if (context instanceof AddFavoriteAlbumActivity) {
            btnAddAlbum.setVisibility(View.GONE);
        }

        // set toolbar
        activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        if (context instanceof MainActivity) {
            Objects.requireNonNull(activity.getSupportActionBar()).setTitle("");
        } else {
            if (context instanceof FavoriteAlbumsActivity) {
                Objects.requireNonNull(activity.getSupportActionBar()).setTitle("Favorite albums");
            } else if (context instanceof AddFavoriteAlbumActivity) {
                Objects.requireNonNull(activity.getSupportActionBar()).setTitle("Add album to favorites");
            }
            Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (albumAdapter.isInMultiSelectMode()) {
                    albumAdapter.setMultiSelectMode(false);
                    changeUI();
                    // cancel multi select mode
                    exitMultiselectMode();
                } else {
                    activity.finish();
                }
            }
        });

        // need to set them when load data to album tab the second time or more
        DefaultCurrentMaxPosition[0] = 0;
        isAllItemsDefaultLoaded[0] = false;
        IdMaxWhenStartingLoadDataDefault[0] = 0;


        // when click button add of activity
        btnAddAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context instanceof MainActivity) { // add new album
                    showDialog();
                } else if (context instanceof FavoriteAlbumsActivity) { // add an exist album to favorites
                    Intent intent = new Intent(context, AddFavoriteAlbumActivity.class);
                    startIntentAddAlbumToFavorites.launch(intent);
                } else if (context instanceof AddFavoriteAlbumActivity) { // add chosen albums to favorites
                    addAlbumsToFavorites();
                }
            }
        });

        // load on scroll
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstItem, int visibelItemCount, int totalItemCount) {
                boolean isAllItemLoaded = isAllItemsDefaultLoaded[0];
                if (CurrentAlbumArrayList == SearchAlbumArrayList)
                    isAllItemLoaded = isAllItemsSearchLoaded[0];

                if (!isLoading && absListView.getLastVisiblePosition() == totalItemCount - 1 && !isAllItemLoaded) {
                    isLoading = true;
                    // Create an executor that executes tasks in the main thread and background thread
                    Executor mainExecutor = ContextCompat.getMainExecutor(context);
                    ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();

                    // Load data in the background thread.
                    backgroundExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (CurrentAlbumArrayList == DefaultAlbumArrayList) {
                                loadDataFromDatabase(SearchName, CurrentAlbumArrayList, DefaultCurrentMaxPosition, isAllItemsDefaultLoaded, IdMaxWhenStartingLoadDataDefault);
                            } else if (CurrentAlbumArrayList == SearchAlbumArrayList) {
                                loadDataFromDatabase(SearchName, CurrentAlbumArrayList, SearchCurrentMaxPosition, isAllItemsSearchLoaded, IdMaxWhenStartingLoadDataSearch);
                            }
                            // Update gridview on the main thread
                            mainExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    albumAdapter.notifyDataSetChanged();
                                    isLoading = false;
                                }
                            });
                        }
                    });
                }
            }
        });

        return constraintLayoutAlbum;
    }

    private void addAlbumToFavorites(int clickPos) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("isFavored", 1);
        String[] args = {String.valueOf(CurrentAlbumArrayList.get(clickPos).getId())};
        long rowID = MainActivity.db.update("Album", contentValues, "id_album = ?", args);
        if (rowID > 0) {
            CurrentAlbumArrayList.get(CurrentClickPosition).setIsFavored(1);
            finishAddFavoriteAlbumActivity();
        } else {
            Toast.makeText(context, "Add failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void finishAddFavoriteAlbumActivity() {
        Intent resultIntent = new Intent();
        if (albumAdapter.isInMultiSelectMode()) {
            ArrayList<Album> selectedAlbums = albumAdapter.getSelectedAlbums();
            resultIntent.putExtra("AlbumsAddedToFavorites", selectedAlbums);
        } else {
            Album album = CurrentAlbumArrayList.get(CurrentClickPosition);
            resultIntent.putExtra("AlbumAddedToFavorites", album);
        }
        activity.setResult(Activity.RESULT_OK, resultIntent);
        activity.finish();
    }

    // Load album from database and add to arraylist
    private void loadDataFromDatabase(String SearchName, ArrayList<Album> albumArrayList, int[] currentMaxPosition, boolean[] isAllItemsLoaded, int[] IdMaxWhenStartingLoadData) {
        String sql = "";
        Cursor cursor = null;
        if (IdMaxWhenStartingLoadData[0] == 0) {
            String[] argsAlbum = {String.valueOf(ItemsPerLoading), String.valueOf(currentMaxPosition[0])};
            try {
                sql = "SELECT MAX(id_album) FROM Album";
                cursor = MainActivity.db.rawQuery(sql, null);
            } catch (Exception exception) {
                return;
            }

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                IdMaxWhenStartingLoadData[0] = cursor.getInt(0);
            }
        }
        String[] argsAlbum = {String.valueOf(IdMaxWhenStartingLoadData[0]), "%" + SearchName + "%", String.valueOf(ItemsPerLoading), String.valueOf(currentMaxPosition[0])};
        try {
            if (context instanceof MainActivity || context instanceof AddFavoriteAlbumActivity) {
                sql = "SELECT * FROM Album WHERE id_album <= ? AND name LIKE ? ORDER BY id_album DESC LIMIT ? OFFSET ?";
            } else if (context instanceof FavoriteAlbumsActivity) {
                sql = "SELECT * FROM Album WHERE isFavored = 1 AND id_album <= ? AND name LIKE ? ORDER BY id_album DESC LIMIT ? OFFSET ?";
            }
            cursor = MainActivity.db.rawQuery(sql, argsAlbum);
        } catch (Exception exception) {
            Toast.makeText(context, "Some errors have occured while loading data", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cursor.moveToFirst()) {
            isAllItemsLoaded[0] = true;
        }
        cursor.moveToPosition(-1);
        // load data and add album to arrayList
        while (cursor.moveToNext()) {
            int idAlbumColumn = cursor.getColumnIndex("id_album");
            int descriptionAlbumColumn = cursor.getColumnIndex("description");
            int nameAlbumColumn = cursor.getColumnIndex("name");
            int isFavoredAlbumColumn = cursor.getColumnIndex("isFavored");
            int coverAlbumColumn = cursor.getColumnIndex("cover");

            int idAlbum = cursor.getInt(idAlbumColumn);
            String descriptionAlbum = cursor.getString(descriptionAlbumColumn);
            String nameAlbum = cursor.getString(nameAlbumColumn);
            int isFavoredAlbum = cursor.getInt(isFavoredAlbumColumn);
            String coverAlbum = cursor.getString(coverAlbumColumn);

            String[] args = {coverAlbum};
            Cursor cursorImage = null;
            try {
                cursorImage = MainActivity.db.rawQuery("SELECT * FROM Image WHERE path = ?", args);
            } catch (Exception exception) {
                return;
            }
            cursorImage.moveToPosition(-1);
            int pathImageColumn = cursorImage.getColumnIndex("path");
            int descriptionImageColumn = cursorImage.getColumnIndex("description");
            int isFavoredImageColumn = cursorImage.getColumnIndex("isFavored");

            String pathImage = MainActivity.pathNoImage;
            String descriptionImage = "";
            int isFavoredImage = 0;

            while (cursorImage.moveToNext()) {
                descriptionImage = cursorImage.getString(descriptionImageColumn);
                isFavoredImage = cursorImage.getInt(isFavoredImageColumn);
                pathImage = cursorImage.getString(pathImageColumn);
            }
            cursorImage.close();

            albumArrayList.add(new Album(new Image(pathImage, descriptionImage, isFavoredImage), nameAlbum, descriptionAlbum, isFavoredAlbum, idAlbum, new ArrayList<>()));
        }

        cursor.close();
        currentMaxPosition[0] += ItemsPerLoading;
    }

    private void init() {
        gridView = (GridView) constraintLayoutAlbum.findViewById(R.id.gridview_album);
        btnAddAlbum = (ImageButton) constraintLayoutAlbum.findViewById(R.id.btnAdd_album);
        toolbar = (Toolbar) constraintLayoutAlbum.findViewById(R.id.toolbar);
    }

    // show dialog when click button add album
    private void showDialog() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_album);

        btnAdd = (Button) dialog.findViewById(R.id.buttonAdd);
        btnCancel = (Button) dialog.findViewById(R.id.buttonCancel);
        edtNameAlbum = (EditText) dialog.findViewById(R.id.edtAlbumName);
        txtTitleDialog = (TextView) dialog.findViewById(R.id.title_dialog);

        // when click button add of dialog
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtNameAlbum.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(context, "Please enter name of album", Toast.LENGTH_SHORT).show();
                } else {
                    rowValues.clear();
                    rowValues.put("description", "");
                    rowValues.put("isFavored", 0);
                    rowValues.put("name", name);
                    rowValues.put("cover", MainActivity.pathNoImage);
                    long rowId = MainActivity.db.insert("Album", null, rowValues);
                    DefaultAlbumArrayList.add(0, new Album(new Image(MainActivity.pathNoImage, "", 0), name, "", 0, (int) rowId, new ArrayList<>()));
                    albumAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });

        // when click button cancel of dialog
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        resizeDialog();
    }

    // resize the dialog to fit the screen size
    private void resizeDialog() {
        // resize dialog size
        Display display = ((WindowManager) context.getSystemService(mainActivity.getApplicationContext().WINDOW_SERVICE)).getDefaultDisplay();
        int width = context.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        Objects.requireNonNull(dialog.getWindow()).setLayout((6 * width) / 7, ViewGroup.LayoutParams.WRAP_CONTENT);

        // get screen size
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float screenWidth = displayMetrics.widthPixels;

        // resize text size
        float newTextSize = screenWidth * 0.05f;
        edtNameAlbum.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);

        newTextSize = screenWidth * 0.08f;
        txtTitleDialog.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);

        newTextSize = screenWidth * 0.04f;
        btnAdd.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
    }

    private void exitMultiselectMode() {
        albumAdapter.setMultiSelectMode(false);
        albumAdapter.clearSelection();
        changeUI();
    }

    private void enterMultiselectMode(int index) {
        albumAdapter.setMultiSelectMode(true);
        albumAdapter.toggleSelection(index);
        changeUI();
    }

    // change UI of activity when enter or exit multi selection mode
    private void changeUI() {
        if (albumAdapter.isInMultiSelectMode()) {
            if (context instanceof MainActivity) {
                mainActivity.hideBottomNavigationView();
            }
            if (context instanceof AddFavoriteAlbumActivity) {
                btnAddAlbum.setVisibility(View.VISIBLE);
            } else {
                btnAddAlbum.setVisibility(View.GONE);
            }
            activity.invalidateOptionsMenu();
        } else {
            if (context instanceof MainActivity) {
                mainActivity.showBottomNavigationView();
            }
            if (context instanceof AddFavoriteAlbumActivity) {
                btnAddAlbum.setVisibility(View.GONE);
            } else {
                btnAddAlbum.setVisibility(View.VISIBLE);
            }
            activity.invalidateOptionsMenu();
        }
    }

    private void addAlbumsToFavorites() {
        ArrayList<Album> selectedAlbums = albumAdapter.getSelectedAlbums();
        ContentValues contentValues = new ContentValues();
        contentValues.put("isFavored", 1);

        // change database
        MainActivity.db.beginTransaction();
        try {
            for (int i = 0; i < selectedAlbums.size(); i++) {
                String[] args = {String.valueOf(selectedAlbums.get(i).getId())};
                MainActivity.db.update("Album", contentValues, "id_album = ?", args);
            }
            MainActivity.db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
        } finally {
            MainActivity.db.endTransaction();
            for (int i = 0; i < selectedAlbums.size(); i++) {
                selectedAlbums.get(i).setIsFavored(1);
            }
            finishAddFavoriteAlbumActivity();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (albumAdapter.isInMultiSelectMode()) {
            requireActivity().getMenuInflater().inflate(R.menu.menu_album_home_page_long_click, menu);
            Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(activity.getSupportActionBar()).setHomeAsUpIndicator(R.drawable.close_icon);
            if (context instanceof MainActivity) {
                menu.findItem(R.id.removeAlbumsFromFavorites).setVisible(false);
            } else if (context instanceof AddFavoriteAlbumActivity) {
                menu.findItem(R.id.removeAlbumsFromFavorites).setVisible(false);
                menu.findItem(R.id.deleteAlbums).setVisible(false);
            }
        } else {
            requireActivity().getMenuInflater().inflate(R.menu.menu_album_home_page, menu);
            if (context instanceof MainActivity) {
                Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            } else {
                Objects.requireNonNull(activity.getSupportActionBar()).setHomeAsUpIndicator(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            }
            MenuItem menuItemSearch = menu.findItem(R.id.search_album);
            searchView = (SearchView) menuItemSearch.getActionView();
            searchView.setMaxWidth(Integer.MAX_VALUE);

            // when click enter to search
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // hide other views
                    btnAddAlbum.setVisibility(View.GONE);
                    if (context instanceof MainActivity) {
                        mainActivity.hideBottomNavigationView();
                    }

                    // load data
                    SearchName = query;
                    SearchCurrentMaxPosition[0] = 0;
                    isAllItemsSearchLoaded[0] = false;
                    IdMaxWhenStartingLoadDataSearch[0] = 0;
                    SearchAlbumArrayList.clear();
                    CurrentAlbumArrayList = SearchAlbumArrayList;
                    albumAdapter.setAlbumArrayList(CurrentAlbumArrayList);
                    loadDataFromDatabase(SearchName, CurrentAlbumArrayList, SearchCurrentMaxPosition, isAllItemsSearchLoaded, IdMaxWhenStartingLoadDataSearch);
                    searchView.clearFocus();
                    albumAdapter.notifyDataSetChanged();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });


            menuItemSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                // when click search button
                @Override
                public boolean onMenuItemActionExpand(@NonNull MenuItem menuItem) {
                    return true;
                }

                // when click button back on SearchView
                @Override
                public boolean onMenuItemActionCollapse(@NonNull MenuItem menuItem) {
                    // show other views
                    if (context instanceof MainActivity || context instanceof FavoriteAlbumsActivity) {
                        btnAddAlbum.setVisibility(View.VISIBLE);
                    }
                    if (context instanceof MainActivity) {
                        mainActivity.showBottomNavigationView();
                    }

                    // load data
                    SearchName = "";
                    SearchAlbumArrayList.clear();
                    CurrentAlbumArrayList = DefaultAlbumArrayList;
                    albumAdapter.setAlbumArrayList(CurrentAlbumArrayList);
                    albumAdapter.notifyDataSetChanged();
                    return true;
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.deleteAlbums) {
            createDialogDeleteAlbums();
        } else if (itemID == R.id.removeAlbumsFromFavorites) {
            createDialogRemoveAlbums();
        }
        return super.onOptionsItemSelected(item);
    }

    private void createDialogRemoveAlbums() {
        if (albumAdapter.getSelectedAlbums().size() == 0) {
            Toast.makeText(context, "You have not chosen any albums", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to remove these albums from favorites?");

        // click yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                removeAlbumsFromFavorites();
            }
        });
        // click no
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeAlbumsFromFavorites() {
        ArrayList<Album> selectedAlbums = albumAdapter.getSelectedAlbums();
        ContentValues contentValues = new ContentValues();
        contentValues.put("isFavored", 0);
        String[] args = {""};
        // change database
        MainActivity.db.beginTransaction();
        try {
            for (int i = 0; i < selectedAlbums.size(); i++) {
                args[0] = String.valueOf(selectedAlbums.get(i).getId());
                MainActivity.db.update("Album", contentValues, "id_album = ?", args);
            }
            MainActivity.db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Toast.makeText(context, "Remove failed", Toast.LENGTH_SHORT).show();
        } finally {
            MainActivity.db.endTransaction();

            for (Album selectedAlbum : selectedAlbums) {
                CurrentAlbumArrayList.remove(selectedAlbum);
            }
            albumAdapter.notifyDataSetChanged();
            exitMultiselectMode();
        }
    }

    public void createDialogDeleteAlbums() {
        if (albumAdapter.getSelectedAlbums().size() == 0) {
            Toast.makeText(context, "You have not chosen any albums", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to deletes these albums ?");

        // click yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAlbums();
            }
        });
        // click no
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAlbums() {
        ArrayList<Album> selectedAlbums = albumAdapter.getSelectedAlbums();
        String[] args = {""};
        // change database
        MainActivity.db.beginTransaction();
        try {
            for (int i = 0; i < selectedAlbums.size(); i++) {
                ContentValues contentValues = new ContentValues();
                args[0] = String.valueOf(selectedAlbums.get(i).getId());
                MainActivity.db.delete("Album", "id_album = ?", args);
                MainActivity.db.delete("Album_Contain_Images", "id_album = ?", args);
            }
            MainActivity.db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
        } finally {
            MainActivity.db.endTransaction();

            for (Album selectedAlbum : selectedAlbums) {
                CurrentAlbumArrayList.remove(selectedAlbum);
            }
            albumAdapter.notifyDataSetChanged();
            exitMultiselectMode();
        }
    }

    public void initActivityResultLauncher() {
        // when click button back in toolbar or in smartphone to finish AlbumInfoActivity
        startIntentAlbumInfo = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String path = data.getStringExtra("CoverPath");
                            String description = data.getStringExtra("description");
                            int isDelete = data.getIntExtra("isDelete", 0);
                            int isFavored = data.getIntExtra("isFavored", 0);
                            String newName = data.getStringExtra("newName");
                            ArrayList<Image> imageArrayListAfterChange = (ArrayList<Image>) data.getSerializableExtra("images");

                            // change images in album if user choose button add image or delete image in album
                            if (imageArrayListAfterChange != null) {
                                CurrentAlbumArrayList.get(CurrentClickPosition).setListImage(imageArrayListAfterChange);
                                if (CurrentAlbumArrayList == SearchAlbumArrayList) {
                                    DefaultAlbumArrayList.get(DefaultAlbumClickPosition).setListImage(imageArrayListAfterChange);
                                }
                            }
                            // remove data if user choose delete album
                            if (isDelete != 0) {
                                CurrentAlbumArrayList.remove(CurrentClickPosition);
                                if (CurrentAlbumArrayList == SearchAlbumArrayList) {
                                    DefaultAlbumArrayList.remove(DefaultAlbumClickPosition);
                                }
                            } else { // change data of album if user change cover or description
                                Album album = CurrentAlbumArrayList.get(CurrentClickPosition);
                                if (path != null) {
                                    Image image = album.getCover();
                                    image.setPath(path);
                                    album.setCover(image);
                                }
                                if (description != null) {
                                    album.setDescription(description);
                                }
                                if (newName != null) {
                                    album.setName(newName);
                                }
                                album.setIsFavored(isFavored);

                                if (context instanceof FavoriteAlbumsActivity && isFavored == 0) {
                                    // if user remove album from Favorites, remove it from arrayList
                                    CurrentAlbumArrayList.remove(CurrentClickPosition);
                                    if (CurrentAlbumArrayList == SearchAlbumArrayList) {
                                        DefaultAlbumArrayList.remove(DefaultAlbumClickPosition);
                                    }

                                } else if (context instanceof MainActivity || context instanceof FavoriteAlbumsActivity) {
                                    // update changes of album to arrayList
                                    CurrentAlbumArrayList.set(CurrentClickPosition, album);
                                    if (CurrentAlbumArrayList == SearchAlbumArrayList) {
                                        DefaultAlbumArrayList.set(DefaultAlbumClickPosition, album);
                                    }
                                }
                            }
                            albumAdapter.notifyDataSetChanged();
                        }
                    }
                }
        );

        // after choosing an album to add to favorites and finish AddFavoriteAlbumActivity
        startIntentAddAlbumToFavorites = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Album addedAlbum = (Album) data.getSerializableExtra("AlbumAddedToFavorites");
                            ArrayList<Album> addedAlbums = (ArrayList<Album>) data.getSerializableExtra("AlbumsAddedToFavorites");
                            if (addedAlbum != null) {
                                CurrentAlbumArrayList.add(0, addedAlbum);
                                albumAdapter.notifyDataSetChanged();
                            }
                            if (addedAlbums != null) {
                                CurrentAlbumArrayList.addAll(addedAlbums);
                                albumAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
        );

        // when click button back in toolbar or in smartphone to finish FavoriteAlbum
        startIntentSeeFavoriteAlbums = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    CurrentAlbumArrayList.clear();
                    DefaultCurrentMaxPosition[0] = 0;
                    isAllItemsDefaultLoaded[0] = false;
                    IdMaxWhenStartingLoadDataDefault[0] = 0;
                    albumAdapter.notifyDataSetChanged();
                }
        );
    }
}