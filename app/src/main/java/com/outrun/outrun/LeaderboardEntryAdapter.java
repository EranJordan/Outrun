package com.outrun.outrun;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class LeaderboardEntryAdapter extends ArrayAdapter<LeaderboardListEntry> {
        private ArrayList<LeaderboardListEntry> dataSet;
        Context mContext;

// View lookup cache
    private static class ViewHolder {
        ImageView photo;
        TextView txtName;
        TextView txtTime;
    }
    public LeaderboardEntryAdapter(ArrayList<LeaderboardListEntry> data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        LeaderboardListEntry entry = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.photo = convertView.findViewById(R.id.photo_imageView);
            viewHolder.txtName = convertView.findViewById(R.id.name_textView);
            viewHolder.txtTime = convertView.findViewById(R.id.time_textView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Glide.with(mContext).load(entry.photo).into(viewHolder.photo);
        viewHolder.txtName.setText(String.valueOf(entry.name));
        viewHolder.txtTime.setText(String.valueOf(entry.time));


        return convertView;
    }
}
