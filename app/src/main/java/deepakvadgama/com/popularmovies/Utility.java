package deepakvadgama.com.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Utility {

    public static AtomicBoolean favoriteUpdated = new AtomicBoolean(false);

    public static String getApiKey() {
        return "api_key";
    }

    public static String getSortCriteria(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key), context.getString(R.string.sort_popularity));
    }

    public static void addToFavorites(Context context, String movieId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> stringSet = prefs.getStringSet(context.getString(R.string.favorite_movies_pref), new HashSet<String>());
        stringSet.add(movieId);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(context.getString(R.string.favorite_movies_pref), stringSet);
        editor.apply();
    }

    public static void removeFromFavorites(Context context, String movieId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> stringSet = prefs.getStringSet(context.getString(R.string.favorite_movies_pref), new HashSet<String>());
        stringSet.remove(movieId);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(context.getString(R.string.favorite_movies_pref), stringSet);
        editor.apply();
    }

    public static boolean isFavorite(Context context, String movieId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> stringSet = prefs.getStringSet(context.getString(R.string.favorite_movies_pref), new HashSet<String>());
        return stringSet.contains(movieId);
    }


    public static Set<String> getFavorites(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(context.getString(R.string.favorite_movies_pref), new HashSet<String>());
    }

    public static boolean isConnectedToInternet(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public static String queryFromNetwork(final URL url, final String LOG_TAG) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String json = null;
        try {

            // Create the request to OpenWeatherMap, and open the connection
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
            json = buffer.toString();
//                Log.v(LOG_TAG, "URI return value for " + builtUri.toString() + ": " + movieDetailsStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
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
        return json;
    }

}
