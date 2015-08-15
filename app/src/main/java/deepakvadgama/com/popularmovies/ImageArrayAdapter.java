package deepakvadgama.com.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import deepakvadgama.com.popularmovies.data.MovieDetails;

public class ImageArrayAdapter extends ArrayAdapter<MovieDetails> {

    private Context mContext;
    private List<MovieDetails> mMovies;

    public ImageArrayAdapter(Context context, int resource, int movieImage, List<MovieDetails> objects) {
        super(context, resource, movieImage, objects);
        mContext = context;
        mMovies = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_movies, parent, false);
        }

        ImageView movieImageView = (ImageView) convertView.findViewById(R.id.movieImage);
        Picasso.with(mContext).load(R.drawable.art_light_clouds).into(movieImageView);

        return movieImageView;
    }

    @Override
    public int getCount() {
        return 10;
    }

    public static class ViewHolder {
        public final ImageView imageView;

        public ViewHolder(ImageView imageView) {
            this.imageView = imageView;
        }
    }


}