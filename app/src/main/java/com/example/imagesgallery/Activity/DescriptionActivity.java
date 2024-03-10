package com.example.imagesgallery.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.util.Map;
import java.util.Objects;

public class DescriptionActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText edtDescription;
    Album album;
    Image image;
    long rowID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        init();

        // set description (album)
        album = (Album) getIntent().getSerializableExtra("album");
        if (album != null) {
            edtDescription.setText(album.getDescription());
        }

        // set description (image)
        image = (Image) getIntent().getSerializableExtra("image");
        if (image != null) {
            edtDescription.setText(image.getDescription());
        }

        edtDescription.setFocusableInTouchMode(false);
        edtDescription.setFocusable(false);

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Description");

        // set return button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });

    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        edtDescription = (EditText) findViewById(R.id.edtDescriptionAlbum);
    }

    @Override
    public void onBackPressed() {
        finishActivity();
        super.onBackPressed();
    }

    private void finishActivity() {
        if (rowID > 0) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("description", edtDescription.getText().toString());
            setResult(Activity.RESULT_OK, resultIntent);
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_description, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.changeDescription) {
            if (edtDescription.isFocusableInTouchMode()) { // click done
                String description_changed = edtDescription.getText().toString();

                ContentValues contentValues = new ContentValues();
                contentValues.put("description", description_changed);
                String[] args = {""};

                if (album != null) {
                    args[0] = String.valueOf(album.getId());
                    rowID = MainActivity.db.update("Album", contentValues, "id_album = ?", args);
                } else if (image != null) {
                    args[0] = image.getPath();
                    rowID = MainActivity.db.update("Image", contentValues, "path = ?", args);
                }

                if (rowID > 0) {
                    edtDescription.setText(description_changed);
                } else {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                }

                edtDescription.setFocusable(false);
                item.setIcon(R.drawable.edit);
            } else { // click edit
                item.setIcon(R.drawable.done);
                edtDescription.setFocusableInTouchMode(true);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}