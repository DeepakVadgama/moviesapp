package deepakvadgama.com.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.List;

import deepakvadgama.com.popularmovies.data.Movie;

public class ImageArrayAdapter extends ArrayAdapter<Movie> {

    private Context mContext;
    private List<Movie> mMovies;

    public ImageArrayAdapter(Context context, int resource, int movieImage, List<Movie> objects) {
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

        ViewHolder holder;
        if (convertView.getTag() == null) {
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.movieImage);
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(0, 0, 0, 0);
            holder = new ViewHolder(imageView);
            convertView.setTag(holder);
        }

        holder = (ViewHolder) convertView.getTag();
        ImageView movieImageView = holder.imageView;
        final Movie movie = mMovies.get(position);
        Picasso.with(mContext).load(movie.getImagePath()).into(movieImageView);

        return movieImageView;
    }

    @Override
    public int getCount() {
        return mMovies.size();
    }

    public static class ViewHolder {
        public final ImageView imageView;
        public ViewHolder(ImageView imageView) {
            this.imageView = imageView;
        }
    }

    @Override
    public void addAll(Collection<? extends Movie> collection) {
        mMovies.addAll(collection);
    }
}