package com.example.imagesgallery.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.imagesgallery.Activity.MainActivity;
import com.example.imagesgallery.R;

import java.util.ArrayList;
import java.util.List;

public class SortAdapter extends ArrayAdapter<String> {
    Context context;

    public SortAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> items) {
        super(context,resource, items);
        this.context = context;
        this.items = items;

    }

    ArrayList<String> items;

    MainActivity mainActivity;

    public class ViewHolder {
        TextView textView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView==null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_sort_spinner_view, null);

            viewHolder.textView = convertView.findViewById(R.id.txtViewSortSpinner);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textView.setText(items.get(position));

        return convertView;
    }


}
