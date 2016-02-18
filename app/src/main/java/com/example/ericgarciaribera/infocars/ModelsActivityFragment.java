package com.example.ericgarciaribera.infocars;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by ericgarciaribera on 14/06/15.
 */
public class ModelsActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private ModelsAdapter modelsAdapter;

    static final String DETAIL_URI = "URI";

    private ListView listView;

    private int mPosition = ListView.INVALID_POSITION;

    private static final String[] MODELS_COLUMNS = {
            Contract.Model.TABLE_NAME + "." + Contract.Model._ID,
            Contract.Model.COLUMN_MODEL_NAME};

    public ModelsActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        modelsAdapter = new ModelsAdapter(getActivity(), null, 0);

        getLoaderManager().initLoader(0, null, this);

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        listView = (ListView) rootView.findViewById(R.id.listview_ModelsActivity);
        listView.setAdapter(modelsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                String send = position + " " + cursor.getString(1);
                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, send);
                    startActivity(intent);
                }
                mPosition = position;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        CursorLoader cursorLoader=  new CursorLoader(getActivity(),
                MyContentProvider.CONTENT_URI_MODELS,
                MODELS_COLUMNS,
                null,
                null,
                null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        modelsAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            listView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        modelsAdapter.swapCursor(null);
    }
}
