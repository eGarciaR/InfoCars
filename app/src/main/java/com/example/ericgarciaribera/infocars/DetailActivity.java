package com.example.ericgarciaribera.infocars;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ericgarciaribera on 10/06/15.
 */
public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_info);
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailActivityFragment.DETAIL_URI, getIntent().getData());

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_detail_container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            CharSequence text = "No settings yet.";
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private String infoModel;

        private static final String[] INFO_COLUMNS = {
                Contract.Info.TABLE_INFO + "." + Contract.Info._ID,
                Contract.Info.COLUMN_INFO_NAME};

        String Model="";
        String Brand="";
        String pos="";

        private ImageView mIconView;
        private TextView mEngineType;
        private TextView mEngineName;
        private TextView mCylinders;
        private TextView mCompressionRatio;
        private TextView mPower;
        private TextView mFuelType;

        static final String DETAIL_URI = "URI";

        public DetailActivityFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(0, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // The detail Activity called via intent. Inspect the intent for forecast data.
            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.detail_activity_fragment, container, false);
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                infoModel = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
            if (infoModel != null) separateInfo();

            mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
            mEngineName = (TextView) rootView.findViewById(R.id.engine_name_textview);
            mEngineType = (TextView) rootView.findViewById(R.id.engine_type_textview);
            mCylinders = (TextView) rootView.findViewById(R.id.detail_cylinders_textview);
            mCompressionRatio = (TextView) rootView.findViewById(R.id.detail_compression_textview);
            mPower = (TextView) rootView.findViewById(R.id.detail_power_textview);
            mFuelType = (TextView) rootView.findViewById(R.id.detail_fuel_textview);

            FetchInfoTask infoTask = new FetchInfoTask();
            infoTask.execute(Brand, Model, pos);
            return rootView;
        }

        private void separateInfo() {
            boolean b = false;
            boolean f = false;
            for (int i = 0; i < infoModel.length(); ++i) {
                if (infoModel.charAt(i) == ' ' && (!b)) {
                    b=true;
                    ++i;
                }
                if (infoModel.charAt(i) == ' ' && b && (!f)) {
                    f = true;
                    ++i;
                }
                if (!b && !f) pos += infoModel.charAt(i);
                if (infoModel.charAt(i) == ' ' && b && f) {
                    Model += ' ';
                    ++i;
                }
                if (b && !f) Brand += infoModel.charAt(i);
                if (b && f) Model += infoModel.charAt(i);
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    MyContentProvider.CONTENT_URI_INFO,
                    INFO_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null && data.moveToFirst()) {

                String[] result = new String[data.getCount()];
                for (int i = 0; i < result.length; ++i) {
                    result[i] = data.getString(1);
                    data.moveToNext();
                }

                mEngineName.setText("Name:  " + result[0]);
                mEngineType.setText("Type:  " + result[1]);
                mFuelType.setText("Fuel type:  " + result[2]);
                mCylinders.setText("Cylinders:  " + result[3]);
                mCompressionRatio.setText("Compression Ratio:  " + result[4]);
                mPower.setText("Power:  " + result[5] + "cv");

                mIconView.setImageResource(R.drawable.ic_engine);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }

        public class FetchInfoTask extends AsyncTask<String, Void, String[]> {

            private final String LOG_TAG = FetchInfoTask.class.getSimpleName();

            /**
             * Take the String representing the complete forecast in JSON Format and
             * pull out the data we need to construct the Strings needed for the wireframes.
             *
             * Fortunately parsing is easy:  constructor takes the JSON string and converts it
             * into an Object hierarchy for us.
             */
            private String getWeatherDataFromJson(String modelsJsonStr, String model, String pos)
                    throws JSONException {

                // These are the names of the JSON objects that need to be extracted.

                final String API_YEAR = "years";
                final String API_INFO = "models";
                final String API_STYLES = "styles";
                final String API_ID = "id";
                final String modelId;

                JSONObject modelsJson = new JSONObject(modelsJsonStr);
                JSONArray modelsArray = modelsJson.getJSONArray(API_INFO);
                JSONObject modelYear = modelsArray.getJSONObject(Integer.valueOf(pos));
                JSONArray yearsArray = modelYear.getJSONArray(API_YEAR);

                int size = yearsArray.length();

                JSONObject stylesJson = yearsArray.getJSONObject(size - 1);
                JSONArray stylesArray = stylesJson.getJSONArray(API_STYLES);
                JSONObject idJson = stylesArray.getJSONObject(0);

                modelId = idJson.getString(API_ID);

                return modelId;
            }

            @Override
            protected String[] doInBackground(String... params) {

                // If there is no zip code, there is nothing to look up. Verify size of params.
                if (params.length == 0) {
                    return null;
                }
                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String modelsJsonStr = null;

                try {
                    // Construct the URL for the API query

                    final String MODELS_BASE_URL = "https://api.edmunds.com/api/vehicle/v2/";
                    final String MODELS_ENDING_URL = "/models?fmt=json&api_key=d6udj8y2wc5e6x8edx83angy";

                    final String MODELS_FINAL_URL = MODELS_BASE_URL + params[0] + MODELS_ENDING_URL;

                    Uri builtUri = Uri.parse(MODELS_FINAL_URL).buildUpon().build();

                    URL url = new URL(builtUri.toString());

                    // Create the request to edmunds API, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    modelsJsonStr = buffer.toString();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attempting
                    // to parse it.
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }

                try {
                    String[] s = new String[1];
                    s[0] = getWeatherDataFromJson(modelsJsonStr, params[1], params[2]);
                    return s;
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                // This will only happen if there was an error getting or parsing the forecast.
                return null;
            }

            @Override
            protected void onPostExecute(String[] strings) {
                if (strings != null) {
                    infoModelFragment.FetchInfoTask detailTask = new infoModelFragment.FetchInfoTask();
                    detailTask.execute(strings);
                }
            }
        }
    }
}