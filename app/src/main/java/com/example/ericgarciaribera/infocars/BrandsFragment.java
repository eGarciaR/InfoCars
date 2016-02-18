package com.example.ericgarciaribera.infocars;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ericgarciaribera on 07/06/15.
 */
public class BrandsFragment extends Fragment {

    public ArrayAdapter<String> brandsAdapter;

    //public ArrayAdapter<String> modelsAdapter;

    public String[] modelsAdapter;

    static DBHelper DB;

    public int size;

    public BrandsFragment() {
    }

    public static String model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.brandsfragment, menu);
    }

    private void updateModels() {
        DB = new DBHelper(getActivity(), model, null, 1);
        FetchModelsTask weatherTask = new FetchModelsTask();
        weatherTask.execute(model);
    }

    private void updateDB() {
        DB.deleteIfExists();
        for (int i = 0; i < size; ++i) {
            DB.addModel(model + " " + modelsAdapter[i]);
        }
        Intent intent = new Intent(getActivity(), ModelsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle action bar item clicks here. The action bar will automatically
        //handle clicks on the Home/Up button, so long as you specify a parent
        //activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final String[] brandsArray = {
                "Audi",
                "BMW",
                "Bugatti",
                "Chevrolet",
                "Ferrari",
                "Ford",
                "Honda",
                "Jaguar",
                "LandRover",
                "Mazda",
                "Mercedes-Benz",
                "Mini",
                "Mitsubishi",
                "Saab",
                "Subaru",
                "Suzuki",
                "Volkswagen"
        };

        List<String> models = new ArrayList<String>(Arrays.asList(brandsArray));

        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        brandsAdapter = new ArrayAdapter<String>(
                getActivity(), // The current context(this activity)
                R.layout.list_item_brands, // The name of the layout ID
                R.id.list_item_brands_textview, // The ID of the textview to populate
                models);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_brands);

        listView.setAdapter(brandsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                model = brandsArray[position];
                updateModels();
            }
        });
        return rootView;
    }

    public class FetchModelsTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchModelsTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String modelsJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.

            final String API_NAME = "name";
            final String API_INFO = "models";
            final String API_COUNT = "modelsCount";

            JSONObject modelsJson = new JSONObject(modelsJsonStr);
            JSONArray modelsArray = modelsJson.getJSONArray(API_INFO);

            int numModels = modelsJson.getInt(API_COUNT);

            String[] resultStrs = new String[numModels];
            size = modelsArray.length();
            for(int i = 0; i < size; i++) {

                String name;
                // Get the JSON object representing the model
                JSONObject modelName = modelsArray.getJSONObject(i);

                name = modelName.getString(API_NAME);

                resultStrs[i] = name;
            }
            return resultStrs;
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
                return getWeatherDataFromJson(modelsJsonStr);
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
                //erase(modelsAdapter);
                modelsAdapter = new String[strings.length];
                int i = 0;
                for (String models : strings) {
                    modelsAdapter[i] = models;
                    ++i;
                }
                // New data is back from the server.
                updateDB();
            }
        }
    }
}
