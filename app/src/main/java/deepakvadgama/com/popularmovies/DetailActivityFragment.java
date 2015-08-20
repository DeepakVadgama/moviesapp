package deepakvadgama.com.popularmovies;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import deepakvadgama.com.popularmovies.data.Movie;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public final String LOG_TAG = this.getClass().getSimpleName();

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            Movie movie = intent.getParcelableExtra(Intent.EXTRA_TEXT);
            Log.d(LOG_TAG, "Got the movie from main activity: " + movie);

            if (movie != null) {
                TextView movieTitle = (TextView) rootView.findViewById(R.id.movie_title);
                movieTitle.setText(movie.getTitle());

                TextView movieRating = (TextView) rootView.findViewById(R.id.movie_rating_average);
                // Movie rating might not be available
                if (movie.getVoteAverage() >= 0d) {
                    movieRating.setText("" + movie.getVoteAverage() + "/10");
                }

                ImageView moviePoster = (ImageView) rootView.findViewById(R.id.movie_poster);
                Picasso.with(getActivity()).load(movie.getImagePath()).into(moviePoster);

                TextView movieRelease = (TextView) rootView.findViewById(R.id.movie_release_date);
                DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
                String releaseDate = movie.getReleaseDate() == null ? "TBD" : df.format(movie.getReleaseDate());
                movieRelease.setText(releaseDate);

                TextView moviePlot = (TextView) rootView.findViewById(R.id.movie_plot);
                moviePlot.setText(movie.getPlotSynopsis());
            }
        }
        return rootView;
    }
}
