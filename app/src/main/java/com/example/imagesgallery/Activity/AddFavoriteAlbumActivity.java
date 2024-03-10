package com.example.imagesgallery.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.imagesgallery.Fragment.AlbumFragment;
import com.example.imagesgallery.R;

public class AddFavoriteAlbumActivity extends AppCompatActivity {

    AlbumFragment albumFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_favorite_album);

        albumFragment = new AlbumFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, albumFragment).commit();

    }
}