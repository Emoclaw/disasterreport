package com.karakostas.disasterreport;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class EarthquakeDetailsAdapter extends BaseAdapter {
    private Context context; //context
    private ArrayList<String> items; //data source of the list adapter
    private ArrayList<Integer> images;
    private boolean nightMode;
    //public constructor
    EarthquakeDetailsAdapter(Context context, ArrayList<String> text, ArrayList<Integer> images,boolean nightMode) {
        this.context = context;
        this.items = text;
        this.images = images;
        this.nightMode = nightMode;
    }

    @Override
    public int getCount() {
        return items.size(); //returns total of items in the list
    }

    @Override
    public Object getItem(int position) {
        return items.get(position); //returns list item at the specified position
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the layout for each list row
        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.listview_details, parent, false);
        }

        // get current item to be displayed
        String currentItem = (String) getItem(position);

        // get the TextView for item name and item description
        TextView textViewItemName = convertView.findViewById(R.id.earthquake_listView_textView);
        ImageView imageView = convertView.findViewById(R.id.earthquake_listView_imageView);
        if(nightMode){
            textViewItemName.setTextColor(Color.WHITE);
        } else {
            textViewItemName.setTextColor(Color.BLACK);
        }
        //sets the text for item name and item description from the current item object
        if (images.get(position) == R.drawable.ic_location) {
            String[] s = currentItem.split("\n");
            SpannableString s1 = new SpannableString(s[0] + "\n");
            SpannableString s2 = new SpannableString(s[1]);
            s2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s2.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            s2.setSpan(new RelativeSizeSpan(0.87f), 0, s2.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(s1);
            builder.append(s2);
            textViewItemName.setText(builder);
        } else {
            textViewItemName.setText(currentItem);
        }
        imageView.setImageResource(images.get(position));
        // returns the view for the current row
        return convertView;
    }
}