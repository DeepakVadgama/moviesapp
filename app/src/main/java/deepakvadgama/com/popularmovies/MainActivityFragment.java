package deepakvadgama.com.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import deepakvadgama.com.popularmovies.data.Movie;


public class MainActivityFragment extends Fragment {

    private static final String SAVED_MOVIE_LIST = "movies_list_save";
    private ImageArrayAdapter mAdapter;
    private ArrayList<Movie> mMovies;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_MOVIE_LIST)) {
            mMovies = savedInstanceState.getParcelableArrayList(SAVED_MOVIE_LIST);
        } else {
            mMovies = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortBy = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.sort_popularity));
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            fetchMoviesTask.execute(sortBy);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        List<Movie> myStringArray = new ArrayList<>();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);

        mAdapter = new ImageArrayAdapter(getActivity(), R.layout.list_item_movies, R.id.movieImage, myStringArray);
        gridview.setAdapter(mAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, movie);
                startActivity(intent);
            }
        });

        if (mMovies != null && !mMovies.isEmpty()) {
            mAdapter.addAll(mMovies);
            mAdapter.notifyDataSetChanged();
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortBy = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.sort_popularity));

            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            fetchMoviesTask.execute(sortBy);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVED_MOVIE_LIST, mMovies);
        super.onSaveInstanceState(outState);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        private final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
        private final String IMAGE_QUALITY = "w185";

        private final String SORT_BY_POPULARITY = "popularity.desc";
        private final String SORT_BY_RATING = "vote_average.desc";

        @Override
        protected List<Movie> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieDetailsStr = null;

            try {

                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";
                String sortBy = SORT_BY_POPULARITY;
                if (getString(R.string.sort_rating).equals(params[0])) {
                    sortBy = SORT_BY_RATING;
                }

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sortBy)
                        .appendQueryParameter(API_KEY_PARAM, Utility.getApiKey())
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

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
                movieDetailsStr = buffer.toString();
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

            List<Movie> movieList = null;
            try {
                movieList = getMovieDetailsListFromJson(movieDetailsStr);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error in JSON conversion", e);
            }

            return movieList;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (movies != null && !movies.isEmpty()) {
                mAdapter.clear();
                mAdapter.addAll(movies);
            }
            mMovies = (ArrayList<Movie>) movies;
            mAdapter.notifyDataSetChanged();
        }

        private List<Movie> getMovieDetailsListFromJson(String movieDetailJson) throws JSONException, ParseException {

            final String RESULTS_LIST = "results";
            final String TITLE = "title";
            final String SYNOPSIS = "overview";
            final String RELEASE_DATE = "release_date";
            final String IMAGE_PATH = "poster_path";
            final String VOTE_AVG = "vote_average";


            List<Movie> movieList = new ArrayList<>();

            JSONObject forecastJson = new JSONObject(movieDetailJson);
            JSONArray movieArray = forecastJson.getJSONArray(RESULTS_LIST);

            for (int i = 0; i < movieArray.length(); i++) {
                Movie movie = new Movie();
                final JSONObject jsonObject = movieArray.getJSONObject(i);
                movie.setTitle(jsonObject.getString(TITLE));
                movie.setPlotSynopsis(jsonObject.getString(SYNOPSIS));
                movie.setReleaseDate(convertToDate(jsonObject.getString(RELEASE_DATE)));
                movie.setImagePath(getCompleteUrl(jsonObject.getString(IMAGE_PATH)));
                movie.setVoteAverage(jsonObject.getDouble(VOTE_AVG));
                movieList.add(movie);
            }

            return movieList;
        }

        private String getCompleteUrl(String string) {
            if (string == null || string.isEmpty()) {
                return null;
            }
            return IMAGE_BASE_URL + IMAGE_QUALITY + string;
        }

        private Date convertToDate(String dateString) throws ParseException {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
            try {
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Error in parsing date for string: " + dateString, e);
            }
            return null;
        }
    }

}
