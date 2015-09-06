package deepakvadgama.com.popularmovies;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import deepakvadgama.com.popularmovies.data.Movie;


public class MainActivityFragment extends Fragment {

    private static final String SAVED_MOVIE_LIST = "movies_list_save";
    private static final String SELECTED_MOVIE_POSITION = "selected_movie_position";
    private static final String TWO_PANE = "two_pane";
    private ImageArrayAdapter mAdapter;
    private GridView mGridView;
    private ArrayList<Movie> mMovies;
    private boolean mTwoPane = false;
    private int mPosition = ListView.INVALID_POSITION;


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_MOVIE_LIST)) {
            mMovies = savedInstanceState.getParcelableArrayList(SAVED_MOVIE_LIST);
            mPosition = savedInstanceState.getInt(SELECTED_MOVIE_POSITION, 0);
            mTwoPane = savedInstanceState.getBoolean(TWO_PANE);
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
            refetchMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        List<Movie> myStringArray = new ArrayList<>();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridview);

        mAdapter = new ImageArrayAdapter(getActivity(), R.layout.list_item_movies, R.id.movieImage, myStringArray);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mAdapter.getItem(position);
                mPosition = position;
                ((Callback) getActivity()).onItemSelected(movie);
            }
        });

        if (mMovies != null && !mMovies.isEmpty()) {
            mAdapter.addAll(mMovies);
            mAdapter.notifyDataSetChanged();

            // Can be avoided if it is loader/cursor?
            // Can also be used in setActivatedItem instead..
            mGridView.smoothScrollToPositionFromTop(mPosition == ListView.INVALID_POSITION ? 0 : mPosition, 0);
        } else {
            refetchMovies();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVED_MOVIE_LIST, mMovies);
        outState.putInt(SELECTED_MOVIE_POSITION, mPosition);
        outState.putBoolean(TWO_PANE, mTwoPane);
        super.onSaveInstanceState(outState);
    }

    public void refetchMovies() {
        String sortBy = Utility.getSortCriteria(getActivity());
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        fetchMoviesTask.execute(sortBy);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        private final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
        private final String IMAGE_QUALITY = "w185";

        private final String SORT_BY_POPULARITY = "popularity.desc";
        private final String SORT_BY_RATING = "vote_average.desc";

        @Override
        protected void onPostExecute(List<Movie> movies) {

            if (isCancelled()) {
                return;
            }

            if (movies != null && !movies.isEmpty()) {
                mAdapter.clear();
                mAdapter.addAll(movies);
                mMovies = (ArrayList<Movie>) movies;
            }
            mAdapter.notifyDataSetChanged();

            // Solved by creating boolean mTwoPane
            // Potential problem, what it this task is called, after a position is selected in tablet, but then someone
            // updated the sort criteria. In that case, the data will be new, but the mPosition will not have been updated right?
            if (mTwoPane && movies != null && !movies.isEmpty()) {
                mPosition = 0;
                ((Callback) getActivity()).onItemSelected(movies.get(0));
            }
            mGridView.smoothScrollToPositionFromTop(mPosition, 0);
        }

        @Override
        protected List<Movie> doInBackground(String... params) {

            if (!Utility.isConnectedToInternet(getActivity())) {
                Snackbar.make(getView(), "Not connected to internet", Snackbar.LENGTH_LONG).show();
                cancel(true);
            }

            List<Movie> movieList = null;
            try {

                Uri builtUri = getMovieUri(params[0]);
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                String movieDetailsStr = Utility.queryFromNetwork(url, LOG_TAG);
                movieList = getMovieDetailsListFromJson(movieDetailsStr);

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error in JSON conversion", e);
            }

            return movieList;
        }

        private Uri getMovieUri(String sortKey) {
            final String DISCOVER_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String API_KEY_PARAM = "api_key";

            String sortBy = SORT_BY_POPULARITY;
            if (getString(R.string.sort_rating).equals(sortKey)) {
                sortBy = SORT_BY_RATING;
            }
            return Uri.parse(DISCOVER_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, sortBy)
                    .appendQueryParameter(API_KEY_PARAM, Utility.getApiKey())
                    .build();
        }

        private List<Movie> getMovieDetailsListFromJson(String movieDetailJson) throws JSONException, ParseException {

            final String RESULTS_LIST = "results";
            final String TITLE = "title";
            final String SYNOPSIS = "overview";
            final String RELEASE_DATE = "release_date";
            final String IMAGE_PATH = "poster_path";
            final String VOTE_AVG = "vote_average";
            final String ID = "id";

            List<Movie> movieList = new ArrayList<>();

            JSONObject forecastJson = new JSONObject(movieDetailJson);
            JSONArray movieArray = forecastJson.getJSONArray(RESULTS_LIST);

            for (int i = 0; i < movieArray.length(); i++) {
                Movie movie = new Movie();
                final JSONObject jsonObject = movieArray.getJSONObject(i);
                movie.setId(jsonObject.getString(ID));
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

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        public void onItemSelected(Movie movie);
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     *//*
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        mGridView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            mGridView.setItemChecked(0, false);
        } else {
            mGridView.setItemChecked(position, true);
        }
    }*/
    public void setIfTwoPane(boolean mTwoPane) {
        this.mTwoPane = mTwoPane;
    }
}
