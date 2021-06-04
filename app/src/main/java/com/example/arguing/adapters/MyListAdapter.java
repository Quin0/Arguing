package com.example.arguing.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.arguing.R;

import java.util.List;

public class MyListAdapter extends ArrayAdapter<String> {

    private Context mContext;
    int mResource;
    static List<String> mObjects;


    public MyListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mObjects = objects;
    }

    public boolean listIsEmpty(){
        if (mObjects.isEmpty()){
            return true;
        }else {
            return false;
        }
    }

    public static List<String> getmObjects() {
        return mObjects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String date = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);
        TextView tv = convertView.findViewById(R.id.textView);
        ImageView iv = convertView.findViewById(R.id.imageView);

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mObjects.remove(position);
                MyListAdapter.this.notifyDataSetChanged();
            }
        });

        tv.setText(date);

        return convertView;
    }
}
