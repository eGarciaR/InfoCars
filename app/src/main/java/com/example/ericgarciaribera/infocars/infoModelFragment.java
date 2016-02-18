package com.example.ericgarciaribera.infocars;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

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
 * Created by ericgarciaribera on 11/06/15.
 */
public class infoModelFragment extends Fragment {

    public infoModelFragment() {
    }

    public static String[] infoAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        //Add this line in order for this fragment to handle menu events.
        //setHasOptionsMenu(true);
    }

    private static void updateDB() {
        BrandsFragment.DB.deleteIfExistsI();
        for (int i = 0; i < infoAdapter.length; ++i) {
            BrandsFragment.DB.addInfo(infoAdapter[i]);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.brandsfragment, menu);
    }

    public static class FetchInfoTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchInfoTask.class.getSimpleName();

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

            final String API_INFO = "engines";
            final String ENGINE_NAME = "name";
            final String ENGINE_TYPE = "equipmentType";
            final String ENGINE_FUEL = "fuelType";
            final String ENGINE_C = "cylinder";
            final String ENGINE_COMPR = "compressionRatio";
            final String ENGINE_POWER = "horsepower";

            JSONObject modelsJson = new JSONObject(modelsJsonStr);
            JSONArray modelsArray = modelsJson.getJSONArray(API_INFO);
            JSONObject engineInfo = modelsArray.getJSONObject(0);

            String engineName;
            String engineType;
            String engineFuelType;
            String engineC;
            String engineCompr;
            String enginePower;

            try {
                engineName = engineInfo.getString(ENGINE_NAME);
            } catch (JSONException e) {
                engineName = "No Engine name info";
            }
            try {
                engineType = engineInfo.getString(ENGINE_TYPE);
            } catch (JSONException e) {
                engineType = "No Engine type info";
            }
            try {
                engineFuelType = engineInfo.getString(ENGINE_FUEL);
            } catch (JSONException e) {
                engineFuelType = "No fuel info";
            }
            try {
                engineC = engineInfo.getString(ENGINE_C);
            } catch (JSONException e) {
                engineC = "No cylinder info";
            }
            try {
                engineCompr = engineInfo.getString(ENGINE_COMPR);
            } catch (JSONException e) {
                engineCompr = "No compression ratio info";
            }
            try {
                enginePower = engineInfo.getString(ENGINE_POWER);
            } catch (JSONException e) {
                enginePower = "No power info";
            }

            String[] resultStrs = new String[6];

            resultStrs[0] = engineName;
            resultStrs[1] = engineType;
            resultStrs[2] = engineFuelType;
            resultStrs[3] = engineC;
            resultStrs[4] = engineCompr;
            resultStrs[5] = enginePower;

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

                final String MODELS_BASE_URL = "https://api.edmunds.com/api/vehicle/v2/styles/";
                final String MODELS_ENDING_URL = "/engines?fmt=json&api_key=d6udj8y2wc5e6x8edx83angy";

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
                infoAdapter = new String[strings.length];
                for (int i = 0; i < strings.length; ++i) {
                    infoAdapter[i] = strings[i];
                }
                updateDB();
            }
        }
    }
}