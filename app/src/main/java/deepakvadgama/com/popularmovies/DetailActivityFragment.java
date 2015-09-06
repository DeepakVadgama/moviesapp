package deepakvadgama.com.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import deepakvadgama.com.popularmovies.data.Movie;
import deepakvadgama.com.popularmovies.data.Review;
import deepakvadgama.com.popularmovies.data.Trailer;

public class DetailActivityFragment extends Fragment {

    public final String LOG_TAG = this.getClass().getSimpleName();
    public static final String MOVIE_TAG = "movie_tag";

    private Movie mMovie;
    private List<Review> mReviews;
    private List<Trailer> mTrailers;
    private TrailerListener mTrailerListener = new TrailerListener();
    private FavoriteListener mFavoriteListener = new FavoriteListener();


    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(MOVIE_TAG)) {
            // One pane UI is started as intent
            mMovie = intent.getParcelableExtra(MOVIE_TAG);
        } else if (getArguments() != null && getArguments().containsKey(MOVIE_TAG)) {
            // Two pane UI is started as bundle
            mMovie = getArguments().getParcelable(MOVIE_TAG);
        }


        if (mMovie != null) {

            // Alternatively, could ask MainActivity to populate this upfront.
            mMovie.setIsFavorite(Utility.isFavorite(getActivity(), mMovie.getId()));

            // Fetch reviews
            FetchReviewsTask fetchReviewsTask = new FetchReviewsTask();
            fetchReviewsTask.execute(mMovie.getId());

            // Fetch trailers
            FetchTrailersTask fetchTrailersTask = new FetchTrailersTask();
            fetchTrailersTask.execute(mMovie.getId());

            // Populate remaining UI
            TextView movieTitle = (TextView) rootView.findViewById(R.id.movie_title);
            movieTitle.setText(mMovie.getTitle());

            TextView movieRating = (TextView) rootView.findViewById(R.id.movie_rating_average);
            // Review rating might not be available
            if (mMovie.getVoteAverage() >= 0d) {
                movieRating.setText("Rating: " + mMovie.getVoteAverage() + "/10");
            }

            ImageView moviePoster = (ImageView) rootView.findViewById(R.id.movie_poster);
            Picasso.with(getActivity()).load(mMovie.getImagePath()).into(moviePoster);

            TextView movieRelease = (TextView) rootView.findViewById(R.id.movie_release_date);
            DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
            String releaseDate = mMovie.getReleaseDate() == null ? "TBD" : df.format(mMovie.getReleaseDate());
            movieRelease.setText(releaseDate);

            ImageButton favoriteButton = (ImageButton) rootView.findViewById(R.id.favorite_button);
            favoriteButton.setSelected(mMovie.isFavorite());
            favoriteButton.setTag(mMovie.getId());
            favoriteButton.setOnClickListener(mFavoriteListener);

            TextView moviePlot = (TextView) rootView.findViewById(R.id.movie_plot);
            moviePlot.setText(mMovie.getPlotSynopsis());
        }

        return rootView;
    }


    private class TrailerListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            String id = (String) v.getTag();
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id));
                startActivity(intent);
            }
            return true;
        }
    }

    private void updateReviewsUi() {
        ViewGroup reviewContainer = (ViewGroup) getActivity().findViewById(R.id.review_list_container);
        if (mReviews == null || mReviews.isEmpty()) {
            reviewContainer.setVisibility(View.INVISIBLE);
        } else {
            for (Review review : mReviews) {
                View reviewView = getActivity().getLayoutInflater().inflate(R.layout.list_item_reviews, reviewContainer, false);
                ((TextView) reviewView.findViewById(R.id.review_author)).setText(review.getAuthor());
                ((TextView) reviewView.findViewById(R.id.review_text)).setText(review.getReviewText());
                reviewContainer.addView(reviewView);
            }
            reviewContainer.setVisibility(View.VISIBLE);
        }
    }

    private void updateTrailersUi() {
        ViewGroup trailerContainer = (ViewGroup) getActivity().findViewById(R.id.trailer_list_container);
        if (mTrailers == null || mTrailers.isEmpty()) {
            trailerContainer.setVisibility(View.INVISIBLE);
        } else {

            for (int i = 0; i < mTrailers.size(); i++) {
                Trailer trailer = mTrailers.get(i);
                View trailerView = getActivity().getLayoutInflater().inflate(R.layout.list_item_trailers, trailerContainer, false);
                trailerView.setTag(trailer.getTrailerKey());
                ((TextView) trailerView.findViewById(R.id.trailer_text)).setText("Trailer " + (i + 1));
                trailerContainer.addView(trailerView);
                trailerView.setOnTouchListener(mTrailerListener);
            }
            trailerContainer.setVisibility(View.VISIBLE);
        }
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, List<Review>> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        @Override
        protected void onPostExecute(List<Review> reviews) {
            if (isCancelled()) {
                return;
            }
            mReviews = reviews;
            updateReviewsUi();
        }

        @Override
        protected List<Review> doInBackground(String... params) {

            if (!Utility.isConnectedToInternet(getActivity())) {
                Snackbar.make(getView(), "Not connected to internet", Snackbar.LENGTH_LONG).show();
                cancel(true);
            }

            List<Review> reviewList = null;
            try {

                Uri reviewsUri = getReviewsUri(params[0]);
                URL url = new URL(reviewsUri.toString());
                Log.v(LOG_TAG, "Built review URI " + reviewsUri.toString());

                String json = Utility.queryFromNetwork(url, LOG_TAG);
                reviewList = getReviewList(json);

            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error in creating URL", e);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error in JSON conversion", e);
            }

            return reviewList;
        }

        private List<Review> getReviewList(String json) throws Exception {
            final String RESULTS_LIST = "results";
            final String AUTHOR = "author";
            final String CONTENT = "content";

            List<Review> reviews = new ArrayList<>();

            JSONObject forecastJson = new JSONObject(json);
            JSONArray reviewsArray = forecastJson.getJSONArray(RESULTS_LIST);

            for (int i = 0; i < reviewsArray.length(); i++) {
                final JSONObject jsonObject = reviewsArray.getJSONObject(i);
                Review review = new Review(jsonObject.getString(AUTHOR), jsonObject.getString(CONTENT));
                reviews.add(review);
            }

            return reviews;
        }

    }

    public class FetchTrailersTask extends AsyncTask<String, Void, List<Trailer>> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            if (isCancelled()) {
                return;
            }
            mTrailers = trailers;
            updateTrailersUi();
        }

        @Override
        protected List<Trailer> doInBackground(String... params) {

            if (!Utility.isConnectedToInternet(getActivity())) {
                Snackbar.make(getView(), "Not connected to internet", Snackbar.LENGTH_LONG).show();
                cancel(true);
            }

            List<Trailer> trailerList = null;
            try {

                Uri trailersUri = getTrailersUri(params[0]);
                URL url = new URL(trailersUri.toString());
                Log.v(LOG_TAG, "Built trailer URI " + trailersUri.toString());

                String json = Utility.queryFromNetwork(url, LOG_TAG);
                trailerList = getTrailerList(json);

            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error in creating URL", e);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error in JSON conversion", e);
            }

            return trailerList;
        }

        private List<Trailer> getTrailerList(String json) throws Exception {
            final String RESULTS_LIST = "results";
            final String TRAILER_KEY = "key";
            final String VIDEO_TYPE = "type";
            final String VIDEO_TYPE_TRAILER = "trailer";
            final String SITE = "site";
            final String SITE_YOUTUBE = "youtube";

            List<Trailer> trailers = new ArrayList<>();

            JSONObject forecastJson = new JSONObject(json);
            JSONArray trailersArray = forecastJson.getJSONArray(RESULTS_LIST);

            for (int i = 0; i < trailersArray.length(); i++) {
                final JSONObject jsonObject = trailersArray.getJSONObject(i);

                if (VIDEO_TYPE_TRAILER.equalsIgnoreCase(jsonObject.getString(VIDEO_TYPE))
                        && SITE_YOUTUBE.equalsIgnoreCase(jsonObject.getString(SITE))) {
                    String trailerKey = jsonObject.getString(TRAILER_KEY);
                    Trailer trailer = new Trailer(trailerKey);
                    trailers.add(trailer);
                }

            }

            return trailers;
        }
    }

    private Uri getReviewsUri(String movieId) {
        return getUri(movieId, "reviews");
    }

    private Uri getTrailersUri(String movieId) {
        return getUri(movieId, "videos");
    }

    private Uri getUri(String movieId, String query) {
        final String DISCOVER_BASE_URL = "http://api.themoviedb.org/3/movie/";
        final String API_KEY_PARAM = "api_key";

        return Uri.parse(DISCOVER_BASE_URL).buildUpon()
                .appendPath(movieId)
                .appendPath(query)
                .appendQueryParameter(API_KEY_PARAM, Utility.getApiKey())
                .build();
    }

    private class FavoriteListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mMovie.setIsFavorite(!mMovie.isFavorite());
            v.setSelected(mMovie.isFavorite());
            FavoritesTask favoritesTask = new FavoritesTask();
            favoritesTask.execute(mMovie.getId(), String.valueOf(mMovie.isFavorite()));
        }
    }

    public class FavoritesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String movieId = params[0];
            boolean isFavorite = Boolean.parseBoolean(params[1]);

            if (isFavorite) {
                Utility.addToFavorites(getActivity(), movieId);
                return "Added to favorites";
            } else {
                Utility.removeFromFavorites(getActivity(), movieId);
                return "Removed from favorites";
            }
        }

        @Override
        protected void onPostExecute(String operation) {
            Snackbar.make(getView(), operation, Snackbar.LENGTH_LONG).show();
        }

    }

}
