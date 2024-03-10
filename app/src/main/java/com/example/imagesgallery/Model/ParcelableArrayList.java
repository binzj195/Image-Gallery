package com.example.imagesgallery.Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ParcelableArrayList implements Parcelable {
    private ArrayList<Image> images_list;
    public ParcelableArrayList(ArrayList<Image> images_list ){
        this.images_list = images_list;
    }
    protected ParcelableArrayList(Parcel in) {
    }

    public static final Creator<ParcelableArrayList> CREATOR = new Creator<ParcelableArrayList>() {
        @Override
        public ParcelableArrayList createFromParcel(Parcel in) {
            return new ParcelableArrayList(in);
        }

        @Override
        public ParcelableArrayList[] newArray(int size) {
            return new ParcelableArrayList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
    }

    public ArrayList<Image> getImages_list() {
        return images_list;
    }

    public void setImages_list(ArrayList<Image> images_list) {
        this.images_list = images_list;
    }
}
