package com.example.ericgarciaribera.infocars;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by ericgarciaribera on 09/06/15.
 */
public class ModelsAdapter extends CursorAdapter{

    public ModelsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type

        View view = LayoutInflater.from(context).inflate(R.layout.list_item_brands, parent, false);


        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView tv = (TextView)view;
        tv.setText(cursor.getString(1));

    }
}
